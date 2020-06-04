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

package org.artifactory.addon.ldapgroup;

import org.artifactory.addon.Addon;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.security.ldap.group.LdapGroupPopulatorStrategies;
import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;

import java.util.List;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public interface LdapUserGroupAddon extends Addon {

    /**
     * refresh ldap groups
     *
     * @param userName         - refresh by user name
     * @param ldapGroupSetting - ldap group settings
     * @param statusHolder     -  import status holder
     * @return List of Groups found following refresh
     */
    default Set<LdapUserGroup> refreshLdapGroups(String userName, LdapGroupSetting ldapGroupSetting,
                                                 BasicStatusHolder statusHolder) {
        return null;
    }

    /**
     * import ldap groups into artifactory
     *
     * @param ldapGroups - ldap groups to be imported
     * @param strategy   - ldap group strategy
     * @return number group imported
     */
    default int importLdapGroupsToArtifactory(List ldapGroups, LdapGroupPopulatorStrategies strategy) {
        return 0;
    }

    default String[] retrieveUserLdapGroups(String userName, LdapGroupSetting ldapGroupSetting) {
        return null;
    }
}
