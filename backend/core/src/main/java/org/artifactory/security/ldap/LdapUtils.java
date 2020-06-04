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
import org.artifactory.api.security.ldap.LdapUserAttributes;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SimpleUser;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.naming.directory.Attribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ldap utility code (moved from {@link ArtifactoryLdapAuthenticationProvider} for usability)
 *
 * @author Nadav Yogev
 * @author Yuval Reches
 */
public abstract class LdapUtils {

    private static final Logger log = LoggerFactory.getLogger(LdapUtils.class);

    private LdapUtils() {
        //Utility class
    }

    /**
     * Performs a query to LDAP to search for the {@param username} and get its groups.
     * User is updated locally (saved to db)
     *
     * @return A {@link UserDetails} instance with updated attributes from LDAP server
     *
     * @throws AuthenticationException in case user was not found
     */
    public static UserDetails refreshUserFromLdap(String username,
            CentralConfigService centralConfig, LdapService ldapService, AddonsManager addonsManager,
            UserGroupService userGroupService) {
        LdapSetting settings = findSettingsForActiveUser(username, centralConfig, ldapService);
        if (settings == null) {
            if (ConstantValues.ldapCleanGroupOnFail.getBoolean()) {
                try {
                    removeUserLdapRelatedGroups(userGroupService.findUser(username), userGroupService);
                } catch (UsernameNotFoundException e) {
                    log.trace("LDAP user: '{}' not found internally, skipping synced groups removal", username, e);
                }
            }
            String message = String
                    .format("Can't reauthenticate LDAP for user: '%s' because user is locked, disabled or does not exist in LDAP",
                            username);
            log.debug(message);
            throw new InternalAuthenticationServiceException(message);
        }
        LdapGroupAddon ldapGroupAddon = addonsManager.addonByType(LdapGroupAddon.class);
        LdapTemplate ldapTemplate = ((LdapServiceImpl) ldapService).createLdapTemplate(settings);
        DirContextOperations dirContextOperations = ((LdapServiceImpl) ldapService)
                .searchUserInLdap(ldapTemplate, username, settings);
        return createSimpleUser(username, settings, dirContextOperations, ldapGroupAddon, userGroupService);
    }

    /**
     * remove user ldap related group as user no longer exist in ldap
     *
     * @param userInfo Artifactory User Data
     */
    static void removeUserLdapRelatedGroups(UserInfo userInfo,
            UserGroupService userGroupService) {
        MutableUserInfo mutableUserInfo = InfoFactoryHolder.get().copyUser(userInfo);
        Set<UserGroupInfo> updateUserGroup = new HashSet<>();
        Set<UserGroupInfo> userGroupInfos = new HashSet<>(mutableUserInfo.getGroups());
        for (UserGroupInfo userGroupInfo : userGroupInfos) {
            if (!LdapService.REALM.equals(userGroupInfo.getRealm())) {
                updateUserGroup.add(userGroupInfo);
            }
        }
        mutableUserInfo.setGroups(updateUserGroup);
        if (!userInfo.isTransientUser() && userGroupInfos.size() != updateUserGroup.size()) {
            log.warn("Updating user: '{}' after LDAP login authentication failure", mutableUserInfo.getUsername());
            log.debug("Updating user: '{}' after LDAP login authentication failure, user groups for update are {}",
                    mutableUserInfo.getUsername(), mutableUserInfo.getGroups());
            userGroupService.updateUser(mutableUserInfo, false);
        }
    }


    /**
     * Find a user in LDAP if if he is not locked, disabled, exist in LDAP and LDAP has been configured, and return the settings the user was found in
     */
    static LdapSetting findSettingsForActiveUser(String username, CentralConfigService centralConfig,
            LdapService ldapService) {
        List<LdapSetting> settings = centralConfig.getMutableDescriptor().getSecurity().getLdapSettings();
        if (settings == null || settings.isEmpty()) {
            log.debug("No LDAP settings defined");
            return null;
        }
        for (LdapSetting setting : settings) {
            if (setting.isEnabled()) {
                log.debug("Trying to find user: '{}' with LDAP settings '{}'", username, setting);
                LdapUserAttributes ldapUser = ldapService.getUserWithAttributesFromDn(setting, username);
                if (ldapUser != null) {
                    log.debug("Found user: '{}' with LDAP settings '{}'", username, setting);
                    Attribute pwdAccountLockedTime = ldapUser.getAttributes().get("pwdAccountLockedTime");
                    Attribute userAccountControlAttr = ldapUser.getAttributes().get("userAccountControl");
                    String userAccountControl =
                            (userAccountControlAttr != null) ? userAccountControlAttr.toString() : null;
                    if (pwdAccountLockedTime == null // no LDAP account locked
                            && !"userAccountControl: 66050".equals(userAccountControl) // AD account disabled
                            && !"userAccountControl: 66082".equals(userAccountControl)) { // AD account disabled
                        return setting;
                    }
                }
            }
        }
        return null;
    }

    static SimpleUser createSimpleUser(String userName, LdapSetting usedLdapSetting, DirContextOperations user,
            LdapGroupAddon ldapGroupAddon, UserGroupService userGroupService) {
        MutableUserInfo userInfo = InfoFactoryHolder.get()
                .copyUser(userGroupService
                        .findOrCreateExternalAuthUser(userName, !usedLdapSetting.isAutoCreateUser(),
                                usedLdapSetting.isAllowUserToAccessProfile()));
        userInfo.setRealm(LdapService.REALM);
        String emailAttribute = usedLdapSetting.getEmailAttribute();
        if (StringUtils.isNotBlank(emailAttribute)) {
            String email = user.getStringAttribute(emailAttribute);
            if (StringUtils.isNotBlank(email) && !userInfo.isTransientUser()) {
                log.debug("The user: '{}' has email address '{}'", userName, email);
                if (!email.equals(userInfo.getEmail())) {
                    userInfo.setEmail(email);
                    userGroupService.updateUser(userInfo, false);
                }
            }
        }

        log.debug("Loading LDAP groups");
        ldapGroupAddon.populateGroups(user, userInfo);
        log.debug("Finished Loading LDAP groups");
        return new SimpleUser(userInfo);
    }

}