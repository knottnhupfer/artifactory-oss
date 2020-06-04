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

package org.artifactory.storage.db.fs.model;

import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFolderInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.fs.MutableVfsFolder;
import org.artifactory.storage.fs.MutableVfsItem;
import org.artifactory.storage.fs.VfsMutableFolderProvider;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.lock.LockingHelper;
import org.artifactory.storage.fs.repo.StoringRepo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provider of VFS files.
 *
 * @author Yossi Shaul
 */
public class DbMutableFolderProvider extends DbFsItemProvider implements VfsMutableFolderProvider {

    public DbMutableFolderProvider(StoringRepo storingRepo, RepoPath repoPath, FsItemsVault fileVault,
            FsItemsVault folderVault) {
        super(storingRepo, repoPath, fileVault, folderVault);
    }

    @Override
    @Nullable
    public MutableVfsFolder getMutableFolder() {
        VfsItem mutableFsItem = super.getMutableFsItem(folderVault);
        if (mutableFsItem != null && !mutableFsItem.isFolder()) {
            LockingHelper.removeLockEntry(getRepoPath());
            throw new FolderExpectedException(getRepoPath());
        }
        return (MutableVfsFolder) mutableFsItem;
    }

    @Override
    @Nonnull
    public MutableVfsFolder getOrCreMutableFolder() {
        MutableVfsItem mutableFsItem = super.getOrCreateMutableFsItem(false);
        if (mutableFsItem.isFile()) {
            LockingHelper.removeLockEntry(getRepoPath());
            throw new FolderExpectedException(getRepoPath());
        }
        return (MutableVfsFolder) mutableFsItem;
    }

    @Override
    protected MutableVfsFolder createNewMutableItem(StoringRepo storingRepo, RepoPath repoPath) {
        MutableFolderInfo folderInfo = InfoFactoryHolder.get().createFolderInfo(repoPath);
        return new DbMutableFolder(storingRepo, DbService.NO_DB_ID, folderInfo);
    }
}
