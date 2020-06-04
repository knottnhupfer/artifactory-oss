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

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.DiffKey;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gidi Shabat
 */
@XmlType(name = "oauthProvidersSettingsType",
        propOrder = {"name","enabled","providerType","id","secret","apiUrl","authUrl","tokenUrl","basicUrl","domain"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class OAuthProviderSettings implements Descriptor {
    @DiffKey
    private String name;
    private Boolean enabled = false;
    private String providerType;
    private String id;
    private String secret;
    private String apiUrl;
    private String authUrl;
    private String tokenUrl;
    private String basicUrl;
    private String domain;

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getBasicUrl() {
        return basicUrl;
    }

    public void setBasicUrl(String basicUrl) {
        this.basicUrl = basicUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthProviderSettings)) return false;

        OAuthProviderSettings that = (OAuthProviderSettings) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getEnabled() != null ? !getEnabled().equals(that.getEnabled()) : that.getEnabled() != null) return false;
        if (getProviderType() != null ? !getProviderType().equals(that.getProviderType()) : that.getProviderType() != null)
            return false;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getSecret() != null ? !getSecret().equals(that.getSecret()) : that.getSecret() != null) return false;
        if (getApiUrl() != null ? !getApiUrl().equals(that.getApiUrl()) : that.getApiUrl() != null) return false;
        if (getAuthUrl() != null ? !getAuthUrl().equals(that.getAuthUrl()) : that.getAuthUrl() != null) return false;
        if (getTokenUrl() != null ? !getTokenUrl().equals(that.getTokenUrl()) : that.getTokenUrl() != null)
            return false;
        if (getBasicUrl() != null ? !getBasicUrl().equals(that.getBasicUrl()) : that.getBasicUrl() != null)
            return false;
        return getDomain() != null ? getDomain().equals(that.getDomain()) : that.getDomain() == null;

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getEnabled() != null ? getEnabled().hashCode() : 0);
        result = 31 * result + (getProviderType() != null ? getProviderType().hashCode() : 0);
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + (getSecret() != null ? getSecret().hashCode() : 0);
        result = 31 * result + (getApiUrl() != null ? getApiUrl().hashCode() : 0);
        result = 31 * result + (getAuthUrl() != null ? getAuthUrl().hashCode() : 0);
        result = 31 * result + (getTokenUrl() != null ? getTokenUrl().hashCode() : 0);
        result = 31 * result + (getBasicUrl() != null ? getBasicUrl().hashCode() : 0);
        result = 31 * result + (getDomain() != null ? getDomain().hashCode() : 0);
        return result;
    }
}
