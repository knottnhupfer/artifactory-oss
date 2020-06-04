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

package org.artifactory.addon;

import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.message.HaMessage;
import org.artifactory.schedule.Task;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.storage.fs.lock.FsItemsVault;

/**
 * @author mamo
 */
public interface HaAddon extends HaCommonAddon {

    String DEBIAN_RECALCULATE_ALL_NOW = "debianRecalculateAllNow";
    String DEBIAN_RECALCULATE_ALL_ASYNC = "debianRecalculateAllAsync";
    String DEBIAN_CACHE_UPDATE = "debianCacheUpdate";
    String XRAY_POLICIES_UPDATE = "xrayPoliciesUpdate";
    String OPKG_RECALCULATE_ALL_NOW = "opkgRecalculateAllNow";
    String OPKG_RECALCULATE_ALL_ASYNC = "opkgRecalculateAllAsync";
    String TRAFFIC_COLLECTOR = "trafficCollector";
    String PROPAGATE_TASK_EVENT = "propagateTask";
    String UI_DEPLOY = "uiDeploy";
    String SUPPORT_BUNDLE_GENERATE = "supportBundleGenerate";
    String LICENSE_CHANGED = "licenseChanged";
    String ADD_REPLICATION_LISTENER = "addReplicationListener";
    String REMOVE_REPLICATION_LISTENER = "removeReplicationListener";
    String PULL_REPLICATION_EVENTS = "removeReplicationListener";
    String STOP_SHA2_MIGRATION = "stopSha256Migration";
    String SYNC_STORAGE_SUMMARY = "syncStorageSummary";

    @Override
    default void updateArtifactoryServerRole() {
    }

    @Override
    default void propagateLicenseChanges() {
    }

    @Override
    default boolean deleteArtifactoryServer(String serverId) {
        return false;
    }

    default void propagateTaskToPrimary(Task task) {
    }

    default void propagateTask(Task task, String serverId) {
    }

    default void initConfigBroadcast(ArtifactoryApplicationContext context) {
    }

    FsItemsVault getFsFileItemVault();

    FsItemsVault getFsFolderItemVault();

    /**
     * @return True if the current authenticated user is another HA node in the cluster
     */
    default boolean isHaAuthentication() {
        return false;
    }

    @Override
    default void propagateConfigReload() {
    }

    /**
     * Handle incoming propagation event from another server.
     *
     * @param haMessage The message propagated to this server from another cluster member
     */
    default void handleIncomingPropagationEvent(String topic, HaMessage haMessage) {
        throw new RuntimeException("Propagation is only supported on HA. Ignoring incoming propagation event");
    }

    default void init() {
    }
}
