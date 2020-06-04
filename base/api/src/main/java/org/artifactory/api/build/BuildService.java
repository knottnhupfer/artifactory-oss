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

package org.artifactory.api.build;

import org.artifactory.api.build.model.BuildGeneralInfo;
import org.artifactory.api.build.model.diff.BuildParams;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.ImportableExportable;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.api.rest.build.ContinueBuildFilter;
import org.artifactory.build.BuildId;
import org.artifactory.build.BuildRun;
import org.artifactory.fs.FileInfo;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.sapi.common.Lock;
import org.artifactory.ui.rest.service.builds.search.BuildsSearchFilter;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.build.api.release.PromotionStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Build service main interface
 *
 * @author Noam Y. Tenne
 */
public interface BuildService extends ImportableExportable {

    /**
     * In case a dependency contains a {@code null} scope, fill it with an unspecified scope that will be used for
     * filtering.
     */
    String UNSPECIFIED_SCOPE = "unspecified";
    String LATEST_BUILD = "LATEST";
    String LAST_RELEASED_BUILD = "LAST_RELEASE";
    String BUILDS_EXPORT_DIR = "builds";

    /**
     * Signifies that Artifactory ran for the first time after {@link org.artifactory.version.ArtifactoryVersion#v660}
     * OR
     * an upgrade was made, all nodes in the cluster were upgraded, and the build info migration job ran its course.
     *
     * @return true means that Build Permissions can be activated
     * false means Build Permissions cannot be determined *YET*.
     */
    boolean isBuildInfoReady();

    /**
     * Finds the one and only {@link org.artifactory.descriptor.repo.RepoType#BuildInfo} repository key.
     *
     * This method exists due to the possibility of Build Info repository to be named differently than expected.
     * (Such scenario can happen after upgrade process in which a similar repo key already existed)
     *
     * @return the current config descriptor Build Info repository key.
     */
    String getBuildInfoRepoKey();

    /**
     * Deploys the given {@param build} as a json to the build-info repo and triggers the interceptor logic to handle
     * the build itself
     */
    @Lock
    void addBuild(Build build);

    /**
     * Persists the changes made to the given existing build configuration
     *
     * @param build Existing build configuration
     */
    void updateBuild(@Nonnull Build build);

    /**
     * Returns the JSON string of the given build details
     *
     * @param buildRun@return Build JSON if parsing succeeded. Empty string if not
     */
    String getBuildAsJson(BuildRun buildRun);

    /**
     * Removes all the builds of the given name
     *
     * @param buildName         Name of builds to remove
     * @param deleteArtifacts   True if build artifacts should be deleted
     */
    void deleteAllBuildsByName(String buildName, boolean deleteArtifacts, BasicStatusHolder status);

    /**
     * Delete multiple builds in one transaction
     * @param buildsNames      list of builds Names to delete
     * @param deleteArtifacts  True if build artifacts should be deleted
     */
    void deleteAllBuildsByName(List<String> buildsNames, boolean deleteArtifacts, BasicStatusHolder status);

    /**
     * Removes the build of the given details, specific build number only
     *
     * @param buildRun          Build info details
     * @param deleteArtifacts   True if build artifacts should be deleted
     * @param basicStatusHolder Status holder
     * @param async             execute delete build either sync or async
     */
    void deleteBuildNumberByRetention(BuildRun buildRun, boolean deleteArtifacts, boolean async, BasicStatusHolder basicStatusHolder);

    /**
     */
    @Lock
    void deleteBuild(BuildRun build, boolean deleteArtifacts, BasicStatusHolder status);

    /**
     * Removes all artifacts related to build
     */
    void removeBuildArtifacts(BuildRun buildRun, BasicStatusHolder status);

    /**
     * Returns the build of the given details
     * With permissions check.
     *
     * @param buildRun Build run retrieved
     * @return Build if found. Null if not
     */
    Build getBuild(BuildRun buildRun);

    ImportableExportableBuild getExportableBuild(BuildRun buildRun);

    /**
     * With permission check
     */
    List<String> getBuildNames();

    /**
     * Returns the latest build for the given name and number
     * With permissions check
     *
     * @param buildName   Name of build to locate
     * @param buildNumber Number of build to locate
     * @return Latest build if found. Null if not
     */
    @Nullable
    Build getLatestBuildByNameAndNumber(String buildName, String buildNumber);

    /**
     * Locates builds that are named as the given name. Sorts the results by started date in reverse order.
     * With permissions check
     *
     * @param buildName Name of builds to locate
     * @return Set of builds with the given name
     */
    SortedSet<BuildRun> searchBuildsByName(String buildName);

    /**
     * Locates builds that are named and numbered as the given name and number
     *
     * @param buildName   Name of builds to locate
     * @param buildNumber Number of builds to locate
     * @return Set of builds with the given name
     */
    Set<BuildRun> searchBuildsByNameAndNumber(String buildName, String buildNumber);

    /**
     * Locates builds that are named and numbered as the given name and number without permission validation
     *
     * @param buildName   Name of builds to locate
     * @param buildNumber Number of builds to locate
     * @return Set of builds with the given name
     */
    Set<BuildRun> searchBuildsByNameAndNumberInternal(String buildName, String buildNumber);

    /**
     * With permission check
     */
    BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted);

    @Override
    void exportTo(ExportSettings settings);

    @Override
    void importFrom(ImportSettings settings);

    /**
     * Promotes a build
     *
     * @param buildRun  Basic info of build to promote
     * @param promotion Promotion settings
     * @return Promotion result
     */
    PromotionResult promoteBuild(BuildRun buildRun, Promotion promotion);

    /**
     * Renames the structure and content of build info objects
     *
     * @param from Name to replace
     * @param to   Replacement build name
     */
    void renameBuilds(String from, String to);

    @Lock
    void addPromotionStatus(Build build, PromotionStatus promotion);

    @Nullable
    List<PublishedModule> getPublishedModules(String buildName, String buildStarted, String name);

    List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String moduleId, String buildStarted);

    List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String buildStarted, String buildName);

    List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams);

    List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams);

    List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate);

    List<BuildProps> getBuildPropsData(BuildParams buildParams);

    List<BuildId> getLatestBuildIDsPaging(ContinueBuildFilter continueBuildFilter);

    List<BuildId> getBuildIDsByName(String buildName, String fromDate, String toDate, long limit, String orderBy, String direction);

    List<GeneralBuild> getBuildVersions(BuildsSearchFilter filter);

    List<GeneralBuild> getBuildForName(String buildName, ContinueBuildFilter continueBuildFilter) throws SQLException;

    /**
     * Retrieves all build artifacts and filters out missing entries (Artifacts that don't exist return a null
     * FileInfo mapping).
     * Logs missing artifacts with level warn
     *
     * @param build         Build to retrieve artifacts for.
     * @param sourceRepos   only include results from these repositories, not mandatory
     * @param excludedRepos exclude results from these repositories, not mandatory
     * @param status        StatusHolder for logging.
     * @return List of FileInfo objects that represent this build's (found) artifacts
     */
    List<FileInfo> collectBuildArtifacts(Build build, @Nullable List<String> sourceRepos,
            @Nullable List<String> excludedRepos, @Nullable BasicStatusHolder status);

    void assertBasicReadPermissions(String buildName, String buildNumber, String buildStarted);

    void assertReadPermissions(String buildName, String buildNumber, String buildStarted);

    void assertDeletePermissions(String buildName, String buildNumber, String buildStarted);

    void assertUploadPermissions(String buildName, String buildNumber, String buildStarted);

    /**
     * *UI USAGE*
     *
     * @return general info for build denoted by {@param buildName}, {@param buildNumber}, {@param buildStarted}.
     * ***Only basic-read permission check is done***
     */
    BuildGeneralInfo getBuildGeneralInfo(String buildName, String buildNumber, String buildStarted);
}
