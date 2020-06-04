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

package org.artifactory.storage.db.upgrades.access.itest;

import com.google.common.collect.ImmutableSet;
import org.artifactory.security.AceInfo;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.RepoAcl;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.db.security.service.access.emigrate.SecurityEmigratorFetchers;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Shemesh
 */
public class SecurityEmigratorFetchersTest extends UpgradeBaseTest {
    private static final Logger log = LoggerFactory.getLogger(SecurityEmigratorFetchersTest.class);

    SecurityEmigratorFetchers securityEmigratorFetchers;
    ImmutableSet<String> admins = ImmutableSet.of("adminWithAdminGroup", "adminWithAdminGroup", "admin",
            "u2", "u3", "adminByGroup");
    ImmutableSet<String> notAdmins = ImmutableSet.of("u1", "anonymous", "userNoGroups");

    @BeforeClass
    public void beforeClass() throws Exception {
        securityEmigratorFetchers = applicationContext.getBean(SecurityEmigratorFetchers.class);
        resetToVersion(ArtifactoryDBVersion.v203);
        deleteTables("permission_target_repos", "aces", "acls", "permission_targets", "users_groups", "user_props", "groups", "users");

        importSql("/sql/user-group.sql");
        importSql("/sql/acls.sql");
    }

    private void deleteTables(String... tables) {
        Stream.of(tables).forEachOrdered(table -> {
            try {
                jdbcHelper.executeUpdate("DELETE FROM " + table);
            } catch (Exception e) {
                log.error("Error", e);
            }
        });
    }

    @Test
    public void testGetAllUserInfos() throws Exception {
        List<UserInfo> allUserInfos = securityEmigratorFetchers.getAllUserInfos();

        assertEquals(allUserInfos.size(), 8);
        UserInfo u2 = allUserInfos.stream().filter(user -> user.getUsername().equals("u2")).findFirst().get();
        assertEquals(u2.getEmail(), "f@mail.com");
        assertTrue(u2.isEnabled());
        assertTrue(u2.isUpdatableProfile());

        UserInfo u1 = allUserInfos.stream().filter(user -> user.getUsername().equals("u1")).findFirst().get();
        assertTrue(u1.isCredentialsExpired());
        assertEquals(u1.getRealm(), "artifactory");
        assertEquals(u1.getSalt(), "salt");
        assertEquals(u1.getGenPasswordKey(), "genPassword");
        assertEquals(u1.getPrivateKey(), "private");
        assertEquals(u1.getPublicKey(), "public");
        assertEquals(u1.getLastLoginTimeMillis(), 100);
        assertEquals(u1.getLastLoginClientIp(), "10.0.0.01");
        assertEquals(u1.getBintrayAuth(), "bintray");
        assertTrue(u1.isLocked());

        assertEquals(u1.getUserProperty("test.null").get(), "");
        assertEquals(u1.getUserProperty("test.dup").get(), "A");

        List<UserInfo> admins = allUserInfos.stream().filter(UserInfo::isEffectiveAdmin).collect(Collectors.toList());
        assertEquals(admins.size(), this.admins.size(), admins.toString());
        assertTrue(admins.stream().map(UserInfo::getUsername).allMatch(this.admins::contains));

        List<UserInfo> notAdmins = allUserInfos.stream().filter(user -> !user.isEffectiveAdmin()).collect(Collectors.toList());
        assertEquals(notAdmins.size(), this.notAdmins.size(), notAdmins.toString());
        assertTrue(notAdmins.stream().map(UserInfo::getUsername).allMatch(this.notAdmins::contains));
    }

    @Test
    public void testGetAllGroupInfos() throws Exception {
        List<GroupInfo> allGroupInfos = securityEmigratorFetchers.getAllGroupInfos();

        assertEquals(allGroupInfos.size(), 5);

        GroupInfo g2 = allGroupInfos.stream().filter(group -> group.getGroupName().equals("g2")).findFirst().get();
        assertEquals(g2.getRealm(), "default realm");
        assertEquals(g2.getDescription(), "is default");
        assertTrue(g2.isNewUserDefault());
        assertEquals(g2.getRealmAttributes(), "default att");

        GroupInfo admins = allGroupInfos.stream().filter(group -> group.getGroupName().equals("admins")).findFirst().get();
        assertTrue(admins.isAdminPrivileges());
    }

    @Test
    public void testGetAllAclInfos() throws Exception {
        List<RepoAcl> allRepoAcls = securityEmigratorFetchers.getAllAclInfos();

        assertEquals(allRepoAcls.size(), 4);

        RepoAcl noam = allRepoAcls.stream().filter(acl -> "noam".equals(acl.getUpdatedBy())).findFirst().get();
        assertEquals(noam.getPermissionTarget().getName(), "perm-target-1");
        assertEquals(noam.getAces().size(), 3);
        assertEquals(noam.getAces().stream().filter(AceInfo::isGroup).findFirst().get().getPrincipal(), "g1");
        assertEquals(noam.getPermissionTarget().getRepoKeys().size(), 1);
        assertEquals(noam.getPermissionTarget().getRepoKeys().stream().findFirst().get(), "ANY");

        RepoAcl permTarget4 = allRepoAcls.stream()
                .filter(acl -> acl.getPermissionTarget().getName().equals("perm-target-4")).findFirst().get();
        assertEquals(permTarget4.getPermissionTarget().getIncludes().size(), 2);
        assertEquals(permTarget4.getPermissionTarget().getExcludes().size(), 1);
        assertEquals(permTarget4.getPermissionTarget().getExcludes().get(0), "codehaus/**");
    }

}