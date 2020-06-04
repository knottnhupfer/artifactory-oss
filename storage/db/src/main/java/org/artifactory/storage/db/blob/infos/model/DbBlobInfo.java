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

package org.artifactory.storage.db.blob.infos.model;

import lombok.Builder;

/**
 * @author Inbar Tal
 */
@Builder(builderClassName = "Builder")
public class DbBlobInfo {

    private String checksum;
    private String blobInfo;

    public DbBlobInfo(String checksum, String blobInfo) {
        this.checksum = checksum;
        this.blobInfo = blobInfo;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setBlobInfo(String blobInfo) {
        this.blobInfo = blobInfo;
    }

    public String getBlobInfo() {
        return blobInfo;
    }
}
