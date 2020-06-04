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

import org.artifactory.config.CentralConfigKey;
import org.artifactory.repo.interceptor.Interceptors;
import org.artifactory.security.Acl;
import org.artifactory.security.MutableAcl;
import org.artifactory.security.PermissionTarget;
import org.artifactory.security.SecurityInfo;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Yossi Shaul
 */
@Service
@Reloadable(beanClass = SecurityConfigurationChangesInterceptors.class, initAfter = DbService.class,
        listenOn = CentralConfigKey.none)
public class SecurityConfigurationChangesInterceptorsImpl extends Interceptors<SecurityConfigurationChangesInterceptor>
        implements SecurityConfigurationChangesInterceptors {

    @Override
    public void onUserAdd(String user) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onUserAdd(user);
        }
    }

    @Override
    public void onUserDelete(String user) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onUserDelete(user);
        }
    }

    @Override
    public void onAddUsersToGroup(String groupName, List<String> usernames) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onAddUsersToGroup(groupName, usernames);
        }
    }

    @Override
    public void onRemoveUsersFromGroup(String groupName, List<String> usernames) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onRemoveUsersFromGroup(groupName, usernames);
        }
    }

    @Override
    public void onGroupAdd(String group, String realm) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onGroupAdd(group, realm);
        }
    }

    @Override
    public void onGroupUpdate(String group, String realm) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onGroupUpdate(group, realm);
        }
    }

    @Override
    public void onGroupDelete(String group) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onGroupDelete(group);
        }
    }

    @Override
    public void onPermissionsAdd(MutableAcl<? extends PermissionTarget> added) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onPermissionsAdd(added);
        }
    }

    @Override
    public void onPermissionsUpdate(MutableAcl<? extends PermissionTarget> updated) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onPermissionsUpdate(updated);
        }
    }

    @Override
    public void onPermissionsDelete(Acl<? extends PermissionTarget> deleted) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onPermissionsDelete(deleted);
        }
    }

    @Override
    public void onBeforeSecurityImport(SecurityInfo securityInfo) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onBeforeSecurityImport(securityInfo);
        }
    }

    @Override
    public void onAfterSecurityImport(SecurityInfo securityInfo) {
        for (SecurityConfigurationChangesInterceptor interceptor : this) {
            interceptor.onAfterSecurityImport(securityInfo);
        }
    }
}