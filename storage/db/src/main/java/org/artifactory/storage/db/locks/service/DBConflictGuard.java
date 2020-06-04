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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author gidis
 */
public class DBConflictGuard implements ConflictGuard {
    private static final Logger log = LoggerFactory.getLogger(DBConflictGuard.class);

    private DbLocksService dbLocksService;
    private String key;
    private String category;
    private String serverId;

    public DBConflictGuard(DbLocksService dbLocksService, String key, String category, String serverId) {
        this.dbLocksService = dbLocksService;
        this.key = key;
        this.category = category;
        this.serverId = serverId;
    }

    @Override
    public boolean tryToLock(long timeout, TimeUnit timeUnit) throws InterruptedException {
        try {
            dbLocksService.acquireLock(category, key, serverId, timeout, timeUnit);
            return true;
        } catch (TimeoutException e) {
            if (timeout > 0) {
                log.warn("Timed out while trying to acquire lock: {}:{}. {}", category, key, e.getMessage());
            }
            log.debug("Timed out while trying to acquire lock", e);
            return false;
        }
    }

    @Override
    public void unlock() {
        dbLocksService.unlock(category, key, serverId);
    }

    @Override
    public void forceUnlock() {
        dbLocksService.unlock(category, key, serverId);
    }

    @Override
    public boolean isLocked() {
        return dbLocksService.isLocked(category, key);
    }

}
