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

package org.artifactory.storage.db.security.service.access;

import org.apache.commons.lang3.StringUtils;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.MutableGroupInfo;
import org.jfrog.access.model.Realm;
import org.jfrog.access.rest.group.Group;
import org.jfrog.access.rest.group.GroupRequest;
import org.jfrog.access.rest.group.UpdateGroupRequest;
import org.jfrog.access.rest.imports.ImportGroupRequest;
import org.jfrog.access.rest.user.CustomDataBuilder;
import org.jfrog.common.ClockUtils;

import javax.annotation.Nonnull;

/**
 * Helper class to map Artifactory group to Access group details and vice versa.
 *
 * @author Yossi Shaul
 */
public class GroupMapper {

    /**
     * List of custom data which maps to built in group info properties in Artifactory
     */
    public enum ArtifactoryBuiltInGroupProperty {
        artifactory_admin   // marks the group members as Artifactory admins
    }

    /**
     * Converts an Access group to Artifactory {@link GroupInfo} without any extra Artifactory group properties.
     *
     * @param group Group model from Access server
     * @return Group info built from the Access group
     */
    @Nonnull
    static GroupInfo toArtifactoryGroup(@Nonnull Group group) {
        MutableGroupInfo groupInfo = InfoFactoryHolder.get().createGroup(group.getName());
        groupInfo.setDescription(group.getDescription());
        groupInfo.setNewUserDefault(group.isAutoJoin());
        groupInfo.setRealm(group.getRealm().getName());
        groupInfo.setRealmAttributes(group.getRealmAttributes());
        groupInfo.setAdminPrivileges(Boolean.valueOf(
                group.getCustomData(ArtifactoryBuiltInGroupProperty.artifactory_admin.name())));
        return groupInfo;
    }

    /**
     * Converts an Artifactory group to Access {@link GroupRequest} without any extra Artifactory group properties.
     *
     * @param group Group model from Artifactory
     * @return Group request built from the Artifactory group
     */
    @Nonnull
    static UpdateGroupRequest toUpdateAccessGroup(@Nonnull GroupInfo group) {
        return toAccessGroup(UpdateGroupRequest.create(), group);
    }

    /**
     * Converts an Artifactory group to Access {@link GroupRequest} without any extra Artifactory group properties.
     *
     * @param group Group model from Artifactory
     * @return Group request built from the Artifactory group
     */
    @Nonnull
    static GroupRequest toAccessGroup(@Nonnull GroupInfo group) {
        return toAccessGroup(GroupRequest.create(), group);
    }

    private static <T extends GroupRequest> T toAccessGroup(T builder, @Nonnull GroupInfo group) {
        builder.name(group.getGroupName())
                .description(group.getDescription())
                .autoJoin(group.isNewUserDefault())
                .realm(toAccessRealm(group.getRealm()))
                .realmAttributes(group.getRealmAttributes());
        addCustomData(builder, group);

        if (!group.isAdminPrivileges() && isUpdateGroupRequest(builder)) {
            // make sure the admin custom data is removed if exists in the server side
            builder.addCustomData(ArtifactoryBuiltInGroupProperty.artifactory_admin.name(), null);
        }
        return builder;
    }

    private static <T extends GroupRequest> boolean isUpdateGroupRequest(T builder) {
        return UpdateGroupRequest.class.isAssignableFrom(builder.getClass());
    }

    public static ImportGroupRequest toFullAccessGroup(GroupInfo group) {
        ImportGroupRequest.Builder builder = ImportGroupRequest.builder()
                .name(group.getGroupName())
                .description(group.getDescription())
                .autoJoin(group.isNewUserDefault())
                .realm(toAccessRealm(group.getRealm()))
                .realmAttributes(group.getRealmAttributes())
                .created(ClockUtils.epochMillis())
                .modified(ClockUtils.epochMillis());
        addCustomData(builder, group);
        return builder.build();
    }

    private static void addCustomData(CustomDataBuilder builder, GroupInfo group) {
        if (group.isAdminPrivileges()) {
            builder.addCustomData(ArtifactoryBuiltInGroupProperty.artifactory_admin.name(), "true");
        }
    }

    static Realm toAccessRealm(String realm) {
        if (StringUtils.isBlank(realm) || realm.equals(SecurityConstants.DEFAULT_REALM) ||
                realm.equals("artifactory")) {
            return Realm.INTERNAL;
        } else {
            return Realm.valueOf(realm);
        }
    }

    static String fromAccessRealm(Realm realm) {
        if (realm == null) {
            return null;
        }

        if (realm.equals(Realm.INTERNAL)) {
            return SecurityConstants.DEFAULT_REALM;
        }

        return realm.getName();
    }
}
