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

package org.artifactory.config;

import org.apache.commons.lang3.StringUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.jfrog.common.config.diff.DiffUtils;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

/**
 * @author Noam Shemesh
 */
@Test
public class CentralConfigKeyTest {

    private String fieldToGetter(Class<?> curr, String name) {
        return Stream.of(curr.getMethods()).map(Method::getName).filter(method -> method.equals("get" + StringUtils.capitalize(name)) ||
                method.equals("is" + StringUtils.capitalize(name))).findFirst().orElse("noMethod");
    }

    public void testConfigurationEnumMatchConfigurationClass() {
        List<CentralConfigKey> keysNotSupported = Arrays.stream(CentralConfigKey.values())
                .filter(key -> !CentralConfigKey.none.equals(key))
                .filter(key -> {
                    String[] paths = key.getKey().split(Pattern.quote(DiffUtils.DELIMITER));
                    Class<?> curr = CentralConfigDescriptor.class;
                    int i = 0;
                    while (i < paths.length) {
                        try {
                            curr = curr.getMethod(fieldToGetter(curr, paths[i])).getReturnType();
                            i++;
                        } catch (NoSuchMethodException e) {
                            return true;
                        }
                    }

                    return false;

                })
                .collect(Collectors.toList());

        assertEquals(keysNotSupported.size(), 0, "There are keys from enum that are missing from CentralConfigDescriptor class. " +
                keysNotSupported.toString());

    }
}