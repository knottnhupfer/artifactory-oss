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

package org.artifactory.ui.rest.model.admin.security.login;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author chen keinan
 */
public class UserLogin extends BaseModel {

    private String user;
    private String password;
    Boolean forgotPassword;
    Boolean canRememberMe;
    private String ssoProviderLink;
    private String redirectTo;

    public UserLogin() {
    }

    public UserLogin(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getForgotPassword() {
        return forgotPassword;
    }

    public void setForgotPassword(Boolean forgotPassword) {
        this.forgotPassword = forgotPassword;
    }

    public Boolean getCanRememberMe() {
        return canRememberMe;
    }

    public void setCanRememberMe(Boolean canRememberMe) {
        this.canRememberMe = canRememberMe;
    }

    public String getSsoProviderLink() {
        return ssoProviderLink;
    }

    public void setSsoProviderLink(String ssoProviderLink) {
        this.ssoProviderLink = ssoProviderLink;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }
}
