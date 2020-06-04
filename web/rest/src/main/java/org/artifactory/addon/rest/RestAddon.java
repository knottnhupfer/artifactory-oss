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

package org.artifactory.addon.rest;

import org.artifactory.addon.Addon;
import org.artifactory.addon.license.LicenseStatus;
import org.artifactory.addon.plugin.ResponseCtx;
import org.artifactory.api.download.FolderDownloadResult;
import org.artifactory.api.repo.Async;
import org.artifactory.api.repo.exception.BlackedOutException;
import org.artifactory.api.rest.artifact.ItemPermissions;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.api.rest.build.artifacts.BuildArtifactsRequest;
import org.artifactory.api.rest.replication.ReplicationRequest;
import org.artifactory.api.rest.search.result.ArtifactVersionsResult;
import org.artifactory.api.rest.search.result.LicensesSearchResult;
import org.artifactory.api.search.exception.InvalidChecksumException;
import org.artifactory.aql.AqlException;
import org.artifactory.download.FolderDownloadException;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.resource.artifact.legacy.DownloadResource;
import org.artifactory.rest.resource.security.AuthDelegationHandler;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.dependency.BuildPatternArtifactsRequest;
import org.jfrog.build.api.release.Promotion;
import org.jfrog.common.StringList;
import org.jfrog.common.archive.ArchiveType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * An interface that holds all the REST related operations that are available only as part of Artifactory's Add-ons.
 *
 * @author Tomer Cohen
 */
public interface RestAddon extends Addon {
    /**
     * Copy an artifact from one path to another.
     *
     * @param path            The source path of the artifact.
     * @param target          The target repository where to copy/move the Artifact to.
     * @param dryRun          A flag to indicate whether to perform a dry run first before performing the actual
     *                        action.
     * @param suppressLayouts Indicates whether path translation across different layouts should be suppressed.
     * @param failFast        Indicates whether the operation should fail upon encountering an error.
     * @param atomic          Indicates whether copy should run in one single tx
     * @return A JSON object of all the messages and errors that occurred during the action.
     * @throws Exception If an error occurred during the dry run or the actual action an exception is thrown.
     */
    Response copy(String path, String target, int dryRun, int suppressLayouts, int failFast, String atomic) throws Exception;

    /**
     * Move an artifact from one path to another.
     *
     * @param path            The source path of the artifact.
     * @param target          The target repository where to copy/move the Artifact to.
     * @param dryRun          A flag to indicate whether to perform a dry run first before performing the actual
     *                        action.
     * @param suppressLayouts Indicates whether path translation across different layouts should be suppressed.
     * @param failFast        Indicates whether the operation should fail upon encountering an error.
     * @return A JSON object of all the messages and errors that occurred during the action.
     * @throws Exception If an error occurred during the dry run or the actual action an exception is thrown.
     */
    Response move(String path, String target, int dryRun, int suppressLayouts, int failFast, String atomic) throws Exception;

    /**
     * @deprecated use {@link RestAddon#replicate(org.artifactory.repo.RepoPath, org.artifactory.api.rest.replication.ReplicationRequest)} instead
     */
    Response download(String path, DownloadResource.Content content, int mark,
            HttpServletResponse response) throws Exception;

    /**
     * Search for artifacts within a repository matching a given pattern.<br> The pattern should be like
     * repo-key:this/is/a/pattern
     *
     * @param pattern Pattern to search for
     * @return Set of matching artifact paths relative to the repo
     */
    Set<String> searchArtifactsByPattern(String pattern) throws ExecutionException, TimeoutException,
            InterruptedException;

    /**
     * Promotes a build
     *
     * @param buildName   Name of build to promote
     * @param buildNumber Number of build to promote
     * @param promotion   Promotion settings
     * @return Promotion result
     */
    PromotionResult promoteBuild(String buildName, String buildNumber, Promotion promotion) throws ParseException;

    @Deprecated
    Response replicate(RepoPath repoPath, ReplicationRequest replicationRequest) throws IOException;

    /**
     * Schedule an immediate replication, support remote, push and multi-push replication
     *
     * @param repoPath            Repository path
     * @param replicationRequests List of ReplicationRequest objects
     * @return Response
     * @throws IOException
     */
    Response replicate(RepoPath repoPath, List<ReplicationRequest> replicationRequests, String forcedStrategy) throws IOException;

    /**
     * Renames structure, content and properties of build info objects. The actual rename is done asynchronously.
     *
     * @param from Name to replace
     * @param to   Replacement build name
     */
    void renameBuilds(String from, String to);

    /**
     * Renames structure, content and properties of build info objects in an asynchronous manner.
     *
     * @param from Name to replace
     * @param to   Replacement build name
     */
    @Async(transactional = true)
    void renameBuildsAsync(String from, String to);

    /**
     * Deletes the build with given name and number
     *
     * @param response
     * @param buildName    Name of build to delete
     * @param buildNumbers Numbers of builds to delete
     * @param artifacts    1 if build artifacts should be deleted
     * @param deleteAll    1 if should delete all the builds
     */
    void deleteBuilds(HttpServletResponse response, String buildName, List<String> buildNumbers, int artifacts,
            int deleteAll) throws IOException;

    /**
     * Returns the latest modified item of the given file or folder (recursively)
     *
     * @param pathToSearch Repo path to search in
     * @return Latest modified item
     */
    org.artifactory.fs.ItemInfo getLastModified(String pathToSearch);

    /**
     * Find licenses in repositories, if empty, a scan of all repositories will take place.
     *
     * @param status            A container to hold the different license statuses.
     * @param repos             The repositories to scan, if empty, all repositories will be scanned.
     * @param servletContextUrl The contextUrl of the server.
     * @return The search results.
     */
    LicensesSearchResult findLicensesInRepos(LicenseStatus status, Set<String> repos, String servletContextUrl);

    /**
     * Delete a repository via REST.
     *
     * @param repoKey The repokey that is associated to the repository that is wanted for deletion.
     */
    Response deleteRepository(String repoKey);

    /**
     * Get Repository configuration according to the repository key in conjunction with the media type to enforce a
     * certain type of repository configuration. Admin user should get the full config, while non admin user with
     * partial permissions should get a response with limited configurations.
     *
     * @param repoKey   The repokey of the repository.
     * @param mediaType The acceptable media type for this request
     * @return The response with the configuration embedded in it.
     */
    Response getRepositoryConfiguration(String repoKey, MediaType mediaType);

    /**
     * Returns Repository configuration according to the repository key and media type.
     * This method returns ONLY the fields belongs to the specific repository type.
     * Admin user should get the full config, while non admin user with partial permissions should get a response
     * with limited configurations.
     *
     * @param repoKey   The repoKey of the repository.
     * @param mediaType The acceptable media type for this request
     * @return The response with the configuration embedded in it.
     */
    Response getRepositoryConfigurationV2(String repoKey, MediaType mediaType);

    /**
     * Create or replace an existing repository via REST.
     *
     * @param repoKey
     * @param repositoryConfig Map of attributes.
     * @param mediaType        The mediatypes of which are applicable. {@link org.artifactory.api.rest.constant.RepositoriesRestConstants}
     * @param position         The position in the map that the newly created repository will be placed
     */
    Response createOrReplaceRepository(String repoKey, Map repositoryConfig, MediaType mediaType, int position);

    /**
     * Update an existing repository via REST.
     *
     * @param repoKey          The repokey of the repository to be updated.
     * @param repositoryConfig The repository config of what is to be updated.
     * @param mediaType        The acceptable media type for this REST command.
     * @return The response for this command.
     */
    Response updateRepository(String repoKey, Map repositoryConfig, MediaType mediaType);

    /**
     * Search for artifacts by their checksums
     *
     * @param md5Checksum   MD5 checksum value
     * @param sha1Checksum  SHA1 checksum value
     * @param reposToSearch Specific repositories to search in
     * @return Set of repo paths matching the given checksum
     */
    Set<RepoPath> searchArtifactsByChecksum(String md5Checksum, String sha1Checksum, String sha256,
            StringList reposToSearch) throws
            InvalidChecksumException;

    /**
     * Search the repository(ies) for artifacts which have a mismatch between their server generated checksums and their
     * client generated checksums, this can result from an inequality or if one is missing.
     *
     * @param type          the type of checksum to search for (md5, sha1).
     * @param reposToSearch The list of repositories to search for the corrupt artifacts, if empty all repositories will
     *                      be searched
     * @param request       The request
     * @return The response object with the result as its entity.
     */
    @Nonnull
    Response searchBadChecksumArtifacts(String type, StringList reposToSearch, HttpServletRequest request);

    ResponseCtx runPluginExecution(String executionName, String method, Map params, @Nullable ResourceStreamHandle body,
            boolean async);

    Response getStagingStrategy(String strategyName, String buildName, Map params);

    /**
     * Used by ArtifactResource for retrieving locally stored(cached) item permissions.
     */
    ItemPermissions getItemPermissions(HttpServletRequest request, String path);

    Response searchDependencyBuilds(HttpServletRequest request, @Nullable String sha1, @Nullable String sha2) throws UnsupportedEncodingException;

    Response calculateYumMetadata(String repoKey, String path, boolean async, String passphrase);

    Response getPermissionTargetV2(String entityKey);

    Response getAllPermissionTargetsV2(HttpServletRequest request);

    Response getUserPermissionsSecurityV2(String entityKey);

    Response getGroupPermissionsSecurityV2(String entityKey);

    Response deletePermissionTargetV2(String entityKey);

    Response isPermissionTargetExistsV2(String entityKey);

    Response createPermissionTargetV2(String entityType, PermissionTargetModel permissionTargetModel);

    Response updatePermissionTargetV2(String entityType, PermissionTargetModel permissionTargetModel);

    /**
     * Returns the latest replication status information
     *
     * @param repoPath Item to check for information annotations
     * @return Response
     */
    Response getReplicationStatus(RepoPath repoPath);

    /**
     * Handles calculating maven index REST requests
     *
     * @param reposToIndex Keys of repositories to index
     * @param force        force indexer execution
     * @return Response
     */
    Response runMavenIndexer(List<String> reposToIndex, int force);

    /**
     * Handles requests for active user plugin info
     *
     * @param pluginType Specific plugin type to return the info for; All types if none is specified
     * @return Response
     */
    Response getUserPluginInfo(@Nullable String pluginType);

    /**
     * Returns the outputs of build matching the request
     *
     * @param buildPatternArtifactsRequest contains build name and build number or keyword
     * @param servletContextUrl            for building urls of current Artifactory
     * @return build outputs (build dependencies and generated artifacts)
     */
    @Nullable
    BuildPatternArtifacts getBuildPatternArtifacts(@Nonnull BuildPatternArtifactsRequest buildPatternArtifactsRequest,
            @Nonnull String servletContextUrl);

    /**
     * Returns diff object between two given builds (same build name, different numbers)
     *
     * @param firstBuild  The first build to compare, must be newer than the second build
     * @param secondBuild The second build to compare against
     * @param request     The request to extract the base uri from
     */
    Response getBuildsDiff(Build firstBuild, Build secondBuild, HttpServletRequest request);

    /**
     * Returns build artifacts map according to the param input regexp patterns.
     *
     * @param buildArtifactsRequest A wrapper which contains the necessary parameters
     * @return A map from {@link FileInfo}s to their target directories relative paths
     * @see BuildArtifactsRequest
     */
    Map<FileInfo, String> getBuildArtifacts(BuildArtifactsRequest buildArtifactsRequest);

    /**
     * Returns an archive file according to the param archive type (zip/tar/tar.gz/tgz) which contains
     * all build artifacts according to the given build name and number (can be latest or latest by status).
     *
     * @param buildArtifactsRequest A wrapper which contains the necessary parameters
     * @return The archived file of build artifacts with their hierarchy rules
     * @see BuildArtifactsRequest
     */
    File getBuildArtifactsArchive(BuildArtifactsRequest buildArtifactsRequest) throws IOException;

    /**
     * Sends back an {@link InputStream} that streams the entire content of the folder or repo, archived according to
     * {@param archiveType} and filtered by the user's authentication.
     *
     * @param pathToDownload       - path to download (folder or repo)
     * @param archiveType          - how to archive the path
     * @param includeChecksumFiles - whether to include .md5 and .sha1 files
     * @throws IOException
     * @throws FolderDownloadException
     */
    FolderDownloadResult downloadFolderOrRepo(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws IOException, FolderDownloadException;

    /**
     * Invokes a user plugin based build promotion action
     *
     * @param promotionName Name of closure
     * @param buildName     Name of build to promote
     * @param buildNumber   Number of build to promote
     * @param params        Promotion params
     * @return Response context
     */
    ResponseCtx promoteBuildPlugin(String promotionName, String buildName, String buildNumber, Map params);

    ResponseCtx deployPlugin(Reader pluginContent, String scriptName);

    /**
     * Reloads user plugins. Nothing is reloaded if there's no plugin present or no plugin modified since the last reload.
     *
     * @return Response context with status for various reloaded user plugins.
     */
    ResponseCtx reloadPlugins();

    /**
     * Searches for artifact versions by it's groupId and artifactId (version is optional and relates to
     * integration versions only). The results are sorted from latest to oldest (latest is first).
     *
     * @param groupId       the groupId of the artifact
     * @param artifactId    the artifactId of the artifact
     * @param version       the artifact version, if null then perform the search on all available versions
     * @param reposToSearch limit the search to specific repos, if null then performs the search on all real repos
     * @param remote        whether to fetch maven-metadata from remote repository or not
     * @param limitSearch
     * @return A wrapper class of the search results
     */
    ArtifactVersionsResult getArtifactVersions(String groupId, String artifactId, @Nullable String version,
            @Nullable StringList reposToSearch, boolean remote, boolean limitSearch);

    void writeStreamingFileList(HttpServletResponse response, String requestUrl, String path, int deep, int depth,
            int listFolders, int mdTimestamps, int statsTimestamps, int includeRootPath) throws IOException, BlackedOutException;

    Response getLatestVersionByProperties(String repoKey, String path, Map<String, String[]> parameterMap,
            HttpServletRequest request) throws AqlException;

    /**
     * @param pluginName The plugin name
     * @return The content of the specified user plugin by its name (with or without the .groovy extension).
     */
    ResponseCtx downloadPlugin(String pluginName);

    AuthDelegationHandler getAuthDelegationHandler(HttpServletRequest request);
}
