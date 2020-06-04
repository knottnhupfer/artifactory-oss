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

package org.artifactory.security.props.auth;


import org.artifactory.security.props.auth.model.TokenKeyValue;

/**
 * DB token manager. Should be replaced with Access in the future
 *
 * @author Chen Keinan
 */
public interface TokenManager {

    /**
     * create token (user props) and store it in DB
     *
     * @param userName - userName to create token for
     * @return Token data
     */
    TokenKeyValue createToken(String userName);

    /**
     * delete current token and create  new token (user props) and store it in DB
     *
     * @param userName - userName to refresh token for
     * @return Token data
     */
    TokenKeyValue refreshToken(String userName);

    /**
     * delete current token and store new given token
     *
     * @param userName - userName to update token for
     * @param token    - new token to store
     * @return Token data
     */
    TokenKeyValue updateToken(String userName, String token);

    /**
     * get current token
     *
     * @param userName - userName to get token for
     * @return Token data
     */
    TokenKeyValue getToken(String userName);

    /**
     * delete current token from DB
     *
     * @param userName - userName to delete token for
     * @return Token data
     */
    boolean revokeToken(String userName);

    /**
     * delete all tokens in user props table DB
     *
     * @return Token data
     */
    boolean revokeAllTokens();

    /**
     * add external Token (git enterprise and etc) to db
     *
     * @param userName
     * @param extToken
     * @return
     */
    TokenKeyValue addExternalToken(String userName, String extToken);
}
