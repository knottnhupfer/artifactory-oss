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

package org.artifactory.api.rest.artifact;

import org.artifactory.fs.StatsInfo;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * @author Yoav Luft
 */
public class ItemStatsInfo implements StatsInfo, Serializable {

    private String uri;
    private long downloadCount;
    private long lastDownloaded;
    private String lastDownloadedBy;

    private long remoteDownloadCount;
    private long remoteLastDownloaded;
    private String remoteLastDownloadedBy;
    private String origin;

    public ItemStatsInfo() {
    }

    public ItemStatsInfo(String uri, long downloadCount, long lastDownloaded,
            String lastDownloadedBy) {
        this.uri = uri;
        this.downloadCount = downloadCount;
        this.lastDownloaded = lastDownloaded;
        this.lastDownloadedBy = lastDownloadedBy;
    }

    public ItemStatsInfo(String uri, StatsInfo artifactStatsInfo) {
        this.uri = uri;
        this.downloadCount = artifactStatsInfo.getDownloadCount();
        this.lastDownloaded = artifactStatsInfo.getLastDownloaded();
        this.lastDownloadedBy = artifactStatsInfo.getLastDownloadedBy();
        this.remoteDownloadCount = artifactStatsInfo.getRemoteDownloadCount();
        this.remoteLastDownloaded = artifactStatsInfo.getRemoteLastDownloaded();
        this.remoteLastDownloadedBy = artifactStatsInfo.getRemoteLastDownloadedBy();
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public void setLastDownloaded(long lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }

    public void setLastDownloadedBy(String lastDownloadedBy) {
        this.lastDownloadedBy = lastDownloadedBy;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public long getDownloadCount() {
        return downloadCount;
    }

    @Override
    public long getLastDownloaded() {
        return lastDownloaded;
    }

    @Override
    public String getLastDownloadedBy() {
        return lastDownloadedBy;
    }

    @Override
    public long getRemoteDownloadCount() {
        return remoteDownloadCount;
    }

    @Override
    public long getRemoteLastDownloaded() {
        return remoteLastDownloaded;
    }

    @Override
    public String getRemoteLastDownloadedBy() {
        return remoteLastDownloadedBy;
    }

    @JsonIgnore
    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
