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

package org.artifactory.ui.rest.model.admin.services.filesystem;

import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class FileSystemBrowser extends BaseModel {

    private List<String> roots;
    private List<FileSystemItem> fileSystemItems;
    private boolean isWindows;

    public List<String> getRoots() {
        return roots;
    }

    public void setRoots(List<String> roots) {
        this.roots = roots;
    }

    public List<FileSystemItem> getFileSystemItems() {
        return fileSystemItems;
    }

    public void setFileSystemItems(List<FileSystemItem> fileSystemItems) {
        this.fileSystemItems = fileSystemItems;
    }

    public boolean isWindows() {
        return isWindows;
    }

    public void setWindows(boolean windows) {
        isWindows = windows;
    }
}
