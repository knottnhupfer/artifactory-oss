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

package org.artifactory.storage.build.service;

import org.artifactory.api.build.*;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.build.BuildId;
import org.artifactory.build.BuildRun;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.ui.rest.service.builds.search.BuildsSearchFilter;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.PromotionStatus;

import java.util.List;
import java.util.Set;

/**
 * Date: 11/14/12
 * Time: 12:40 PM
 *
 * @author freds
 */
public interface BuildStoreService {

    /**
     * Adds the build to the DB tables (the build.json is handled in BuildService)
     */
    void addBuild(Build build, boolean stateReady);

    /**
     * Locates and fills in missing checksums of all the modules of a build
     *
     * @param build the build with modules to fill checksums
     */
    void populateMissingChecksums(Build build);

    BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted);

    BuildRun getLatestBuildRun(String buildName, String buildNumber);

    Set<BuildRun> findBuildsByName(String buildName);

    Set<BuildRun> findBuildsByNameAndNumber(String buildName, String buildNumber);

    List<String> getAllBuildNames();

    /**
     * Delete all build number under specific build
     */
    void deleteBuild(String buildName, boolean stateReady);

    /**
     * Delete specific build number
     */
    void deleteBuild(String buildName, String buildNumber, String buildStarted, boolean stateReady);

    /**
     * Deletes ALL builds in the db (all builds tables)
     */
    void deleteAllBuilds(boolean stateReady);

    List<BuildId> getLatestBuildIDsPaging(ContinueBuildFilter continueBuildFilter);

    List<BuildId> getLatestBuildIDsByName(String buildName, String fromDate, String toDate, String orderBy, String direction);

    List<GeneralBuild> getBuildVersions(BuildsSearchFilter filter);

    Set<BuildRun> getLatestBuildsByName();

    List<GeneralBuild> getBuildForName(String buildName, ContinueBuildFilter continueBuildFilter);

    void addPromotionStatus(Build build, PromotionStatus promotion, String currentUser, boolean stateReady);

    Set<BuildRun> findBuildsForChecksum(BuildSearchCriteria criteria, ChecksumType type, String checksum);

    List<PublishedModule> getPublishedModules(String buildNumber, String date);

    List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String moduleId, String date);

    List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date);

    List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams);

    List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams);

    List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate);

    List<BuildProps> getBuildPropsData(BuildParams buildParams);

    boolean exists(Build build);

    /**
     * The {@link Build} object returned by this method represents the json in the build_jsons table.
     */
    Build getBuild(BuildRun buildRun);

    /**
     * Returns the build id according to build coordinates.
     *
     * @return 0L in case build not found
     */
    long findIdFromBuild(BuildRun build);
}
