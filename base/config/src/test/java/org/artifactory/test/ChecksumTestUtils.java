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

import org.apache.commons.lang.RandomStringUtils;

/**
 * Utility class to help checksum related tests.
 *
 * @author Yossi Shaul
 */
public class ChecksumTestUtils {

    /**
     * @return Randomly generated 32 bytes MD5 string
     */
    public static String randomMd5() {
        return randomHex(32);
    }

    /**
     * @return Randomly generated 40 bytes SHA-1 string
     */
    public static String randomSha1() {
        return randomHex(40);
    }

    /**
     * @param count Number of characters to include in the random string
     * @return Randomly generated HEX string with the specified length
     */
    public static String randomHex(int count) {
        return RandomStringUtils.random(count, "abcdef0123456789");
    }

}
