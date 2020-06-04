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

package org.artifactory.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.io.Serializable;

/**
 * Auth token that 'impersonates' as the user that invoked the action on the originating node.
 *
 * @author Dan Feldman
 */
public class HaUserDelegatingAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    private UserInfo user;

    public HaUserDelegatingAuthenticationToken(UserInfo user) {
        super(user.isEffectiveAdmin() ? SimpleUser.ADMIN_GAS : SimpleUser.USER_GAS);
        this.user = user;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return new SimpleUser(user);
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
