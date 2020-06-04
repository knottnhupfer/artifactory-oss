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

package org.artifactory.security.interceptor;

import org.artifactory.interceptor.Interceptor;
import org.artifactory.security.Acl;
import org.artifactory.security.MutableAcl;
import org.artifactory.security.PermissionTarget;
import org.artifactory.security.SecurityInfo;

import java.util.List;

/**
 * @author Yossi Shaul
 */
public interface SecurityConfigurationChangesInterceptor extends Interceptor {

    // TODO: Missing on updates events. Cannot be used for SecurityListener.

    /**
     * Called when a new user is added
     *
     * @param user Username
     */
    default void onUserAdd(String user) {

    }

    /**
     * Called when a user is removed
     *
     * @param user Username
     */
    default void onUserDelete(String user) {

    }

    /**
     * Called when users are added to a group
     *
     * @param groupName Name of group the users were added to
     * @param usernames Name of users that were added to the group
     */
    default void onAddUsersToGroup(String groupName, List<String> usernames) {

    }

    /**
     * Called when users are added to a group
     *
     * @param groupName Name of group the users were removed from
     * @param usernames Name of users that were removed from the group
     */
    default void onRemoveUsersFromGroup(String groupName, List<String> usernames) {

    }

    /**
     * Called when a new group is added
     *
     * @param group Group name
     * @param realm
     */
    default void onGroupAdd(String group, String realm) {

    }

    /**
     * Called when a group is removed
     *
     * @param group Group name
     */
    default void onGroupDelete(String group) {

    }

    /**
     * Called when a group is removed
     *
     * @param group Group name
     * @param realm
     */
    default void onGroupUpdate(String group, String realm) {

    }

    /**
     * Called when new permissions are added
     */
    default void onPermissionsAdd(MutableAcl<? extends PermissionTarget> added) {

    }

    /**
     * Called when permissions are updated
     */
    default void onPermissionsUpdate(MutableAcl<? extends PermissionTarget> updated) {

    }

    /**
     * Called when permissions are removed
     */
    default void onPermissionsDelete(Acl<? extends PermissionTarget> deleted) {

    }

    /**
     * Called before security data is imported
     *
     * @param securityInfo Security data
     */
    default void onBeforeSecurityImport(SecurityInfo securityInfo) {

    }


    /**
     * Called After security data is imported
     *
     * @param securityInfo Security data
     */
    default void onAfterSecurityImport(SecurityInfo securityInfo) {

    }
}