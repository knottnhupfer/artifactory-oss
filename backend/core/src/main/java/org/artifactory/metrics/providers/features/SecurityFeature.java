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

package org.artifactory.metrics.providers.features;

import org.artifactory.api.callhome.FeatureGroup;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represent the security feature group of the CallHome feature
 *
 * @author Shay Bagants
 */
@Component
public class SecurityFeature implements CallHomeFeature {

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private AclService aclService;

    @Override
    public FeatureGroup getFeature() {
        SecurityDescriptor security = configService.getDescriptor().getSecurity();

        //Holds the entire security information, this is what the method returns
        FeatureGroup securityFeature = new FeatureGroup("security");

        //General security configurations
        addGeneralSecurityInformation(securityFeature, security);

        //Users
        addUsers(securityFeature);

        //Groups
        addGroups(securityFeature);

        //Permissions
        FeatureGroup permissionsFeature = new FeatureGroup("permissions");
        // We query only for repo acls as we always double the number of repo PT (1 for repo, 1 for build)
        permissionsFeature.addFeatureAttribute("number_of_permission_targets", aclService.getAllRepoAcls().size());
        securityFeature.addFeature(permissionsFeature);

        //LDAP
        boolean isLdapEnabled = security.isLdapEnabled();
        if (isLdapEnabled){
            FeatureGroup ldapFeature = new FeatureGroup("ldap");
            ldapFeature.addFeatureAttribute("enabled", true);
            securityFeature.addFeature(ldapFeature);
        }

        //Crowd
        if (security.getCrowdSettings() != null) {
            boolean isCrowdEnabled = security.getCrowdSettings().isEnableIntegration();
            if (isCrowdEnabled){
                FeatureGroup crowdFeature = new FeatureGroup("crowd");
                crowdFeature.addFeatureAttribute("enabled", true);
                securityFeature.addFeature(crowdFeature);
            }
        }

        //SAML
        if (security.getSamlSettings() != null) {
            boolean isSamlEnabled = security.getSamlSettings().isEnableIntegration();
            if (isSamlEnabled){
                FeatureGroup samlFeature = new FeatureGroup("saml");
                samlFeature.addFeatureAttribute("enabled", true);
                securityFeature.addFeature(samlFeature);
            }
        }

        //SSO
        if (security.getHttpSsoSettings() != null) {
            boolean isSsoEnabled = security.getHttpSsoSettings().isHttpSsoProxied();
            if (isSsoEnabled){
                FeatureGroup ssoFeature = new FeatureGroup("sso");
                ssoFeature.addFeatureAttribute("enabled", true);
                securityFeature.addFeature(ssoFeature);
            }
        }

        //OAuth
        if (security.getOauthSettings() != null) {
            boolean isOauthEnabled = security.getOauthSettings().getEnableIntegration();
            if (isOauthEnabled){
                FeatureGroup oauthFeature = new FeatureGroup("oauth");
                oauthFeature.addFeatureAttribute("enabled", true);
                securityFeature.addFeature(oauthFeature);
            }
        }

        //SSH Server
        if (security.getSshServerSettings() != null) {
            boolean isSshServerEnabled = security.getSshServerSettings().isEnableSshServer();
            if (isSshServerEnabled){
                FeatureGroup sshServerFeature = new FeatureGroup("ssh_server");
                sshServerFeature.addFeatureAttribute("enabled", true);
                securityFeature.addFeature(sshServerFeature);
            }
        }

        return securityFeature;
    }

    /**
     * Adds users information to the security feature group
     *
     * @param securityFeature that holds the entire security features
     */
    private void addUsers(FeatureGroup securityFeature) {
        List<UserInfo> users = userGroupService.getAllUsers(true);
        FeatureGroup usersFeature = new FeatureGroup("users");
        usersFeature.addFeatureAttribute("number_of_users", users.size());
        Map<String, Integer> realms = new HashMap<>();
        users.forEach(userInfo -> incrementRealmCount(realms, userInfo.getRealm(), "internal"));
        //add the count of each realm
        realms.forEach((realm, count) -> usersFeature.addFeatureAttribute(realm + "_users", count));
        securityFeature.addFeature(usersFeature);
    }

    /**
     * Adds groups information to the security feature group
     *
     * @param securityFeature that holds the entire security features
     */
    private void addGroups(FeatureGroup securityFeature) {
        List<GroupInfo> groups = userGroupService.getAllGroups();
        FeatureGroup groupsFeature = new FeatureGroup("groups");
        groupsFeature.addFeatureAttribute("number_of_groups", groups.size());
        Map<String, Integer> realms = new HashMap<>();
        groups.forEach(groupInfo -> incrementRealmCount(realms, groupInfo.getRealm(), "internal"));
        realms.forEach((realm, count) -> {
            //groups internal realm is 'artifactory', but we want that the resulted json will be consistent in both
            //users and groups, therefore, translating 'artifactory' realm to 'internal'
            if (realm.equalsIgnoreCase("artifactory")) {
                realm = "internal";
            }
            groupsFeature.addFeatureAttribute(realm + "_groups", count);
        });
        securityFeature.addFeature(groupsFeature);
    }

    /**
     * This method used when counting the number of users/groups realms
     *
     * @param realms            which stores the realm name and count
     * @param currentRealm      the name of the realm which we should increment it's count
     * @param internalRealmName since group internal realm is called 'artifactory' and users internal realm called
     *                          'internal', we are using this value when adding the user/group realm to the map in case
     *                          of a null realm name.
     */
    private void incrementRealmCount(Map<String, Integer> realms, String currentRealm, String internalRealmName) {
        if (currentRealm != null) {
            currentRealm = currentRealm.toLowerCase();
            realms.compute(currentRealm, (key, value) -> value == null ? 1 : value + 1);
        } //null = anonymous user or users which have yet logged it into Artifactory
        else {
            //When used by users, the internal realm is 'internal'
            //when using artifactory, the internal realm is artifactory
            currentRealm = internalRealmName;
            realms.compute(currentRealm, (key, value) -> value == null ? 1 : value + 1);
        }
    }

    /**
     * Add general security information to the security feature group
     *
     * @param securityFeature that holds the entire security features
     * @param security SecurityDescriptor that used to access the security data
     */
    private void addGeneralSecurityInformation(FeatureGroup securityFeature, SecurityDescriptor security) {
        FeatureGroup securityConfigurationsFeature = new FeatureGroup("configurations");
        securityConfigurationsFeature.addFeatureAttribute
                ("allow_anonymous_access", security.isAnonAccessEnabled());
        securityConfigurationsFeature.addFeatureAttribute
                ("password_encryption_policy", security.getPasswordSettings().getEncryptionPolicy()
                        .toString().toLowerCase());
        securityConfigurationsFeature.addFeatureAttribute
                ("lock_users", security.getUserLockPolicy().isEnabled());
        securityFeature.addFeature(securityConfigurationsFeature);
    }
}
