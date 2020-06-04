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

package org.artifactory.webapp.servlet.authentication;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.artifactory.api.security.SecurityService;
import org.artifactory.security.props.auth.ApiKeyManager;
import org.artifactory.security.props.auth.OauthManager;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.artifactory.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
public class AuthenticationFilterUtils {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilterUtils.class);

    public static TokenKeyValue getTokenKeyValueFromHeader(HttpServletRequest request) {
        TokenKeyValue tokenKeyValue;
        // 1st check if api key token exist
        if ((tokenKeyValue = getApiKeyTokenKeyValue(request)) != null) {
            return tokenKeyValue;
            // 2nd check if oauth token exist
        }
        if ((tokenKeyValue = getOauthTokenKeyValue(request)) != null) {
            return tokenKeyValue;
        }
        return null;
    }

    /**
     * check weather api key is found on request
     *
     * @param request - http servlet request
     * @return Token key value
     */
    private static TokenKeyValue getApiKeyTokenKeyValue(HttpServletRequest request) {
        String apiKeyValue = request.getHeader(ApiKeyManager.API_KEY_HEADER);
        if (StringUtils.isBlank(apiKeyValue)) {
            apiKeyValue = request.getHeader(ApiKeyManager.OLD_API_KEY_HEADER);
        }
        if (apiKeyValue != null) {
            return new TokenKeyValue(ApiKeyManager.API_KEY, apiKeyValue);
        }
        return null;
    }

    /**
     * check weather oauth key is found on request
     *
     * @param request - http servlet request
     * @return Token key value
     */
    private static TokenKeyValue getOauthTokenKeyValue(HttpServletRequest request) {
        String oauthToken = request.getHeader(OauthManager.AUTHORIZATION_HEADER);
        int prefixLength = OauthManager.OAUTH_TOKEN_PREFIX.length();
        if (oauthToken != null && oauthToken.startsWith(OauthManager.OAUTH_TOKEN_PREFIX) && oauthToken.length() > prefixLength + 1) {
            oauthToken = oauthToken.substring(prefixLength);
            return new TokenKeyValue(OauthManager.OAUTH_KEY, oauthToken);
        }
        return null;
    }

    public static boolean isAcceptedByPropsFilter(HttpServletRequest request) {
        return getTokenKeyValueFromHeader(request) != null;
    }

    public static boolean isAuthHeaderPresent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION) != null;
    }

    public static boolean isAcceptSsoFilter(HttpServletRequest request, SecurityService securityService) {
        if (!securityService.isHttpSsoProxied()) {
            return false;
        }
        String ssoUserName = getRemoteUserName(securityService, request);

        //Accept the request if the header contains an SSO username
        return StringUtils.isNotBlank(ssoUserName);
    }

    /**
     * Checks whether the request has any way of authentication attached, or http session is already authenticated.
     *
     * It might be one of the following:
     * API token is present OR OAuth token is present OR Authorization header is present OR SSO username is present
     */
    static boolean isRequestContainsAuthentication(HttpServletRequest request, SecurityService securityService) {
        return isAuthHeaderPresent(request) || isAcceptedByPropsFilter(request) ||
                isAcceptSsoFilter(request, securityService) || isHttpSessionAuthenticated(request);
    }

    private static boolean isHttpSessionAuthenticated(HttpServletRequest request) {
        Authentication authentication = SessionUtils.getAuthentication(request);
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Extracts the remote user name from the request, either from an attribute/header whose name was defined in the
     * SSO config or from the {@link javax.servlet.http.HttpServletRequest#getRemoteUser()} method.
     *
     * @param request HTTP request
     * @return Remote user name if found. Null or blank if not.
     */
    public static String getRemoteUserName(SecurityService securityService, HttpServletRequest request) {
        log.debug("Entering ArtifactorySsoAuthenticationFilter.getRemoteUserName");

        String ssoUserName = null;

        String requestVariable = securityService.getHttpSsoRemoteUserRequestVariable();
        if (StringUtils.isNotBlank(requestVariable)) {
            log.debug("Remote user request variable = '{}'.", requestVariable);
            // first attempt to read from attribute (to support custom filters)
            Object userAttribute = request.getAttribute(requestVariable);
            if (userAttribute != null) {
                ssoUserName = userAttribute.toString();
                log.debug("Remote user attribute: '{}'.", ssoUserName);
            }

            if (StringUtils.isBlank(ssoUserName)) {
                // check if the container got the remote user (e.g., using ajp)
                ssoUserName = request.getRemoteUser();
                log.debug("Remote user from request: '{}'.", ssoUserName);
            }

            if (StringUtils.isBlank(ssoUserName)) {
                // check if the request header contains the remote user
                ssoUserName = request.getHeader(requestVariable);
                log.debug("Remote user from header: '{}'.", ssoUserName);
            }
        }
        return ssoUserName != null ? ssoUserName.toLowerCase() : null;
    }
}
