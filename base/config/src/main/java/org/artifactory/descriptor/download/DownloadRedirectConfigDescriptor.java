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

package org.artifactory.descriptor.download;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Download redirect descriptor for Cloud storage
 *
 * @author Yuval Reches
 */
@XmlType(name = "DownloadRedirectConfigType", propOrder = {"fileMinimumSize"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class DownloadRedirectConfigDescriptor implements Descriptor {

    // Download redirect threshold file size in mb
    @XmlElement(required = true, defaultValue = "1")
    private int fileMinimumSize = 1;

    public int getFileMinimumSize() {
        return fileMinimumSize;
    }

    public void setFileMinimumSize(int fileMinimumSize) {
        if (fileMinimumSize < 0) {
            fileMinimumSize = 0;
        }
        this.fileMinimumSize = fileMinimumSize;
    }

}