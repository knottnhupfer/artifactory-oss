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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.*;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Tomer Cohen
 */
public class AbstractLdapService {
    private static final Logger log = LoggerFactory.getLogger(AbstractLdapService.class);


    protected void handleException(Exception e, BasicStatusHolder status, String username,
            boolean isSearchAndBindActive) {
        log.debug("LDAP connection test failed with exception", e);
        if (e instanceof CommunicationException) {
            status.error("Failed connecting to the server (probably wrong url or port)", e, log);
        } else if (e instanceof NameNotFoundException) {
            status.error("Server failed to parse the request: " +
                    ((NameNotFoundException) e).getMostSpecificCause().getMessage(), e, log);
        } else if (e instanceof InvalidNameException) {
            status.error("Server failed to parse the request: " +
                    ((InvalidNameException) e).getMostSpecificCause().getMessage(), e, log);
        } else if (e instanceof AuthenticationException) {
            if (isSearchAndBindActive) {
                status.warn(String.format(
                        "LDAP authentication failed for user: '%s'. Note: you have configured direct user binding and manager-based search, which are usually mutually exclusive. For AD leave the User DN Pattern field empty.",
                        username), e, log);
            } else {
                status.error("Authentication failed. Probably a wrong manager dn or manager password", e,
                        log);
            }
        } else if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException) {
            status.error(String.format("Failed to authenticate user '%s'", username), e, log);
        } else if (e instanceof BadLdapGrammarException) {
            status.error("Failed to parse R\\DN", e, log);
        } else {
            String message = "Error connecting to the LDAP server: ";
            log.error(message, e);
            status.error(message, log);
        }
    }

    /**
     * Create LDAP template to be used for performing LDAP queries.
     *
     * @param settings The LDAP settings with which to create the LDAP template.
     * @return The LDAP template.
     */
    public LdapTemplate createLdapTemplate(LdapSetting settings) {
        LdapContextSource securityContext = ArtifactoryLdapAuthenticator.createSecurityContext(settings);
        LdapTemplate ldapTemplate = new LdapTemplate(securityContext);
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate;
    }
}
