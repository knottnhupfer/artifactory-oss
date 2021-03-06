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

package org.artifactory.storage.fs.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Debug logger for locking issues.
 *
 * @author Yossi Shaul
 */
public class LockingDebugUtils {

    private LockingDebugUtils() {
    }

    private static final Logger log = LoggerFactory.getLogger(LockingDebugUtils.class);

    public static synchronized void debugLocking(LockEntryId entry, StringBuilder message) {
        message.append("\nCurrent thread: ").append(Thread.currentThread().getName());
        Collection<Thread> queuedWriters = entry.getLock().getQueuedThreads();
        message.append("\nQueued writers: ");
        for (Thread queuedWriter : queuedWriters) {
            message.append(queuedWriter.getName()).append(' ');
        }
        ThreadDumpUtils.builder()
                .count(1)
                .build()
                .dumpThreads(message);
        log.trace(message.toString());
    }

    public static synchronized void debugLocking(StringBuilder message) {
        message.append("\nCurrent thread: ").append(Thread.currentThread().getName());
        ThreadDumpUtils.builder()
                .count(1)
                .build()
                .dumpThreads(message);
        log.warn(message.toString());
    }
}
