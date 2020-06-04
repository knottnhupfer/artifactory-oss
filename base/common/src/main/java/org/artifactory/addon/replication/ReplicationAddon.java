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

package org.artifactory.addon.replication;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.Addon;
import org.artifactory.addon.jobs.JobStatus;
import org.artifactory.addon.license.EdgeBlockedAddon;
import org.artifactory.addon.replication.event.ReplicationChannelListener;
import org.artifactory.addon.replication.event.ReplicationEventQueueWorkItem;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.replication.ReplicationJobInfo;
import org.artifactory.api.rest.replication.ReplicationStatus;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.exception.MissingRestAddonException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
@EdgeBlockedAddon
public interface ReplicationAddon extends Addon, ReplicationChannelListener {

    //replication
    String PROP_REPLICATION_PREFIX = "artifactory.replication.";
    String PROP_REPLICATION_STARTED_SUFFIX = ".started";
    String PROP_REPLICATION_FINISHED_SUFFIX = ".finished";
    String PROP_REPLICATION_STATS_PROGRESS_SUFFIX = ".stats.progress";
    String PROP_REPLICATION_EVENT_PROGRESS_SUFFIX = ".event.progress";
    String PROP_REPLICATION_SIGNATURE_SUFFIX = ".sig";
    String PROP_REPLICATION_RESULT_SUFFIX = ".result";
    String TASK_MANUAL_DESCRIPTOR = "task_manual_settings";

    BasicStatusHolder performRemoteReplication(RemoteReplicationSettings settings) throws IOException;

    BasicStatusHolder performLocalReplication(PushReplicationSettings settings) throws IOException;

    void scheduleImmediateLocalReplicationTask(LocalReplicationDescriptor replicationDescriptor,
            BasicStatusHolder statusHolder);

    default void scheduleImmediateFullTreeLocalReplicationTask(LocalReplicationDescriptor replicationDescriptor,
            BasicStatusHolder statusHolder) {
    }

    default void setPushStrategy(LocalReplicationDescriptor replicationDescriptor, String strategy) {
    }

    void scheduleImmediateRemoteReplicationTask(RemoteReplicationDescriptor replicationDescriptor,
            BasicStatusHolder statusHolder);

    default ReplicationStatus getReplicationStatus(RepoPath repoPath) {
        return null;
    }

    default void offerLocalReplicationDeploymentEvent(RepoPath repoPath, boolean isFile) {
    }

    default void offerLocalReplicationMkDirEvent(RepoPath repoPath, boolean isFile) {
    }

    default void offerLocalReplicationDeleteEvent(RepoPath repoPath, boolean isFile) {
    }

    default void offerLocalReplicationPropertiesChangeEvent(RepoPath repoPath, boolean isFile) {
    }

    default void validateTargetIsDifferentInstance(ReplicationBaseDescriptor descriptor, RealRepoDescriptor repoDescriptor) throws IOException {
    }


    default void validateTargetLicense(ReplicationBaseDescriptor descriptor, RealRepoDescriptor repoDescriptor, int numOfReplicationConfigured) throws IOException {
    }

    /**
     * Check if license is HA (only HA license support multi-push). If license is non-HA, keep only one replication
     * within the list of replications
     *
     * @param pushReplications The list of replications to perform
     * @return BasicStatusHolder Status holder that aggregates messages.
     */
    BasicStatusHolder filterIfMultiPushIsNotAllowed(List<LocalReplicationDescriptor> pushReplications);

    /**
     * When a local replication is removed, or changes it's url call this method to get rid of it's (now) unused properties
     */
    default void cleanupLocalReplicationProperties(LocalReplicationDescriptor replication) {
    }

    default void handlePropagatedRemoteReplicationEvents(String originNode, ReplicationEventQueueWorkItem events) {
    }

    /**
     * Returns a set of all the remote event based replication event subscribers to the given repo
     */
    default Set<String> getRemoteRepoSubscribers(String repoKey) {
        return null;
    }

    /**
     * Release all of the remote event based replication event subscribers
     */
    default void releaseAllReposSubscribers() {
    }

    /**
     * return a list of active/finished/stopped replications
     */
    default List<ReplicationJobInfo> getReplicationJobs(String startedAfter, String finishedBefore, JobStatus jobStatus,
            ReplicationType replicationType, ReplicationStrategy replicationStrategy, String sourceRepo, String targetURL) {
        return null;
    }

    default Response getGlobalReplicationConfig() {
        throw new MissingRestAddonException();
    }

    default Response blockPushPull(String push, String pull) {
        throw new MissingRestAddonException();
    }

    default Response unblockPushPull(String push, String pull) {
        throw new MissingRestAddonException();
    }

    enum Overwrite {
        never, force
    }
}
