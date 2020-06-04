package org.artifactory.repo.cleanup;

import org.artifactory.repo.virtual.VirtualRepo;

/**
 * @author Yoaz Menda
 */
public interface VirtualCacheCleaner {

    /**
     * performs a cleanup of the virtual cache of the given virtual repo
     *
     * @param virtualRepo - virtual repo key
     * @return - number of deleted cached files
     */
    long cleanCache(VirtualRepo virtualRepo);

    /**
     * determines whether or not this cleaner should run for the given repo
     * @param virtualRepo - virtual repo to check
     */
    boolean shouldRun(VirtualRepo virtualRepo);
}
