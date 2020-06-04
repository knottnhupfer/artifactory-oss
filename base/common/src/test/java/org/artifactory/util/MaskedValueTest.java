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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Yinon Avraham
 */
public class MaskedValueTest {

    @Test
    public void testMaskedValue() {
        assertEquals(MaskedValue.of(null).toString(), "null");
        assertEquals(MaskedValue.of("1234567890abcde").toString(), "*******cde");
        assertEquals(MaskedValue.of("1234567890abcd").toString(), "*******bcd");
        assertEquals(MaskedValue.of("1234567890abc").toString(), "*******abc");
        assertEquals(MaskedValue.of("1234567890ab").toString(), "*******0ab");
        assertEquals(MaskedValue.of("1234567890a").toString(), "*******90a");
        assertEquals(MaskedValue.of("1234567890").toString(), "*******890");
        assertEquals(MaskedValue.of("123456789").toString(), "**********");
        assertEquals(MaskedValue.of("12345678").toString(), "**********");
        assertEquals(MaskedValue.of("1234567").toString(), "**********");
        assertEquals(MaskedValue.of("123456").toString(), "**********");
        assertEquals(MaskedValue.of("12345").toString(), "**********");
        assertEquals(MaskedValue.of("1234").toString(), "**********");
        assertEquals(MaskedValue.of("123").toString(), "**********");
        assertEquals(MaskedValue.of("12").toString(), "**********");
        assertEquals(MaskedValue.of("1").toString(), "**********");
        assertEquals(MaskedValue.of("").toString(), "**********");
        assertEquals(MaskedValue.of(Long.MAX_VALUE).toString(), "*******807");
        assertEquals(MaskedValue.of(Integer.MAX_VALUE).toString(), "*******647");
    }

}