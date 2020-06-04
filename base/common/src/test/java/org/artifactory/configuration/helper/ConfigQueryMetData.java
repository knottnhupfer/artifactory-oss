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

package org.artifactory.configuration.helper;

/**
 * @author gidis
 */
public class ConfigQueryMetData {
    private String blob;
    private String path;

    public void setBlob(String blob) {
        this.blob = blob;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBlob() {
        return blob;
    }

    public String getPath() {
        return path;
    }
}
