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

import com.google.common.collect.Lists;
import org.artifactory.model.xstream.security.GroupImpl;
import org.artifactory.security.GroupInfo;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.GroupFilter.*;
import static org.fest.assertions.Assertions.assertThat;

/**
 * Unit tests for the {@link AccessUserGroupStoreService.GroupFilter}.
 *
 * @author Yossi Shaul
 */
@Test
public class GroupFilterTest {

    private static final GroupImpl internal = new GroupImpl("internal_group");
    private static final GroupImpl internalAutoJoin = new GroupImpl("internal_auto_group", "", true, false);
    private static final GroupImpl admin = new GroupImpl("admin_group", "", false, true);
    private static final GroupImpl saml = new GroupImpl("saml_group", "", "saml");
    private static final GroupImpl ldapAutoJoin = new GroupImpl("ldap_group", "", true, "ldap", "", false);
    private static final GroupImpl ldapAdmin = new GroupImpl("ldap_admin_group", "", false, "ldap", "", true);

    private static final List<GroupInfo> groups = Lists.newArrayList(
            internal, internalAutoJoin, admin, saml, ldapAutoJoin, ldapAdmin);

    public void internalGroupsFilter() {
        List<GroupInfo> result = groups.stream().filter(INTERNAL.filterFunction).collect(Collectors.toList());
        assertThat(result).hasSize(3).containsExactly(internal, internalAutoJoin, admin);
    }

    public void externalGroupsFilter() {
        List<GroupInfo> result = groups.stream().filter(EXTERNAL.filterFunction).collect(Collectors.toList());
        assertThat(result).hasSize(3).containsExactly(saml, ldapAutoJoin, ldapAdmin);
    }

    public void adminGroupsFilter() {
        List<GroupInfo> result = groups.stream().filter(ADMIN.filterFunction).collect(Collectors.toList());
        assertThat(result).hasSize(2).containsExactly(admin, ldapAdmin);
    }

    public void autoJoinGroupsFilter() {
        List<GroupInfo> result = groups.stream().filter(DEFAULTS.filterFunction).collect(Collectors.toList());
        assertThat(result).containsExactly(internalAutoJoin, ldapAutoJoin);
    }

}
