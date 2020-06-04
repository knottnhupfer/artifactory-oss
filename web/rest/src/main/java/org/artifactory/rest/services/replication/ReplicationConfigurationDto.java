package org.artifactory.rest.services.replication;

import lombok.Data;
import org.artifactory.addon.replication.ReplicationType;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;

import static org.artifactory.addon.replication.ReplicationType.PULL;
import static org.artifactory.addon.replication.ReplicationType.PUSH;

@Data
public class ReplicationConfigurationDto {
    private ReplicationType replicationType;
    private boolean enabled;
    private String cronExp;
    private boolean syncDeletes;
    private boolean syncProperties;
    private String pathPrefix;
    private String repoKey;
    private boolean enableEventReplication;
    private boolean checkBinaryExistenceInFilestore;
    //local replication specific
    private String url;
    private boolean syncStatistics;

    public ReplicationConfigurationDto(){

    }

    public ReplicationConfigurationDto(LocalReplicationDescriptor localReplicationDescriptor){
        replicationType = PUSH;
        enabled = localReplicationDescriptor.isEnabled();
        cronExp = localReplicationDescriptor.getCronExp();
        syncDeletes = localReplicationDescriptor.isSyncDeletes();
        syncProperties = localReplicationDescriptor.isSyncProperties();
        pathPrefix = localReplicationDescriptor.getPathPrefix();
        repoKey = localReplicationDescriptor.getRepoKey();
        enableEventReplication = localReplicationDescriptor.isEnableEventReplication();
        checkBinaryExistenceInFilestore = localReplicationDescriptor.isCheckBinaryExistenceInFilestore();
        url = localReplicationDescriptor.getUrl();
        syncStatistics = localReplicationDescriptor.isSyncStatistics();
    }

    public ReplicationConfigurationDto(RemoteReplicationDescriptor remoteReplicationDescriptor){
        replicationType = PULL;
        enabled = remoteReplicationDescriptor.isEnabled();
        cronExp = remoteReplicationDescriptor.getCronExp();
        syncDeletes = remoteReplicationDescriptor.isSyncDeletes();
        syncProperties = remoteReplicationDescriptor.isSyncProperties();
        pathPrefix = remoteReplicationDescriptor.getPathPrefix();
        repoKey = remoteReplicationDescriptor.getRepoKey();
        enableEventReplication = remoteReplicationDescriptor.isEnableEventReplication();
        checkBinaryExistenceInFilestore = remoteReplicationDescriptor.isCheckBinaryExistenceInFilestore();
    }
}
