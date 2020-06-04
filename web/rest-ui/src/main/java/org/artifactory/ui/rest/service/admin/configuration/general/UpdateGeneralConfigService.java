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
import org.artifactory.descriptor.download.DownloadRedirectConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.descriptor.replication.GlobalReplicationsConfigDescriptor;
import org.artifactory.descriptor.signature.SignedUrlConfig;
import org.artifactory.descriptor.subscription.SubscriptionConfig;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.GeneralConfig;
import org.artifactory.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateGeneralConfigService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;
    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        GeneralConfig generalConfig = (GeneralConfig) request.getImodel();
        // update general setting and set config descriptor
        updateDescriptorAndSave(generalConfig);
        response.info("Successfully updated settings");
    }

    /**
     * update config descriptor with general config setting and save
     *
     * @param generalConfig - general setting sent from client
     */
    private void updateDescriptorAndSave(GeneralConfig generalConfig) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        mutableDescriptor.setServerName(generalConfig.getServerName());
        mutableDescriptor.setDateFormat(generalConfig.getDateFormat());
        mutableDescriptor.setUrlBase(generalConfig.getCustomUrlBase());
        mutableDescriptor.setFileUploadMaxSizeMb(generalConfig.getFileUploadMaxSize());
        mutableDescriptor.setOfflineMode(generalConfig.isGlobalOfflineMode());
        mutableDescriptor.getAddons().setShowAddonsInfo(generalConfig.isShowAddonSettings());
        mutableDescriptor.setLogo(generalConfig.getLogoUrl());
        mutableDescriptor.setHelpLinksEnabled(generalConfig.isHelpLinksEnabled());
        // update bintray config descriptor
        updateBintrayDescriptor(generalConfig, mutableDescriptor);
        //System message config
        updateSystemMessageConfig(generalConfig, mutableDescriptor);
        //Folder download config
        updateFolderDownloadConfig(generalConfig, mutableDescriptor);
        //Trashcan config
        updateTrashcanConfig(generalConfig, mutableDescriptor);
        //Update global replication config
        updateGlobalReplicationConfig(generalConfig, mutableDescriptor);
        // Update Sumo Logic config
        updateSumoLogicConfig(generalConfig, mutableDescriptor);
        // Update Release Bundles config
        updateReleaseBundlesConfig(generalConfig, mutableDescriptor);
        updateSignedUrlConfig(generalConfig, mutableDescriptor);
        // Update Cloud storage download redirect
        updateDownloadRedirectConfig(generalConfig, mutableDescriptor);
        // Update Subscription config
        updateSubscriptionConfig(generalConfig, mutableDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    private void updateSubscriptionConfig(GeneralConfig generalConfig,
            MutableCentralConfigDescriptor mutableDescriptor) {
        if (generalConfig.getSubscription() != null
                && CollectionUtils.notNullOrEmpty(generalConfig.getSubscription().getEmails())) {

            SubscriptionConfig subscriptionConfig = new SubscriptionConfig();
            subscriptionConfig.setEmails(generalConfig.getSubscription().getEmails());
            mutableDescriptor.setSubscriptionConfig(subscriptionConfig);
        }
    }

    private void updateBintrayDescriptor(GeneralConfig generalConfig,
            MutableCentralConfigDescriptor mutableDescriptor) {
        BintrayConfigDescriptor bintrayMutableDescriptor = Optional.ofNullable(mutableDescriptor.getBintrayConfig())
                .orElse(new BintrayConfigDescriptor());
        bintrayMutableDescriptor.setFileUploadLimit(generalConfig.getBintrayFilesUploadLimit());
        mutableDescriptor.setBintrayConfig(bintrayMutableDescriptor);
    }

    //Does not override defaults if UI sent empty model.
    private void updateSystemMessageConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        SystemMessageDescriptor systemMessageDescriptor =
                Optional.ofNullable(descriptor.getSystemMessageConfig()).orElse(new SystemMessageDescriptor());
        systemMessageDescriptor.setEnabled(Optional.ofNullable(
                generalConfig.isSystemMessageEnabled()).orElse(systemMessageDescriptor.isEnabled()));
        systemMessageDescriptor.setTitle(Optional.ofNullable(
                generalConfig.getSystemMessageTitle()).orElse(systemMessageDescriptor.getTitle()));
        systemMessageDescriptor.setTitleColor(Optional.ofNullable(
                generalConfig.getSystemMessageTitleColor()).orElse(systemMessageDescriptor.getTitleColor()));
        systemMessageDescriptor.setMessage(Optional.ofNullable(
                generalConfig.getSystemMessage()).orElse(systemMessageDescriptor.getMessage()));
        systemMessageDescriptor.setShowOnAllPages(Optional.ofNullable(
                generalConfig.isShowSystemMessageOnAllPages()).orElse(systemMessageDescriptor.isShowOnAllPages()));
        descriptor.setSystemMessageConfig(systemMessageDescriptor);
    }

    //Does not override defaults if UI sent empty model.
    private void updateFolderDownloadConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        FolderDownloadConfigDescriptor folderDownloadConfig = descriptor.getFolderDownloadConfig();
        folderDownloadConfig.setEnabled(
                Optional.ofNullable(generalConfig.isFolderDownloadEnabled()).orElse(folderDownloadConfig.isEnabled()));
        folderDownloadConfig.setEnabledForAnonymous(
                Optional.ofNullable(generalConfig.isFolderDownloadEnabledForAnonymous())
                        .orElse(folderDownloadConfig.isEnabledForAnonymous()));
        folderDownloadConfig.setMaxConcurrentRequests(Optional.ofNullable(
                generalConfig.getFolderDownloadMaxConcurrentRequests())
                .orElse(folderDownloadConfig.getMaxConcurrentRequests()));
        folderDownloadConfig.setMaxDownloadSizeMb(Optional.ofNullable(
                generalConfig.getFolderDownloadMaxSizeMb()).orElse(folderDownloadConfig.getMaxDownloadSizeMb()));
        folderDownloadConfig.setMaxFiles(Optional.ofNullable(
                generalConfig.getMaxFolderDownloadFilesLimit()).orElse(folderDownloadConfig.getMaxFiles()));
        descriptor.setFolderDownloadConfig(folderDownloadConfig);
    }

    //Does not override defaults if UI sent empty model.
    private void updateTrashcanConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        TrashcanConfigDescriptor trashcanConfig = descriptor.getTrashcanConfig();
        trashcanConfig.setEnabled(Optional.ofNullable(
                generalConfig.getTrashcanEnabled()).orElse(trashcanConfig.isEnabled()));
        trashcanConfig.setAllowPermDeletes(Optional.ofNullable(
                generalConfig.getAllowPermDeletes()).orElse(trashcanConfig.isAllowPermDeletes()));
        trashcanConfig.setRetentionPeriodDays(Optional.ofNullable(
                generalConfig.getTrashcanRetentionPeriodDays()).orElse(trashcanConfig.getRetentionPeriodDays()));
        descriptor.setTrashcanConfig(trashcanConfig);
    }

    //Does not override defaults if UI sent empty model.
    private void updateGlobalReplicationConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        GlobalReplicationsConfigDescriptor replicationsConfig = descriptor.getReplicationsConfig();
        replicationsConfig.setBlockPullReplications(Optional.ofNullable(
                generalConfig.getBlockPullReplications()).orElse(replicationsConfig.isBlockPullReplications()));
        replicationsConfig.setBlockPushReplications(Optional.ofNullable(
                generalConfig.getBlockPushReplications()).orElse(replicationsConfig.isBlockPushReplications()));

    }

    //Does not override defaults if UI sent empty model.
    private void updateSumoLogicConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        SumoLogicConfigDescriptor sumoLogicConfig = descriptor.getSumoLogicConfig();
        sumoLogicConfig.setEnabled(
                Optional.ofNullable(generalConfig.getSumoLogicEnabled()).orElse(sumoLogicConfig.isEnabled()));
        sumoLogicConfig.setCollectorUrl(Optional.ofNullable(
                generalConfig.getSumoLogicCollectorUrl()).orElse(sumoLogicConfig.getCollectorUrl()));

    }

    //Does not override defaults if UI sent empty model.
    private void updateReleaseBundlesConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        ReleaseBundlesConfigModel model = new ReleaseBundlesConfigModel(
                Optional.ofNullable(generalConfig.getReleaseBundlesCleanup())
                        .orElse(descriptor.getReleaseBundlesConfig().getIncompleteCleanupPeriodHours()));
        addonsManager.addonByType(ReleaseBundleAddon.class).setReleaseBundlesConfig(model);
    }

    //Does not override defaults if UI sent empty model.
    private void updateSignedUrlConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        SignedUrlConfig signedUrlConfig = descriptor.getSignedUrlConfig();
        signedUrlConfig.setMaxValidForSeconds(Optional.ofNullable(generalConfig.getSignedUrlMaxValidForSecs())
                .orElse(signedUrlConfig.getMaxValidForSeconds()));
    }

    //Does not override defaults if UI sent empty model.
    private void updateDownloadRedirectConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        DownloadRedirectConfigDescriptor downloadRedirectConfig = descriptor.getDownloadRedirectConfig();
        downloadRedirectConfig.setFileMinimumSize(Optional.ofNullable(
                generalConfig.getDownloadRedirectFileMinimumSize())
                .orElse(downloadRedirectConfig.getFileMinimumSize()));
    }

}
