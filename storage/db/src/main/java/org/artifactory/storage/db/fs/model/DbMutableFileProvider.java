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

import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.fs.MutableVfsFile;
import org.artifactory.storage.fs.MutableVfsItem;
import org.artifactory.storage.fs.VfsMutableFileProvider;
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
public class DbMutableFileProvider extends DbFsItemProvider implements VfsMutableFileProvider {

    public DbMutableFileProvider(StoringRepo storingRepo, RepoPath repoPath,
            FsItemsVault fileVault, FsItemsVault folderVault) {
        super(storingRepo, repoPath, fileVault, folderVault);
    }

    @Override
    @Nullable
    public MutableVfsFile getMutableFile() {
        MutableVfsItem mutableFsItem = super.getMutableFsItem(fileVault);
        if (mutableFsItem != null && !mutableFsItem.isFile()) {
            LockingHelper.removeLockEntry(getRepoPath());
            throw new FileExpectedException(getRepoPath());
        }
        return (MutableVfsFile) mutableFsItem;
    }

    @Override
    @Nonnull
    public MutableVfsFile getOrCreMutableFile() {
        MutableVfsItem mutableFsItem = super.getOrCreateMutableFsItem(true);
        if (!mutableFsItem.isFile()) {
            LockingHelper.removeLockEntry(getRepoPath());
            throw new FileExpectedException(getRepoPath());
        }
        return (MutableVfsFile) mutableFsItem;
    }

    @Override
    protected MutableVfsFile createNewMutableItem(StoringRepo storingRepo, RepoPath repoPath) {
        MutableFileInfo fileInfo = InfoFactoryHolder.get().createFileInfo(repoPath);
        return new DbMutableFile(storingRepo, DbService.NO_DB_ID, fileInfo);
    }
}
