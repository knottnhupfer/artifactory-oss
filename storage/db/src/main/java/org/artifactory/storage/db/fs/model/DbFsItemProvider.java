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

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.common.StatusHolder;
import org.artifactory.exception.CancelException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.MutableFolderInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.fs.MutableVfsFolder;
import org.artifactory.storage.fs.MutableVfsItem;
import org.artifactory.storage.fs.VfsItemNotFoundException;
import org.artifactory.storage.fs.VfsItemProvider;
import org.artifactory.storage.fs.lock.FsItemLockEntry;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.lock.LockEntryId;
import org.artifactory.storage.fs.lock.LockingHelper;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.tree.MinimalInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The exclusive provider of vfs items.
 *
 * @author Yossi Shaul
 */
public class DbFsItemProvider implements VfsItemProvider {
    private static final Logger log = LoggerFactory.getLogger(DbFsItemProvider.class);

    private final StoringRepo storingRepo;
    private final RepoPath repoPath;
    final FsItemsVault fileVault;
    final FsItemsVault folderVault;

    DbFsItemProvider(StoringRepo storingRepo, RepoPath repoPath, FsItemsVault fileVault,
            FsItemsVault folderVault) {
        if (!storingRepo.getKey().equals(repoPath.getRepoKey())) {
            throw new IllegalArgumentException(
                    "Attempt to lock '" + repoPath + "' with repository '" + storingRepo.getKey());
        }
        this.storingRepo = storingRepo;
        this.folderVault = folderVault;
        this.fileVault = fileVault;
        this.repoPath = repoPath;
    }

    @Nullable
    public MutableVfsItem getMutableFsItem() {
        // TODO [By Gidi] check with Yossi if we can return mutable item without locking
        // Try to get mutableFsItem from local session
        MutableVfsItem mutableItem = LockingHelper.getIfWriteLockedByMe(getRepoPath());
        if (mutableItem != null) {
            return mutableItem;
        }
        FsItemsVault vault = getLockVault();
        // Fetch mutable item
        return getMutableFsItem(vault);
    }

    private FsItemsVault getLockVault() {
        // If the file and folder vaults are the same instance, there is no use in differentiating between them by
        // querying for the type of this repoPath.
        if (fileVault == folderVault) {
            return fileVault;
        } else {
            // Support the Optimistic Locking model, where the file and folder vaults are unique
            MinimalInfo minimalInfo = MinimalInfo.resolve(repoPath);
            // If file doesn't exist return null
            if (!minimalInfo.isExist()) {
                return null;
            }
            return minimalInfo.isFile() ? fileVault : folderVault;
        }
    }

    @Nullable
    public MutableVfsItem getMutableFsItem(FsItemsVault vault) {
        LockEntryId lockEntryId = vault.getLock(repoPath);
        // Acquire the write lock
        FsItemLockEntry sessionLockEntry = LockingHelper.writeLock(lockEntryId);
        // Try to get the item from the lock
        MutableVfsItem mutableItem = sessionLockEntry.getMutableFsItem();
        try {
            if (mutableItem == null) {
                // get the requested fs item from the storage
                mutableItem = fetchMutableVfsItem(repoPath, sessionLockEntry);
            }
            return mutableItem;
        } finally {
            if (mutableItem == null) {
                // item not found -> release the lock on this repo path
                LockingHelper.removeLockEntry(repoPath);
            }
        }
    }

    @Override
    @Nonnull
    public MutableVfsItem getOrCreateMutableFsItem(boolean file) {
        // first try to get an existing mutable item
        MutableVfsItem mutableItem = LockingHelper.getIfWriteLockedByMe(getRepoPath());
        if (mutableItem != null) {
            // item exists nothing new to create
            return mutableItem;
        }

        // time to create a new item. start checking from the parent to the requested repo path
        try {
            createAncestors();
            // get mutable item for the requested repo path
            LockEntryId lockEntryId = file ? fileVault.getLock(repoPath) : folderVault.getLock(repoPath);
            FsItemLockEntry sessionLockEntry = LockingHelper.writeLock(lockEntryId);

            mutableItem = fetchMutableVfsItem(repoPath, sessionLockEntry);
            if (mutableItem == null) {
                // item doesn't exist and we hold the write lock -> create and set the write lock
                mutableItem = createNewMutableItem(storingRepo, repoPath);
                sessionLockEntry.setWriteFsItem(mutableItem);
                //TODO: [by YS] put here before/after create events? for files it's in the repo mixin
                if (mutableItem.isFolder()) {
                    invokeBeforeCreateInterceptors((MutableVfsFolder) mutableItem);
                    invokeAfterCreateInterceptors((MutableVfsFolder) mutableItem);
                }
            }
            return mutableItem;
        } catch (RepoRejectException e) {
            throw new IllegalArgumentException("Could not create file system item " + e.getMessage(), e);
        } finally {
            if (mutableItem == null) {
                // item not found -> release the lock on this repo path
                LockingHelper.removeLockEntry(repoPath);
            }
        }
    }

    private void createAncestors() throws RepoRejectException {
        // Return if root
        if (repoPath.getParent() == null) {
            return;
        }
        // get mutable item for the requested repo path
        MutableVfsItem parentVfsItem = LockingHelper.getIfWriteLockedByMe(repoPath.getParent());
        if(parentVfsItem != null){
            return;
        }
        // In most cases the parent already exist so perform a quick check before looping from the root
        MinimalInfo parentMinimalInfo = MinimalInfo.resolve(repoPath.getParent());
        if (parentMinimalInfo.isFile()) {
            throw new RepoRejectException("Parent " + repoPath.getParent().getPath() + " must be a folder",
                    HttpStatus.SC_BAD_REQUEST);
        }
        if (parentMinimalInfo.isExist()) {
            return;
        }
        List<RepoPath> ancestors = getAncestors(repoPath);
        for (RepoPath ancestor : ancestors) {
            // first check if it's already locked by this session
            FsItemLockEntry sessionLockEntry = LockingHelper.getLockEntry(ancestor);
            MinimalInfo ancestorMinimalInfo = MinimalInfo.resolve(ancestor);
            if (ancestorMinimalInfo.isFile()) {
                throw new RepoRejectException("Parent " + repoPath.getParent().getPath() + " must be a folder",
                        HttpStatus.SC_BAD_REQUEST);
            }
            if ((sessionLockEntry == null || sessionLockEntry.getMutableFsItem() == null) && !
                    ancestorMinimalInfo.isExist()) {
                // acquire the write lock
                LockEntryId lockEntryId = folderVault.getLock(ancestor);
                sessionLockEntry = LockingHelper.writeLock(lockEntryId);
                // after acquiring the lock check again that the item doesn't exist in the database
                ancestorMinimalInfo = MinimalInfo.resolve(ancestor);
                if (ancestorMinimalInfo.isExist()) {
                    // somebody already created the folder - release the write lock
                    LockingHelper.removeLockEntry(ancestor);
                } else {
                    //TORE: [by YS] share the folder creation with dbfolderprovider
                    //TODO: [by YS] basically now we can create all the way down. no need to checks exists anymore. but consider keeping it like this for simplicity
                    // item doesn't exist and we hold the write lock -> create and set the write lock
                    log.debug("Creating ancestor {} for {}", ancestor, repoPath);
                    MutableFolderInfo folderInfo = InfoFactoryHolder.get().createFolderInfo(ancestor);
                    DbMutableFolder newlyCreatedFolder =
                            new DbMutableFolder(storingRepo, DbService.NO_DB_ID, folderInfo);
                    sessionLockEntry.setWriteFsItem(newlyCreatedFolder);
                    // set current user as the creator/modifier. This can be later overridden in case of an import
                    String userId = ContextHelper.get().getAuthorizationService().currentUsername();
                    newlyCreatedFolder.setModifiedBy(userId);
                    newlyCreatedFolder.setCreatedBy(userId);
                    invokeBeforeCreateInterceptors(newlyCreatedFolder);
                    invokeAfterCreateInterceptors(newlyCreatedFolder);
                }
            }
        }
    }

    public static List<RepoPath> getAncestors(RepoPath repoPath) {
        List<RepoPath> ancestors = Lists.newArrayList();
        RepoPath ancestor = repoPath.getParent();
        while (ancestor != null && !ancestor.isRoot()) {
            ancestors.add(0, ancestor);
            ancestor = ancestor.getParent();
        }
        return ancestors;
    }

    /**
     * Create a new mutable fs item. This method is called only if the item doesn't exist in the database
     *
     * @param storingRepo The repository that will store the new item
     * @param repoPath    Repo path of the new item
     * @return A newly created mutable fs item. The item doesn't exist in the database yet.
     */
    protected MutableVfsItem createNewMutableItem(StoringRepo storingRepo, RepoPath repoPath) {
        throw new UnsupportedOperationException("Cannot create mutable item from a generic item provider: " + repoPath);
    }

    @Nullable
    private MutableVfsItem fetchMutableVfsItem(RepoPath repoPath, FsItemLockEntry lockEntry) {
        FileService fileService = ContextHelper.get().beanForType(FileService.class);
        VfsItem vfsItem = fetchVfsItem(repoPath, fileService);
        MutableVfsItem mutableItem = null;
        if (vfsItem != null) {
            // item exists in db -> create a mutable version
            if (vfsItem.isFolder()) {
                mutableItem = new DbMutableFolder(storingRepo, vfsItem.getId(), (FolderInfo) vfsItem.getInfo());
            } else {
                mutableItem = new DbMutableFile(storingRepo, vfsItem.getId(), (FileInfo) vfsItem.getInfo());
            }

            // set the mutable item on the lock entry
            lockEntry.setWriteFsItem(mutableItem);
        }
        return mutableItem;
    }

    @Nullable
    private VfsItem fetchVfsItem(RepoPath repoPath, FileService fileService) {
        VfsItem item;
        try {
            item = fileService.loadVfsItem(storingRepo, repoPath);
        } catch (VfsItemNotFoundException e) {
            item = null;
        } catch (StorageException e) {
            throw new StorageException(e);
        }
        return item;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    private void invokeBeforeCreateInterceptors(MutableVfsFolder mutableItem) throws CancelException {
        if (mutableItem.getInfo().getRepoPath().isRoot()) {
            return;
        }
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        StorageInterceptors interceptors = ContextHelper.get().beanForType(StorageInterceptors.class);
        interceptors.beforeCreate(mutableItem, statusHolder);
        checkForCancelException(mutableItem, statusHolder);
    }

    private void invokeAfterCreateInterceptors(MutableVfsFolder mutableItem) {
        if (mutableItem.getInfo().getRepoPath().isRoot()) {
            return;
        }
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        StorageInterceptors interceptors = ContextHelper.get().beanForType(StorageInterceptors.class);
        interceptors.afterCreate(mutableItem, statusHolder, new InterceptorCreateContext());
        checkForCancelException(mutableItem, statusHolder);
    }

    private void checkForCancelException(MutableVfsFolder mutableFolder, StatusHolder status) {
        if (status.getCancelException() != null) {
            mutableFolder.markError();
            LockingHelper.removeLockEntry(mutableFolder.getRepoPath());
            throw status.getCancelException();
        }
    }


}
