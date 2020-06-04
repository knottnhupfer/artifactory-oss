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

package org.artifactory.maven.index;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.resource.ResourceStreamHandle;

import java.io.File;

/**
 * @author Roman Gurevitch
 */
class RepoIndex {
    private ResourceStreamHandle indexHandle;
    private ResourceStreamHandle propertiesHandle;
    private File repoIndexDir;

    RepoIndex(ResourceStreamHandle indexHandle, ResourceStreamHandle propertiesHandle, File repoIndexDir) {
        this.indexHandle = indexHandle;
        this.propertiesHandle = propertiesHandle;
        this.repoIndexDir = repoIndexDir;
    }

    RepoIndex(ResourceStreamHandle indexHandle, ResourceStreamHandle propertiesHandle) {
        this.indexHandle = indexHandle;
        this.propertiesHandle = propertiesHandle;
    }

    ResourceStreamHandle getIndexHandle() {
        return indexHandle;
    }

    ResourceStreamHandle getPropertiesHandle() {
        return propertiesHandle;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public File getRepoIndexDir() {
        return repoIndexDir;
    }

    void closeHandles() {
        IOUtils.closeQuietly(indexHandle);
        IOUtils.closeQuietly(propertiesHandle);
        FileUtils.deleteQuietly(repoIndexDir);
    }
}