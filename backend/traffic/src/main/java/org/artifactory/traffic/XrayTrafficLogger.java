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

package org.artifactory.traffic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.artifactory.traffic.entry.TransferEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger class for Xray traffic
 *
 * @author Tamir Hadad
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract class XrayTrafficLogger {
    private static final Logger log = LoggerFactory.getLogger(XrayTrafficLogger.class);

    /**
     * Log only Xray traffic entries
     */
    static void logTransferEntry(TransferEntry entry) {
        String userIdentifier = entry.getUserIdentifier();
        if (userIdentifier != null && userIdentifier.toLowerCase().contains("xray")) {
            log.info(entry.toString());
        }
    }
}