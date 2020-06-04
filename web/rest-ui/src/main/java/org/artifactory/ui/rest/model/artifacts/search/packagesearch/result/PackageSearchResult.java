/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.model.artifacts.search.packagesearch.result;

import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.result.rows.FullRow;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria.PackageSearchCriterion;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.PackageSearchService;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageSearchHelper;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria.PackageSearchType;

/**
 * An Aql result containing relevant fields for a UI item domain search (single row) with any extra fields and
 * properties that were part of the query (if requested)
 *
 * @author Dan Feldman
 */
@JsonTypeName("package")
public class PackageSearchResult extends BaseSearchResult implements AqlUISearchResult {
    private static final Logger log = LoggerFactory.getLogger(PackageSearchResult.class);

    @JsonUnwrapped
    protected HashMultimap<String, String> extraFields = HashMultimap.create();
    private PackageSearchType packageType;
    protected String relativePath;

    public PackageSearchResult(FullRow row) {
        this(createRepoPath(row), row.getModified(), null);
    }

    public PackageSearchResult(RepoPath repoPath, Date dateModified, PackageSearchType packageType) {
        setRepoKey(repoPath.getRepoKey());
        setName(repoPath.getName());
        setModifiedDate(dateModified.getTime());
        setModifiedString(dateModified.toString());
        this.repoPath = repoPath;
        this.relativePath = repoPath.getPath();
        this.packageType = packageType;
    }

    private static RepoPath createRepoPath(FullRow row) {
        return InfoFactoryHolder.get().createRepoPath(row.getRepo(), row.getPath() + "/" + row.getName());
    }

    //For Stash results
    public PackageSearchResult() {
    }

    public PackageSearchType getPackageType() {
        return packageType;
    }

    public void setPackageType(
            PackageSearchType packageType) {
        this.packageType = packageType;
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public Map<String, Collection<String>> getExtraFields() {
        return extraFields.asMap();
    }

    @Override
    public HashMultimap<String, String> getExtraFieldsMap() {
        return extraFields;
    }

    @Override
    @JsonIgnore
    public AqlDomainEnum getDomain() {
        return AqlDomainEnum.items;
    }

    @JsonIgnore
    public RepoPath getRepoPath() {
        return repoPath;
    }

    public PackageSearchResult setDownloadLinkAndActions(ArtifactoryRestRequest request) {
        if (repoPath != null) {
            if (packageType.isDownloadEnabled()) {
                setDownloadLink(request.getDownloadLink(repoPath));
            }
            super.updateActions();
        }
        return this;
    }

    @Override
    public ItemSearchResult getSearchResult() {
        RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), getRelativePath());
        ItemInfo itemInfo;
        try {
            itemInfo = ContextHelper.get().getRepositoryService().getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            log.debug("", e);
            itemInfo = getItemInfo(repoPath);
        }
        return new ArtifactSearchResult(itemInfo);
    }

    /**
     * Used by the {@link PackageSearchService}'s reduction mechanism to aggregate result rows into one result
     * NOTE: assumes this model is already constructed using a row representing the same path as all rows being
     * aggregated.
     */
    @Override
    public PackageSearchResult aggregateRow(FullRow row) {
        //Row contains a property
        if (row.getKey() != null) {
            PackageSearchCriterion criterion;
            try {
                criterion = PackageSearchHelper.getMatchingPackageSearchCriteria(row);
            } catch (IllegalArgumentException iae) {
                //This is a property that doesn't exist in the criteria enum - do not add it to extra fields map
                log.debug("Unable to match a search criterion for prop {} on path {}", row.getKey(),
                        row.getRepo() + ":" + row.getPath() + "/" + row.getName());
                return this;
            }
            if (packageType == null) {
                packageType = criterion.getType();
            }
            //Only add properties that correlate to the same package type
            if (PackageSearchCriteria.getCriteriaByPackage(packageType).contains(criterion)) {
                log.debug("Found matching criterion {} for row package type {}, aggregating into result",
                        criterion.getModel().getId(), packageType);
                extraFields.put(criterion.getModel().getId(), row.getValue());
                return this;
            }
        }
        // TODO: [by dan] check if row has any fields that are not in items domain and add to the map with
        // TODO: [by dan] AqlUISearchCriteria.getCriterionByAqlFieldOrPropName if yes.
        return this;
    }

    /**
     * Used by the {@link PackageSearchService}'s reduction mechanism to aggregate result rows into one result
     * NOTE: Assumes both results were constructed from rows referencing the same path!
     */
    public static PackageSearchResult merge(PackageSearchResult res1, PackageSearchResult res2) {
        if (res1.repoPath == null && res2.repoPath != null) {
            res1.repoPath = res2.repoPath;
        }
        if (StringUtils.isBlank(res1.getRepoKey()) && StringUtils.isNotBlank(res2.getRepoKey())) {
            res1.setRepoKey(res2.getRepoKey());
        }
        if (StringUtils.isBlank(res1.getName()) && StringUtils.isNotBlank(res2.getName())) {
            res1.setName(res2.getName());
        }
        if (StringUtils.isBlank(res1.getModifiedString()) && StringUtils.isNotBlank(res2.getModifiedString())) {
            res1.setModifiedDate(res2.getModifiedDate());
            res1.setModifiedString(res2.getModifiedString());
        }
        return res1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageSearchResult)) {
            return false;
        }

        PackageSearchResult that = (PackageSearchResult) o;

        if (getExtraFields() != null ? !getExtraFields().equals(that.getExtraFields()) :
                that.getExtraFields() != null) {
            return false;
        }
        if (getPackageType() != that.getPackageType()) {
            return false;
        }
        return !(getRepoPath() != null ? !getRepoPath().equals(that.getRepoPath()) :
                that.getRepoPath() != null);

    }

    @Override
    public int hashCode() {
        int result = getExtraFields() != null ? getExtraFields().hashCode() : 0;
        result = 31 * result + (getPackageType() != null ? getPackageType().hashCode() : 0);
        result = 31 * result + (getRepoPath() != null ? getRepoPath().hashCode() : 0);
        return result;
    }
}
