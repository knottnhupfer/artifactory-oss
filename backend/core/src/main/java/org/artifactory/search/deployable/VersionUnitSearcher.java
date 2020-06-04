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

package org.artifactory.search.deployable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.api.module.VersionUnit;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.deployable.VersionUnitSearchControls;
import org.artifactory.api.search.deployable.VersionUnitSearchResult;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.search.AqlSearcherBase;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * Holds the version unit search logic
 *
 * @author Noam Y. Tenne
 */
public class VersionUnitSearcher extends AqlSearcherBase<VersionUnitSearchControls, VersionUnitSearchResult> {

    @Override
    public ItemSearchResults<VersionUnitSearchResult> doSearch(VersionUnitSearchControls controls) {
        RepoPath pathToSearch = controls.getPathToSearchWithin();
        Repo repo = getRepoService().repositoryByKey(pathToSearch.getRepoKey());
        if (repo == null) {
            return new ItemSearchResults<>(Lists.newArrayList());
        }

        AqlEagerResult<AqlItem> result;
        AqlApiItem aql = getAqlQueryForSearch(pathToSearch);
        result = executeQuery(aql, controls);

        Multimap<ModuleInfo, RepoPath> moduleInfoToRepoPaths = HashMultimap.create();
        for (AqlItem item : result.getResults()) {
            RepoPath repoPath = AqlUtils.fromAql(item);
            ModuleInfo moduleInfo = repo.getItemModuleInfo(repoPath.getPath());
            if (moduleInfo.isValid()) {
                ModuleInfo stripped = stripModuleInfoFromUnnecessaryData(moduleInfo);
                moduleInfoToRepoPaths.put(stripped, repoPath);
            }
        }
        Set<VersionUnitSearchResult> results = getVersionUnitResults(moduleInfoToRepoPaths);
        return new ItemSearchResults<>(Lists.newArrayList(results), result.getSize());
    }

    private AqlApiItem getAqlQueryForSearch(RepoPath pathToSearch) {
        // only if the path is empty use a wildcard for the path
        AqlApiItem aql;
        if(pathToSearch.getPath().equals("")) {
             aql = AqlApiItem.create().filter(
                    AqlApiItem.and(
                            AqlApiItem.repo().equal(pathToSearch.getRepoKey()),
                            AqlApiItem.path().matches(pathToSearch.getPath() + "*")

                    ));

        } else {
            // else search for an exact path match or any sub-path artifacts
             aql = AqlApiItem.create().filter(
                    AqlApiItem.and(
                            AqlApiItem.repo().equal(pathToSearch.getRepoKey()),
                            AqlApiItem.or(
                                    AqlApiItem.path().matches(pathToSearch.getPath() + "/*"),
                                    AqlApiItem.path().equal(pathToSearch.getPath())
                            )

                    ));

        }
        return aql;
    }

    private Set<VersionUnitSearchResult> getVersionUnitResults(Multimap<ModuleInfo, RepoPath> moduleInfoToRepoPaths) {
        return moduleInfoToRepoPaths.keySet().stream()
                .map(moduleInfo -> new VersionUnitSearchResult(
                        new VersionUnit(moduleInfo, Sets.newHashSet(moduleInfoToRepoPaths.get(moduleInfo)))))
                .collect(Collectors.toSet());
    }

    private ModuleInfo stripModuleInfoFromUnnecessaryData(ModuleInfo moduleInfo) {
        ModuleInfoBuilder moduleInfoBuilder = new ModuleInfoBuilder().organization(moduleInfo.getOrganization()).
                module(moduleInfo.getModule()).baseRevision(moduleInfo.getBaseRevision());
        if (moduleInfo.isIntegration()) {
            String pathRevision = moduleInfo.getFolderIntegrationRevision();
            String artifactRevision = moduleInfo.getFileIntegrationRevision();

            boolean hasPathRevision = StringUtils.isNotBlank(pathRevision);
            boolean hasArtifactRevision = StringUtils.isNotBlank(artifactRevision);

            if (hasPathRevision && !hasArtifactRevision) {
                moduleInfoBuilder.folderIntegrationRevision(pathRevision);
                moduleInfoBuilder.fileIntegrationRevision(pathRevision);
            } else if (!hasPathRevision && hasArtifactRevision) {
                moduleInfoBuilder.fileIntegrationRevision(artifactRevision);
                moduleInfoBuilder.folderIntegrationRevision(artifactRevision);
            } else {
                moduleInfoBuilder.folderIntegrationRevision(pathRevision);
                moduleInfoBuilder.fileIntegrationRevision(artifactRevision);
            }
        }
        return moduleInfoBuilder.build();
    }
}