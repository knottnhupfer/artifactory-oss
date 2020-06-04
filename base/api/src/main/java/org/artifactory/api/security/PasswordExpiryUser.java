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

package org.artifactory.api.security;

/**
 * A lean object that holds only necessary info to avoid thousand of user objects on expiry and
 * notification jobs.
 *
 * @author Dan Feldman
 */
public class PasswordExpiryUser {

    private String userName;
    private String email;
    private long passwordCreated;

    public PasswordExpiryUser(String userName, String email, long passwordCreated) {
        this.userName = userName;
        this.email = email;
        this.passwordCreated = passwordCreated;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public long getPasswordCreated() {
        return passwordCreated;
    }
}
