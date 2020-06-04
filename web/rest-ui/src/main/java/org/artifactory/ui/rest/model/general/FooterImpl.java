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

package org.artifactory.ui.rest.model.general;

import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Chen Keinan
 */
public class FooterImpl extends BaseFooterImpl implements Footer {
    private String versionInfo;
    private String buildNumber;
    private String licenseInfo;
    private String copyRights;
    private String copyRightsUrl;
    private String versionID;
    private boolean isAol;
    private boolean isDedicatedAol;
    private boolean isHttpSsoEnabledAOL;
    private boolean helpLinksEnabled;
    private boolean systemMessageEnabled;
    private String systemMessageTitle;
    private String systemMessageTitleColor;
    private String systemMessage;
    private boolean showSystemMessageOnAllPages;
    private String serverId;
    private boolean trashDisabled;
    private boolean allowPermDeletes;
    private boolean samlRedirectEnabled;
    private boolean xrayConfigured;
    private boolean xrayEnabled;
    private boolean xrayLicense;
    private boolean gaAccount;
    private boolean edgeLicense;
    private boolean mdsPackageNativeUI;
    private boolean isCloudProviderConfigured;
    private boolean treebrowserFolderCompact;

    public FooterImpl(String licenseInfo, String versionInfo, String copyRights, String copyRightsUrl,
            String buildNumber, boolean isAol, boolean isDedicatedAol, boolean isHttpSsoEnabledAOL, String versionID,
            boolean userLogo, String logoUrl, String serverName, SystemMessageDescriptor systemMessageDescriptor,
            boolean helpLinksEnabled, String serverId, boolean trashDisabled, boolean allowPermDeletes,
            boolean samlRedirectEnabled, boolean xrayConfigured, boolean xrayEnabled, boolean xrayLicense,
            boolean haConfigured, boolean gaAccount, boolean edgeLicense, boolean mdsPackageNativeUI, boolean isCloudProviderConfigured,
            boolean treebrowserFolderCompact) {
        super(serverName, userLogo, logoUrl, haConfigured, isAol, versionID, gaAccount);
        this.licenseInfo = licenseInfo;
        this.versionInfo = versionInfo;
        this.copyRights = copyRights;
        this.copyRightsUrl = copyRightsUrl;
        this.buildNumber = buildNumber;
        this.versionID = versionID;
        this.isDedicatedAol = isDedicatedAol;
        this.isHttpSsoEnabledAOL = isHttpSsoEnabledAOL;
        this.trashDisabled = trashDisabled;
        this.allowPermDeletes = allowPermDeletes;
        this.systemMessageEnabled = systemMessageDescriptor.isEnabled();
        this.systemMessageTitle = systemMessageDescriptor.getTitle();
        this.systemMessageTitleColor = systemMessageDescriptor.getTitleColor();
        this.systemMessage = systemMessageDescriptor.getMessage();
        this.showSystemMessageOnAllPages = systemMessageDescriptor.isShowOnAllPages();
        this.helpLinksEnabled = helpLinksEnabled;
        this.serverId = serverId;
        this.samlRedirectEnabled = samlRedirectEnabled;
        this.xrayConfigured = xrayConfigured;
        this.xrayEnabled = xrayEnabled;
        this.xrayLicense = xrayLicense;
        this.gaAccount = gaAccount;
        this.edgeLicense = edgeLicense;
        this.mdsPackageNativeUI = mdsPackageNativeUI;
        this.isCloudProviderConfigured = isCloudProviderConfigured;
        this.treebrowserFolderCompact = treebrowserFolderCompact;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }

    public void setLicenseInfo(String licenseInfo) {
        this.licenseInfo = licenseInfo;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getCopyRights() {
        return copyRights;
    }

    public void setCopyRights(String copyRights) {
        this.copyRights = copyRights;
    }

    public String getCopyRightsUrl() {
        return copyRightsUrl;
    }

    public void setCopyRightsUrl(String copyRightsUrl) {
        this.copyRightsUrl = copyRightsUrl;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getVersionID() {
        return versionID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }

    public boolean isSystemMessageEnabled() {
        return systemMessageEnabled;
    }

    public String getSystemMessageTitle() {
        return systemMessageTitle;
    }

    public String getSystemMessageTitleColor() {
        return systemMessageTitleColor;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public boolean isShowSystemMessageOnAllPages() {
        return showSystemMessageOnAllPages;
    }

    public boolean isHelpLinksEnabled() {
        return helpLinksEnabled;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isTrashDisabled() {
        return trashDisabled;
    }

    public boolean isAllowPermDeletes() {
        return allowPermDeletes;
    }

    public boolean isSamlRedirectEnabled() {
        return samlRedirectEnabled;
    }

    public void setSamlRedirectEnabled(boolean samlRedirectEnabled) {
        this.samlRedirectEnabled = samlRedirectEnabled;
    }

    public boolean isXrayConfigured() { return xrayConfigured; }

    public void setXrayConfigured(boolean xrayConfigured) { this.xrayConfigured = xrayConfigured; }

    public boolean isXrayEnabled() { return xrayEnabled; }

    public void setXrayEnabled(boolean xrayEnabled) { this.xrayEnabled = xrayEnabled; }

    public boolean isXrayLicense() { return xrayLicense; }

    public void setXrayLicense(boolean xrayLicense) { this.xrayLicense = xrayLicense; }

    @JsonProperty("isDedicatedAol")
    public boolean isDedicatedAol() {
        return isDedicatedAol;
    }

    public void setDedicatedAol(boolean dedicatedAol) {
        isDedicatedAol = dedicatedAol;
    }

    public boolean isHttpSsoEnabledAOL() {
        return isHttpSsoEnabledAOL;
    }

    public void setHttpSsoEnabledAOL(boolean httpSsoEnabledAOL) {
        isHttpSsoEnabledAOL = httpSsoEnabledAOL;
    }

    public boolean isEdgeLicense() {
        return edgeLicense;
    }

    public void setEdgeLicense(boolean edgeLicense) {
        this.edgeLicense = edgeLicense;
    }

    public boolean isMdsPackageNativeUI() { return mdsPackageNativeUI; }

    public void setMdsPackageNativeUI(boolean mdsPackageNativeUI) { this.mdsPackageNativeUI = mdsPackageNativeUI; }

    @JsonProperty("isCloudProviderConfigured")
    public boolean isCloudProviderConfigured() {return isCloudProviderConfigured;}

    public void setCloudProviderConfigured(boolean isCloudProviderConfigured) {this.isCloudProviderConfigured = isCloudProviderConfigured;}

    public boolean isTreebrowserFolderCompact() {
        return treebrowserFolderCompact;
    }

    public void setTreebrowserFolderCompact(boolean treebrowserFolderCompact) {
        this.treebrowserFolderCompact = treebrowserFolderCompact;
    }
}
