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

package org.artifactory.storage.db.security.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Gidi Shabat
 */
public class VersioningCacheImpl<T extends BasicCacheModel> implements VersionCache<T> {
    private static final Logger log = LoggerFactory.getLogger(VersioningCacheImpl.class);

    private final ReentrantLock cacheLock = new ReentrantLock();
    private final long timeout;
    private final Callable<T> cacheLoader;
    // promoted on each DB change (permission change/add/delete)2
    private final AtomicInteger dbVersion = new AtomicInteger(1);
    private volatile int version = 0; // promoted each time we load the cache from DB
    protected volatile T cache;

    public VersioningCacheImpl(long timeout, Callable<T> cacheLoader) {
        this.timeout = timeout;
        this.cacheLoader = cacheLoader;
    }

    /**
     * Call this method each permission update/change/delete in DB.
     */
    public int promoteVersion() {
        return dbVersion.incrementAndGet();
    }

    /**
     * Returns cache.
     */
    public T get() {
        T currentCache = cache;
        if (hasNewVersion()) {
            // Need to update cache (new version in dbVersion).
            // Try to acquire lock
            log.debug("Attempting to acquire a lock on cacheLock");
            boolean lockAcquired = tryToWaitForLock();
            if (!lockAcquired) {
                // Timeout occurred : Return the current cache without waiting to thew new cache which is being reloaded.
                log.debug("cache lock timeout occurred returning current cache instead the one that is being loaded");
                return cache;
            } else {
                try {
                    log.debug("cacheLock lock has been acquired");
                    currentCache = cache;
                    //print only if debug is enabled to avoid performances degradation on large cache
                    if (log.isDebugEnabled()) {
                        log.debug("Current cache : {}", currentCache);
                    }
                    // Double check after cacheLoader synchronization.
                    if (hasNewVersion()) {
                        log.debug("DbVersion '{}' is higher than version: {}", dbVersion.get(), version);
                        // The map will be valid for version the current sDbVersion.
                        int startingVersion = dbVersion.get();
                        try {
                            currentCache = cacheLoader.call();
                        } catch (Exception e) {
                            String errorMessage = StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "";
                            throw new VersioningCacheException("Fail to reload cache: " + errorMessage, e);
                        }
                        T oldCache = cache;
                        cache = currentCache;
                        if (oldCache != null) {
                            oldCache.destroy();
                        }
                        //print only if debug is enabled to avoid performances degradation on large cache
                        if (log.isDebugEnabled()) {
                            log.debug("current cache has been updated with: {}", currentCache);
                        }
                        currentCache.setVersion(startingVersion);
                        version = startingVersion;
                    } else {
                        log.debug("Skipping cache update, newer version exist: dbVersion is: '{}'" +
                                " while version version is: {}", dbVersion.get(), version);
                    }
                } finally {
                    cacheLock.unlock();
                }
            }
        }
        return currentCache;
    }

    @Override
    public void destroy() {

    }

    boolean hasNewVersion() {
        return dbVersion.get() > version;
    }

    private boolean tryToWaitForLock() {
        boolean acquireLock = false;
        try {
            acquireLock = cacheLock.tryLock(timeout, TimeUnit.MILLISECONDS);
            if (!acquireLock && cache == null) {
                log.debug("Blocking thread while cache is being processed for the first time");
                acquireLock = tryToWaitForLock();
            }
        } catch (InterruptedException e) {
            if (cache == null) {
                log.debug("Blocking thread while cache is being processed for the first time");
                acquireLock = tryToWaitForLock();
            }
        }
        return acquireLock;
    }
}