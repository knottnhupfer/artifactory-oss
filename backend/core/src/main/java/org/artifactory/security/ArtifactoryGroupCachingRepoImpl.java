package org.artifactory.security;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.event.CacheType;
import org.artifactory.event.InvalidateCacheEvent;
import org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.GroupFilter;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.jfrog.common.LazyCache;
import org.jfrog.common.LazyCacheImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Uriah Levy
 */
@Component
public class ArtifactoryGroupCachingRepoImpl implements ArtifactoryGroupCachingRepo {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryGroupCachingRepoImpl.class);

    private final UserGroupStoreService userGroupStoreService;
    private final FilterBasedGroupsCache filterBasedGroupsCache;
    private AddonsManager addonsManager;
    private final LazyCache<String, GroupInfo> nameToInfoCache;

    @Autowired
    ArtifactoryGroupCachingRepoImpl(UserGroupStoreService userGroupStoreService, AddonsManager addonsManager) {
        this.userGroupStoreService = userGroupStoreService;
        this.filterBasedGroupsCache = new FilterBasedGroupsCacheImpl(userGroupStoreService);
        this.addonsManager = addonsManager;
        nameToInfoCache = new LazyCacheImpl<>(this::fetchGroupInfoByName, this::fetchGroupInfos,
                this::fetchAllGroups);
    }

    @Override
    public Map<String, GroupInfo> fetchAllGroups() {
        log.debug("Fetching all groups");
        return filterBasedGroupsCache.getGroupsByFilter(GroupFilter.ALL);
    }

    @Override
    public GroupInfo getGroupInfoByName(String groupName) {
        return nameToInfoCache.value(groupName).orElse(null);
    }

    @Override
    public Map<String, GroupInfo> getGroupsByFilter(GroupFilter groupFilter) {
        return filterBasedGroupsCache.getGroupsByFilter(groupFilter);
    }

    @Nonnull
    @Override
    public Map<String, Optional<GroupInfo>> getGroupInfosByNames(List<String> groupNames) {
        return nameToInfoCache.getValues(groupNames);
    }

    @Override
    public void invalidateGroupsCache(String groupName) {
        if (StringUtils.isNotBlank(groupName)) {
            nameToInfoCache.invalidate(groupName);
        }
        filterBasedGroupsCache.invalidate();
    }

    @Override
    public void onGroupAdd(String groupName, String realm) {
        filterBasedGroupsCache.invalidate();
        addonsManager.addonByType(HaAddon.class).propagateGroupCacheInvalidation(groupName);
    }

    @Override
    public void onGroupUpdate(String groupName, String realm) {
        invalidateByNameAndPropagate(groupName);
    }

    @Override
    public void onGroupDelete(String groupName) {
        invalidateByNameAndPropagate(groupName);
    }

    @Override
    public void onApplicationEvent(InvalidateCacheEvent event) {
        if (CacheType.GROUPS.equals(event.getCacheType())) {
            invalidateNameToInfoCache();
            invalidateFilterBasedCache();
            addonsManager.addonByType(HaAddon.class).propagateGroupCacheInvalidation(null);
        }
    }

    private void invalidateByNameAndPropagate(String groupName) {
        nameToInfoCache.invalidate(groupName);
        filterBasedGroupsCache.invalidate();
        addonsManager.addonByType(HaAddon.class).propagateGroupCacheInvalidation(groupName);
    }

    private Map<String, GroupInfo> fetchGroupInfos(List<String> groupNames) {
        return userGroupStoreService.getAllGroupsByNames(groupNames);
    }

    private GroupInfo fetchGroupInfoByName(String groupName) {
        return userGroupStoreService.findGroup(groupName);
    }

    private void invalidateNameToInfoCache() {
        nameToInfoCache.invalidateAll();
    }

    private void invalidateFilterBasedCache() {
        filterBasedGroupsCache.invalidate();
    }
}