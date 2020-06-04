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


package org.artifactory.api.rest.search.result;

import org.artifactory.api.search.stats.StatsSearchResult;
import org.jfrog.client.util.PathUtils;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Json object retuning the UsageSinceResource search results
 *
 * @author Eli Givoni
 */
public class LastDownloadRestResult {

    public List<DownloadedEntry> results = new ArrayList<>();

    public static class DownloadedEntry {

        public String uri;
        public long downloadCount;
        public String lastDownloaded;
        public long remoteDownloadCount;
        public String remoteLastDownloaded;

        //Don't remove this, the test needs it
        public DownloadedEntry() {

        }

        public DownloadedEntry(String uri, StatsSearchResult result) {
            this.uri = uri;
            this.downloadCount = result.getDownloadCount();
            this.lastDownloaded = toIsoDateString(result.getLastDownloaded());
            this.remoteDownloadCount = result.getRemoteDownloadCount();
            this.remoteLastDownloaded = toIsoDateString(result.getRemoteLastDownloaded());
        }

        @Override
        public String toString() {
            return "DownloadedEntry{" +
                    "uri='" + uri + '\'' +
                    ", downloadCount=" + downloadCount +
                    ", lastDownloaded='" + lastDownloaded + '\'' +
                    ", remoteDownloadCount=" + remoteDownloadCount +
                    ", remoteLastDownloaded='" + remoteLastDownloaded + '\'' +
                    '}';
        }
    }

    //Copied from RestUtils
    public static String toIsoDateString(long time) {
        return ISODateTimeFormat.dateTime().print(time);
    }

    @Override
    public String toString() {
        return PathUtils.collectionToDelimitedString(results.stream()
                .map(DownloadedEntry::toString)
                .collect(Collectors.toList()));
    }
}
