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

package org.artifactory.repo.http;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

/**
 * Thread safe class that holds connections managers with weak reference.
 *
 * @author Gal Ben Ami
 */
@Component
public class GuaveCacheConnectionManagersHolder implements ConnectionManagersHolder {

    private final Cache<String, PoolingHttpClientConnectionManager> connections = CacheBuilder
            .newBuilder()
            .weakKeys()
            .build();


    @Override
    public long size() {
        return connections.size();
    }

    @Override
    public void put(String key, PoolingHttpClientConnectionManager value) {
        connections.put(key, value);
    }

    @Override
    public void remove(String key) {
        connections.invalidate(key);
    }

    @Override
    public Iterable<PoolingHttpClientConnectionManager> values() {
        return connections.asMap().values();
    }

    public PoolingHttpClientConnectionManager get(String key) {
        return connections.getIfPresent(key);
    }
}
