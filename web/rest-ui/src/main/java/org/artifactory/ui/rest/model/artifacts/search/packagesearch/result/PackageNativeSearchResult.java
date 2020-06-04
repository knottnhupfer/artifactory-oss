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
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.aql.result.rows.FullRow;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.util.CollectionUtils;

import java.util.Date;
import java.util.Set;

/**
 * @author Inbar Tal
 */
@Data
public class PackageNativeSearchResult extends BaseSearchResult {

    private HashMultimap<String, String> extraFields = HashMultimap.create();

    public PackageNativeSearchResult(FullRow row) {
        this(createRepoPath(row), row.getModified());
    }

    public PackageNativeSearchResult(RepoPath repoPath, Date dateModified) {
        setRepoKey(repoPath.getRepoKey());
        setName(repoPath.getName());
        setModifiedDate(dateModified.getTime());
        setModifiedString(dateModified.toString());
        this.repoPath = repoPath;
    }

    private static RepoPath createRepoPath(FullRow row) {
        return InfoFactoryHolder.get().createRepoPath(row.getRepo(), row.getPath() + "/" + row.getName());
    }

    @Override
    public ItemSearchResult getSearchResult() {
        return null;
    }

    public PackageNativeSearchResult aggregateRow(FullRow row, Set<String> propKeys) {
        if (row.getKey() == null || CollectionUtils.isNullOrEmpty(propKeys)) {
            return this;
        }
        propKeys.forEach(
                propKey -> {
                    if (StringUtils.equals(row.getKey(), propKey)) {
                        extraFields.put(propKey, row.getValue());
                    }
                });
        return this;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageNativeSearchResult)) {
            return false;
        }
        PackageNativeSearchResult that = (PackageNativeSearchResult) o;

        if (getRepoKey() != null ? !getRepoKey().equals(that.getRepoKey()) : that.getRepoKey() != null) {
            return false;
        }

        if (getRepoPath() != null ? !getRepoPath().equals(that.getRepoPath()) : that.getRepoPath() != null) {
            return false;
        }
        return (getExtraFields() != null ? getExtraFields().equals(that.getExtraFields()) : that.getExtraFields() == null);
    }

    @Override
    public int hashCode() {
        int result = getExtraFields() != null ? getExtraFields().hashCode() : 0;
        result = 31 * result + (getRepoKey() != null ? getRepoKey().hashCode() : 0);
        result = 31 * result + (getRepoPath() != null ? getRepoPath().hashCode() : 0);
        return result;
    }
}
