package org.artifactory.security;

import org.artifactory.event.InvalidateCacheEvent;
import org.artifactory.security.interceptor.SecurityConfigurationChangesInterceptor;
import org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.GroupFilter;
import org.jfrog.common.BiDirectionalLazyCache;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Uriah Levy
 * A group repo that caches groups. This repo uses two different types of cache:
 * 1. {@link BiDirectionalLazyCache} - a name -> {@link GroupInfo} cache for lookup of individual groups
 * 2. {@link FilterBasedGroupsCache} - a {@link GroupFilter} -> {@link GroupInfo} cache for lookup of a group of groups
 * (Admin groups, external groups, all groups, etc)
 */
public interface ArtifactoryGroupCachingRepo
        extends SecurityConfigurationChangesInterceptor, ApplicationListener<InvalidateCacheEvent> {
    /**
     * Fetch all groups. Uses a {@link FilterBasedGroupsCache}
     *
     * @return - all the groups
     */
    Map<String, GroupInfo> fetchAllGroups();

    /**
     * Fetch all the groups that correspond to a given filter. Uses a {@link FilterBasedGroupsCache}.
     *
     * @param groupFilter - a groups filter (external, admins, all, etc)
     * @return - all groups that correspond to the given filter
     */
    Map<String, GroupInfo> getGroupsByFilter(GroupFilter groupFilter);

    /**
     * Fetch multiple groups by a given list of group names. Uses a {@link BiDirectionalLazyCache}
     *
     * @param groupNames - a list of group names to fetch
     * @return - a map of group names to group information
     */
    Map<String, Optional<GroupInfo>> getGroupInfosByNames(List<String> groupNames);

    /**
     * Fetch a single group by name. Uses a {@link BiDirectionalLazyCache}
     *
     * @param groupName - the name of the group to be fetched
     * @return - group information
     */
    GroupInfo getGroupInfoByName(String groupName);

    /**
     * Invalidate the groups cache. Name-based invalidation clears the {@link FilterBasedGroupsCache} and
     * the {@link BiDirectionalLazyCache}. When a name is not provided, only the filter-based cache is invalidated
     *
     * @param groupName - an optional group name to invalidate
     */
    void invalidateGroupsCache(String groupName);
}