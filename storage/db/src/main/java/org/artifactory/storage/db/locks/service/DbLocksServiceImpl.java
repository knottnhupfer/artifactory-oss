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

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.locks.DbUnlockSupplier;
import org.artifactory.storage.db.locks.LockInfo;
import org.artifactory.storage.db.locks.dao.DbDistributeLocksDao;
import org.artifactory.storage.fs.lock.LockingDebugUtils;
import org.artifactory.util.CollectionUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.config.diff.DataDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.artifactory.common.ConstantValues.hazelcastMaxLockLeaseTime;
import static org.artifactory.storage.db.locks.service.HaDbLockHelper.*;

/**
 * @author gidis
 */
@Service
@Reloadable(beanClass = DbLocksService.class, initAfter = DbService.class)
public class DbLocksServiceImpl implements DbLocksService {
    private static final Logger log = LoggerFactory.getLogger(DbLocksServiceImpl.class);

    private final DbDistributeLocksDao dbDistributeLocksDao;
    private final Map<String, LockInfo> localLocks;

    @Autowired
    public DbLocksServiceImpl(DbDistributeLocksDao dbDistributeLocksDao) {
        this.dbDistributeLocksDao = dbDistributeLocksDao;
        localLocks = new ConcurrentHashMap<>();
    }

    @Override
    public void acquireLock(String category, String key, String owner, long timeout, TimeUnit timeUnit)
            throws TimeoutException {
        timeout = timeUnit.toMillis(timeout);
        long start = System.currentTimeMillis();
        log.debug("Acquiring lock for: " + getLockInfo(category, key, owner));
        long startTime = System.currentTimeMillis();
        long sleepTime = 8;
        long waitingTime;
        LockInfo lockInfo = new LockInfo(category, key, owner, Thread.currentThread().getId(),
                Thread.currentThread().getName(), start);
        while (true) {
            try {
                // Assume key is not locked
                log.trace("Attempting to acquire lock for: " + getLockInfo(category, key, owner));
                // we update the time on the model before inserting into the db, because in case we wait for x time
                // until we were able to acquire the lock, we don't want it's expiry to be shorter.
                lockInfo.setStartedTime(System.currentTimeMillis());
                if (dbDistributeLocksDao.tryToAcquireLock(lockInfo)) {
                    localLocks.put(toLocalId(category, key), lockInfo);
                    log.trace("Successfully acquired lock: '{}'.", lockInfo);
                    long timeTookToAcquireLock = System.currentTimeMillis() - start;
                    log.debug("Lock acquired in '{}' milliseconds.", timeTookToAcquireLock);
                    return;
                } else {
                    LockInfo dbLockInfo = dbDistributeLocksDao.getLockInfo(category, key);
                    if (dbLockInfo != null && dbLockInfo.getThreadId() == Thread.currentThread().getId() &&
                            dbLockInfo.getOwner().equals(owner)) {
                        throw new RuntimeException("Reentrant lock is not supported");
                    }
                }
            } catch (Exception e) {
                log.error("Failed to acquire lock for: " + lockInfo, e);
                throw new RuntimeException("Failed to acquire lock for: " + getLockInfo(category, key, owner), e);
            }
            // If we got here the lock exist but we don't own it
            long currentTime = System.currentTimeMillis();
            waitingTime = currentTime - startTime;
            waitLimitedTime(timeout, sleepTime, waitingTime);
            sleepTime = Math.min(sleepTime * 4, 2048);
            log.trace("Waiting while trying to acquiring lock for: " + getLockInfo(category, key, owner));
        }
    }

    @Override
    public boolean isLocked(String category, String key) {
        log.debug("Checking if lock exist for: " + getLockInfo(category, key));
        try {
            if (localLocks.containsKey(toLocalId(category, key))) {
                log.trace("Lock exist locally for: " + getLockInfo(category, key));
                return true;
            }
            if (dbDistributeLocksDao.isLocked(category, key)) {
                log.trace("Lock exist for: " + getLockInfo(category, key));
                return true;
            }
            log.trace("Lock doesn't exist for: " + getLockInfo(category, key));
            return false;
        } catch (SQLException e) {
            log.debug("Failed to check if lock exist for:" + getLockInfo(category, key), e);
            throw new RuntimeException("Failed to check if lock exist for:" + getLockInfo(category, key), e);
        }
    }

    @Override
    public boolean isLockedByMe(String category, String key) {
        LockInfo lockInfo = localLocks.get(toLocalId(category, key));
        return lockInfo != null && Thread.currentThread().getId() == lockInfo.getThreadId();
    }


    @Override
    public boolean unlock(String category, String key, String owner) {
        log.debug("Attempting to release lock for: " + getLockInfo(category, key));
        try {
            // Assume lock acquired by this thread and try to update lock
            log.trace("Attempting to release lock from cache for: " + getLockInfo(category, key, owner));
            LockInfo lockInfo = localLocks.get(toLocalId(category, key));
            if (lockInfo != null) {
                if (lockInfo.getThreadId() == Thread.currentThread().getId()) {
                    localLocks.remove(toLocalId(category, key));
                    log.trace("Successfully delete lock from cache for: " + lockInfo);
                } else {
                    String msg = "Failed to release lock (inconsistent state) for: {}. Current thread is not the " +
                            "owner of the lock." + lockInfo;
                    StringBuilder messageBuilder = new StringBuilder().append(msg);
                    LockingDebugUtils.debugLocking(messageBuilder);
                    throw new RuntimeException(msg);
                }
            }
            DbUnlockSupplier unlockSupplier = buildUnlockSupplier(category, key, owner);
            boolean dbLockRemoval = unlockInternal(unlockSupplier, category, key, owner, 3);
            if (dbLockRemoval) {
                log.trace("Successfully delete lock from cache for: " + getLockInfo(category, key, owner));
                return true;
            } else {
                log.error("Could not remove {} lock.", getLockInfo(category, key, owner));
            }
        } catch (Exception e) {
            log.error("Failed to release lock for:" + getLockInfo(category, key, owner), e.getMessage());
            log.debug("Failed to release lock for:" + getLockInfo(category, key, owner), e);
            throw new RuntimeException("Failed to release lock for: " + getLockInfo(category, key, owner), e);
        }
        log.debug("Lock is either not exist or not owned by current owner for: " + getLockInfo(category, key));
        return false;
    }

    /**
     * Unlock combination of 'category', 'key' and optinally 'owner' from the DB with retries and short sleep between each retry
     *
     * @param numOfRetries Num of times to re-try in case that the first attempt failed
     * @throws SQLException In
     */
    private boolean unlockInternal(DbUnlockSupplier unlockSupplier, String category, String key, String owner,
            int numOfRetries) throws SQLException {
        // basically, we need retry when catching an exception only, however, although it does not makes sense without DB cluster, we saw once that a lock was exists in Oracle DB, and the unlock operation returned false.
        boolean success = false;
        int sleepTime = 4; // sleep time in ms
        while (numOfRetries >= 0) {
            try {
                success = unlockSupplier.unlock();
            } catch (SQLException e) {
                log.trace("SQL Error occurred while trying to delete lock.", e);
                if (numOfRetries == 0) {
                    throw e;
                }
            }
            if (success) {
                return true;
            }
            log.debug("Failed removing lock for {}", getLockInfo(category, key, owner));
            if (numOfRetries > 0) {
                // sleep, no more than half a second
                sleepTime = Math.min(sleepTime, 256);
                sleep(sleepTime);
                sleepTime = sleepTime * 4;
            }
            numOfRetries--;
        }
        return false;
    }

    @Override
    public int lockingMapSize(String category) {
        log.debug("Querying for category map size for: " + getLockInfo(category, null));
        try {
            int numberOfLocks = dbDistributeLocksDao.lockingMapSize(category);
            log.trace("Found " + numberOfLocks + " locks for:" + getLockInfo(category));
            return numberOfLocks;
        } catch (SQLException e) {
            String message = StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "";
            throw new RuntimeException(
                    "Failed to query out number of locks for: " + getLockInfo(category) + ". " + message);
        }
    }

    @Override
    public Set<String> lockingMapKeySet(String category) {
        log.debug("Querying for category map size for: " + getLockInfo(category));
        try {
            Set<String> set = dbDistributeLocksDao.lockingMapKeySet(category);
            log.trace("Found the following locks " + set + "  for:" + getLockInfo(category));
            return set;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query out locks for:" + getLockInfo(category));
        }
    }

    @Override
    public boolean forceUnlock(String category, String key) {
        log.debug("Releasing lock for: " + getLockInfo(category));
        try {
            LockInfo removed = localLocks.remove(toLocalId(category, key));
            DbUnlockSupplier forceUnlockSupplier = getForceUnlockSupplier(category, key);
            boolean dbLockRemoved = unlockInternal(forceUnlockSupplier, category, key, null, 3);
            return removed != null || dbLockRemoved;
        } catch (SQLException e) {
            throw new RuntimeException("" +
                    "Failed to force lock release for: " + getLockInfo(category, key));
        }
    }

    @Override
    public void cleanOrphanLocks(List<String> serverIdToKeepLocks) {
        if (CollectionUtils.isNullOrEmpty(serverIdToKeepLocks)) {
            throw new IllegalStateException("List of servers to keep locks must not be empty.");
        }
        try {
            Set<LockInfo> orphanLocks = dbDistributeLocksDao.getAllLocksNotOwnedBy(serverIdToKeepLocks);
            log.debug("Attempting to remove {} orphan locks", orphanLocks.size());
            forceRemoveLocks(orphanLocks, false);
        } catch (SQLException e) {
            log.warn("Failed to clean orphan locks. {}", e.getMessage());
            log.debug("Failed to clean orphan locks.", e);
        }
    }

    @Override
    public void init() {
        String serverId = ContextHelper.get().getServerId();
        try {
            int effectedRows = dbDistributeLocksDao.deleteAllOwnerLocks(serverId);
            log.debug("{} rows were removed during initialization.", effectedRows);
        } catch (SQLException e) {
            log.warn("Failed deleting old locks during initialization. {}", e.getMessage());
            log.debug("Failed deleting old locks during initialization.", e);
        }
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {

    }

    @Override
    public void cleanDbExpiredLocks() {
        log.debug("Attempting to release DB expired locks.");
        Set<LockInfo> locksToClean = Sets.newHashSet();
        try {
            long current = System.currentTimeMillis();
            log.trace("Current time is: {}", current);
            long minAllowedTime = getMinAllowedTime(current, hazelcastMaxLockLeaseTime.getLong());
            Set<LockInfo> expiredLocks = dbDistributeLocksDao.getExpiredLocks(minAllowedTime).stream()
                    .filter(filterOutActiveReplicationLocks(current))
                    .collect(Collectors.toSet());
            locksToClean.addAll(expiredLocks);
        } catch (SQLException e) {
            throw new StorageException("Failed to list locks.", e);
        }
        forceRemoveLocks(locksToClean, false);
    }

    @Override
    public void cleanCachedExpiredLocks() {
        log.debug("Attempting to release cached expired locks.");
        long current = System.currentTimeMillis();
        log.trace("Current time is: {}", current);
        long minAllowedTime = getMinAllowedTime(current, hazelcastMaxLockLeaseTime.getLong());
        Set<LockInfo> locks = localLocks.values().stream()
                .filter(Objects::nonNull)
                .filter(lockInfo -> lockInfo.getStartedTime() < minAllowedTime)
                .filter(filterOutActiveReplicationLocks(current))
                .collect(Collectors.toSet());
        if (CollectionUtils.notNullOrEmpty(locks)) {
            log.debug("Cleaning cached '{}' expired locks", locks.size());
            forceRemoveLocks(locks, true);
        }
    }

    @Override
    public void destroy() {
        String owner = ContextHelper.get().getServerId();
        log.debug("Destroying all locks for server: {} ", owner);
        try {
            Set<LockInfo> locks = dbDistributeLocksDao.getAllCurrentServerLocks(owner);
            forceRemoveLocks(locks, false);
        } catch (SQLException e) {
            throw new RuntimeException("" +
                    "Failed to destroy locks for server: " + owner);
        }
    }

    /**
     * Replication locks lease time out is 24x bigger than the regular lock lease time, therefore, we filter out all
     * the replication locks that their expiry is less than 24x then the regular lease time.
     */
    private Predicate<LockInfo> filterOutActiveReplicationLocks(long current) {
        return lockInfo -> {
            if (isReplicationLock(lockInfo)) {
                long minAllowedTimeForReplication = getMinAllowedTime(current, hazelcastMaxLockLeaseTime.getLong() * 24);
                return lockInfo.getStartedTime() < minAllowedTimeForReplication;
            } else {
                return true;
            }
        };
    }

    private boolean isReplicationLock(LockInfo lockInfo) {
        return REPLICATION_LOCK_CATEGORY.equals(lockInfo.getCategory());
    }

    private long getMinAllowedTime(long current, long maxLeaseTimeMinutes) {
        return current - TimeUnit.MINUTES.toMillis(maxLeaseTimeMinutes);
    }

    /**
     * @param locks            List of locks to release, should not be null or empty.
     * @param removeCachedOnly Whether to release the locks from the cache only, or from both DB and cache
     */
    private void forceRemoveLocks(@Nonnull Set<LockInfo> locks, boolean removeCachedOnly) {
        log.debug("Cleaning '{}' expired locks", locks.size());
        for (LockInfo lock : locks) {
            log.debug("Destroying lock for category: {}, key: {} ", lock.getCategory(), lock.getKey());
            localLocks.remove(toLocalId(lock.getCategory(), lock.getKey()));
            if (!removeCachedOnly) {
                try {
                    DbUnlockSupplier unlockSupplier = getForceUnlockSupplier(lock.getCategory(), lock.getKey());
                    unlockInternal(unlockSupplier, lock.getCategory(), lock.getKey(), null, 1);
                } catch (Exception e) {
                    // the lock might have already deleted, so we ignore.
                    log.debug("Failed to destroy lock for category: {}, key: {} . {}", lock.getCategory(),
                            lock.getKey(), e.getMessage());
                    log.trace("Failed to destroy lock.", e);
                }
            }
        }
    }

    private DbUnlockSupplier getForceUnlockSupplier(String category, String key) {
        return () -> dbDistributeLocksDao.releaseForceLock(category, key);
    }

    private DbUnlockSupplier buildUnlockSupplier(String category, String key, String owner) {
        return () -> dbDistributeLocksDao.deleteLock(category, key, owner);
    }

    public String toLocalId(String category, String key) {
        return "category:" + category + ",key:" + key;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {

    }
}
