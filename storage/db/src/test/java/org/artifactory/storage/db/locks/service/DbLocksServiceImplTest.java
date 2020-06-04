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

package org.artifactory.storage.db.locks.service;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.locks.LockInfo;
import org.artifactory.storage.db.locks.dao.DbDistributeLocksDao;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Sets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.artifactory.storage.db.locks.service.DbLocksService.REPLICATION_LOCK_CATEGORY;
import static org.mockito.Mockito.*;

/**
 * @author Shay Bagants
 */
public class DbLocksServiceImplTest extends ArtifactoryHomeBoundTest {

    private DbLocksServiceImpl dbLocksService;
    private DbDistributeLocksDao lockDaoMock;
    private ConcurrentHashMap<String, LockInfo> lockCache;
    private ArtifactoryContext context;

    @SuppressWarnings("unchecked")
    @BeforeClass
    public void setupClass() {
        lockDaoMock = mock(DbDistributeLocksDao.class);
        lockCache = mock(ConcurrentHashMap.class);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        context = mock(ArtifactoryContext.class);
        when(context.getServerId()).thenReturn("server");
        ArtifactoryContextThreadBinder.bind(context);
        dbLocksService = new DbLocksServiceImpl(lockDaoMock);
        setMockCacheLockingMap();
    }

    private void setMockCacheLockingMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = dbLocksService.getClass().getDeclaredField("localLocks");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(dbLocksService, lockCache);
    }

    @AfterMethod
    public void tearDown() {
        ArtifactoryContextThreadBinder.unbind();
        reset(lockCache, lockDaoMock);
    }

    @Test
    public void testCleanOrphanLock() throws Exception {
        List<String> activeServers = ImmutableList.of("activeServer1", "activeServer2");
        Set<LockInfo> locksToDelete = Sets.newHashSet(Arrays.asList(
                new LockInfo("my-category2", "key1", "staleServer1", 1L, "thread1", 1L),
                new LockInfo("my-category2", "key2", "staleServer2", 1L, "thread1", 1L),
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key3", "nonExistingServer", 1L, "thread1", 1L)));
        when(lockDaoMock.getAllLocksNotOwnedBy(anyList())).thenReturn(locksToDelete);
        when(lockDaoMock.releaseForceLock(anyString(), anyString())).thenReturn(true);

        dbLocksService.cleanOrphanLocks(activeServers);
        verify(lockDaoMock, times(1)).getAllLocksNotOwnedBy(activeServers);
        for (LockInfo lock : locksToDelete) {
            verify(lockCache).remove(dbLocksService.toLocalId(lock.getCategory(), lock.getKey()));
            verify(lockDaoMock).releaseForceLock(lock.getCategory(), lock.getKey());
        }
    }

    @Test
    public void testLockingMapSize() throws SQLException {
        when(lockDaoMock.lockingMapSize(anyString())).thenReturn(3);
        int myCategory = dbLocksService.lockingMapSize("myCategory");
        Assert.assertEquals(myCategory, 3);
        verify(lockDaoMock, times(1)).lockingMapSize(any());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed to query out number of locks for.*\\. my reason.*")
    public void testLockingMapSizeErrorMessage() throws SQLException {
        when(lockDaoMock.lockingMapSize("myCategory")).thenThrow(new SQLException("my reason"));
        dbLocksService.lockingMapSize("myCategory");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCleanOrphanLockWithEmptyList() {
        dbLocksService.cleanOrphanLocks(Lists.newArrayList());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCleanOrphanLockUsingNull() {
        dbLocksService.cleanOrphanLocks(null);
    }

    @Test
    public void testCleanCachedExpiredLocks() throws SQLException {
        long currentTime = System.currentTimeMillis();
        Set<LockInfo> expectedGenericLocksToClean = getExpectedGenericExpiredLocks(currentTime);
        Set<LockInfo> expectedGenericLocksToRemain = getExpectedGenericNonExpiredLocks(currentTime);
        Set<LockInfo> expectedReplicationLocksToClean = getExpectedReplicationExpiredLock(currentTime);
        Set<LockInfo> expectedReplicationLocksToRemain = getExpectedReplicationNonExpiredLocks(currentTime);
        Set<LockInfo> allLocks = Sets.newHashSet();
        allLocks.addAll(expectedGenericLocksToClean);
        allLocks.addAll(expectedGenericLocksToRemain);
        allLocks.addAll(expectedReplicationLocksToClean);
        allLocks.addAll(expectedReplicationLocksToRemain);

        Set<LockInfo> locksToDelete = com.google.common.collect.Sets.union(expectedGenericLocksToClean, expectedReplicationLocksToClean);
        Set<LockInfo> locksRoRemain = com.google.common.collect.Sets.union(expectedGenericLocksToRemain, expectedReplicationLocksToRemain);
        when(lockCache.values()).thenReturn(allLocks);
        dbLocksService.cleanCachedExpiredLocks();
        for (LockInfo lock : locksToDelete) {
            verify(lockCache, times(1)).remove(dbLocksService.toLocalId(lock.getCategory(), lock.getKey()));
            verify(lockDaoMock,times(0)).releaseForceLock(lock.getCategory(), lock.getKey());
        }
        for (LockInfo lock : locksRoRemain) {
            verify(lockCache, times(0)).remove(dbLocksService.toLocalId(lock.getCategory(), lock.getKey()));
            verify(lockDaoMock, times(0)).releaseForceLock(lock.getCategory(), lock.getKey());
        }
    }

    @Test
    public void testCleanDbExpiredLocks() throws Exception {
        long currentTime = System.currentTimeMillis();
        Set<LockInfo> expectedGenericLocksToClean = getExpectedGenericExpiredLocks(currentTime);
        Set<LockInfo> expectedReplicationLocksToClean = getExpectedReplicationExpiredLock(currentTime);
        Set<LockInfo> expectedReplicationLocksToRemain = getExpectedReplicationNonExpiredLocks(currentTime);
        Set<LockInfo> allLocks = Sets.newHashSet();
        allLocks.addAll(expectedGenericLocksToClean);
        allLocks.addAll(expectedReplicationLocksToClean);
        allLocks.addAll(expectedReplicationLocksToRemain);

        when(lockDaoMock.getExpiredLocks(anyLong())).thenReturn(allLocks);
        when(lockDaoMock.releaseForceLock(anyString(), anyString())).thenReturn(true);

        dbLocksService.cleanDbExpiredLocks();
        Set<LockInfo> locksToDelete = com.google.common.collect.Sets.union(expectedGenericLocksToClean, expectedReplicationLocksToClean);

        for (LockInfo lock : locksToDelete) {
            verify(lockCache).remove(dbLocksService.toLocalId(lock.getCategory(), lock.getKey()));
            verify(lockDaoMock).releaseForceLock(lock.getCategory(), lock.getKey());
        }
        for (LockInfo lock : expectedReplicationLocksToRemain) {
            verify(lockCache, times(0)).remove(dbLocksService.toLocalId(lock.getCategory(), lock.getKey()));
            verify(lockDaoMock, times(0)).releaseForceLock(lock.getCategory(), lock.getKey());
        }
    }

    private Set<LockInfo> getExpectedReplicationNonExpiredLocks(long currentTime) {
        int lockLeaseTimeoutMin = ConstantValues.hazelcastMaxLockLeaseTime.getInt();
        int replicationLockLeaseTimeoutMin = ConstantValues.hazelcastMaxLockLeaseTime.getInt() * 24;
        return Sets.newHashSet(Arrays.asList(
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key5", "ownerWho", 5L, "thread3", currentTime),
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key6", "ownerWhy", 3L, "thread3",
                        currentTime - TimeUnit.MINUTES.toMillis(lockLeaseTimeoutMin)),
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key7", "ownerMe", 12L, "threadZ",
                        currentTime - TimeUnit.MINUTES.toMillis(lockLeaseTimeoutMin * 2)),
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key8", "ownerHim", 52L, "threadY",
                        currentTime - TimeUnit.MINUTES.toMillis(replicationLockLeaseTimeoutMin - 1)),
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key9", "ownerZ", 3L, "threadO",
                        currentTime - TimeUnit.MINUTES.toMillis(replicationLockLeaseTimeoutMin - 10))));
    }

    private Set<LockInfo> getExpectedReplicationExpiredLock(long currentTime) {
        int replicationLockLeaseTimeoutMin = ConstantValues.hazelcastMaxLockLeaseTime.getInt() * 24;
        return Sets.newHashSet(Arrays.asList(
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key3", "owner1", 2L, "thread1",
                        currentTime - TimeUnit.MINUTES.toMillis(replicationLockLeaseTimeoutMin + 1)),
                new LockInfo(REPLICATION_LOCK_CATEGORY, "key4", "owner2", 1L, "thread1",
                        currentTime - TimeUnit.MINUTES.toMillis(replicationLockLeaseTimeoutMin + 12))));
    }

    private Set<LockInfo> getExpectedGenericExpiredLocks(long currentTime) {
        int lockLeaseTimeoutMin = ConstantValues.hazelcastMaxLockLeaseTime.getInt();
        return Sets.newHashSet(Arrays.asList(
                    new LockInfo("my-category", "key1", "owner1", 1L, "thread1",
                            currentTime - TimeUnit.MINUTES.toMillis(lockLeaseTimeoutMin + 1)),
                    new LockInfo("my-category2", "key2", "owner1", 1L, "thread1",
                            currentTime - TimeUnit.MINUTES.toMillis(lockLeaseTimeoutMin * 3))));
    }

    private Set<LockInfo> getExpectedGenericNonExpiredLocks(long currentTime) {
        int lockLeaseTimeoutMin = ConstantValues.hazelcastMaxLockLeaseTime.getInt();
        return Sets.newHashSet(Arrays.asList(
                new LockInfo("my-awesome-category", "myNonExpiredLock", "owner1", 1L, "thread1",
                        currentTime),
                new LockInfo("my-awesome-category", "myNonExpiredLock2", "owner1", 1L, "thread1",
                        currentTime - TimeUnit.MINUTES.toMillis(lockLeaseTimeoutMin - 10))));
    }
}