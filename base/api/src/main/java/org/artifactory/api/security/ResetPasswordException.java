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
 * @author Yinon Avraham
 */
public class ResetPasswordException extends RuntimeException {

    public ResetPasswordException(String message) {
        super(message);
    }

    public static ResetPasswordException tooManyAttempts(String remoteAddress) {
        return new ResetPasswordException("Too many reset password requests, retry again at a later time.");
    }

    public static ResetPasswordException tooFrequentAttempts(String remoteAddress) {
        return new ResetPasswordException("Too frequent reset password requests, retry again shortly.");
    }
}
