package org.artifactory.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.GroupFilter;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 */
public class FilterBasedGroupsCacheImpl implements FilterBasedGroupsCache {
    private static final Logger log = LoggerFactory.getLogger(FilterBasedGroupsCacheImpl.class);

    private UserGroupStoreService userGroupStoreService;

    private LoadingCache<GroupFilter, Map<String, GroupInfo>> cache;

    FilterBasedGroupsCacheImpl(UserGroupStoreService userGroupStoreService) {
        this.userGroupStoreService = userGroupStoreService;
        initCacheLoader();
    }

    void initCacheLoader() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(GroupFilter.values().length)
                .expireAfterWrite(ConstantValues.groupsCacheRetentionSecs.getLong(), TimeUnit.SECONDS)
                .build(new CacheLoader<GroupFilter, Map<String, GroupInfo>>() {
                    @Override
                    public Map<String, GroupInfo> load(@Nonnull GroupFilter key) {
                        log.debug("Reloading groups cache for with key '{}'", key);
                        return userGroupStoreService.getAllGroups().stream()
                                .filter(key.filterFunction)
                                .collect(Collectors.toMap(GroupInfo::getGroupName, Function.identity()));
                    }
                });
    }

    @Override
    public Map<String, GroupInfo> getGroupsByFilter(GroupFilter filter) {
        try {
            return cache.get(filter);
        } catch (ExecutionException e) {
            log.error("Unable to retrieve cached groups value by filter: {}", e);
            throw new IllegalStateException("Unable to retrieve external groups", e);
        }
    }

    @Override
    public void invalidate() {
        cache.invalidateAll();
    }
}
