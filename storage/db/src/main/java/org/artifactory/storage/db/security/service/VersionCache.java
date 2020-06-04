package org.artifactory.storage.db.security.service;

/**
 * @author Shay Bagants
 */
public interface VersionCache<T extends BasicCacheModel> {

    /**
     * @return cached instance model
     */
    T get();

    /**
     * Increment the required version. This creates marker for the cache loading mechanism to update it's cache
     * on demand
     */
    int promoteVersion();

    /**
     * Destroy the cache
     */
    void destroy();
}
