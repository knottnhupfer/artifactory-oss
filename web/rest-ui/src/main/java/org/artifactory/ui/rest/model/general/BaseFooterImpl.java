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

import org.artifactory.rest.common.model.BaseModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Basic footer implementation, must not include sensitive information and should be returned to the client when
 * anonymous access is disabled and the user is not logged in
 *
 * @author Shay Bagants
 */
public class BaseFooterImpl extends BaseModel implements Footer {

    private String serverName;
    private boolean userLogo;
    private String logoUrl;
    private boolean haConfigured;
    private boolean isAol;
    private String versionID;
    private boolean gaAccount;


    public BaseFooterImpl(String serverName, boolean userLogo, String logoUrl, boolean haConfigured, boolean isAol, String versionID, boolean gaAccount) {
        this.serverName = serverName;
        this.userLogo = userLogo;
        this.logoUrl = logoUrl;
        this.haConfigured = haConfigured;
        this.isAol = isAol;
        this.versionID = versionID;
        this.gaAccount = gaAccount;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public boolean isUserLogo() {
        return userLogo;
    }

    public void setUserLogo(boolean userLogo) {
        this.userLogo = userLogo;
    }

    @Override
    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isHaConfigured() {
        return haConfigured;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    @JsonProperty("isAol")
    public boolean isAol() {
        return isAol;
    }

    public String getVersionID() {
        return versionID;
    }

    public void setVersionID(String versionID) {
        this.versionID = versionID;
    }

    public boolean isGaAccount() {
        return gaAccount;
    }
}
