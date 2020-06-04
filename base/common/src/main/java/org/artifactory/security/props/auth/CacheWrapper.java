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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Yinon Avraham
 */
public interface CacheWrapper<K, V> {

    void put(K key, V value);

    V get(K key);

    void invalidate(K key);

    void invalidateAll();

    void invalidateAll(Iterable<K> keys);

    ConcurrentMap<K, V> asMap();

    class CacheConfig {
        private final Long duration;
        private final TimeUnit timeUnit;

        private CacheConfig(Long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = timeUnit;
        }

        static CacheConfigBuilder newConfig() {
            return new CacheConfigBuilder();
        }

        public long getExpirationDuration() {
            assert hasExpiration();
            return duration;
        }

        public TimeUnit getExpirationTimeUnit() {
            assert hasExpiration();
            return timeUnit;
        }

        public boolean hasExpiration() {
            return duration != null && timeUnit != null;
        }
    }

    class CacheConfigBuilder {
        private Long duration = null;
        private TimeUnit timeUnit = null;

        public CacheConfigBuilder expireAfterWrite(long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = timeUnit;
            return this;
        }

        public CacheConfigBuilder noExpiration() {
            this.duration = null;
            this.timeUnit = null;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(duration, timeUnit);
        }
    }
}
