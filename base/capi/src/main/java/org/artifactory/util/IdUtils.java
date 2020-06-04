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

import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

/**
 * @author Noam Shemesh
 */
public abstract class IdUtils {
    private IdUtils() {}

    private static final int MAXIMUM_LENGTH = 30;
    private static final int HASH_LENGTH = 10;

    public static String createReplicationKey(String repoKey, String url) {
        String concat = normalize((repoKey == null ? "" : repoKey) + "_" + (url == null ? "" : url));

        if (concat.length() > MAXIMUM_LENGTH - HASH_LENGTH) {
            return concat.substring(0, MAXIMUM_LENGTH - HASH_LENGTH) + hash(concat, HASH_LENGTH);
        }

        return concat;
    }

    private static String hash(String concat, int hashLength) {
        return Hashing.md5().hashString(concat, Charset.defaultCharset()).toString().substring(0, hashLength);
    }

    private static String normalize(String s) {
        return s.replaceAll("[:/@\\\\.]", "_");
    }

}
