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

package org.artifactory.storage.db.fs.itest.dao;

import com.google.common.collect.ImmutableList;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.locks.LockInfo;
import org.artifactory.storage.db.locks.dao.DbDistributeLocksDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import static org.artifactory.storage.db.locks.service.DbLocksService.REPLICATION_LOCK_CATEGORY;
import static org.testng.Assert.*;

/**
 * Test the {@link DbDistributeLocksDao} functionality
 *
 * @author Shay Bagants
 */
@Test
public class DbDistributeLocksDaoTest extends DbBaseTest {

    @Autowired
    private DbDistributeLocksDao dbLockDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/db_locks.sql");
    }

    // test pre-defined locks and make sure these exists
    public void testIsLocked() throws SQLException {
        // expected to be locked
        assertTrue(dbLockDao.isLocked("shay", "path/to/file.jar"));
        assertTrue(dbLockDao.isLocked("shay", "mykey"));
        assertTrue(dbLockDao.isLocked("bagants", "path/to/file2.exe"));
        assertTrue(dbLockDao.isLocked("arti", "path/to/file3.zip"));
        assertTrue(dbLockDao.isLocked("somemap", "imtheindexerjob"));
        assertTrue(dbLockDao.isLocked("somemap", "nugetAddPkg"));

        // non locked paths
        assertFalse(dbLockDao.isLocked("shay", "/path/to/file.jar"));
        assertFalse(dbLockDao.isLocked("shay", "path/to/file2.exe"));
        assertFalse(dbLockDao.isLocked("generic", "path/to/file2.jar"));
        assertFalse(dbLockDao.isLocked("zyd", "path/to/file2.jar"));
        assertFalse(dbLockDao.isLocked("somemap", "newpath"));
    }

    // test pre-defined locks per category and make sure the lockingMapSize return the correct num of locks per category
    public void testLockingMapSize() throws SQLException {
        assertEquals(dbLockDao.lockingMapSize("shay"), 2);
        assertEquals(dbLockDao.lockingMapSize("bagants"), 1);
        assertEquals(dbLockDao.lockingMapSize("arti"), 1);
        assertEquals(dbLockDao.lockingMapSize("somemap"), 2);
        assertEquals(dbLockDao.lockingMapSize("nonexistscategory"), 0);
    }

    // test pre-defined locks per category
    public void testLockingMapKeySet() throws SQLException {
        // test 'shay' category
        Set<String> shay = dbLockDao.lockingMapKeySet("shay");
        assertEquals(shay.size(), 2);
        assertTrue(shay.contains("path/to/file.jar"));
        assertTrue(shay.contains("mykey"));
        assertFalse(shay.contains("blabla"));

        // test 'bagants' category
        Set<String> bagants = dbLockDao.lockingMapKeySet("bagants");
        assertEquals(bagants.size(), 1);
        assertTrue(bagants.contains("path/to/file2.exe"));
        assertFalse(bagants.contains("path/to/where"));

        // test non existing category
        Set<String> who = dbLockDao.lockingMapKeySet("who");
        assertEquals(who.size(), 0);
    }

    // test the getAllCurrentServerLocks method using pre-defined locks
    public void testGetAllCurrentServerLocks() throws SQLException {
        Set<LockInfo> locks = dbLockDao.getAllCurrentServerLocks("froggy");
        assertOnlyFroggyLocksExist(locks);
    }

    private void assertOnlyFroggyLocksExist(Set<LockInfo> locks) {
        assertEquals(locks.size(), 4);
        assertTrue(locks
                .contains(new LockInfo("somemap", "nugetAddPkg", "froggy", 745, "thread-async-1", 1284289204000L)));
        assertTrue(locks
                .contains(new LockInfo("somemap", "imtheindexerjob", "froggy", 746, "thread-async-0", 1284292504000L)));
        assertTrue(locks
                .contains(new LockInfo(REPLICATION_LOCK_CATEGORY, "my:key", "froggy", 746, "thread-async-0", 1284290404000L)));
        assertTrue(locks
                .contains(new LockInfo(REPLICATION_LOCK_CATEGORY, "my:expiredKey", "froggy", 746, "thread-async-5", 1284287400000L)));
    }

    // test the getExpiredLocks method
    public void testGetExpiredLocks() throws SQLException {
        Set<LockInfo> expiredLocks = dbLockDao.getExpiredLocks(1284289624000L);
        assertEquals(expiredLocks.size(), 6);
        assertTrue(expiredLocks
                .contains(new LockInfo("shay", "path/to/file.jar", "owner1", 513, "thread-test1", 1284289504000L)));
        assertTrue(expiredLocks.contains(new LockInfo("shay", "mykey", "owner1", 513, "thread-test1", 1284289504000L)));
        assertTrue(expiredLocks
                .contains(new LockInfo("bagants", "path/to/file2.exe", "owner2", 513, "thread-test1", 1284289504000L)));
        assertTrue(expiredLocks
                .contains(new LockInfo("arti", "path/to/file3.zip", "owner5", 111, "thread-bugger-8", 1284289204000L)));
        assertTrue(expiredLocks
                .contains(new LockInfo("somemap", "nugetAddPkg", "froggy", 745, "thread-async-1", 1284289204000L)));
        assertTrue(expiredLocks
                .contains(new LockInfo(REPLICATION_LOCK_CATEGORY, "my:expiredKey", "froggy", 746, "thread-async-5", 1284287400000L)));

        // assert false
        assertFalse(expiredLocks
                .contains(new LockInfo("somemap", "mtheindexerjob", "froggy", 746, "thread-async-0", 1284292504000L)));
    }

    // test deleteLock method - add lock, make sure it exists, delete it, make sure it was deleted and that the pre-defined locks still exists
    public void testDeleteLock() throws SQLException {
        LockInfo lockInfo = acquireLockInternal("ownerxyz", "cat1", "nibuvy");
        boolean deleted = dbLockDao.deleteLock(lockInfo.getCategory(), lockInfo.getKey(), lockInfo.getOwner());
        assertTrue(deleted);
        assertFalse(dbLockDao.isLocked(lockInfo.getCategory(), lockInfo.getKey()));

        // make sure all other pre-defined locks still exists
        testIsLocked();
        testLockingMapSize();
        testLockingMapKeySet();
    }

    // test deleteAllOwnerLocks method - add two lock for same owner, make sure these exists, delete locks per owner, make sure these were deleted and that the pre-defined locks still exists
    public void testDeleteAllOwnerLocks() throws SQLException {
        String owner = "pim";
        // acquire two locks
        LockInfo pimLock = acquireLockInternal(owner, "yyy", "ooo");
        LockInfo pim2Lock = acquireLockInternal(owner, "qqq", "abc");
        assertTrue(dbLockDao.isLocked(pimLock.getCategory(), pimLock.getKey()));
        assertTrue(dbLockDao.isLocked(pim2Lock.getCategory(), pim2Lock.getKey()));

        // make sure all locks of 'pim' were deleted
        int deleted = dbLockDao.deleteAllOwnerLocks(owner);
        assertEquals(deleted, 2);
        assertFalse(dbLockDao.isLocked(pimLock.getCategory(), pimLock.getKey()));
        assertFalse(dbLockDao.isLocked(pim2Lock.getCategory(), pim2Lock.getKey()));

        // make sure all other pre-defined locks still exists
        testIsLocked();
        testLockingMapSize();
        testLockingMapKeySet();
    }

    public void testGetAllLocksNotOwnedBy() throws SQLException {
        Set<LockInfo> locksNotOwnedBy = dbLockDao.getAllLocksNotOwnedBy(ImmutableList.of("owner1", "owner2", "owner5"));
        assertOnlyFroggyLocksExist(locksNotOwnedBy);
    }

    public void testGetAllLocksNotOwnedByUsingEmptyList() throws SQLException {
        Set<LockInfo> locksNotOwnedBy = dbLockDao.getAllLocksNotOwnedBy(Collections.emptyList());
        assertEquals(locksNotOwnedBy.size(), 0);
    }

    // test the releaseForceLock method
    public void testReleaseForceLock() throws SQLException {
        LockInfo lockInfo = acquireLockInternal("me", "000", "111");
        assertTrue(dbLockDao.isLocked(lockInfo.getCategory(), lockInfo.getKey()));
        boolean released = dbLockDao.releaseForceLock(lockInfo.getCategory(), lockInfo.getKey());
        assertTrue(released);
        assertFalse(dbLockDao.isLocked(lockInfo.getCategory(), lockInfo.getKey()));

        // make sure all other pre-defined locks still exists
        testIsLocked();
        testLockingMapSize();
        testLockingMapKeySet();
    }

    private LockInfo acquireLockInternal(String owner, String category, String key) throws SQLException {
        LockInfo lockInfo = new LockInfo(category, key, owner, Thread.currentThread().getId(),
                Thread.currentThread().getName(), System.currentTimeMillis());
        boolean locked = dbLockDao.tryToAcquireLock(lockInfo);
        assertTrue(locked);
        assertTrue(dbLockDao.isLocked(lockInfo.getCategory(), lockInfo.getKey()));
        LockInfo dbLockInfo = dbLockDao.getLockInfo(lockInfo.getCategory(), lockInfo.getKey());
        assertEquals(lockInfo, dbLockInfo);
        return lockInfo;
    }
}
