package org.artifactory.security;

import org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService;
import org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.GroupFilter;

import java.util.Map;

/**
 * @author Uriah Levy
 * A filter-based groups cache. This cache is in line with the storage layer ({@link AccessUserGroupStoreService}), that
 * queries Access for Groups based on the various {@link GroupFilter}'s
 */
public interface FilterBasedGroupsCache {

    /**
     * Get the cached value for the given {@link GroupFilter}
     * @param filter the filter
     * @return the cached value
     */
    Map<String, GroupInfo> getGroupsByFilter(GroupFilter filter);

    void invalidate();
}