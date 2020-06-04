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

package org.artifactory.storage.db.locks.provider;

import com.google.common.collect.Lists;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.locks.service.DbLocksService;
import org.artifactory.storage.fs.lock.provider.LockWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author mamo
 */
public class DbMapLockWrapper implements LockWrapper {
    private static final Logger log = LoggerFactory.getLogger(DbMapLockWrapper.class);

    private final String category;
    private final String key;
    private final DbLocksService locksService;
    private String serverId;

    public DbMapLockWrapper(RepoPath repoPath, DbLocksService locksService, String nodeId) {
        this.locksService = locksService;
        this.serverId = nodeId;
        category = repoPath.getRepoKey();
        key = repoPath.getPath();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        log.trace("before tryLock {}/{}", category, key);
        boolean success;
        try {
            locksService.acquireLock(category, key, serverId, timeout,unit);
            success = true;
        } catch (TimeoutException e) {
            log.warn("Could not acquire lock within {} seconds. {}", timeout, e.getMessage());
            success = false;
        }
        log.trace("after tryLock {}/{}: {}", category, key, success);
        return success;
    }

    @Override
    public void unlock() {
        log.trace("unlock {}/{}", category, key);
        locksService.unlock(category, key, serverId);
    }

    @Override
    public boolean isLocked() {
        try {
            return locksService.isLocked(category, key);
        } catch (Exception e) {
            log.error("Exception checking for lock on object: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return locksService.isLockedByMe(category, key);
    }

    @Override
    public Collection<Thread> getQueuedThreads() {
        return Lists.newArrayList();
    }

    @Override
    public void destroy() {
        //noop, auto gc'ed by hz
    }

    @Override
    public void forceUnlock() {
        locksService.forceUnlock(category,key);
    }
}
