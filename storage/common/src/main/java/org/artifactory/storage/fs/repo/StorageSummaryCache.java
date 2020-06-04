package org.artifactory.storage.fs.repo;

import org.artifactory.storage.StorageSummaryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for storage summary.
 * Contains the number of files, folders and overall size per repository and the time the cache was last updated, binaries info, etc.
 *
 * @author Inbar Tal
 */
public class StorageSummaryCache {
    private static final Logger log = LoggerFactory.getLogger(StorageSummaryCache.class);

    private StorageSummaryInfo cache;

    public StorageSummaryInfo get() {
        if (cache == null) {
            throw new CacheUnAvailableException();
        }
        log.debug("Storage summary cache hit.");
        return cache;
    }

    public void load(StorageSummaryInfo updatedCache) {
        cache = updatedCache;
        if (log.isDebugEnabled()) {
            log.debug("Storage summary cache was updated successfully");
            log.debug("Current cache : {}", cache);
        }
    }
}
