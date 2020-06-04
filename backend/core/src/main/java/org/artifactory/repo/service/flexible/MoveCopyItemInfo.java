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

package org.artifactory.repo.service.flexible;

import org.artifactory.fs.FileInfo;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.fs.MutableVfsItem;

/**
 * @author gidis
 */
public class MoveCopyItemInfo {
    private RepoPath sourceRepoPath;
    private RepoPath targetRepoPath;
    private VfsItem sourceItem;
    private VfsItem targetItem;
    private RepoRepoPath<LocalRepo> sourceRrp;
    private RepoRepoPath<LocalRepo> targetRrp;
    private long dept;
    private FileInfo targetOriginalFileInfo;
    private MutableVfsItem mutableTargetItem;

    public MoveCopyItemInfo(RepoPath sourceRepoPath, RepoPath targetRepoPath, VfsItem sourceItem, VfsItem targetItem,
                            RepoRepoPath<LocalRepo> sourceRrp, RepoRepoPath<LocalRepo> targetRrp, long dept, FileInfo targetOriginalFileInfo) {
        this.sourceRepoPath = sourceRepoPath;
        this.targetRepoPath = targetRepoPath;
        this.sourceItem = sourceItem;
        this.targetItem = targetItem;
        this.sourceRrp = sourceRrp;
        this.targetRrp = targetRrp;
        this.dept = dept;
        this.targetOriginalFileInfo = targetOriginalFileInfo;
    }

    public long getDepth() {
        return dept;
    }

    public VfsItem getSourceItem() {
        return sourceItem;
    }

    public VfsItem getTargetItem() {
        return targetItem;
    }

    public FileInfo getTargetOriginalFileInfo() {
        return targetOriginalFileInfo;
    }

    public RepoRepoPath<LocalRepo> getTargetRrp() {
        return targetRrp;
    }

    public RepoPath getSourceRepoPath() {
        return sourceRepoPath;
    }

    public RepoPath getTargetRepoPath() {
        return targetRepoPath;
    }

    public MutableVfsItem getMutableTargetItem() {
        return mutableTargetItem;
    }

    public void setMutableTargetItem(MutableVfsItem mutableTargetItem) {
        this.mutableTargetItem = mutableTargetItem;
    }

    public RepoRepoPath<LocalRepo> getSourceRrp() {
        return sourceRrp;
    }


}
