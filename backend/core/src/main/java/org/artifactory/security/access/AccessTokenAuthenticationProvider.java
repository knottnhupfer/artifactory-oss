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

package org.artifactory.security.access;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.*;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.jfrog.access.client.token.TokenVerifyResult;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.token.JwtAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yinon Avraham
 */
public class AccessTokenAuthenticationProvider implements RealmAwareAuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenAuthenticationProvider.class);
    /**
     * Max length of a username. This is the limitation due to the username holding columns in the database
     * (e.g. 'created_by', 'last_downloaded_by', etc.)
     */
    private static final int MAX_USERNAME_LENGTH = 64;

    @Autowired
    AccessService accessService;

    @Autowired
    private UserGroupStoreService userGroupStore;

    @Autowired
    private UserGroupService userGroupService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(AccessTokenAuthentication.class, authentication,
                "Only instances of AccessTokenAuthentication are expected");
        AccessTokenAuthentication accessTokenAuth = (AccessTokenAuthentication) authentication;
        JwtAccessToken accessToken = accessTokenAuth.getAccessToken();
        verifyToken(accessToken);
        //TODO [YA] Currently, we match the token to a real user. Once we rely on the scope the token provides, the user principal is unnecessary.
        UserDetails principal = findPrincipalByToken(accessToken);
        verifyMatchingPrincipal(accessTokenAuth, accessToken);
        return newSuccessfulAccessTokenAuthentication(accessTokenAuth, principal);
    }

    private Authentication newSuccessfulAccessTokenAuthentication(AccessTokenAuthentication originalAuth,
            UserDetails principal) {
        AccessTokenAuthentication accessTokenAuth = new AccessTokenAuthentication(originalAuth.getAccessToken(),
                principal, originalAuth.getAuthorities());
        accessTokenAuth.setAuthenticated(true);
        accessTokenAuth.setDetails(originalAuth.getDetails());
        return accessTokenAuth;
    }

    public boolean isAccessToken(@Nonnull String value) {
        try {
            accessService.parseToken(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void verifyToken(JwtAccessToken accessToken) {
        TokenVerifyResult result;
        try {
            result = accessService.verifyAndGetResult(accessToken);
        } catch (Exception e) {
            log.error("Failed to verify token with id '{}': {}", accessToken.getTokenId(), e.toString());
            log.debug("Failed to verify token {}", accessToken, e);
            throw new AccessTokenAuthenticationException("Failed to verify token.", e);
        }
        if (!result.isSuccessful()) {
            throw new AccessTokenAuthenticationException("Token failed verification: " + result.getReason());
        }
    }

    @Nonnull
    private UserDetails findPrincipalByToken(JwtAccessToken accessToken) throws AccessTokenAuthenticationException {
        String username = extractUsername(accessToken);
        UserInfo realUser = userGroupService.findUser(username, false);
        String principalName = getPrincipalName(accessToken);
        MutableUserInfo transientUser = new UserInfoBuilder(principalName).transientUser().build();
        if (realUser == null && log.isDebugEnabled()) {
            log.debug("The user: '{}' not found, extracted from access token subject: {}",
                    username, accessToken.getSubject());
        }
        MutableUserInfo user = transientUser;
        user = populateGroups(user, realUser, accessToken);
        user = populateArtifactoryPrivileges(user, accessToken);
        return new SimpleUser(user);
    }

    MutableUserInfo populateArtifactoryPrivileges(MutableUserInfo user, JwtAccessToken accessToken) {
        ServiceId artifactoryServiceId = accessService.getArtifactoryServiceId();
        if (accessToken.getScope().stream()
                .filter(ArtifactoryAdminScopeToken::accepts)
                .anyMatch(scope -> ArtifactoryAdminScopeToken.isAdminScopeOnService(scope, artifactoryServiceId))) {
            user.setAdmin(true);
        }
        return user;
    }

    /**
     * Update the user with groups according to the real user (if exists, otherwise <code>null</code>) and the
     * token. The user will have only the groups that came from the token.
     * In case the token has only "*" in the groups, we use all the groups assigned to the real user (if exists).
     */
    private MutableUserInfo populateGroups(MutableUserInfo user, UserInfo realUser, JwtAccessToken accessToken) {
        Set<String> groups = accessService.extractAppliedGroupNames(accessToken).stream()
                .collect(Collectors.toSet());
        // In case the token scope contains member-of-groups:* and the real user exists we use a copy of the real user.
        if (realUser != null && groups.size() == 1 && groups.contains("*")) {
            user = InfoFactoryHolder.get().copyUser(realUser);
            return user;
        }
        Set<String> existingGroups = userGroupService.getAllGroups().stream()
                .map(GroupInfo::getGroupName)
                .collect(Collectors.toSet());
        groups.retainAll(existingGroups);
        groups.forEach(user::addGroup); // add all the existing groups which are applied by the token
        return user;
    }

    private void verifyMatchingPrincipal(AccessTokenAuthentication accessTokenAuth, JwtAccessToken accessToken) {
        if (accessTokenAuth.getPrincipal() != null) {
            String principalAsString = accessTokenAuth.getPrincipal().toString();
            String subject = accessToken.getSubject();
            String username = UserTokenSpec.isUserTokenSubject(subject) ?
                    UserTokenSpec.extractUsername(subject) :
                    subject;
            if (!StringUtils.equals(principalAsString, username)) {
                String tokenId = accessTokenAuth.getAccessToken().getTokenId();
                log.error("Principal mismatch for token with id '{}'.", tokenId);
                log.debug("Principal mismatch for token with id '{}'. (existing='{}', expected='{}')",
                        tokenId, principalAsString, username);
                throw new AccessTokenAuthenticationException("Token principal mismatch.");
            }
        }
    }

    @Nonnull
    private String extractUsername(@Nonnull JwtAccessToken accessToken) throws AccessTokenAuthenticationException {
        String username = accessService.extractSubjectUsername(accessToken);
        if (username != null) {
            return username;
        }
        return accessToken.getSubject();
    }

    @Nonnull
    private String getPrincipalName(JwtAccessToken accessToken) {
        String extractedUsername = extractUsername(accessToken);
        String username = extractedUsername;
        int lastSlashIndex = extractedUsername.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            username = extractedUsername.substring(lastSlashIndex + 1);
        }
        String principalName = "token:" + username;
        if (principalName.length() > MAX_USERNAME_LENGTH) {
            String trimmedUsername = principalName.substring(0, MAX_USERNAME_LENGTH);
            log.debug("Principal username extracted from access token subject is too long and got trimmed. " +
                    "subject = '{}', principalName = '{}'", accessToken.getSubject(), principalName);
            principalName = trimmedUsername;
        }
        return principalName;
    }

    @Override
    public String getRealm() {
        return null;
    }

    @Override
    public void addExternalGroups(String username, Set<UserGroupInfo> groups) {
        //Not relevant
    }

    @Override
    public boolean userExists(String username) {
        return userGroupStore.userExists(username);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
