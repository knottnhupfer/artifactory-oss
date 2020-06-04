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

package org.artifactory.api.bintray.distribution.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Yinon Avraham
 */
class CaptureGroupValues {

    private final List<String> values = Lists.newArrayList();
    private final Map<String, String> namedValues = Maps.newHashMap();

    public void addGroupValue(String value) {
        values.add(value);
    }

    public void addGroupValue(String groupName, String value) {
        namedValues.put(groupName, value);
    }

    public String getByName(String groupName) throws NoSuchElementException {
        if (namedValues.containsKey(groupName)) {
            return namedValues.get(groupName);
        }
        throw new NoSuchElementException("No such named group: " + groupName);
    }

    public String getByNumber(int groupNumber) throws NoSuchElementException {
        int groupIndex = groupNumber - 1;
        if (0 <= groupIndex && groupIndex < values.size()) {
            return values.get(groupIndex);
        }
        throw new NoSuchElementException("No such group number: " + groupNumber);
    }

    public boolean isEmpty() {
        return values.isEmpty() && namedValues.isEmpty();
    }

}
