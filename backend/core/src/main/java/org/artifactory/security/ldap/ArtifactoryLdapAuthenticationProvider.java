/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2019 JFrog Ltd.
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

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LdapGroupAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.ldap.LdapService;
import org.artifactory.api.security.ldap.LdapUser;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.exception.InvalidNameException;
import org.artifactory.security.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;

import static org.artifactory.security.ldap.LdapUtils.*;

/**
 * Custom LDAP authentication provider just for creating local users for newly ldap authenticated users.
 *
 * @author Yossi Shaul
 */
@Component("ldapAuthenticationProvider")
public class ArtifactoryLdapAuthenticationProvider extends ExternalProviderBase implements RealmAwareAuthenticationProvider, MessageSourceAware {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryLdapAuthenticationProvider.class);

    private UserGroupService userGroupService;

    private CentralConfigService centralConfig;

    private InternalLdapAuthenticator authenticator;

    private LdapService ldapService;

    private AddonsManager addonsManager;

    /**
     * Keep the message source to in initialize LdapAuthenticationProvider when created
     */
    private MessageSource messageSource;

    private Map<String, LdapAuthenticationProvider> ldapAuthenticationProviders = null;

    @Autowired
    public ArtifactoryLdapAuthenticationProvider(AddonsManager addonsManager,
            LdapService ldapService, InternalLdapAuthenticator authenticator,
            CentralConfigService centralConfig, UserGroupService userGroupService) {
        super(addonsManager);
        this.addonsManager = addonsManager;
        this.ldapService = ldapService;
        this.authenticator = authenticator;
        this.centralConfig = centralConfig;
        this.userGroupService = userGroupService;
    }

    /**
     * Get the LDAP authentication providers, by iterating over all the bind authenticators and putting them in a map of
     * the settings key.
     *
     * @return The LDAP authentication provers
     */
    public Map<String, LdapAuthenticationProvider> getLdapAuthenticationProviders() {
        if (ldapAuthenticationProviders == null) {
            ldapAuthenticationProviders = new HashMap<>();
            Map<String, BindAuthenticator> authMap = authenticator.getAuthenticators();
            for (Map.Entry<String, BindAuthenticator> entry : authMap.entrySet()) {
                LdapAuthenticationProvider ldapAuthenticationProvider =
                        new LdapAuthenticationProvider(entry.getValue());
                if (messageSource != null) {
                    ldapAuthenticationProvider.setMessageSource(messageSource);
                }
                ldapAuthenticationProviders.put(entry.getKey(), ldapAuthenticationProvider);
            }
        }
        return ldapAuthenticationProviders;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
        if (ldapAuthenticationProviders != null) {
            for (LdapAuthenticationProvider ldapAuthenticationProvider : ldapAuthenticationProviders.values()) {
                ldapAuthenticationProvider.setMessageSource(messageSource);
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        if (centralConfig.getDescriptor().getSecurity().isLdapEnabled()) {
            for (LdapAuthenticationProvider ldapAuthenticationProvider : getLdapAuthenticationProviders().values()) {
                if (ldapAuthenticationProvider.supports(authentication)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (shouldNotAuthenticate(authentication)) {
            return null;
        }
        String userName = authentication.getName();
        log.debug("Trying to authenticate user: '{}' via ldap.", userName);
        LdapSetting usedLdapSetting = null;
        DirContextOperations user = null;
        LdapGroupAddon ldapGroupAddon = addonsManager.addonByType(LdapGroupAddon.class);
        try {
            RuntimeException authenticationException = null;
            for (Map.Entry<String, BindAuthenticator> entry : authenticator.getAuthenticators().entrySet()) {
                LdapSetting currentLdapSetting =
                        centralConfig.getDescriptor().getSecurity().getLdapSettings(entry.getKey());
                BindAuthenticator bindAuthenticator = entry.getValue();
                try {
                    user = bindAuthenticator.authenticate(authentication);
                    if (user != null) {
                        usedLdapSetting = currentLdapSetting;
                        break;
                    }
                } catch (AuthenticationException | org.springframework.security.core.AuthenticationException e) {
                    authenticationException = e;
                    checkIfBindAndSearchActive(currentLdapSetting, userName);
                } catch (RuntimeException e) {
                    authenticationException = e;
                }
            }
            if (user == null) {
                if (authenticationException != null) {
                    log.debug("Failed to authenticate user: '{}' using ldap.", userName, authenticationException);
                    if (shouldRemoveUserLdapRelatedGroups(authenticationException) &&
                            findSettingsForActiveUser(userName, centralConfig, ldapService) == null) {
                        UserInfo userInfo = userGroupService.findUser(userName);
                        removeUserLdapRelatedGroups(userInfo, userGroupService);
                    }
                    throw authenticationException;
                }
                throw new AuthenticationServiceException(ArtifactoryLdapAuthenticator.LDAP_SERVICE_MISCONFIGURED);
            }

            // user authenticated via ldap
            log.debug("'{}' authenticated successfully by ldap server.", userName);

            //Collect internal groups, and if using external groups add them to the user info
            SimpleUser simpleUser = createSimpleUser(userName, usedLdapSetting, user, ldapGroupAddon, userGroupService);
            // create new authentication response containing the user and it's authorities
            return new LdapRealmAwareAuthentication(simpleUser, authentication.getCredentials(),
                    simpleUser.getAuthorities());
        } catch (AuthenticationException e) {
            String message = String.format("Failed to authenticate user: '%s' via LDAP: %s", userName, e.getMessage());
            log.debug(message);
            throw new AuthenticationServiceException(message, e);
        } catch (CommunicationException ce) {
            String message = String.format("Failed to authenticate user: '%s' via LDAP: communication error", userName);
            log.warn(message);
            log.debug(message, ce);
            throw new AuthenticationServiceException(message, ce);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.debug(String.format("Failed to authenticate user: '%s': %s", userName, e.getMessage()));
            throw e;
        } catch (NamingException e) {
            String message = String.format("Failed to locate directory entry for authenticated user: %s",
                    e.getMostSpecificCause().getMessage());
            log.debug(message);
            throw new AuthenticationServiceException(message, e);
        } catch (InvalidNameException e) {
            String message = String.format("Failed to persist user '%s': %s", userName, e.getMessage());
            log.warn(message);
            log.debug("Cause: {}", e);
            throw new InternalAuthenticationServiceException(message, e);
        } catch (Exception e) {
            String message = "Unexpected exception in LDAP authentication:";
            log.error(message, e);
            throw new AuthenticationServiceException(message, e);
        } finally {
            LdapUtils.closeContext(user);
        }
    }

    private void checkIfBindAndSearchActive(LdapSetting ldapSetting, String userName) {
        if (StringUtils.isNotBlank(ldapSetting.getUserDnPattern()) &&
                ldapSetting.getSearch() != null) {
            log.warn("LDAP authentication failed for user: '{}'. Note: you have configured direct user binding and " +
                    "manager-based search, which are usually mutually exclusive. For AD leave the User DN Pattern " +
                    "field empty.", userName);
        }
    }

    @Override
    public String getRealm() {
        return LdapService.REALM;
    }

    @Override
    public void addExternalGroups(String username, Set<UserGroupInfo> groups) {
        addonsManager.addonByType(LdapGroupAddon.class).addExternalGroups(username, groups);
    }

    @Override
    public boolean userExists(String username) {
        List<LdapSetting> settings = centralConfig.getMutableDescriptor().getSecurity().getLdapSettings();
        if (settings == null || settings.isEmpty()) {
            log.debug("No LDAP settings defined");
            return false;
        }
        for (LdapSetting setting : settings) {
            if (setting.isEnabled()) {
                log.debug("Trying to find user: '{}' with LDAP settings '{}'", username, setting);
                LdapUser ldapUser = ldapService.getDnFromUserName(setting, username);
                if (ldapUser != null) {
                    log.debug("Found user: '{}' with LDAP settings '{}'", username, setting);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Should not remove user's ldap-related groups in case of bad credentials, communication or timeout exception
     * {@see RTFACT-15394}
     */
    private boolean shouldRemoveUserLdapRelatedGroups(RuntimeException e) {
        return ConstantValues.ldapCleanGroupOnFail.getBoolean() &&
                !(e instanceof BadCredentialsException) &&
                !(e instanceof CommunicationException) &&
                !(e instanceof UncategorizedLdapException && e.getCause().getMessage().contains("LDAP response read timed out"));
    }

    @Override
    @Nonnull
    public UserDetails reauthenticateRememberMe(String username) {
        try {
            return refreshUserFromLdap(username, centralConfig, ldapService, addonsManager, userGroupService);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new RememberMeAuthenticationException(e.getMessage(), e);
        }
    }

}