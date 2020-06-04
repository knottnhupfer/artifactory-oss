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

package org.artifactory.security.db;

import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.InternalRealmAwareAuthentication;
import org.artifactory.security.RealmAwareAuthenticationProvider;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.Set;

/**
 * @author Fred Simon
 */
@Deprecated
public class DbAuthenticationProvider extends DaoAuthenticationProvider implements RealmAwareAuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(DbAuthenticationProvider.class);

    @Autowired
    private UserGroupStoreService userGroupStore;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authenticate = super.authenticate(authentication);
        return new InternalRealmAwareAuthentication(authenticate.getPrincipal(), authenticate.getCredentials(),
                authenticate.getAuthorities());
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // defend against the case where password is null, the MD5 password encoder can't handle null, nor defend against
        if (Objects.isNull(userDetails.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }

    @Override
    public String getRealm() {
        return SecurityConstants.DEFAULT_REALM;
    }

    @Override
    public void addExternalGroups(String username, Set<UserGroupInfo> groups) {
        log.debug("The user: '{}' is an internal user that belongs to the following groups: '{}'", username, groups);
        // nop
    }

    @Override
    public boolean userExists(String username) {
        return userGroupStore.userExists(username);
    }
}