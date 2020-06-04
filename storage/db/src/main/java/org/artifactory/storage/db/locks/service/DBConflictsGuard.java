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

import org.jfrog.storage.common.ConflictGuard;
import org.jfrog.storage.common.ConflictsGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author gidis
 */
public class DBConflictsGuard implements ConflictsGuard<String> {
    private static final Logger log = LoggerFactory.getLogger(DBConflictsGuard.class);

    private DbLocksService dbLocksService;
    private String category;
    private String currentMemberServerId;

    public DBConflictsGuard(DbLocksService dbLocksService, String category, String currentMemberServerId) {
        this.dbLocksService = dbLocksService;
        this.category = category;
        this.currentMemberServerId = currentMemberServerId;
    }

    @Override
    public void unlock(String key) {
        dbLocksService.unlock(category,key,currentMemberServerId);
    }

    @Override
    public void forceUnlock(String key) {
        dbLocksService.forceUnlock(category,key);
    }

    @Override
    public boolean tryToLock(String key, long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        try {
            dbLocksService.acquireLock(category, key, currentMemberServerId, timeout, timeoutUnit);
            return true;
        } catch (TimeoutException e) {
            log.debug("Timed out while trying to acquire lock: {}:{}. {}", category, key, e.getMessage());
            log.trace("Timed out while trying to acquire lock.", e);
            return false;
        }
    }

    @Override
    public int size() {
        return dbLocksService.lockingMapSize(category);
    }

    @Override
    public boolean isLocked(String key) {
        return dbLocksService.isLocked(category,key);
    }

    @Override
    public Set<String> keySet() {
        return dbLocksService.lockingMapKeySet(category);
    }

    @Override
    public ConflictGuard getLock(String key) {
        return new DBConflictGuard(dbLocksService,key,category,currentMemberServerId);
    }
}
