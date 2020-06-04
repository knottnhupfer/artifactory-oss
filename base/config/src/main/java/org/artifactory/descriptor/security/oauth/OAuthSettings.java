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

package org.artifactory.descriptor.security.oauth;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Set;

/**
 * @author Gidi Shabat
 */
@XmlType(name = "oauthSettingsType",
        propOrder = {"enableIntegration", "allowUserToAccessProfile", "persistUsers", "defaultNpm", "oauthProvidersSettings"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class OAuthSettings implements Descriptor {

    private Boolean enableIntegration = false;
    @XmlElement(defaultValue = "false")
    private boolean allowUserToAccessProfile = false;
    private Boolean persistUsers = false;
    private String defaultNpm;

    @XmlElementWrapper(name = "oauthProvidersSettings")
    private List<OAuthProviderSettings> oauthProvidersSettings = Lists.newArrayList();


    public Boolean getEnableIntegration() {
        return enableIntegration;
    }

    public void setEnableIntegration(Boolean enableIntegration) {
        this.enableIntegration = enableIntegration;
    }


    public List<OAuthProviderSettings> getOauthProvidersSettings() {
        return oauthProvidersSettings;
    }

    public void setOauthProvidersSettings(List<OAuthProviderSettings> oauthProvidersSettings) {
        this.oauthProvidersSettings = oauthProvidersSettings;
    }

    public String getDefaultNpm() {
        return defaultNpm;
    }

    public void setDefaultNpm(String defaultNpm) {
        this.defaultNpm = defaultNpm;
    }

    public Boolean getPersistUsers() {
        return persistUsers;
    }

    public void setPersistUsers(Boolean persistUsers) {
        this.persistUsers = persistUsers;
    }

    public boolean isAllowUserToAccessProfile() {
        return allowUserToAccessProfile;
    }

    public void setAllowUserToAccessProfile(boolean allowUserToAccessProfile) {
        this.allowUserToAccessProfile = allowUserToAccessProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthSettings)) return false;

        OAuthSettings that = (OAuthSettings) o;

        if (isAllowUserToAccessProfile() != that.isAllowUserToAccessProfile()) return false;
        if (getEnableIntegration() != null ? !getEnableIntegration().equals(that.getEnableIntegration()) : that.getEnableIntegration() != null)
            return false;
        if (getPersistUsers() != null ? !getPersistUsers().equals(that.getPersistUsers()) : that.getPersistUsers() != null)
            return false;
        if (getDefaultNpm() != null ? !getDefaultNpm().equals(that.getDefaultNpm()) : that.getDefaultNpm() != null)
            return false;
        return oauthProviderSettingsIdentical(this.getOauthProvidersSettings(), that.getOauthProvidersSettings());

    }

    @Override
    public int hashCode() {
        int result = getEnableIntegration() != null ? getEnableIntegration().hashCode() : 0;
        result = 31 * result + (isAllowUserToAccessProfile() ? 1 : 0);
        result = 31 * result + (getPersistUsers() != null ? getPersistUsers().hashCode() : 0);
        result = 31 * result + (getDefaultNpm() != null ? getDefaultNpm().hashCode() : 0);
        result = 31 * result + (getOauthProvidersSettings() != null ? getOauthProvidersSettings().hashCode() : 0);
        return result;
    }

    private boolean oauthProviderSettingsIdentical(List<OAuthProviderSettings> l1, List<OAuthProviderSettings> l2) {
        if (l1 == null && l2 == null || (l1 == l2)) {
            return true;
        } else if (l1 == null || l2 == null) {
            return false;
        } else if (l1.size() != l2.size()) {
            return false;
        }
        Set<OAuthProviderSettings> l1Set = Sets.newHashSet(l1);
        return l2.stream()
                .filter(providerSetting -> !l1Set.contains(providerSetting))
                .count() != 0;
    }
}
