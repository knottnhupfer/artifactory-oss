/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2020 JFrog Ltd.
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

import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;
import org.artifactory.addon.oauth.OAuthHandler;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.oauth.OAuthSettings;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.filters.AuthCacheKey;
import org.artifactory.security.filters.AuthenticationCacheService;
import org.artifactory.security.props.auth.OauthManager;
import org.artifactory.security.props.auth.model.AuthenticationModel;
import org.artifactory.security.props.auth.model.OauthModel;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.artifactory.security.providermgr.TokenProviderResponseAuthentication;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.date.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Chen  Keinan
 */
@Component
public class LoginHandlerImpl implements LoginHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginHandlerImpl.class);

    private final OauthManager oauthManager;
    private final UserGroupService userGroupService;
    private final AccessService accessService;
    private final AuthenticationCacheService authenticationCacheService;

    @Autowired
    public LoginHandlerImpl(OauthManager oauthManager, UserGroupService userGroupService, AccessService accessService,
            AuthenticationCacheService authenticationCacheService) {
        this.oauthManager = oauthManager;
        this.userGroupService = userGroupService;
        this.accessService = accessService;
        this.authenticationCacheService = authenticationCacheService;
    }

    @Override
    public OauthModel doBasicAuthWithDb(String[] tokens,
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource,
            HttpServletRequest servletRequest) {
        if (tokens.length != 2) {
            throw new IllegalStateException(
                    "Tokens array is malformed. Should be 2 parts but is: " + Arrays.toString(tokens));
        }
        String username = tokens[0];
        Authentication authentication = getAuthentication(tokens, authenticationDetailsSource, servletRequest,
                username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TokenKeyValue tokenKeyValue = oauthManager.getToken(username);
        if (tokenKeyValue == null) {
            tokenKeyValue = oauthManager.createToken(username);
        }
        boolean externalUserToken = false;
        if (tokenKeyValue == null) {
            log.debug(
                    "could not create and persist token for authenticated user: '{}', storing generated token in shared cache.",
                    username);
            tokenKeyValue = generateToken(((UserDetails) authentication.getPrincipal()).getUsername());
            if (tokenKeyValue == null) {
                throw new RuntimeException(
                        String.format("failed to generate token for authenticated user: '%s'", username));
            }
            externalUserToken = true;
        }
        AuthenticationModel oauthModel = new AuthenticationModel(tokenKeyValue.getToken(),
                DateUtils.formatBuildDate(System.currentTimeMillis()));
        if (externalUserToken) {
            oauthModel.setExpiresIn(ConstantValues.genericTokensCacheIdleTimeSecs.getInt());
        }
        return oauthModel;
    }

    /**
     * Gets authentication either from cache or re-authenticate
     */
    Authentication getAuthentication(String[] tokens,
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource,
            HttpServletRequest servletRequest, String username) {

        Authentication authentication;
        AuthCacheKey authCacheKey = getAuthCacheKey(servletRequest);

        if (ConstantValues.securityAuthenticationCacheForTokenEnabled.getBoolean()) {
            authentication = authenticationCacheService.getCachedAuthentication(authCacheKey, servletRequest);
            if (authentication != null && authentication.isAuthenticated()
                    && !authenticationCacheService
                    .reAuthenticatedRequiredUserChanged(authentication)) {
                log.debug("Header authentication {} found in cache.", authentication);
                // Add to user change cache the login state
                authenticationCacheService.addToUserChange(authentication);
                return authentication;
            }
        }
        // Couldn't get authentication from cache, performing re-auth
        log.debug("Authentication for {} wasn't in cache (or cache disabled), preforming authentication", username);
        authentication = authenticateAndRetrieve(username, tokens[1], authenticationDetailsSource);
        if (ConstantValues.securityAuthenticationCacheForTokenEnabled.getBoolean()) {
            // Cache the new authentication
            authenticationCacheService.cacheAuthAndRetrieveUsername(servletRequest, authCacheKey, authentication);
        }
        return authentication;
    }

    private AuthCacheKey getAuthCacheKey(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        log.debug("Cached key has been found for request: '{} {}'", request.getMethod(), request.getRequestURI());
        return new AuthCacheKey(authorizationHeader, request.getRemoteAddr());
    }

    private Authentication authenticateAndRetrieve(String username, String token,
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, token);
        AuthenticationManager authenticationManager = ContextHelper.get().beanForType(AuthenticationManager.class);
        authRequest.setDetails(authenticationDetailsSource);
        Authentication authentication = authenticationManager.authenticate(authRequest);
        // Create a dummy authentication so we'll know it was made by this mechanism (and won't re-enter to cache later)
        return new TokenProviderResponseAuthentication(authentication.getPrincipal(), authentication.getCredentials(),
                authentication.getAuthorities());
    }

    public TokenKeyValue generateToken(String userName) {
        TokenKeyValue token = null;
        CreatedTokenInfo createdTokenInfo;
        String key = "accesstoken";
        try {
            UserInfo userInfo = userGroupService.currentUser();
            String scope = getScope(userInfo);
            UserTokenSpec tokenSpec = UserTokenSpec.create(userName)
                    .expiresIn(ConstantValues.genericTokensCacheIdleTimeSecs.getLong())
                    .refreshable(false)
                    .scope(Lists.newArrayList(scope));
            createdTokenInfo = accessService.createToken(tokenSpec);
            token = new TokenKeyValue(key, createdTokenInfo.getTokenValue());
        } catch (Exception e) {
            log.debug("Failed generating token for user: '{}' with key '{}'. {}", userName, key, e.getMessage());
            log.trace("Failed generating token.", e);
        }
        return token;
    }

    private String getScope(UserInfo userInfo) {
        StringBuilder builder = new StringBuilder("member-of-groups:");
        Set<UserGroupInfo> groups = userInfo.getGroups();
        if (CollectionUtils.isNullOrEmpty(groups)) {
            builder.append("*");
        } else {
            Iterator<UserGroupInfo> it = groups.iterator();
            boolean hasNext = it.hasNext();
            while (hasNext) {
                builder.append(it.next());
                if (hasNext = it.hasNext()) {
                    builder.append(",");
                }
            }
        }
        return builder.toString();
    }

    @Override
    public OauthModel doBasicAuthWithProvider(String header, String username) {
        OAuthHandler oAuthHandler = ContextHelper.get().beanForType(OAuthHandler.class);
        CentralConfigDescriptor descriptor = ContextHelper.get().getCentralConfig().getDescriptor();
        OAuthSettings oauthSettings = descriptor.getSecurity().getOauthSettings();
        String defaultProvider = oauthSettings.getDefaultNpm();
        // try to get token from provider
        return oAuthHandler.getCreateToken(defaultProvider, username, header);
    }

    @Override
    public String[] extractAndDecodeHeader(String header) {
        byte[] base64Token = header.substring(6).getBytes(UTF_8);
        byte[] decoded;
        try {
            decoded = Base64.decodeBase64(base64Token);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }
        String token = new String(decoded, UTF_8);
        int delimiterIndex = token.indexOf(':');
        if (delimiterIndex == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        return new String[]{token.substring(0, delimiterIndex), token.substring(delimiterIndex + 1)};
    }
}
