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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.security.service.UserLockInMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Noam Shemesh
 */
@Component
public class UserLockInMemoryServiceImpl implements UserLockInMemoryService {
    private static final Logger log = LoggerFactory.getLogger(UserLockInMemoryServiceImpl.class);

    // cache meaning  <username, lock-time>
    private final Cache<String, Long> userAccessUsersCache = CacheBuilder.newBuilder()
            .maximumSize(ConstantValues.maxUsersToTrack.getInt()).
                    expireAfterWrite(24, TimeUnit.HOURS).build();

    private final Cache<String, Long> lockedUsersCache = CacheBuilder.newBuilder()
            .maximumSize(ConstantValues.maxUsersToTrack.getInt()).
                    expireAfterWrite(24, TimeUnit.HOURS).build();

    private final Map<String, AtomicInteger> incorrectLoginAttemptsCache = Maps.newConcurrentMap();

    private final long LOGIN_BASE_DELAY = ConstantValues.loginBlockDelay.getLong();
    private final long MAX_BLOCKED_DELAY = ConstantValues.loginMaxBlockDelay.getLong();
    private final long MAX_FAILED_ATTEMPTS_FOR_CALCULATION = (LOGIN_BASE_DELAY == 0 ? Integer.MAX_VALUE :
            64 - Long.numberOfLeadingZeros(MAX_BLOCKED_DELAY / LOGIN_BASE_DELAY) - 1); // ceil log_2 of max wait units

    /**
     * Calculates user login delay multiplier,
     * the value (security.loginBlockDelay) is
     * taken from system properties file,
     * <p/>
     * delay may not exceed
     *
     * @return user login delay multiplier
     */
    private long getLoginDelay(int numberOffAttempts) {
        int numberOfAttemptsAllowed = ConstantValues.maxIncorrectLoginAttempts.getInt();
        int punishedAttempts = numberOffAttempts - numberOfAttemptsAllowed;
        return calculateBackoff(punishedAttempts);
    }

    private long calculateBackoff(int attempts) {
        return (attempts > MAX_FAILED_ATTEMPTS_FOR_CALCULATION) ?
                MAX_BLOCKED_DELAY :
                LOGIN_BASE_DELAY << attempts;
    }

    @Override
    public void updateUserAccess(String loginIdentifier, boolean userLockPolicyEnabled, long accessTimeMillis) {
        if (StringUtils.isNotBlank(loginIdentifier) && !userLockPolicyEnabled && !UserInfo.ANONYMOUS.equals(loginIdentifier)) {
            if (!isUserLocked(loginIdentifier)) {
                userAccessUsersCache.put(loginIdentifier, accessTimeMillis);
            }
        }
    }

    @Override
    public void lockUser(@Nonnull String loginIdentifier) {
        try {
            synchronized (lockedUsersCache) {
                registerLockedOutUser(loginIdentifier);
            }
        } catch (Exception e) {
            log.debug("Could not lock user, cause: {}", e);
            throw new StorageException("Could not lock user, reason: " + e.getMessage());
        }
    }

    @Override
    public boolean isUserLocked(String username) {
        if (shouldCacheLockedUsers()) {
            return lockedUsersCache.getIfPresent(username) != null;
        }

        return false;
    }

    @Override
    public long getNextLoginTime(String loginIdentifier) {
        Long lastAccessTime = userAccessUsersCache.getIfPresent(loginIdentifier);
        if (lastAccessTime != null) {
            return getNextLoginTime(
                    getIncorrectLoginAttempts(loginIdentifier),
                    lastAccessTime
            );
        }
        return -1;
    }

    @Override
    public long getNextLoginTime(int incorrectLoginAttempts, long lastAccessTimeMillis) {
        int maxIncorrectLoginAttempts = ConstantValues.maxIncorrectLoginAttempts.getInt();
        int maxLoginDelay = ConstantValues.loginMaxBlockDelay.getInt();

        if (incorrectLoginAttempts >= maxIncorrectLoginAttempts) {
            long delay = getLoginDelay(incorrectLoginAttempts);
            log.debug("Delay user login in {} millis after {} incorrect attempts", delay, incorrectLoginAttempts);
            if (delay != 0) {
                return lastAccessTimeMillis + (delay <= maxLoginDelay ? delay : maxLoginDelay);
            }
        }
        return -1;
    }

    @Override
    public void unlockUser(@Nonnull String username) {
        try {
            synchronized (lockedUsersCache) {
                unregisterLockedOutUser(username);
            }
        } catch (Exception e) {
            log.debug("Could not unlock user: '{}', cause: {}", username, e);
            throw new StorageException(
                    String.format("Could not unlock user: '%s', reason: %s", username, e.getMessage()));
        }
    }

    @Override
    public void unlockAllUsers() {
        lockedUsersCache.invalidateAll();
    }

    //TODO: [by YS] consider moving this to Access client

    /**
     * Registers locked out user in cache
     */
    private void registerLockedOutUser(String username) {
        if (shouldCacheLockedUsers()) {
            lockedUsersCache.put(username, System.currentTimeMillis());
        }
    }

    /**
     * @return whether locked out users should be cached
     */
    private boolean shouldCacheLockedUsers() {
        return ConstantValues.useFrontCacheForBlockedUsers.getBoolean();
    }

    /**
     * Unregisters locked out user/s from cache
     *
     * @param user a user name to unlock or all users via ALL_USERS
     *             {@see UserGroupServiceImpl.ALL_USERS}
     */
    private void unregisterLockedOutUser(String user) {
        if (shouldCacheLockedUsers()) {
            lockedUsersCache.invalidate(user);
        }
    }

    @Override
    public int getIncorrectLoginAttempts(@Nonnull String loginIdentifier) {
        AtomicInteger incorrectLoginAttempts = incorrectLoginAttemptsCache.get(loginIdentifier);
        return incorrectLoginAttempts != null ? incorrectLoginAttempts.get() : 0;
    }

    @Override
    public void registerIncorrectLoginAttempt(@Nonnull String loginIdentifier) {
        AtomicInteger atomicInteger = incorrectLoginAttemptsCache.get(loginIdentifier);
        if (atomicInteger == null) {
            synchronized (incorrectLoginAttemptsCache) {
                atomicInteger = incorrectLoginAttemptsCache.get(loginIdentifier);
                if (atomicInteger == null) {
                    atomicInteger = new AtomicInteger(0);
                    incorrectLoginAttemptsCache.put(loginIdentifier, atomicInteger);
                }
            }
        }
        int incorrectLoginAttempt = atomicInteger.incrementAndGet();
        log.debug("Increased IncorrectLoginAttempts to '{}'", incorrectLoginAttempt);
    }

    @Override
    public void resetIncorrectLoginAttempts(@Nonnull String loginIdentifier) {
        if (incorrectLoginAttemptsCache.containsKey(loginIdentifier)) {
            synchronized (incorrectLoginAttemptsCache) {
                incorrectLoginAttemptsCache.remove(loginIdentifier);
            }
            log.debug("Reset IncorrectLoginAttempts");
        }
    }
}
