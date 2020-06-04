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

package org.artifactory.security.ldap;


import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddonsImpl;
import org.artifactory.addon.LdapGroupAddon;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.spring.InternalContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControl;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControlExtractor;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import java.util.List;

/**
 * @author freds
 */
public class ArtifactoryBindAuthenticator extends BindAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryBindAuthenticator.class);

    /**
     * The spring context connected to LDAP server
     */
    private LdapContextSource contextSource;

    /**
     * A list of user search that can be used to search and authenticate the user. Used with AD.
     */
    private List<LdapUserSearch> userSearches;

    public ArtifactoryBindAuthenticator(LdapContextSource contextSource, LdapSetting ldapSetting, boolean aol) {
        super(contextSource);
        init(contextSource, ldapSetting, aol);
    }

    public void init(LdapContextSource contextSource, LdapSetting ldapSetting, boolean aol) {
        Assert.notNull(contextSource, "contextSource must not be null.");
        this.contextSource = contextSource;
        boolean hasDnPattern = StringUtils.hasText(ldapSetting.getUserDnPattern());
        SearchPattern search = ldapSetting.getSearch();
        boolean hasSearch = search != null && StringUtils.hasText(search.getSearchFilter());
        Assert.isTrue(hasDnPattern || hasSearch,
                "An Authentication pattern should provide a userDnPattern or a searchFilter (or both)");

        if (hasDnPattern) {
            setUserDnPatterns(new String[]{ldapSetting.getUserDnPattern()});
        }

        if (hasSearch) {
            this.userSearches = getLdapGroupAddon().getLdapUserSearches(contextSource, ldapSetting, aol);
        }
    }

    private LdapGroupAddon getLdapGroupAddon() {
        InternalArtifactoryContext context = InternalContextHelper.get();
        if (context != null) {
            AddonsManager addonsManager = context.beanForType(AddonsManager.class);
            return addonsManager.addonByType(LdapGroupAddon.class);
        } else {
            return new CoreAddonsImpl();
        }
    }

    @Override
    protected LdapContextSource getContextSource() {
        return contextSource;
    }

    @Override
    public void afterPropertiesSet() {
        // Nothing to do, check done at constructor time
    }

    @Override
    public DirContextOperations authenticate(Authentication authentication) {
        DirContextOperations user = null;
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Can only process UsernamePasswordAuthenticationToken objects");

        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        // may have LDAPs that have no password for the users, see RTFACT-3103, RTFACT-3378
        if (!StringUtils.hasText(password)) {
            throw new BadCredentialsException("Empty password used.");
        }

        // If DN patterns are configured, try authenticating with them directly
        for (String dn : getUserDns(username)) {
            try {
                user = bindWithDn(dn, null, username, password);
            } catch (org.springframework.ldap.AuthenticationException | org.springframework.security.core.AuthenticationException e) {
                log.debug("Failed to authenticate {} using DN pattern", username);
                log.trace("", e);
            }
            if (user != null) {
                break;
            }
        }
        int notFoundCount = 0;
        boolean authenticationException = false;
        if (user == null && (userSearches != null && !userSearches.isEmpty())) {
            for (LdapUserSearch userSearch : userSearches) {
                try {
                    DirContextOperations userFromSearch = userSearch.searchForUser(username);
                    user = bindWithDn(userFromSearch.getDn().toString(), userFromSearch.getAttributes(), username,
                            password);
                    if (user != null) {
                        break;
                    }
                } catch (UsernameNotFoundException e) {
                    log.debug("Searching for user: '{}' failed for {}: {}", userSearch, username, e.getMessage());
                    notFoundCount++;
                } catch (IncorrectResultSizeDataAccessException irsae) {
                    log.error("User: '{}' found {} times in LDAP server", username, irsae.getActualSize());
                } catch (org.springframework.ldap.AuthenticationException | org.springframework.security.core.AuthenticationException e) {
                    log.debug("Searching for user: '{}' failed for user: '{}': {}", userSearch, username, e.getMessage());
                    authenticationException = true;
                }
            }
        }

        if (user == null) {
            if (authenticationException) {
                throw new AuthenticationServiceException("The user '" + username + "' failed to authenticate");
            }
            // if all of the searches returned UsernameNotFoundException
            if (userSearches != null && notFoundCount == userSearches.size()) {
                log.debug("The user: '{}' can't be found in LDAP search", username);
                throw new UsernameNotFoundException(
                        String.format("The user: '%s' can't be found in LDAP search", username));
            }
            // user search didn't return UsernameNotFoundException in at least one search
            throw new BadCredentialsException(
                    messages.getMessage("BindAuthenticator.badCredentials", "Bad credentials"));
        }

        return user;
    }

    private DirContext bind(String userDnStr, String username, String password) {
        BaseLdapPathContextSource ctxSource = getContextSource();
        DistinguishedName userDn = new DistinguishedName(userDnStr);
        DistinguishedName fullDn = new DistinguishedName(userDn);
        fullDn.prepend(ctxSource.getBaseLdapPath());
        log.debug("Attempting to bind as {}", fullDn);
        try {
            return ctxSource.getContext(fullDn.toString(), password);
        } catch (org.springframework.ldap.AuthenticationException | org.springframework.ldap.OperationNotSupportedException e) {
            // This will be thrown if an invalid user name is used and the method may
            // be called multiple times to try different names, so we trap the exception
            // unless a subclass wishes to implement more specialized behaviour.
            handleBindException(userDnStr, username, e);
            throw e;
        }
    }

    private DirContextOperations bindWithDn(String userDnStr, @Nullable Attributes attrs, String username, String password) {
        DirContext ctx = bind(userDnStr, username, password);
        if (ctx == null) {
            return null;
        }
        DirContextOperations user;
        try {
            //Attributes are optional and signify if user was found via search or has dn
            if (attrs == null) {
                attrs = ctx.getAttributes(new DistinguishedName(userDnStr), getUserAttributes());
            }
            user = checkPasswordPolicy(attrs, ctx, userDnStr);
        } catch (javax.naming.NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
        return user;
    }

    private DirContextOperations checkPasswordPolicy(Attributes attrs, DirContext ctx, String userDnStr) {
        DistinguishedName userDn = new DistinguishedName(userDnStr);
        PasswordPolicyControl passwordPolicy = PasswordPolicyControlExtractor.extractControl(ctx);
        log.debug("Retrieving attributes...");
        DirContextAdapter result = new DirContextAdapter(attrs, userDn, getContextSource().getBaseLdapPath());
        if (passwordPolicy != null) {
            result.setAttributeValue(passwordPolicy.getID(), passwordPolicy);
        }
        return result;
    }
}