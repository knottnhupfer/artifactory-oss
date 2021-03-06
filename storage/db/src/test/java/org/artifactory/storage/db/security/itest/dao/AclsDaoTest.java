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

package org.artifactory.storage.db.security.itest.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.artifactory.security.PermissionTarget;
import org.artifactory.storage.db.security.dao.AclsDao;
import org.artifactory.storage.db.security.dao.PermissionTargetsDao;
import org.artifactory.storage.db.security.entity.Ace;
import org.artifactory.storage.db.security.entity.Acl;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Date: 11/13/12
 * Time: 4:41 PM
 *
 * @author freds
 */
@Deprecated
public class AclsDaoTest extends SecurityBaseDaoTest {

    @Autowired
    private AclsDao aclsDao;

    @Autowired
    private PermissionTargetsDao permissionTargetsDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/user-group.sql");
        importSql("/sql/acls.sql");
    }

    public void testLoadPermissionTargets() throws SQLException {
        for (long targetId = 1L; targetId < 5L; targetId++) {
            assertAnyPermissionTarget(targetId, permissionTargetsDao.findPermissionTarget(targetId));
        }
    }

    private void assertAnyPermissionTarget(long id, org.artifactory.storage.db.security.entity.PermissionTarget pt) {
        switch ((int) id) {
            case 1:
                assertPermissionTarget1(pt);
                break;
            case 2:
                assertPermissionTarget2(pt);
                break;
            case 3:
                assertPermissionTarget3(pt);
                break;
            case 4:
                assertPermissionTarget4(pt);
                break;
            default:
                fail("Permission Target unknown " + pt);
                break;
        }
    }

    private void assertPermissionTarget1(org.artifactory.storage.db.security.entity.PermissionTarget pt) {
        assertEquals(pt.getName(), "perm-target-1");
        assertNull(pt.getIncludesPattern());
        assertNull(pt.getExcludesPattern());
        assertTrue(pt.getIncludes().isEmpty());
        assertTrue(pt.getExcludes().isEmpty());
        assertEquals(pt.getRepoKeys(), ImmutableSet.of(PermissionTarget.ANY_REPO));
    }

    private void assertPermissionTarget2(org.artifactory.storage.db.security.entity.PermissionTarget pt) {
        assertEquals(pt.getName(), "perm-target-2");
        assertEquals(pt.getIncludesPattern(), "com/**,org/**");
        assertNull(pt.getExcludesPattern());
        assertEquals(pt.getIncludes(), ImmutableList.of("com/**", "org/**"));
        assertTrue(pt.getExcludes().isEmpty());
        assertEquals(pt.getRepoKeys(), ImmutableSet.of(PermissionTarget.ANY_LOCAL_REPO));
    }

    private void assertPermissionTarget3(org.artifactory.storage.db.security.entity.PermissionTarget pt) {
        assertEquals(pt.getName(), "perm-target-3");
        assertNull(pt.getIncludesPattern());
        assertEquals(pt.getExcludesPattern(), "apache/**");
        assertTrue(pt.getIncludes().isEmpty());
        assertEquals(pt.getExcludes(), ImmutableList.of("apache/**"));
        assertEquals(pt.getRepoKeys(),
                ImmutableSet.of("libs-release-local", PermissionTarget.ANY_REMOTE_REPO, "libs-snapshot-local"));
    }

    private void assertPermissionTarget4(org.artifactory.storage.db.security.entity.PermissionTarget pt) {
        assertEquals(pt.getName(), "perm-target-4");
        assertEquals(pt.getIncludesPattern(), "jfrog/**,**/art-*.xml");
        assertEquals(pt.getExcludesPattern(), "codehaus/**");
        assertEquals(pt.getIncludes(), ImmutableList.of("jfrog/**", "**/art-*.xml"));
        assertEquals(pt.getExcludes(), ImmutableList.of("codehaus/**"));
        assertTrue(pt.getRepoKeys().isEmpty());
    }

    @Test(dependsOnMethods = "testDeleteAcls")
    public void testLoadAllAcls() throws SQLException {
        Map<Long, org.artifactory.storage.db.security.entity.PermissionTarget> allPermissionTargets = permissionTargetsDao.getAllPermissionTargets();
        for (Map.Entry<Long, org.artifactory.storage.db.security.entity.PermissionTarget> entry : allPermissionTargets.entrySet()) {
            assertAnyPermissionTarget(entry.getKey(), entry.getValue());
        }
        assertAclCollection(aclsDao.getAllAcls(), ImmutableSet.of(10, 20, 30, 40));
    }

    public void testFindAclById() throws SQLException {
        assertAnyAcl(aclsDao.findAcl(10L), 10);
        assertAnyAcl(aclsDao.findAcl(20L), 20);
        assertAnyAcl(aclsDao.findAcl(30L), 30);
    }

    public void testFindAclByPermissionTargetId() throws SQLException {
        assertAnyAcl(aclsDao.findAclByPermissionTargetId(1L), 10);
        assertAnyAcl(aclsDao.findAclByPermissionTargetId(2L), 20);
        assertAnyAcl(aclsDao.findAclByPermissionTargetId(3L), 30);
    }

    public void testUserOrGroupHasAce() throws SQLException {
        assertTrue(aclsDao.userHasAce(1L));
        assertTrue(aclsDao.userHasAce(2L));
        assertFalse(aclsDao.userHasAce(3L));
        assertTrue(aclsDao.groupHasAce(1L));
        assertFalse(aclsDao.groupHasAce(2L));
    }

    public void testCreatePermissionTargetNoRepoKeys() throws SQLException {
        org.artifactory.storage.db.security.entity.PermissionTarget pt = new org.artifactory.storage.db.security.entity.PermissionTarget(100L, "temp-perm-100", "", "");
        pt.setRepoKeys(new HashSet<String>());
        assertEquals(permissionTargetsDao.createPermissionTarget(pt), 1);
        assertTrue(pt.isIdentical(permissionTargetsDao.findPermissionTarget(100L)));
        assertTrue(pt.isIdentical(permissionTargetsDao.findPermissionTarget("temp-perm-100")));
    }

    @Test(dependsOnMethods = "testCreatePermissionTargetNoRepoKeys")
    public void testCreatePermissionTargetWithRepoKeys() throws SQLException {
        org.artifactory.storage.db.security.entity.PermissionTarget pt = new org.artifactory.storage.db.security.entity.PermissionTarget(101L, "temp-perm-101", "", "");
        HashSet<String> repoKeys = new HashSet<String>();
        repoKeys.add("test-repo");
        repoKeys.add("test-repo-2");
        pt.setRepoKeys(repoKeys);
        assertEquals(permissionTargetsDao.createPermissionTarget(pt), 3);
        assertTrue(pt.isIdentical(permissionTargetsDao.findPermissionTarget(101L)));
        assertTrue(pt.isIdentical(permissionTargetsDao.findPermissionTarget("temp-perm-101")));
    }

    @Test(dependsOnMethods = "testCreatePermissionTargetWithRepoKeys",
            expectedExceptions = {IllegalStateException.class},
            expectedExceptionsMessageRegExp = "Permission Target.*not initialized.*repo keys missing.")
    public void testCreatePermissionTargetWrongRepoKeys() throws SQLException {
        org.artifactory.storage.db.security.entity.PermissionTarget pt = new org.artifactory.storage.db.security.entity.PermissionTarget(102L, "temp-perm-102", "", "");
        permissionTargetsDao.createPermissionTarget(pt);
    }

    @Test(dependsOnMethods = {"testCreatePermissionTargetWrongRepoKeys"})
    public void testDeletePermissionTarget() throws SQLException {
        assertEquals(permissionTargetsDao.deletePermissionTarget(102L), 1);
        assertEquals(permissionTargetsDao.deletePermissionTarget(101L), 3);
        assertEquals(permissionTargetsDao.deletePermissionTarget(100L), 1);
    }

    public void testCreateAclNoAces() throws SQLException {
        org.artifactory.storage.db.security.entity.PermissionTarget pt = new org.artifactory.storage.db.security.entity.PermissionTarget(200L, "temp-perm-200", "", "");
        pt.setRepoKeys(new HashSet<String>());
        assertEquals(permissionTargetsDao.createPermissionTarget(pt), 1);
        Acl acl = new Acl(2000L, 200L, System.currentTimeMillis(), "me");
        acl.setAces(new HashSet<Ace>());
        assertEquals(aclsDao.createAcl(acl), 1);
        aclEquals(acl, aclsDao.findAcl(2000L));
        aclEquals(acl, aclsDao.findAclByPermissionTargetId(200L));
    }

    @Test(dependsOnMethods = "testCreateAclNoAces")
    public void testCreateAclWithAces() throws SQLException {
        org.artifactory.storage.db.security.entity.PermissionTarget pt = new org.artifactory.storage.db.security.entity.PermissionTarget(201L, "temp-perm-201", "", "");
        pt.setRepoKeys(new HashSet<String>());
        assertEquals(permissionTargetsDao.createPermissionTarget(pt), 1);
        Acl acl = new Acl(2010L, 201L, System.currentTimeMillis(), "me1");
        HashSet<Ace> aces = new HashSet<Ace>();
        aces.add(new Ace(20100L, 2010L, 5, 15L, 0L));
        aces.add(new Ace(20101L, 2010L, 5, 0L, 15L));
        acl.setAces(aces);
        assertEquals(aclsDao.createAcl(acl), 3);
        aclEquals(acl, aclsDao.findAcl(2010L));
        aclEquals(acl, aclsDao.findAclByPermissionTargetId(201L));
    }

    @Test(dependsOnMethods = "testCreateAclWithAces")
    public void testDeleteAcls() throws SQLException {
        assertEquals(aclsDao.deleteAcl(2010L), 3);
        assertEquals(aclsDao.deleteAcl(2000L), 1);
        assertEquals(permissionTargetsDao.deletePermissionTarget(200L), 1);
        assertEquals(permissionTargetsDao.deletePermissionTarget(201L), 1);
    }

    private void aclEquals(Acl acl, Acl readAcl) {
        assertEquals(acl, readAcl);
        assertEquals(acl.getLastModified(), readAcl.getLastModified());
        assertEquals(acl.getLastModifiedBy(), readAcl.getLastModifiedBy());
        assertEquals(acl.getPermTargetId(), readAcl.getPermTargetId());
        assertEquals(acl.getAces(), readAcl.getAces());
    }

    private void assertAclCollection(Collection<Acl> allAcls, Collection<Integer> aclIds) {
        assertEquals(allAcls.size(), aclIds.size());
        for (Acl acl : allAcls) {
            int aclId = (int) acl.getAclId();
            assertTrue(aclIds.contains(aclId));
            assertAnyAcl(acl, aclId);
        }
    }

    private void assertAnyAcl(Acl acl, int aclId) {
        assertEquals((int) acl.getPermTargetId() * 10, aclId);
        switch (aclId) {
            case 10:
                equalAces(acl.getAces(), ImmutableMap.<Long, Ace>of(
                        1L, new Ace(1L, 10L, 1, 1L, 0L),
                        2L, new Ace(2L, 10L, 2, 0L, 1L),
                        3L, new Ace(3L, 10L, 3, 2L, 0L)
                ));
                assertEquals(acl.getLastModified(), 100L);
                assertEquals(acl.getLastModifiedBy(), "noam");
                break;
            case 20:
                equalAces(acl.getAces(), ImmutableMap.<Long, Ace>of(
                        4L, new Ace(4L, 20L, 3, 1L, 0L),
                        5L, new Ace(5L, 20L, 3, 0L, 1L)
                ));
                assertEquals(acl.getLastModified(), 0L);
                assertNull(acl.getLastModifiedBy());
                break;
            case 30:
                assertTrue(acl.getAces().isEmpty());
                assertEquals(acl.getLastModified(), 0L);
                assertNull(acl.getLastModifiedBy());
                break;
            case 40:
                assertTrue(acl.getAces().isEmpty());
                assertEquals(acl.getLastModified(), 0L);
                assertNull(acl.getLastModifiedBy());
                break;
            default:
                fail("Acl " + acl + " is unknown!");
        }
    }

    private void equalAces(ImmutableSet<Ace> aces, ImmutableMap<Long, Ace> expectedAces) {
        for (Ace ace : aces) {
            Ace aceExpected = expectedAces.get(ace.getAceId());
            assertEquals(ace, aceExpected);
            assertEquals(ace.getAclId(), aceExpected.getAclId());
            assertEquals(ace.getMask(), aceExpected.getMask());
            assertEquals(ace.getUserId(), aceExpected.getUserId());
            assertEquals(ace.getGroupId(), aceExpected.getGroupId());
        }
    }

}
