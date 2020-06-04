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

package org.artifactory.security.exceptions;

import org.springframework.security.authentication.CredentialsExpiredException;

/**
 * Thrown when user credentials have expired
 *
 * @author Michael Pasternak
 */
public class UserCredentialsExpiredException extends CredentialsExpiredException {
    public UserCredentialsExpiredException(String msg) {
        super(msg);
    }

    public UserCredentialsExpiredException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Produces UserCredentialsExpiredException
     *
     * @param userName
     * @return {@link UserCredentialsExpiredException}
     */
    public static UserCredentialsExpiredException instance(String userName) {
        return new UserCredentialsExpiredException(
                "Your credentials have expired, You must change your password before trying to login again"
        );
    }
}
