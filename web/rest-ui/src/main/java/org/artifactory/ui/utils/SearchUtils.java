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

package org.artifactory.ui.utils;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.checksum.ChecksumType;

/**
 * @author Dan Feldman
 */
public class SearchUtils {

    public static void addChecksumCriteria(String query, ChecksumSearchControls searchControls) {
        if (StringUtils.length(query) == ChecksumType.md5.length()) {
            searchControls.addChecksum(ChecksumType.md5, query);
            searchControls.setLimitSearchResults(true);
        } else if (StringUtils.length(query) == ChecksumType.sha1.length()) {
            searchControls.addChecksum(ChecksumType.sha1, query);
        } else if (StringUtils.length(query) == ChecksumType.sha256.length()) {
            searchControls.addChecksum(ChecksumType.sha256, query);
        }
    }

}
