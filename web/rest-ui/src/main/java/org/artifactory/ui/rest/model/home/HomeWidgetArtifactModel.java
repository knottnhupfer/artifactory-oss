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

package org.artifactory.ui.rest.model.home;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Dan Feldman
 */
public class HomeWidgetArtifactModel extends BaseModel {

    private String path;
    private String downloadLink;
    private long downloads;

    public HomeWidgetArtifactModel(String path, String downloadLink, long downloads) {
        this.path = path;
        this.downloadLink = downloadLink;
        this.downloads = downloads;
    }

    public String getPath() {
        return path;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public long getDownloads() {
        return downloads;
    }
}
