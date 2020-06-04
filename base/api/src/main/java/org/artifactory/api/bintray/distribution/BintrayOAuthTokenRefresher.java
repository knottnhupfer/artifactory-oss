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

package org.artifactory.api.bintray.distribution;


/**
 * Queries Bintray for an OAuth token and saves the response refreshToken into the config descriptor if required.
 *
 * @author Dan Feldman
 * @author Shay Yaakov
 */
public interface BintrayOAuthTokenRefresher {

    /**
     * Executes a 'refresh token' call in cases where the current token has expired and saves the new token
     * in the config where {@param bintrayAppKey} is the key.
     * Will save the change to the corresponding {@link org.artifactory.descriptor.repo.BintrayApplicationConfig}
     * if the returned refresh token has changed.
     *
     * @param repoKey Repository to update the token for.
     * @return the new OAuth token to access the Bintray OAuth app denoted by {@param bintrayAppKey}
     */
    String refreshBintrayOAuthAppToken(String repoKey) throws Exception;
}
