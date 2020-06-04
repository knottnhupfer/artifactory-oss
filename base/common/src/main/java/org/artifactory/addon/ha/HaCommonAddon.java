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

package org.artifactory.addon.ha;

import org.artifactory.addon.Addon;
import org.artifactory.addon.ha.message.HaMessage;
import org.artifactory.addon.ha.message.HaMessageTopic;
import org.artifactory.addon.ha.propagation.uideploy.UIDeployPropagationResult;
import org.artifactory.addon.ha.workitem.HaMessageWorkItem;
import org.artifactory.addon.replication.event.ReplicationEventQueueWorkItem;
import org.artifactory.addon.replication.event.ReplicationOwnerModel;
import org.artifactory.api.repo.Async;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.util.HttpUtils;
import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.jfrog.support.rest.model.SupportBundleGenerationResult;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mamo, gidi, fred
 */
public interface HaCommonAddon extends Addon {

    /**
     * Response header for an HA Artifactory Node
     */
    String ARTIFACTORY_NODE_ID = "X-Artifactory-Node-Id";

    /**
     * Server ID in case of a non HA Artifactory node
     */
    String ARTIFACTORY_PRO = "Artifactory";

    String SUPPORT_BUNDLE_SEMAPHORE_NAME = "supportBundleSemaphore";
    String STATS_SEMAPHORE_NAME = "flushStatsSemaphore";
    String STATS_REMOTE_SEMAPHORE_NAME = "flushRemoteStatsSemaphore";
    String INDEXING_SEMAPHORE_NAME = "indexingSemaphore";
    String INDEX_MARKED_ARCHIVES_SEMAPHORE_NAME = "indexMarkedArchivesSemaphore";
    String XRAY_EVENTS_SEMAPHORE_NAME = "xrayEventsSemaphore";

    int DEFAULT_SEMAPHORE_PERMITS = 1;

    // we block concurrent executions to prevent race condition
    // between delete(async) and list(),
    //
    // also there is no justification to perform concurrent execution
    // as we support taking several thread dumps with or without interval
    int SUPPORT_BUNDLE_SEMAPHORE_INITIAL_PERMITS = 1;

    /**
     * Determines if HA is enabled,
     * <p>that is {@link org.artifactory.common.ArtifactoryHome#ARTIFACTORY_HA_NODE_PROPERTIES_FILE} exists.
     *
     * @return {@code true} if the current Artifactory instance is HA enabled
     */
    default boolean isHaEnabled() {
        return false;
    }

    /**
     * @return {@code true} if HA is enabled and activated, and current Artifactory instance is the primary.
     *         <p>is HA is <b>not enabled</b>, return true
     */
    default boolean isPrimary() {
        return true;
    }

    /**
     * @return {@code true} if HA is configured.
     */
    default boolean isHaConfigured() {
        try {
            return ArtifactoryHome.get().isHaConfigured();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Similar to {@link HaCommonAddon#notify(HaMessageTopic, HaMessage)}, but async with a work queue.
     * To aggregate events, we implement the workItem as always, hashcode and equals are the same as the uniqueKey, however,
     * if you don't want to aggregate the events but we do want to make sure only one worker will work on a topic, you
     * should pass a workItem implementation that it's uniqueKey is the subject (worker per subject), but the equals and
     * hashcode are the actual content and not the subject name.
     */
    @Async(delayUntilAfterCommit = true, workQueue = true)
    void notifyAsync(HaMessageWorkItem workItem);

    //todo move into HaAddon, should not be a common interface
    default void notify(HaMessageTopic haMessageTopic, HaMessage haMessage) {
    }

    /**
     * @return A unique hostId for a Pro/OSS installation, a hashed token for an HA node
     */
    default String getHostId() {
        return HttpUtils.getHostId();
    }

    default SessionRepository<? extends Session> createSessionRepository(int sessionDuration) {
        MapSessionRepository mapSessionRepository = new MapSessionRepository(new ConcurrentHashMap<>());
        mapSessionRepository.setDefaultMaxInactiveInterval(sessionDuration);
        return mapSessionRepository;
    }

    default void shutdown() {
    }

    default List<ArtifactoryServer> getAllArtifactoryServers() {
        return new ArrayList<>();
    }

    boolean deleteArtifactoryServer(String id);

    default boolean artifactoryServerHasHeartbeat(ArtifactoryServer artifactoryServer) {
        return false;
    }

    default String getCurrentMemberServerId() {
        return null;
    }

    /**
     * Notify other nodes to update metadata cache for {@param path} so next index requests run on correct copy of metadata
     */
    default void propagateDebianUpdateCache(RepoPath path) {
    }

    default void propagateOpkgReindexAll(ArtifactoryServer server, String repoKey, boolean async, boolean writeProps) {
    }

    /**
     * Notify other nodes to update Xray client cache for repositories policies
     */
    default void propagateXrayPolicyChangeNotification() {
    }

    /**
     * In case where a UI upload request was started on another node (which means the file is in its local temp dir)
     * this call will propagate the second deploy request (when user presses the 'upload' button) to the relevant
     * {@param nodeId}.
     * @param payload       - The deploy request
     */
    default UIDeployPropagationResult propagateUiUploadRequest(String nodeId, String payload) {
        return null;
    }

    default void propagateArtifactoryEncryptionKeyChanged() {
    }

    /**
     * Tell other node to activate a license with excluding a specific license
     *
     * @param server      The server to communicate with
     * @param skipLicense The licenses to skip while trying to activate a license
     */
    default void propagateActivateLicense(ArtifactoryServer server, Set<String> skipLicense) {
    }

    default void propagatePluginReload() {
    }

    default List<SupportBundleGenerationResult> propagateGenerateSupportBundle(String bundleInfo, int timeout) {
        return Collections.emptyList();
    }

    default <T> List<T> propagateTrafficCollector(long startLong, long endLong, List<String> ipsToFilter,
                                                  List<ArtifactoryServer> servers, Class<T> clazz) {
        return null;
    }

    default <T> List<T> propagateTasksList(List<ArtifactoryServer> servers, Class<T> clazz) {
        return null;
    }

    default void forceOptimizationOnce() {
    }

    void propagateConfigReload();

    default void propagateGroupCacheInvalidation(String groupName) {

    }

    void updateArtifactoryServerRole();

    /**
     * Used to inform other nodes that the artifactory.cluster.licenses file has changed and a reload of the license
     * cache is required
     */
    void propagateLicenseChanges();

    ConflictGuard getConflictGuard(String key);

    <T>ConflictsGuard<T> getConflictsGuard(String mapName);

    void propagateReplicationListener(ReplicationOwnerModel replicationChannelModel);

    @Async
    void propagateRemoveReplicationListener(ReplicationOwnerModel replicationChannelModel);

    @Async
    void propagateReplicationEvents(String target, ReplicationEventQueueWorkItem queue);

    void propagateStopSha256Migration(long sleepIntervalMillis);

    void propagateDbProperties(boolean encrypt);

    default void propagateStorageSummaryCache(StorageSummaryInfo cache) {}
}
