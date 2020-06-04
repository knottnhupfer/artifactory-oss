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

/**
 * @author Michael Pasternak
 */
public class InternalStringUtils {

    public static final String LINE_SEPARATOR = System.lineSeparator();

    private InternalStringUtils() {;}

    /**
     * Replaces last occurrence of given string
     *
     * @param string string to work on
     * @param from replace candidate
     * @param to replace content
     *
     * @return modified string
     */
    public static String replaceLast(String string, String from, String to) {
        if (!org.apache.commons.lang.StringUtils.isBlank(string)) {
            int lastIndex = string.lastIndexOf(from);
            if (lastIndex < 0) return string;
            String tail = string.substring(lastIndex).replaceFirst(from, to);
            return string.substring(0, lastIndex) + tail;
        }
        return string;
    }

    /**
     * Capitalizes first latter in string
     *
     * @return capitalized string
     */
    public static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static int compareNullLast(String s1, String s2) {
        if (s1 == null) {
            if (s2 == null) {
                return 0;
            }
            return -1;
        } else if (s2 == null) {
            return 1;
        }
        return s1.compareTo(s2);
    }
}
