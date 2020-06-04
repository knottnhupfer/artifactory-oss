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

package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.rest.release.ReleaseBundlesConfigModel;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.descriptor.replication.GlobalReplicationsConfigDescriptor;
import org.artifactory.descriptor.signature.SignedUrlConfig;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.ArtifactoryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Yoaz Menda
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateArtifactoryConfigService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;
    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ArtifactoryConfig artifactoryConfig = (ArtifactoryConfig) request.getImodel();
        // update general setting and set config descriptor
        updateDescriptorAndSave(artifactoryConfig);
        response.info("Successfully updated Artifactory settings");
    }

    /**
     * update config descriptor with general config setting and save
     *
     * @param artifactoryConfig - general setting sent from client
     */
    private void updateDescriptorAndSave(ArtifactoryConfig artifactoryConfig) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        mutableDescriptor.setDateFormat(artifactoryConfig.getDateFormat());
        mutableDescriptor.setFileUploadMaxSizeMb(artifactoryConfig.getFileUploadMaxSize());
        mutableDescriptor.setOfflineMode(artifactoryConfig.isGlobalOfflineMode());
        mutableDescriptor.getAddons().setShowAddonsInfo(artifactoryConfig.isShowAddonSettings());
        mutableDescriptor.setLogo(artifactoryConfig.getLogoUrl());
        mutableDescriptor.setHelpLinksEnabled(artifactoryConfig.isHelpLinksEnabled());
        // update bintray config descriptor
        updateBintrayDescriptor(artifactoryConfig, mutableDescriptor);
        //System message config
        updateSystemMessageConfig(artifactoryConfig, mutableDescriptor);
        //Folder download config
        updateFolderDownloadConfig(artifactoryConfig, mutableDescriptor);
        //Trashcan config
        updateTrashcanConfig(artifactoryConfig, mutableDescriptor);
        //Update global replication config
        updateGlobalReplicationConfig(artifactoryConfig, mutableDescriptor);
        // Update Sumo Logic config
        updateSumoLogicConfig(artifactoryConfig, mutableDescriptor);
        // Update Release Bundles config
        updateReleaseBundlesConfig(artifactoryConfig, mutableDescriptor);
        updateSignedUrlConfig(artifactoryConfig, mutableDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    private void updateBintrayDescriptor(ArtifactoryConfig artifactoryConfig,
            MutableCentralConfigDescriptor mutableDescriptor) {
        BintrayConfigDescriptor bintrayMutableDescriptor = Optional.ofNullable(mutableDescriptor.getBintrayConfig())
                .orElse(new BintrayConfigDescriptor());
        bintrayMutableDescriptor.setFileUploadLimit(artifactoryConfig.getBintrayFilesUploadLimit());
        mutableDescriptor.setBintrayConfig(bintrayMutableDescriptor);
    }

    //Does not override defaults if UI sent empty model.
    private void updateSystemMessageConfig(ArtifactoryConfig artifactoryConfig,
            MutableCentralConfigDescriptor descriptor) {
        SystemMessageDescriptor systemMessageDescriptor =
                Optional.ofNullable(descriptor.getSystemMessageConfig()).orElse(new SystemMessageDescriptor());
        systemMessageDescriptor.setEnabled(Optional.ofNullable(
                artifactoryConfig.isSystemMessageEnabled()).orElse(systemMessageDescriptor.isEnabled()));
        systemMessageDescriptor.setTitle(Optional.ofNullable(
                artifactoryConfig.getSystemMessageTitle()).orElse(systemMessageDescriptor.getTitle()));
        systemMessageDescriptor.setTitleColor(Optional.ofNullable(
                artifactoryConfig.getSystemMessageTitleColor()).orElse(systemMessageDescriptor.getTitleColor()));
        systemMessageDescriptor.setMessage(Optional.ofNullable(
                artifactoryConfig.getSystemMessage()).orElse(systemMessageDescriptor.getMessage()));
        systemMessageDescriptor.setShowOnAllPages(Optional.ofNullable(
                artifactoryConfig.isShowSystemMessageOnAllPages()).orElse(systemMessageDescriptor.isShowOnAllPages()));
        descriptor.setSystemMessageConfig(systemMessageDescriptor);
    }

    //Does not override defaults if UI sent empty model.
    private void updateFolderDownloadConfig(ArtifactoryConfig artifactoryConfig,
            MutableCentralConfigDescriptor descriptor) {
        FolderDownloadConfigDescriptor folderDownloadConfig = descriptor.getFolderDownloadConfig();
        folderDownloadConfig.setEnabled(
                Optional.ofNullable(artifactoryConfig.isFolderDownloadEnabled())
                        .orElse(folderDownloadConfig.isEnabled()));
        folderDownloadConfig.setEnabledForAnonymous(
                Optional.ofNullable(artifactoryConfig.isFolderDownloadEnabledForAnonymous())
                        .orElse(folderDownloadConfig.isEnabledForAnonymous()));
        folderDownloadConfig.setMaxConcurrentRequests(Optional.ofNullable(
                artifactoryConfig.getFolderDownloadMaxConcurrentRequests())
                .orElse(folderDownloadConfig.getMaxConcurrentRequests()));
        folderDownloadConfig.setMaxDownloadSizeMb(Optional.ofNullable(
                artifactoryConfig.getFolderDownloadMaxSizeMb()).orElse(folderDownloadConfig.getMaxDownloadSizeMb()));
        folderDownloadConfig.setMaxFiles(Optional.ofNullable(
                artifactoryConfig.getMaxFolderDownloadFilesLimit()).orElse(folderDownloadConfig.getMaxFiles()));
        descriptor.setFolderDownloadConfig(folderDownloadConfig);
    }

    //Does not override defaults if UI sent empty model.
    private void updateTrashcanConfig(ArtifactoryConfig artifactoryConfig, MutableCentralConfigDescriptor descriptor) {
        TrashcanConfigDescriptor trashcanConfig = descriptor.getTrashcanConfig();
        trashcanConfig.setEnabled(Optional.ofNullable(
                artifactoryConfig.getTrashcanEnabled()).orElse(trashcanConfig.isEnabled()));
        trashcanConfig.setAllowPermDeletes(Optional.ofNullable(
                artifactoryConfig.getAllowPermDeletes()).orElse(trashcanConfig.isAllowPermDeletes()));
        trashcanConfig.setRetentionPeriodDays(Optional.ofNullable(
                artifactoryConfig.getTrashcanRetentionPeriodDays()).orElse(trashcanConfig.getRetentionPeriodDays()));
        descriptor.setTrashcanConfig(trashcanConfig);
    }

    //Does not override defaults if UI sent empty model.
    private void updateGlobalReplicationConfig(ArtifactoryConfig artifactoryConfig,
            MutableCentralConfigDescriptor descriptor) {
        GlobalReplicationsConfigDescriptor replicationsConfig = descriptor.getReplicationsConfig();
        replicationsConfig.setBlockPullReplications(Optional.ofNullable(
                artifactoryConfig.getBlockPullReplications()).orElse(replicationsConfig.isBlockPullReplications()));
        replicationsConfig.setBlockPushReplications(Optional.ofNullable(
                artifactoryConfig.getBlockPushReplications()).orElse(replicationsConfig.isBlockPushReplications()));

    }

    //Does not override defaults if UI sent empty model.
    private void updateSumoLogicConfig(ArtifactoryConfig artifactoryConfig, MutableCentralConfigDescriptor descriptor) {
        SumoLogicConfigDescriptor sumoLogicConfig = descriptor.getSumoLogicConfig();
        sumoLogicConfig.setEnabled(
                Optional.ofNullable(artifactoryConfig.getSumoLogicEnabled()).orElse(sumoLogicConfig.isEnabled()));
        sumoLogicConfig.setCollectorUrl(Optional.ofNullable(
                artifactoryConfig.getSumoLogicCollectorUrl()).orElse(sumoLogicConfig.getCollectorUrl()));

    }

    //Does not override defaults if UI sent empty model.
    private void updateReleaseBundlesConfig(ArtifactoryConfig artifactoryConfig,
            MutableCentralConfigDescriptor descriptor) {
        ReleaseBundlesConfigModel model = new ReleaseBundlesConfigModel(
                Optional.ofNullable(artifactoryConfig.getReleaseBundlesCleanup())
                        .orElse(descriptor.getReleaseBundlesConfig().getIncompleteCleanupPeriodHours()));
        addonsManager.addonByType(ReleaseBundleAddon.class).setReleaseBundlesConfig(model);
    }

    //Does not override defaults if UI sent empty model.
    private void updateSignedUrlConfig(ArtifactoryConfig artifactoryConfig, MutableCentralConfigDescriptor descriptor) {
        SignedUrlConfig signedUrlConfig = descriptor.getSignedUrlConfig();
        signedUrlConfig.setMaxValidForSeconds(Optional.ofNullable(artifactoryConfig.getSignedUrlMaxValidForSecs())
                .orElse(signedUrlConfig.getMaxValidForSeconds()));
    }

}
