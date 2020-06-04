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

package org.artifactory.repo.service.mover;

import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.fs.MutableVfsFolder;

/**
 * @author Chen Keinan
 */
public class VfsFolderRepo {

    private RepoRepoPath repoRepoPath;
    private VfsItem vfsItem;

    public VfsFolderRepo(MutableVfsFolder mutableVfsFolder, RepoRepoPath<LocalRepo> targetRrp) {
        this.repoRepoPath = targetRrp;
        this.vfsItem = mutableVfsFolder;
    }

    public RepoRepoPath getRepoRepoPath() {
        return repoRepoPath;
    }

    public void setRepoRepoPath(RepoRepoPath repoRepoPath) {
        this.repoRepoPath = repoRepoPath;
    }

    public VfsItem getVfsItem() {
        return vfsItem;
    }

    public void setVfsItem(VfsItem vfsItem) {
        this.vfsItem = vfsItem;
    }
}
