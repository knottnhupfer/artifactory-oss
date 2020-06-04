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

import org.artifactory.api.security.access.CreatedTokenInfo;
import org.jfrog.access.rest.user.UserWithGroups;
import org.jfrog.access.token.JwtAccessToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

/**
 * @author Noam Shemesh
 */
public interface SingleSignOnService {
    String createAccessTokenForUri(String callerServiceId, String serviceUrl, String subject);

    Optional<JwtAccessToken> extractAndVerifyToken(String redirectUrl);

    String convertUserToJson(UserWithGroups tokenUserWithGroupData);

    String getRedirectTargetUrlWithToken(String username, UserInfo userInfo, String redirectUrl,
                                         Map<String, String> extraArgs);

    UserInfo extractAuthenticatedUserInfo(String username, Authentication authentication);

    UserWithGroups findUserWithGroups(String username, Authentication authentication);

    Optional<String> extractRedirectUrlFromToken(JwtAccessToken jwtAccessToken);

    Optional<String> extractServiceIdFromToken(JwtAccessToken jwtAccessToken);

    Map<String, String> extractExtraOpenidParameters(JwtAccessToken jwtAccessToken);

    CreatedTokenInfo verifyTokenAndCreateIdToken(String accessToken);

    CreatedTokenInfo verifyTokenAndAppendStateParamToToken(String serviceId, String state, String redirectUri);
}
