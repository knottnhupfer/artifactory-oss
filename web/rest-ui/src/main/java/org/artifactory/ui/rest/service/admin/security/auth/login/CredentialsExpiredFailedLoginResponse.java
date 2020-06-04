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

package org.artifactory.ui.rest.service.admin.security.auth.login;

import org.artifactory.rest.common.model.BaseModel;

/**
 * On failed login due to expired credentials, also signifies if the user is updatable or not
 * so that the UI can decide which error message to show (either lets the user change credentials or
 * instructs user to contact admin)
 *
 * @author Dan Feldman
 */
public class CredentialsExpiredFailedLoginResponse extends BaseModel {
    private static final String CREDENTIALS_EXPIRED_CODE = "CREDENTIALS_EXPIRED";

    private boolean profileUpdatable;
    private String code;

    public CredentialsExpiredFailedLoginResponse() {
    }

    public CredentialsExpiredFailedLoginResponse(boolean profileUpdatable) {
        this.profileUpdatable = profileUpdatable;
        this.code = CREDENTIALS_EXPIRED_CODE;
    }

    public boolean isProfileUpdatable() {
        return profileUpdatable;
    }

    public void setProfileUpdatable(boolean profileUpdatable) {
        this.profileUpdatable = profileUpdatable;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
