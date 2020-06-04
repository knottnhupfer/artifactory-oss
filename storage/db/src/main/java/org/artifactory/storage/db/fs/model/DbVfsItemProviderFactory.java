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

import org.artifactory.repo.RepoPath;
import org.artifactory.storage.fs.VfsItemProvider;
import org.artifactory.storage.fs.VfsItemProviderFactory;
import org.artifactory.storage.fs.VfsMutableFileProvider;
import org.artifactory.storage.fs.VfsMutableFolderProvider;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.tree.VfsImmutableProvider;
import org.springframework.stereotype.Component;

/**
 * Factory of of DB items provider.
 *
 * @author Yossi Shaul
 */
@Component
public class DbVfsItemProviderFactory implements VfsItemProviderFactory {
    @Override
    public VfsImmutableProvider createImmutableItemProvider(StoringRepo storingRepo, RepoPath repoPath) {
        return new DBImmutableItemProvider(storingRepo, repoPath);
    }

    @Override
    public VfsImmutableProvider createImmutableFileProvider(StoringRepo storingRepo, RepoPath repoPath) {
        return new DBImmutableItemProvider(storingRepo, repoPath);
    }

    @Override
    public VfsImmutableProvider createImmutableFolderProvider(StoringRepo storingRepo, RepoPath repoPath) {
        return new DBImmutableItemProvider(storingRepo, repoPath);
    }

    @Override
    public VfsItemProvider createItemProvider(StoringRepo storingRepo, RepoPath repoPath,
            FsItemsVault fileVault, FsItemsVault folderVault) {
        return new DbFsItemProvider(storingRepo, repoPath, fileVault,folderVault);
    }

    @Override
    public VfsMutableFileProvider createFileProvider(StoringRepo storingRepo, RepoPath repoPath,
            FsItemsVault fsIFileVault, FsItemsVault fsIFolderVault) {
        return new DbMutableFileProvider(storingRepo, repoPath, fsIFileVault,fsIFolderVault);
    }

    @Override
    public VfsMutableFolderProvider createFolderProvider(StoringRepo storingRepo, RepoPath repoPath,
            FsItemsVault fsIFileVault, FsItemsVault fsIFolderVault) {
        return new DbMutableFolderProvider(storingRepo, repoPath, fsIFileVault,fsIFolderVault);
    }
}
