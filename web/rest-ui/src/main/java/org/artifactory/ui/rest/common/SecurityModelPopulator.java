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

package org.artifactory.ui.rest.common;

import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdIntegration;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.artifactory.ui.rest.model.admin.security.saml.Saml;
import org.artifactory.ui.rest.model.admin.security.user.User;
import org.artifactory.ui.rest.model.empty.EmptyModel;
import org.artifactory.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
public class SecurityModelPopulator {

    @Nonnull
    public static User getUserConfiguration(@Nonnull UserInfo user) {
        User userModel = new User(user);
        if (!("internal".equals(user.getRealm()) || "system".equals(
                user.getRealm()) || user.getRealm() == null || user.getRealm().isEmpty() || user.isAnonymous())) {
            userModel.setExternalRealmLink("Check external status");
        }
        // Get group for UI does not populate permission targets as the group form calls GetGroupPermissionsService
        // Whenever groups are added or removed for a user in the form.
        Set<UserGroupInfo> groups = user.getGroups();
        if (CollectionUtils.notNullOrEmpty(groups)) {
            userModel.setGroups(groups.stream()
                    .filter(Objects::nonNull)
                    .map(UserGroupInfo::getGroupName)
                    .collect(Collectors.toSet())
            );
        }
        return userModel;
    }

    // Get group for UI does not populate permission targets as the group form calls GetGroupPermissionsService whenever
    // Groups are added or removed for a user in the form.
    @Nonnull
    public static Group getGroupConfiguration(@Nonnull GroupInfo group) {
        Group groupConfiguration = new Group();
        groupConfiguration.setDescription(group.getDescription());
        groupConfiguration.setAutoJoin(group.isNewUserDefault());
        groupConfiguration.setAdminPrivileges(group.isAdminPrivileges());
        groupConfiguration.setName(group.getGroupName());
        groupConfiguration.setRealm(group.getRealm());
        groupConfiguration.setRealmAttributes(group.getRealmAttributes());
        groupConfiguration.setExternal(group.getRealm() != null && !SecurityConstants.DEFAULT_REALM.equals(group.getRealm()));
        return groupConfiguration;
    }

    /**
     * populate samlSettings descriptor data to Saml model
     *
     * @param samlSettings - saml descriptor
     * @return licenseInfo model
     */
    public static RestModel populateSamlInfo(SamlSettings samlSettings) {
        if (samlSettings != null) {
            return new Saml(samlSettings);
        }
        return new EmptyModel();
    }

    @Nonnull
    public static CrowdIntegration getCrowdConfiguration(@Nonnull CrowdSettings crowdSettings) {
        return new CrowdIntegration(crowdSettings);
    }
}
