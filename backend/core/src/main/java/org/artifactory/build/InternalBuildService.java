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

package org.artifactory.build;

import org.artifactory.api.build.BuildRetentionWorkItem;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.build.ImportableExportableBuild;
import org.artifactory.api.build.request.BuildArtifactoryRequest;
import org.artifactory.api.repo.Async;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.Lock;
import org.artifactory.spring.ContextCreationListener;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.storage.jobs.migration.buildinfo.BuildInfoCalculationWorkItem;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Dependency;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The system-internal interface of the build service
 *
 * @author Noam Y. Tenne
 */
public interface InternalBuildService extends ReloadableBean, BuildService, ContextCreationListener {

    String ACCESS_DENIED_MSG = "User is not authorized to access build info";

    /**
     * Name of folder to temporarily store previous build backups during an incremental
     */
    String BACKUP_BUILDS_FOLDER = "builds.previous";

    /**
     * Performs upload process of builds reaching from deploy endpoint (not REST API of api/build)
     */
    void handleBuildUploadRedirect(BuildArtifactoryRequest originalRequest, ArtifactoryResponse response) throws IOException;

    /**
     * DO NOT!!!!!!!! CALL DIRECTLY!!!
     *
     * Adds the given {@param build} to the DB.
     */
    @Lock
    void addBuildInternal(Build build);

    /**
     * Returns a Mapping of {@link Artifact} to {@link FileInfo} that was  matched to it.
     * This method tries to search by build name and number properties first and if it can't match all Artifacts to
     * FileInfo objects it will fallback to a weaker search that's based on the build DB entries to fill in the missing
     * artifacts, if indicated by {@param useFallBack}
     * <p/>
     * **NOTE:**
     *
     * @param build                The searched build (searching within it's artifacts)
     * @param useFallBack          Indicates whether to fallback to the weaker search if not all artifacts were matched
     *                             against FileInfo objects
     * @param sourceRepositories   only include results from these repositories, not mandatory
     * @param excludedRepositories exclude results from these repositories, not mandatory
     */
    Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build, boolean useFallBack,
            @Nullable List<String> sourceRepositories, @Nullable List<String> excludedRepositories);

    /**
     * Returns a map of build dependency and it's matching FileInfo
     *
     * @param build            The searched build (searching within it's dependencies)
     */
    Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build);

    /**
     * Imports an exportable build info.
     * This is an internal method and should be used to import a single build within a transaction.
     *
     * @param settings Import settings
     * @param build    The build to import
     */
    @Lock
    void importBuild(ImportSettings settings, ImportableExportableBuild build) throws Exception;

    /**
     * Renames the JSON content within a build
     *
     * @param buildRun Build to rename
     * @param to       Replacement build name
     */
    @Lock
    void renameBuild(BuildRun buildRun, String to);

    /**
     * Reads the build json at Build Info repo and convert it to a Build model.
     *
     * @param buildJsonPath path according to coordinates
     * @throws RuntimeException in case read failed
     */
    Build getBuildModelFromFile(RepoPath buildJsonPath);

    /**
     * Returns latest build by name and status (which can be {@link BuildService#LATEST_BUILD} or a status value (e.g: "Released")
     *
     * @param buildName   the name of the build
     * @param buildStatus the desired status (which can be {@link BuildService#LATEST_BUILD} or a status value (e.g: "Released")
     * @return the build (if found)
     */
    @Nullable
    Build getLatestBuildByNameAndStatus(String buildName, String buildStatus);

    /**
     * INTERNAL USE - Without permission check
     */
    BuildRun getBuildRunInternally(String buildName, String buildNumber, String buildStarted);

    /**
     * Returns the build of the given details
     * INTERNAL USE - no permissions check done.
     *
     * @param buildRun Build run retrieved
     * @return Build if found. Null if not
     */
    Build getBuildInternally(BuildRun buildRun);

    /**
     * Returns the latest build for the given name and number
     * INTERNAL USE - no permissions check done.
     *
     * @param buildName   Name of build to locate
     * @param buildNumber Number of build to locate
     * @return Latest build if found. Null if not
     */
    @Nullable
    Build getLatestBuildByNameAndNumberInternally(String buildName, String buildNumber);

    /**
     * Locates builds that are named as the given name
     * INTERNAL USE - no permissions check done.
     *
     * @param buildName Name of builds to locate
     * @return Set of builds with the given name
     */
    Set<BuildRun> searchBuildsByNameInternally(String buildName);

    /**
     * INTERNAL USE - no permissions check done.
     * @return A list of build names, sorted alphabetically, distinct
     */
    List<String> getBuildNamesInternally();

    /**
     * INTERNAL USE - no permissions check done.
     * This method is called from BuildInfoInterceptor after the .json file is deleted
     */
    void deleteBuildInternal(Build build, MutableStatusHolder status);

    /**
     * ONLY FOR BUILD RETENTION DO NOT CALL DIRECTLY
     */
    @Async(workQueue = true)
    void deleteBuildAsync(BuildRetentionWorkItem buildRetentionWorkItem);


    /**
     * Writes a Build Info .json file from build_jsons db table into {@link #getBuildInfoRepoKey()}
     * Used only by migration job!
     */
    @Async(workQueue = true)
    void migrateBuildInfo(BuildInfoCalculationWorkItem workItem);

    /**
     * TO BE USED ONLY BY THE BUILD INFO MIGRATION JOB
     * tests the configs table for the presence of {@link #BUILD_INFO_MIGRATION_STATE} which is set by the job
     */
    boolean verifyBuildInfoState();
}