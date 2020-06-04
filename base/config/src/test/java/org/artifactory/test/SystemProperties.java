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

package org.artifactory.test;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Yinon Avraham.
 */
public class SystemProperties {

    private final Map<String, String> originalSystemProps = Maps.newHashMap();

    public String get(String key) {
        return System.getProperty(key);
    }

    public String clear(String key) {
        return set(key, null);
    }

    public String set(String key, String value) {
        if (!originalSystemProps.containsKey(key)) {
            originalSystemProps.put(key, System.getProperty(key));
        }
        if (value != null) {
            return System.setProperty(key, value);
        } else {
            return System.clearProperty(key);
        }
    }

    public void init(String key) {
        set(key, System.getProperty(key));
    }

    public void restoreOriginals() {
        originalSystemProps.forEach((key, value) -> {
            if (value != null) {
                System.setProperty(key, value);
            } else {
                System.clearProperty(key);
            }
        });
        originalSystemProps.clear();
    }
}
