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

package org.artifactory.ui.rest.model.admin.configuration.generalconfig;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.subscription.Subscription;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.distribution.ReleaseBundlesConfig;
import org.artifactory.descriptor.download.DownloadRedirectConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.descriptor.replication.GlobalReplicationsConfigDescriptor;
import org.artifactory.descriptor.signature.SignedUrlConfig;
import org.artifactory.descriptor.subscription.SubscriptionConfig;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.descriptor.trashcan.TrashcanConfigDescriptor;
import org.artifactory.rest.common.model.BaseModel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public class GeneralConfig extends BaseModel {

    private String serverName;
    private String customUrlBase;
    private Integer fileUploadMaxSize;
    private String dateFormat;
    private Boolean globalOfflineMode;
    private Boolean showAddonSettings;
    private String logoUrl;
    private int bintrayFilesUploadLimit;
    private Boolean helpLinksEnabled;
    private Boolean forceBaseUrl;

    //System message
    private Boolean systemMessageEnabled;
    private String systemMessageTitle;
    private String systemMessageTitleColor;
    private String systemMessage;
    private Boolean showSystemMessageOnAllPages;
    //Folder download
    private Boolean folderDownloadEnabled;
    private Boolean folderDownloadEnabledForAnonymous;
    private Integer folderDownloadMaxSizeMb;
    private Long maxFolderDownloadFilesLimit;
    private Integer FolderDownloadMaxConcurrentRequests;
    //Trashcan
    private Boolean trashcanEnabled;
    private Boolean allowPermDeletes;
    private Integer trashcanRetentionPeriodDays;
    //Global replication config
    private Boolean blockPullReplications;
    private Boolean blockPushReplications;
    //Sumo Logic
    private Boolean sumoLogicEnabled;
    private String sumoLogicCollectorUrl;
    //Release bundle settings
    private Long releaseBundlesCleanup;
    //Signed URL settings
    private Long signedUrlMaxValidForSecs;
    // Cloud storage download redirect
    private Integer downloadRedirectFileMinimumSize;
    private Boolean s3Configured; // Used only for GET config to signal the UI whether to display the download redirect
    private Subscription subscription;

    public GeneralConfig(){}

    public GeneralConfig(CentralConfigDescriptor mutableDescriptor) {
        serverName =  mutableDescriptor.getServerName();
        customUrlBase = mutableDescriptor.getUrlBase();
        fileUploadMaxSize = mutableDescriptor.getFileUploadMaxSizeMb();
        dateFormat = mutableDescriptor.getDateFormat();
        globalOfflineMode = mutableDescriptor.isOfflineMode();
        showAddonSettings = mutableDescriptor.getAddons().isShowAddonsInfo();
        logoUrl = mutableDescriptor.getLogo();
        bintrayFilesUploadLimit = getBintrayFileUploadLimit(mutableDescriptor);
        helpLinksEnabled = mutableDescriptor.isHelpLinksEnabled();
        //System Message
        SystemMessageDescriptor messageDescriptor = Optional.ofNullable(mutableDescriptor.getSystemMessageConfig())
                .orElse(new SystemMessageDescriptor());
        systemMessageEnabled = messageDescriptor.isEnabled();
        systemMessageTitle = messageDescriptor.getTitle();
        systemMessageTitleColor = messageDescriptor.getTitleColor();
        systemMessage = messageDescriptor.getMessage();
        showSystemMessageOnAllPages = messageDescriptor.isShowOnAllPages();
        //Folder Download
        FolderDownloadConfigDescriptor folderDownloadDescriptor = mutableDescriptor.getFolderDownloadConfig();
        folderDownloadEnabled = folderDownloadDescriptor.isEnabled();
        folderDownloadEnabledForAnonymous = folderDownloadDescriptor.isEnabledForAnonymous();
        folderDownloadMaxSizeMb = folderDownloadDescriptor.getMaxDownloadSizeMb();
        maxFolderDownloadFilesLimit = folderDownloadDescriptor.getMaxFiles();
        FolderDownloadMaxConcurrentRequests = folderDownloadDescriptor.getMaxConcurrentRequests();
        //Trashcan
        TrashcanConfigDescriptor trashcanConfigDescriptor = mutableDescriptor.getTrashcanConfig();
        trashcanEnabled = trashcanConfigDescriptor.isEnabled();
        trashcanRetentionPeriodDays = trashcanConfigDescriptor.getRetentionPeriodDays();
        allowPermDeletes = trashcanConfigDescriptor.isAllowPermDeletes();
        //Global replication config
        GlobalReplicationsConfigDescriptor replicationsConfig = mutableDescriptor.getReplicationsConfig();
        blockPullReplications = replicationsConfig.isBlockPullReplications();
        blockPushReplications = replicationsConfig.isBlockPushReplications();
        // Sumo Logic config
        SumoLogicConfigDescriptor sumoLogicConfig = mutableDescriptor.getSumoLogicConfig();
        sumoLogicEnabled = sumoLogicConfig.isEnabled();
        sumoLogicCollectorUrl = sumoLogicConfig.getCollectorUrl();
        //Release bundles config
        ReleaseBundlesConfig releaseBundlesConfig = mutableDescriptor.getReleaseBundlesConfig();
        releaseBundlesCleanup = releaseBundlesConfig.getIncompleteCleanupPeriodHours();
        // Signed URL config
        SignedUrlConfig signedUrlConfig = mutableDescriptor.getSignedUrlConfig();
        signedUrlMaxValidForSecs = signedUrlConfig.getMaxValidForSeconds();
        // Cloud storage download redirect
        DownloadRedirectConfigDescriptor downloadRedirectConfig = mutableDescriptor.getDownloadRedirectConfig();
        downloadRedirectFileMinimumSize = downloadRedirectConfig.getFileMinimumSize();
        s3Configured = isS3Configured();
        // Subscription
        SubscriptionConfig subscriptionConfig = mutableDescriptor.getSubscriptionConfig();
        Set<String> emails = subscriptionConfig != null
                && subscriptionConfig.getEmails() != null ? subscriptionConfig.getEmails()
                : new HashSet<>();
        subscription = new Subscription(emails);
    }

    private Boolean isS3Configured() {
        return ContextHelper.get().beanForType(AddonsManager.class).isBinaryProviderConfigured("s3") ? true : null;
    }


    private int getBintrayFileUploadLimit(CentralConfigDescriptor mutableDescriptor) {
        if(mutableDescriptor.getBintrayConfig() != null){
            return mutableDescriptor.getBintrayConfig().getFileUploadLimit();
        }
        else {
            return 0;
        }
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getCustomUrlBase() {
        return customUrlBase;
    }

    public void setCustomUrlBase(String customUrlBase) {
        this.customUrlBase = customUrlBase;
    }

    public Integer getFileUploadMaxSize() {
        return fileUploadMaxSize;
    }

    public void setFileUploadMaxSize(Integer fileUploadMaxSize) {
        this.fileUploadMaxSize = fileUploadMaxSize;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Boolean isGlobalOfflineMode() {
        return globalOfflineMode;
    }

    public void setGlobalOfflineMode(Boolean globalOfflineMode) {
        this.globalOfflineMode = globalOfflineMode;
    }

    public Boolean isShowAddonSettings() {
        return showAddonSettings;
    }

    public void setShowAddonSettings(Boolean showAddonSettings) {
        this.showAddonSettings = showAddonSettings;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public int getBintrayFilesUploadLimit() {
        return bintrayFilesUploadLimit;
    }

    public void setBintrayFilesUploadLimit(int bintrayFilesUploadLimit) {
        this.bintrayFilesUploadLimit = bintrayFilesUploadLimit;
    }

    public Boolean isHelpLinksEnabled() {
        return helpLinksEnabled;
    }

    public void setHelpLinksEnabled(Boolean helpLinksEnabled) {
        this.helpLinksEnabled = helpLinksEnabled;
    }

    public boolean isSystemMessageEnabled() {
        return systemMessageEnabled;
    }

    public void setSystemMessageEnabled(boolean systemMessageEnabled) {
        this.systemMessageEnabled = systemMessageEnabled;
    }

    public String getSystemMessageTitle() {
        return systemMessageTitle;
    }

    public void setSystemMessageTitle(String systemMessageTitle) {
        this.systemMessageTitle = systemMessageTitle;
    }

    public String getSystemMessageTitleColor() {
        return systemMessageTitleColor;
    }

    public void setSystemMessageTitleColor(String systemMessageTitleColor) {
        this.systemMessageTitleColor = systemMessageTitleColor;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public boolean isShowSystemMessageOnAllPages() {
        return showSystemMessageOnAllPages;
    }

    public void setShowSystemMessageOnAllPages(boolean showSystemMessageOnAllPages) {
        this.showSystemMessageOnAllPages = showSystemMessageOnAllPages;
    }

    public Boolean isFolderDownloadEnabled() {
        return folderDownloadEnabled;
    }

    public void setFolderDownloadEnabled(boolean folderDownloadEnabled) {
        this.folderDownloadEnabled = folderDownloadEnabled;
    }

    public Boolean isFolderDownloadEnabledForAnonymous() {
        return folderDownloadEnabledForAnonymous;
    }

    public void setFolderDownloadEnabledForAnonymous(boolean folderDownloadEnabledForAnonymous) {
        this.folderDownloadEnabledForAnonymous = folderDownloadEnabledForAnonymous;
    }

    public Integer getFolderDownloadMaxSizeMb() {
        return folderDownloadMaxSizeMb;
    }

    public void setFolderDownloadMaxSizeMb(int folderDownloadMaxSizeMb) {
        this.folderDownloadMaxSizeMb = folderDownloadMaxSizeMb;
    }

    public Long getMaxFolderDownloadFilesLimit() {
        return maxFolderDownloadFilesLimit;
    }

    public void setMaxFolderDownloadFilesLimit(long maxFolderDownloadFilesLimit) {
        this.maxFolderDownloadFilesLimit = maxFolderDownloadFilesLimit;
    }

    public Integer getFolderDownloadMaxConcurrentRequests() {
        return FolderDownloadMaxConcurrentRequests;
    }

    public void setFolderDownloadMaxConcurrentRequests(int folderDownloadMaxConcurrentRequests) {
        FolderDownloadMaxConcurrentRequests = folderDownloadMaxConcurrentRequests;
    }

    public Boolean getTrashcanEnabled() {
        return trashcanEnabled;
    }

    public void setTrashcanEnabled(Boolean trashcanEnabled) {
        this.trashcanEnabled = trashcanEnabled;
    }

    public Integer getTrashcanRetentionPeriodDays() {
        return trashcanRetentionPeriodDays;
    }

    public void setTrashcanRetentionPeriodDays(Integer trashcanRetentionPeriodDays) {
        this.trashcanRetentionPeriodDays = trashcanRetentionPeriodDays;
    }

    public Boolean getAllowPermDeletes() {
        return allowPermDeletes;
    }

    public void setAllowPermDeletes(Boolean allowPermDeletes) {
        this.allowPermDeletes = allowPermDeletes;
    }

    public Boolean getForceBaseUrl() {
        return forceBaseUrl;
    }

    public void setForceBaseUrl(Boolean forceBaseUrl) {
        this.forceBaseUrl = forceBaseUrl;
    }

    public Boolean getBlockPullReplications() {
        return blockPullReplications;
    }

    public void setBlockPullReplications(Boolean blockPullReplications) {
        this.blockPullReplications = blockPullReplications;
    }

    public Boolean getBlockPushReplications() {
        return blockPushReplications;
    }

    public void setBlockPushReplications(Boolean blockPushReplications) {
        this.blockPushReplications = blockPushReplications;
    }

    public Boolean getSumoLogicEnabled() {
        return sumoLogicEnabled;
    }

    public void setSumoLogicEnabled(Boolean sumoLogicEnabled) {
        this.sumoLogicEnabled = sumoLogicEnabled;
    }

    public String getSumoLogicCollectorUrl() {
        return sumoLogicCollectorUrl;
    }

    public void setSumoLogicCollectorUrl(String sumoLogicCollectorUrl) {
        this.sumoLogicCollectorUrl = sumoLogicCollectorUrl;
    }

    public Long getReleaseBundlesCleanup() {
        return releaseBundlesCleanup;
    }

    public void setReleaseBundlesCleanup(Long releaseBundlesCleanup) {
        this.releaseBundlesCleanup = releaseBundlesCleanup;
    }

    public Long getSignedUrlMaxValidForSecs() {
        return signedUrlMaxValidForSecs;
    }

    public void setSignedUrlMaxValidForSecs(Long signedUrlMaxValidForSecs) {
        this.signedUrlMaxValidForSecs = signedUrlMaxValidForSecs;
    }

    public Integer getDownloadRedirectFileMinimumSize() {
        return downloadRedirectFileMinimumSize;
    }

    public void setDownloadRedirectFileMinimumSize(Integer downloadRedirectFileMinimumSize) {
        this.downloadRedirectFileMinimumSize = downloadRedirectFileMinimumSize;
    }

    public Boolean getS3Configured() {
        return s3Configured;
    }

    public void setS3Configured(Boolean s3Configured) {
        this.s3Configured = s3Configured;
    }
}