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

package org.artifactory.descriptor.security.sso;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configuration object for the SAML settings.
 *
 * @author Gidi Shabat
 */
@XmlType(name = "SamlSettingsType",
        propOrder = {"enableIntegration", "loginUrl", "logoutUrl", "certificate", "serviceProviderName",
                "noAutoUserCreation", "allowUserToAccessProfile", "useEncryptedAssertion",
                "autoRedirect", "syncGroups", "groupAttribute", "emailAttribute"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class SamlSettings implements Descriptor {

    @XmlElement(defaultValue = "false")
    private boolean enableIntegration = false;

    private String loginUrl;

    private String logoutUrl;

    private String certificate;

    private String serviceProviderName;

    @XmlElement(defaultValue = "true")
    private Boolean noAutoUserCreation = true;

    @XmlElement(defaultValue = "false")
    private boolean allowUserToAccessProfile = false;

    private boolean useEncryptedAssertion;

    private boolean autoRedirect;

    @XmlElement(defaultValue = "false")
    private boolean syncGroups;

    private String groupAttribute;

    private String emailAttribute;

    public boolean isEnableIntegration() {
        return enableIntegration;
    }

    public void setEnableIntegration(boolean enableIntegration) {
        this.enableIntegration = enableIntegration;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public Boolean getNoAutoUserCreation() {
        return noAutoUserCreation;
    }

    public void setNoAutoUserCreation(Boolean noAutoUserCreation) {
        this.noAutoUserCreation = noAutoUserCreation;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public boolean isUseEncryptedAssertion() {
        return useEncryptedAssertion;
    }

    public void setUseEncryptedAssertion(boolean useEncryptedAssertion) {
        this.useEncryptedAssertion = useEncryptedAssertion;
    }

    public boolean isAllowUserToAccessProfile() {
        return allowUserToAccessProfile;
    }

    public void setAllowUserToAccessProfile(boolean allowUserToAccessProfile) {
        this.allowUserToAccessProfile = allowUserToAccessProfile;
    }

    public boolean isSyncGroups() {
        return syncGroups;
    }

    public void setSyncGroups(boolean syncGroups) {
        this.syncGroups = syncGroups;
    }

    public String getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(String groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    public String getEmailAttribute() {
        return emailAttribute;
    }

    public void setEmailAttribute(String emailAttribute) {
        this.emailAttribute = emailAttribute;
    }

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SamlSettings that = (SamlSettings) o;

        if (enableIntegration != that.enableIntegration) {
            return false;
        }
        if (allowUserToAccessProfile != that.allowUserToAccessProfile) {
            return false;
        }
        if (autoRedirect != that.autoRedirect) {
            return false;
        }
        if (syncGroups != that.syncGroups) {
            return false;
        }
        if (loginUrl != null ? !loginUrl.equals(that.loginUrl) : that.loginUrl != null) {
            return false;
        }
        if (logoutUrl != null ? !logoutUrl.equals(that.logoutUrl) : that.logoutUrl != null) {
            return false;
        }
        if (certificate != null ? !certificate.equals(that.certificate) : that.certificate != null) {
            return false;
        }
        if (useEncryptedAssertion != that.useEncryptedAssertion) {
            return false;
        }
        if (serviceProviderName != null ? !serviceProviderName.equals(that.serviceProviderName) :
                that.serviceProviderName != null) {
            return false;
        }
        if (noAutoUserCreation != null ? !noAutoUserCreation.equals(that.noAutoUserCreation) :
                that.noAutoUserCreation != null) {
            return false;
        }
        if (groupAttribute != null ? !groupAttribute.equals(that.groupAttribute) : that.groupAttribute != null) {
            return false;
        }
        return emailAttribute != null ? emailAttribute.equals(that.emailAttribute) : that.emailAttribute == null;
    }

    @Override
    public int hashCode() {
        int result = (enableIntegration ? 1 : 0);
        result = 31 * result + (loginUrl != null ? loginUrl.hashCode() : 0);
        result = 31 * result + (logoutUrl != null ? logoutUrl.hashCode() : 0);
        result = 31 * result + (certificate != null ? certificate.hashCode() : 0);
        result = 31 * result + (serviceProviderName != null ? serviceProviderName.hashCode() : 0);
        result = 31 * result + (noAutoUserCreation != null ? noAutoUserCreation.hashCode() : 0);
        result = 31 * result + (allowUserToAccessProfile ? 1 : 0);
        result = 31 * result + (autoRedirect ? 1 : 0);
        result = 31 * result + (syncGroups ? 1 : 0);
        result = 31 * result + (groupAttribute != null ? groupAttribute.hashCode() : 0);
        result = 31 * result + (emailAttribute != null ? emailAttribute.hashCode() : 0);
        return result;
    }
}
