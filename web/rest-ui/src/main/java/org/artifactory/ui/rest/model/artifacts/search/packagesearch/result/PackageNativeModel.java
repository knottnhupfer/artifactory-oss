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

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.PackageNativeDockerSearchService;
import org.artifactory.util.CollectionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeKeywordsUtil.getKeywordsAsSet;

/**
 * @author ortalh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class PackageNativeModel {
    private static final Logger log = LoggerFactory.getLogger(PackageNativeModel.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private String name;
    private long lastModified;
    private int numOfRepos;
    private int numOfVersions;
    private Set<String> repositories = Sets.newHashSet();
    private Set<String> keywords = Sets.newHashSet();

    public PackageNativeModel(String pkgName, AqlBaseFullRowImpl row) {
        String repo = row.getRepo();
        this.name = pkgName;
        this.repositories = Sets.newTreeSet();
        this.repositories.add(repo);
    }

    public void addKeywords(String keywords) {
        this.keywords.addAll(getKeywordsAsSet(keywords));
    }

    /**
     * Used by the {@link PackageNativeDockerSearchService}'s reduction mechanism to aggregate result rows into one result
     * NOTE: assumes this model is already constructed using a row representing the same path as all rows being
     * aggregated.
     */
    public PackageNativeModel aggregateRow(AqlBaseFullRowImpl row) {
        //Row contains a property
        String repoKey = row.getRepo();

        if (CollectionUtils.isNullOrEmpty(repositories)) {
            repositories = Sets.newTreeSet();
            repositories.add(repoKey);
        }

        //Only add repoKey that correlate to the same package path
        log.debug("Found matching package {} and Re, aggregating into result", name);
        repositories.add(repoKey);
        numOfRepos = repositories.size();
        return this;
    }

    public static PackageNativeModel merge(PackageNativeModel res1, PackageNativeModel res2) {
        if (CollectionUtils.isNullOrEmpty(res1.repositories) && CollectionUtils.notNullOrEmpty(res2.repositories)) {
            res1.setRepositories(res2.repositories);
        }
        if (StringUtils.isBlank(res1.getName()) && StringUtils.isNotBlank(res2.getName())) {
            res1.setName(res2.getName());
        }
        if (res1.name.equals(res2.name)) {
            if (!res1.repositories.isEmpty()) {
                res1.repositories.addAll(res2.repositories);
            }
        }
        return res1;
    }

    public void addRepoKey(String repoKey) {
        if (StringUtils.isNotEmpty(repoKey)) {
            this.repositories.add(repoKey);
            this.numOfRepos = this.repositories.size();
        }
    }
}
