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

package org.artifactory.security.providermgr;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.security.oauth.OAuthSettings;
import org.artifactory.security.LoginHandler;
import org.artifactory.security.props.auth.model.OauthErrorEnum;
import org.artifactory.security.props.auth.model.OauthErrorModel;
import org.artifactory.security.props.auth.model.OauthModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Chen Keinan
 */
public class OAuthProviderMgr implements ProviderMgr {
    private static final Logger log = LoggerFactory.getLogger(OAuthProviderMgr.class);

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
    private String authHeader;
    private HttpServletRequest servletRequest;

    public OAuthProviderMgr(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource,
            String authHeader, HttpServletRequest servletRequest) {
        this.authenticationDetailsSource = authenticationDetailsSource;
        this.authHeader = authHeader;
        this.servletRequest = servletRequest;
    }

    @Override
    public final OauthModel fetchAndStoreTokenFromProvider() {
        LoginHandler loginHandler = ContextHelper.get().beanForType(LoginHandler.class);
        // if basic auth not present return with unauthorized
        String[] tokens = new String[]{"anonymous", ""};
        try {
            if (StringUtils.isNotBlank(authHeader)) {
                tokens = loginHandler.extractAndDecodeHeader(authHeader);
            }
        } catch (IOException e) {
            log.error("Failed to extract credential from headers", e);
        }
        String username = tokens[0];
        OauthModel oauthModel;
        try {
            return loginHandler.doBasicAuthWithDb(tokens, authenticationDetailsSource, servletRequest);
        } catch (Exception e) {
            oauthModel = createErrorModel(HttpServletResponse.SC_UNAUTHORIZED, OauthErrorEnum.BAD_CREDENTIAL);
            log.debug("Failed to authenticate with basic authentication", e);
        }
        // if auth integration isn't enabled return
        if (!isOauthSettingEnable()) {
            log.debug("Artifactory basic authentication failed, OAuth integration isn't enabled");
            return oauthModel;
        }
        return loginHandler.doBasicAuthWithProvider(authHeader, username);
    }

    protected OauthModel createErrorModel(int statusCode, OauthErrorEnum errorEnum) {
        return new OauthErrorModel(statusCode, errorEnum);
    }

    /**
     * check weather oauth integration is enabled
     *
     * @return true if auth integration is enabled
     */
    private boolean isOauthSettingEnable() {
        OAuthSettings oauthSettings = ContextHelper.get().getCentralConfig().getDescriptor().getSecurity().getOauthSettings();
        return (oauthSettings != null && oauthSettings.getEnableIntegration());
    }
}
