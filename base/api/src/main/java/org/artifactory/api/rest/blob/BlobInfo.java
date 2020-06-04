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

package org.artifactory.api.rest.blob;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Rotem Kfir
 */
@Data
public class BlobInfo {
    public String checksum;
    public Parts parts;
    public String version;

    public static BlobInfo emptyBlobInfo(String checksum) {
        BlobInfo blobinfo = new BlobInfo();
        blobinfo.checksum = checksum;
        blobinfo.parts = new Parts();
        blobinfo.parts.setChecksumsOrdinal(new HashMap<>());
        blobinfo.parts.setPartsList(new ArrayList<>());
        return blobinfo;
    }
}
