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

package org.artifactory.traffic.entry;

import org.artifactory.traffic.TrafficAction;

/**
 * Artifact download redirect traffic entry
 * This is used when using S3 for sending HTTP 302 with signed url instead of providing the artifact ourselves from S3.
 *
 * @author Yuval Reches
 */
public class RedirectEntry extends TransferEntry {

    /**
     * Parses the given textual entry and sets the object fields accordingly
     *
     * @param entry Textual entry
     */
    public RedirectEntry(String entry) {
        super(entry);
    }

    /**
     * Sets the given entry data in the relevant fields
     *
     * @param repoPath      Requested artifact repo path
     * @param contentLength Requested artifact size
     */
    public RedirectEntry(String repoPath, long contentLength, long duration, String userAddress) {
        super(repoPath, contentLength, duration, userAddress);
    }

    @Override
    public TrafficAction getAction() {
        return TrafficAction.REDIRECT;
    }
}