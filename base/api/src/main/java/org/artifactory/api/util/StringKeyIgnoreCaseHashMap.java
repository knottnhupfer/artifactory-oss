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

package org.artifactory.api.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yinon Avraham
 */
public class StringKeyIgnoreCaseHashMap<V> implements Map<String, V> {

    private final Map<String, V> map = Maps.newHashMap();
    private final Map<String, String> originalKeys = Maps.newHashMap();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(normalizeKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(normalizeKey(key));
    }

    @Override
    public V put(String key, V value) {
        String normalizedKey = normalizeKey(key);
        originalKeys.put(normalizedKey, key);
        return map.put(normalizedKey, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(normalizeKey(key));
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        m.entrySet().forEach(entry -> put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return Sets.newHashSet(originalKeys.values());
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return map.entrySet().stream()
                .map(entry -> new MapEntry<>(entry, originalKeys.get(entry.getKey())))
                .collect(Collectors.toSet());
    }

    private String normalizeKey(Object key) {
        return key == null ? null : String.valueOf(key).toLowerCase();
    }

    private static class MapEntry<V> implements Map.Entry<String, V> {

        private final Map.Entry<String, V> entry;
        private final String originalKey;

        private MapEntry(Map.Entry<String, V> entry, String originalKey) {
            this.entry = entry;
            this.originalKey = originalKey;
        }

        @Override
        public String getKey() {
            return originalKey;
        }

        @Override
        public V getValue() {
            return entry.getValue();
        }

        @Override
        public V setValue(V value) {
            return entry.setValue(value);
        }
    }
}
