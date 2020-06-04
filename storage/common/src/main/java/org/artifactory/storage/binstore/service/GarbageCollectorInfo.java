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

package org.artifactory.storage.binstore.service;

import org.jfrog.common.TimeUnitFormat;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.artifactory.util.NumberFormatter.formatLong;

/**
 * Holds the garbage collection information
 *
 * @author Noam Y. Tenne
 */
public class GarbageCollectorInfo {
    private static final Logger log = LoggerFactory.getLogger(GarbageCollectorInfo.class);

    private final long gcStartTime;
    public long gcEndTime;
    public long stopScanTimestamp;
    public int candidatesForDeletion;
    public long initialSize;
    public long initialCount;
    public AtomicInteger checksumsCleaned;// checksum entries cleaned from the binaries table
    public AtomicInteger binariesCleaned; // either files from the filestore or blobs from the database (usually same as checksumsCleaned)
    public AtomicLong totalSizeCleaned;
    public AtomicInteger archivePathsCleaned; // the amount of unique archive paths cleaned
    public AtomicInteger archiveNamesCleaned; // the amount of unique archive names cleaned

    public GarbageCollectorInfo() {
        gcStartTime = System.currentTimeMillis();
        checksumsCleaned = new AtomicInteger(0);
        binariesCleaned = new AtomicInteger(0);
        totalSizeCleaned = new AtomicLong(0);
        archivePathsCleaned = new AtomicInteger(0);
        archiveNamesCleaned = new AtomicInteger(0);
    }

    /**
     * Prints a summary of the collected info to the log
     *
     * @param dataStoreSize The measured size of the datastore
     */
    public void printCollectionInfo(long dataStoreSize) {
        String duration = TimeUnitFormat.getTimeString((gcEndTime - gcStartTime), TimeUnit.MILLISECONDS);
        StringBuilder msg = new StringBuilder("Storage garbage collector report:\n");
        if (initialCount >= 0) {
            msg.append("Number of binaries:      ").append(formatLong(initialCount)).append("\n");
        }
        msg.append("Total execution time:    ").append(duration).append("\n").append(
                "Candidates for deletion: ").append(formatLong(candidatesForDeletion)).append("\n").append(
                "Checksums deleted:       ").append(formatLong(checksumsCleaned.get())).append("\n").append(
                "Binaries deleted:        ").append(formatLong(binariesCleaned.get())).append("\n").append(
                "Total size freed:        ").append(StorageUnit.toReadableString(totalSizeCleaned.get()));

        if (log.isDebugEnabled()) {
            msg.append("\n").append("Unique paths deleted:    ").append(formatLong(archivePathsCleaned.get()));
            msg.append("\n").append("Unique names deleted:    ").append(formatLong(archiveNamesCleaned.get()));
        }

        if (dataStoreSize >= 0) {
            msg.append("\n").append("Current total size:      ").append(StorageUnit.toReadableString(dataStoreSize));
        }

        log.info(msg.toString());
    }
}