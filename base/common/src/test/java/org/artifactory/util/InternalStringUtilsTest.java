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

package org.artifactory.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Yinon Avraham
 */
@Test
public class InternalStringUtilsTest {

    @Test(dataProvider = "provideCompareNullLast")
    public void testCompareNullLast(String s1, String s2, int expected) {
        assertEquals(Math.signum(InternalStringUtils.compareNullLast(s1, s2)), Math.signum(expected));
    }

    @DataProvider
    private Object[][] provideCompareNullLast() {
        return new Object[][] {
                { "abc", "xyz", -1 },
                { "abc", "abc", 0 },
                { "xyz", "abc", 1 },
                { "abc", null, 1 },
                { null, "abc", -1 },
                { null, null, 0 },
        };
    }

}