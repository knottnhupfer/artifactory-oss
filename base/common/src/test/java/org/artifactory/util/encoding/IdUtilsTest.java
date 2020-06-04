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

package org.artifactory.util.encoding;

import org.artifactory.util.IdUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Noam Shemesh
 */
public class IdUtilsTest {
    @Test
    public void testProduceIdWithEmptyUrl() throws Exception {
        String res = IdUtils.createReplicationKey("abc", null);
        assertEquals(res, "abc_");
    }

    @Test
    public void testProduceIdWithLongUrl() throws Exception {
        String res = IdUtils.createReplicationKey("abc",
                "http://example.com/hello/world/goingtobeshortenedandreplacedwithhash");

        assertEquals(res, "abc_http___example_c95e299139f");
    }

    @Test
    public void testProduceIdWithEmptyRepoKey() throws Exception {
        String res = IdUtils.createReplicationKey(null, "http://example");
        assertEquals(res, "_http___example");
    }

    @Test
    public void testProduceIdWithMediumSizeConcat() throws Exception {
        String res = IdUtils.createReplicationKey("0123456789",
                "http://example.com");

        assertNotEquals(res, "0123456789_http___example_com");
        assertTrue(res.startsWith("0123456789_http___ex"));
    }
}