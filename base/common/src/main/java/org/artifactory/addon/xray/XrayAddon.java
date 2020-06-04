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

package org.artifactory.addon.xray;

import com.google.common.collect.Lists;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.artifactory.addon.Addon;
import org.artifactory.addon.license.EdgeBlockedAddon;
import org.artifactory.api.repo.Async;
import org.artifactory.exception.UnsupportedOperationException;
import org.artifactory.repo.RepoPath;
import org.jfrog.build.api.Build;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * @author Chen Keinan
 */
@EdgeBlockedAddon
public interface XrayAddon extends Addon {

    //The user that's set up in Artifactory to allow Xray access to various resources
    String ARTIFACTORY_XRAY_USER = "xray";

    default boolean isXrayConfigMissing() {
        return false;
    }

    /**
     * @return True if Xray is configured in descriptor and enabled
     */
    default boolean isXrayEnabled() {
        return false;
    }

    /**
     * @return True if Xray client reached Xray server and is in stable mode
     */
    default boolean isXrayAlive() {
        return false;
    }

    /**
     * @return A message that explains why Xray is not alive, null otherwise (in case there is no problem)
     */
    @Nullable
    default String getOfflineMessage() {
        return null;
    }

    /**
     * @return True if Xray server is running with a compatible version with Xray client
     */
    default boolean isXrayVersionValid() {
        return false;
    }

    default CloseableHttpResponse scanBuild(XrayScanBuild xrayScanBuild) throws IOException {
        return null;
    }

    default String createXrayUser() {
        return null;
    }

    default void removeXrayConfig() {
    }

    default void deleteXrayUser(String xrayUser) {
    }

    default void setXrayEnabledOnRepos(List<XrayRepo> repos, boolean enabled) {
    }

    /**
     * Get the list of repositories that should be enabled with 'xrayIndex'
     *
     * @param repos The list of repositories that should be enabled with 'xrayIndex'
     */
    default void updateXraySelectedIndexedRepos(List<XrayRepo> repos) {
    }

    /**
     * Get the number of artifact that are potential for xray index
     *
     * @param repoKey The repository to search on
     * @return The count result
     */
    default int getXrayPotentialCountForRepo(String repoKey) throws UnsupportedOperationException {
        return 0;
    }

    /**
     * Cleans all "xray.*" properties from DB asynchronously
     */
    default void cleanXrayPropertiesFromDB() {
    }

    /**
     * Calls the Xray client to invalidate all it's caches
     */
    default void cleanXrayClientCaches() {
    }

    /**
     * List of repositories that are of type that is supported by Xray server connected to Artifactory.
     * Can be called only if Xray integration is enabled and connected to Xray.
     * Otherwise list will be empty (warn message will be printed).
     *
     * @param indexed look for repos that are marked enabled Xray or not
     */
    default List<XrayRepo> getXrayIndexedAndNonIndexed(boolean indexed) {
        return Lists.newArrayList();
    }

    /**
     * Sends Xray event for a new build
     *
     * @param build holds the build name and number
     */
    default void callAddBuildInterceptor(Build build) {
    }

    /**
     * Sends Xray event for build deletion
     *
     * @param buildName   Build Name
     * @param buildNumber Build Number
     */
    default void callDeleteBuildInterceptor(String buildName, String buildNumber) {
    }

    @Async
    void indexRepos(List<String> repos);

    default void clearAllIndexTasks() {
    }

    default void blockXrayGlobally() {
        throw new java.lang.UnsupportedOperationException("Block Xray globally requires the Xray resources add-on.");
    }

    default void unblockXrayGlobally() {
        throw new java.lang.UnsupportedOperationException("Unblock Xray globally requires the Xray resources add-on.");
    }

    @Nonnull
    default ArtifactXrayInfo getArtifactXrayInfo(@Nonnull RepoPath path) {
        return ArtifactXrayInfo.EMPTY;
    }

    @Nonnull
    default XrayArtifactsSummary getArtifactXraySummary(String sha2, List<RepoPath> repoPaths,
            String version, String type, boolean includeSummary) {
        return new XrayArtifactsSummary();
    }

    /**
     * return npm audit from xray
     *
     * @param isQuick                 - is quick audit
     * @param auditRequestData        - client request data
     * @param optionalReportToEnhance - null or audit report from npm registry
     * @return enhanced audit report or null if failed
     */
    @Nullable
    default CloseableHttpResponse npmAuditReport(boolean isQuick, String auditRequestData,
            String optionalReportToEnhance) {
        return null;
    }

    /**
     * @return true if {@param path} should be blocked for download according to its storing repo's xray configuration.
     */
    default boolean isDownloadBlocked(RepoPath path) {
        return false;
    }

    default ArtifactXrayDownloadStatus isDownloadBlockedWithReason(RepoPath path) {
        return ArtifactXrayDownloadStatus.VALID;
    }

    /**
     * true if xray can index this file
     */
    default boolean isHandledByXray(RepoPath path) {
        return false;
    }

    /**
     * Sets alert ignore on {@param path} via Xray client
     * It will ignore the current alert only.
     */
    default boolean setAlertIgnored(RepoPath path) {
        return false;
    }

    default boolean isAllowBlockedArtifactsDownload() {
        return false;
    }

    default boolean updateAllowBlockedArtifactsDownload(boolean allow) {
        return false;
    }

    default boolean updateAllowDownloadWhenUnavailable(boolean allow) {
        return false;
    }

    default boolean setBlockUnscannedArtifactsDownloadTimeout(Integer seconds) {
        return false;
    }

    /**
     * This method invokes an update request from Artifactory to Xray to fetch latest repositories policies.
     * It will also trigger a propagation to other nodes to perform the same for their Xray client.
     *
     * @param internal In case True: will not propagate to other nodes (means the request came from other Artifactory
     *                 server that got the original request)
     */
    default boolean createPolicyChangeNotification(boolean internal) {
        return false;
    }

    /**
     * @return true if {@param folder} should be blocked for download:
     * we return true in case there is at least one file under the folder that is blocked for download in Xray
     */
    default boolean isBlockedFolder(RepoPath folder) {
        return false;
    }

    /**
     * @return true if there is xray config for repo and it is enabled and Xray global integration is enabled
     * false otherwise
     */
    default boolean isXrayConfPerRepoAndIntegrationEnabled(String repoKey) {
        return false;
    }

    default boolean isDecryptionNeeded(String username) {
        return true;
    }

    /**
     * retrieves xray version previously fetched by the most recent heartbeat against Xray server
     *
     * @return String representing Xray version or 'Unavailable' if not available
     */
    String getXrayVersion();
}
