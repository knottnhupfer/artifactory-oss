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

package org.artifactory.storage.fs.lock;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Uriah Levy
 */
@Test
public class ThreadDumpUtilsTest {

    public void twoThreadDumpsExactlyWithInterval() {
        long startTime = System.currentTimeMillis();
        StringBuilder dumps = new StringBuilder();
        ThreadDumpUtils.builder()
                .count(2)
                .intervalMillis(1000) // 1s
                .build()
                .dumpThreads(dumps);
        assertTrue(System.currentTimeMillis() - startTime > 1000); // the util waited at least 1s
        assertTrue(System.currentTimeMillis() - startTime < 2000); // but not more than 2...
        assertEquals(2, StringUtils.countMatches(dumps.toString(), "Dump number:"));
    }

    public void threeThreadDumpsExactlyNoInterval() {
        StringBuilder dumps = new StringBuilder();
        ThreadDumpUtils.builder()
                .count(3)
                .intervalMillis(0)
                .build()
                .dumpThreads(dumps);
        assertEquals(1, StringUtils.countMatches(dumps.toString(), "Dump number: 1"));
        assertEquals(1, StringUtils.countMatches(dumps.toString(), "Dump number: 2"));
        assertEquals(1, StringUtils.countMatches(dumps.toString(), "Dump number: 3"));
    }
}