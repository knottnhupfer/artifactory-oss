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

package org.artifactory.storage.db.build.service;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Saffi Hartal
 */
public class BuildPropertyAsKeyValueTest {
    @Test
    public void testEquals() throws Exception {
        BuildPropertyAsKeyValue kv1 = new BuildPropertyAsKeyValue("a", "b");
        BuildPropertyAsKeyValue kv2 = new BuildPropertyAsKeyValue("a", "b");
        BuildPropertyAsKeyValue kv3 = new BuildPropertyAsKeyValue("b", "a");
        BuildPropertyAsKeyValue kv4 = new BuildPropertyAsKeyValue("c", "b");
        BuildPropertyAsKeyValue kv5 = new BuildPropertyAsKeyValue("a", "c");
        assertEquals(kv1, kv1);
        assertEquals(kv1, kv2);
        assertNotEquals(kv1, kv3);
        assertNotEquals(kv1, kv4);
        assertNotEquals(kv1, kv5);
    }

    @Test
    public void testHashCode() throws Exception {
        BuildPropertyAsKeyValue kv1 = new BuildPropertyAsKeyValue("a", "b");
        BuildPropertyAsKeyValue kv2 = new BuildPropertyAsKeyValue("a", "b");
        BuildPropertyAsKeyValue kv3 = new BuildPropertyAsKeyValue("b", "a");
        BuildPropertyAsKeyValue kv4 = new BuildPropertyAsKeyValue("c", "b");
        BuildPropertyAsKeyValue kv5 = new BuildPropertyAsKeyValue("a", "c");
        assertEquals(kv1.hashCode(), kv1.hashCode());
        assertEquals(kv1.hashCode(), kv2.hashCode());
        assertNotEquals(kv1.hashCode(), kv3.hashCode());
        assertNotEquals(kv1.hashCode(), kv4.hashCode());
        assertNotEquals(kv1.hashCode(), kv5.hashCode());
    }

}