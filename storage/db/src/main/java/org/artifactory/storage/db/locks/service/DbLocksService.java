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

import org.artifactory.spring.ReloadableBean;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author gidis
 */
public interface DbLocksService extends ReloadableBean {
    String REPLICATION_LOCK_CATEGORY = "replications";

    /**
     * Clean DB locks which are expired
     */
    void cleanDbExpiredLocks();

    /**
     * Clean cached locks which are expired
     */
    void cleanCachedExpiredLocks();

    /**
     * Try to acquire lock on combination of category and key. Throws {@link TimeoutException} if lock could not be
     * acquired within the specified timeout.
     *
     * @param category the lock category
     * @param key      the lock key
     * @param owner    the owner instance name
     * @param timeout  the time to wait for the lock
     * @param timeUnit the time unit of the timeout argument
     * @throws TimeoutException if the waiting time elapsed before acquiring a lock on the key-category combination
     */
    void acquireLock(String category, String key, String owner, long timeout, TimeUnit timeUnit)
            throws TimeoutException;

    /**
     * Check is a combination of category and key is locked
     */
    boolean isLocked(String category, String key);

    /**
     * Check a combination of category and key is locked in the cache by the current thread
     */
    boolean isLockedByMe(String category, String key);

    boolean unlock(String category, String key, String owner);

    int lockingMapSize(String category);

    Set<String> lockingMapKeySet(String category);

    boolean forceUnlock(String category, String key);

    /**
     * Cleans all the locks that are owned by a node that is stale or down
     *
     * @param serverIdToKeepLocks clean all locks are not owned by these server ids
     */
    void cleanOrphanLocks(List<String> serverIdToKeepLocks);
}
