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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * @author gidis
 * This class is simple helper with static methodshind this class
 * The main idea behind this class is to keep the {@link DbLocksServiceImpl} clean as much as posible
 */
class HaDbLockHelper {
    private static final Logger log = LoggerFactory.getLogger(HaDbLockHelper.class);

    static void waitLimitedTime(long timeout, long sleepTime, long waitingTime) throws TimeoutException {
        if ((timeout > 0) && (waitingTime < timeout)) {
            long maxTimeTOWait = timeout - waitingTime;
            long timeToSleep = Math.min(sleepTime, maxTimeTOWait);
            sleep(timeToSleep);
        } else {
            throw new TimeoutException("Couldn't acquire lock for: " + timeout + " milliseconds");
        }
    }

    static void sleep(long timeToSleep) {
        try {
            log.trace("Going to sleep for: " + timeToSleep);
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            log.warn("Interrupted exception occur while waiting to acquire lock.", e);
        }
    }

    static String getLockInfo(String category) {
        return getLockInfo(category, null, null);
    }

    static String getLockInfo(String category, String key) {
        return getLockInfo(category, key, null);
    }

    static String getLockInfo(String category, String key, String owner) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(category)) {
            builder.append("category: '").append(category).append("' ");
        }
        if (StringUtils.isNotBlank(key)) {
            builder.append("key: '").append(key).append("' ");
        }
        if (StringUtils.isNotBlank(owner)) {
            builder.append("owner: '").append(owner).append("' ");
        }
        return builder
                .append("thread id: '")
                .append(Thread.currentThread().getId())
                .append("' thread name: '")
                .append(Thread.currentThread().getName())
                .append("'")
                .toString();
    }
}
