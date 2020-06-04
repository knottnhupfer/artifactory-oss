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

package org.artifactory.security.props.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Yinon Avraham
 */
public class SimpleCacheWrapper<K, V> implements CacheWrapper<K, V> {

    private final Cache<K, V> cache;

    public SimpleCacheWrapper(CacheConfig cacheConfig) {
        cache = buildCache(cacheConfig);
    }

    private static <K, V> Cache<K, V> buildCache(CacheConfig cacheConfig) {
        CacheBuilder<Object, Object> builder = CacheBuilder.<K, V>newBuilder();
        if (cacheConfig.hasExpiration()) {
            builder.expireAfterWrite(cacheConfig.getExpirationDuration(), cacheConfig.getExpirationTimeUnit());
        }
        return builder.build();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void invalidate(K key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public void invalidateAll(Iterable<K> keys) {
        cache.invalidateAll(keys);
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return cache.asMap();
    }
}
