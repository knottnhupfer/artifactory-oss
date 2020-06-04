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

package org.artifactory.ui.rest.model.admin.security.oauth;

import org.artifactory.api.rest.restmodel.IModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class OAuthUIModel implements IModel {
    private boolean enabled;
    private boolean persistUsers;
    private String defaultNpm;
    private List<OAuthProviderInfo> availableTypes;
    private List<OAuthProviderUIModel> providers;
    private boolean allowUserToAccessProfile;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<OAuthProviderInfo> getAvailableTypes() {
        return availableTypes;
    }

    public void setAvailableTypes(List<OAuthProviderInfo> availableTypes) {
        this.availableTypes = availableTypes;
    }

    public List<OAuthProviderUIModel> getProviders() {
        return providers;
    }

    public void setProviders(List<OAuthProviderUIModel> providers) {
        this.providers = providers;
    }

    public boolean isPersistUsers() {
        return persistUsers;
    }

    public void setPersistUsers(boolean persistUsers) {
        this.persistUsers = persistUsers;
    }

    public String getDefaultNpm() {
        return defaultNpm;
    }

    public void setDefaultNpm(String defaultNpm) {
        this.defaultNpm = defaultNpm;
    }

    public boolean isAllowUserToAccessProfile() {
        return allowUserToAccessProfile;
    }

    public void setAllowUserToAccessProfile(boolean allowUserToAccessProfile) {
        this.allowUserToAccessProfile = allowUserToAccessProfile;
    }
}
