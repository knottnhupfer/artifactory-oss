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

package org.artifactory.rest.common.list;

import com.google.common.collect.Lists;
import org.artifactory.descriptor.property.Property;
import org.artifactory.util.KeyValueList;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Shay Yaakov
 */
public class KeyValueListTest {

    @Test
    public void testToStringMap() throws Exception {
        Map<String, List<String>> map = new KeyValueList("a=1,2,3|b\\=1=1\\=1,2\\,3|c=\\;|d=").toStringMap();
        assertEquals(map.get("a"), Lists.newArrayList("1", "2", "3"));
        assertEquals(map.get("b=1"), Lists.newArrayList("1=1", "2,3"));
        assertEquals(map.get("c"), Lists.newArrayList(";"));
        assertEquals(map.get("d"), null);
    }

    @Test
    public void testToStringWithSemicolonMap() throws Exception {
        Map<String, List<String>> map = new KeyValueList("a=1,2,3;b\\=1=1\\=1,2\\,3;c=\\;;d\\;=\\;;e=").toStringMap();
        assertEquals(map.get("a"), Lists.newArrayList("1", "2", "3"));
        assertEquals(map.get("b=1"), Lists.newArrayList("1=1", "2,3"));
        assertEquals(map.get("c"), Lists.newArrayList(";"));
        assertEquals(map.get("d;"), Lists.newArrayList(";"));
        assertEquals(map.get("e"), null);
    }

    @Test
    public void testToPropertyMap() throws Exception {
        Map<Property, List<String>> map = new KeyValueList("a\\,1=1\\=1,2\\,3,4\\5|b=1\\|1,2\\3|c=").toPropertyMap();
        assertEquals(map.get(new Property("a,1")), Lists.newArrayList("1=1", "2,3", "4\\5"));
        assertEquals(map.get(new Property("b")), Lists.newArrayList("1|1", "2\\3"));
        assertEquals(map.get(new Property("c")), Lists.newArrayList(""));
    }

    @Test
    public void testToPropertyWithSemicolonMap() throws Exception {
        Map<Property, List<String>> map = new KeyValueList("a\\,1=1\\=1,2\\,3,4\\5;b=1\\|1,2\\3;c=\\;;d\\;=\\;;e=").toPropertyMap();
        assertEquals(map.get(new Property("a,1")), Lists.newArrayList("1=1", "2,3", "4\\5"));
        assertEquals(map.get(new Property("b")), Lists.newArrayList("1|1", "2\\3"));
        assertEquals(map.get(new Property("c")), Lists.newArrayList(";"));
        assertEquals(map.get(new Property("d;")), Lists.newArrayList(";"));
        assertEquals(map.get(new Property("e")),  Lists.newArrayList(""));
    }
}
