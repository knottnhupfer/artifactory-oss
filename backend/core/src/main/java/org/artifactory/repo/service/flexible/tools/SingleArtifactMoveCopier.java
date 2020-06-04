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

package org.artifactory.repo.service.flexible.tools;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.TrashRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.storage.fs.MutableVfsFile;
import org.artifactory.storage.fs.MutableVfsFolder;
import org.artifactory.storage.fs.MutableVfsItem;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The class execute single item move/copy task.
 * Note In  case of move folder the source folder will not be deleted, the folder deletion should be executed
 * only after full copy of the entire folder.
 *
 * @author gidis
 */
public class SingleArtifactMoveCopier {
    private static final Logger log = LoggerFactory.getLogger(SingleArtifactMoveCopier.class);
    private InternalRepositoryService repositoryService;

    public SingleArtifactMoveCopier(InternalRepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * Execute single item move/copy
     */
    void executeOperation(MoveCopyItemInfo itemInfo, MoveCopyContext context, MoveMultiStatusHolder status) {
        log.debug("Executing Single file copy from:{} to:{}", itemInfo.getSourceRepoPath(), itemInfo.getTargetRepoPath());
        // Do move copy
        MutableVfsItem mutableVfsItem = doMoveCopy(itemInfo, status, context);
        itemInfo.setMutableTargetItem(mutableVfsItem);
    }

    private MutableVfsItem doMoveCopy(MoveCopyItemInfo element, MoveMultiStatusHolder status, MoveCopyContext context) {
        RepoRepoPath<LocalRepo> targetRrp = element.getTargetRrp();
        VfsItem sourceItem = element.getSourceItem();
        VfsItem targetItem = element.getTargetItem();
        MutableVfsItem mutableVfsItem = null;
        if (!context.isDryRun()) {
            if (sourceItem.isFolder()) {
                mutableVfsItem = shallowCopyDirectory((VfsFolder) sourceItem, targetItem, targetRrp);
                List<MoveCopyItemInfo> foldersToDelete = context.getFoldersToDelete();
                // Update the folders to delete list. We will use this list to notify after delete during the folders deletion
                if(foldersToDelete.size() < ConstantValues.moveCopyMaxFoldersCacheSize.getInt()) {
                    foldersToDelete.add(element);
                }else{
                    log.warn(String.format("The number of directories being moved is greater than allowed moving the %s " +
                            "folder without deleting the source",element.getSourceRepoPath()));
                }
            } else {
                mutableVfsItem = moveFile((VfsFile) sourceItem, targetItem, targetRrp, status, context);
            }
        }
        return mutableVfsItem;
    }

    private MutableVfsFile moveFile(VfsFile sourceItem, VfsItem targetItem, RepoRepoPath<LocalRepo> targetRrp, MoveMultiStatusHolder status, MoveCopyContext context) {
        LocalRepo targetRepo = targetRrp.getRepo();
        if (!(targetRepo.getDescriptor() instanceof TrashRepoDescriptor)) {
            overrideTargetFileIfExist(targetRrp, targetItem, targetRepo, status);
        }
        // Copy or move file
        MutableVfsFile mutableVfsFile = copyFile(sourceItem, targetItem, targetRrp, context);
        if (!context.isCopy()) {
            deleteFile(sourceItem.getRepoPath());
        }
        status.artifactMoved();
        return mutableVfsFile;
    }

    private void deleteFile(RepoPath repoPath) {
        LocalRepo localRepo = repositoryService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        MutableVfsFile mutableSourceFile = localRepo != null ? localRepo.getMutableFile(repoPath) : null;
        if (mutableSourceFile != null) {
            mutableSourceFile.delete(new DeleteContext(mutableSourceFile.getRepoPath()).triggeredByMove());
        } else {
            log.error("About to delete {} but it is null", repoPath);
        }
    }

    protected MutableVfsFile copyFile(VfsFile sourceItem, VfsItem targetItem, RepoRepoPath<LocalRepo> targetRrp, MoveCopyContext context) {
        if (targetItem == null) {
            log.debug("Copying file {} to {}", sourceItem.getRepoPath(), targetRrp.getRepoPath());
        } else {
            log.debug("Copying file {} to Overriding file {} already exist", sourceItem.getRepoPath(), targetRrp.getRepoPath());
        }
        MutableVfsFile targetFile = targetRrp.getRepo().createOrGetFile(targetRrp.getRepoPath());
        // Copy the info and the properties only (stats and watches are not required)
        targetFile.tryUsingExistingBinary(sourceItem.getSha1(), sourceItem.getSha2(), sourceItem.getMd5(), sourceItem.length());
        targetFile.fillInfo(sourceItem.getInfo());
        Properties newProperties = mergeProperties(sourceItem.getProperties(), context);
        targetFile.setProperties(newProperties);
        return targetFile;
    }


    private Properties mergeProperties(Properties properties, MoveCopyContext context) {
        PropertiesImpl newProperties = new PropertiesImpl();
        for (String key : properties.keySet()) {
            newProperties.putAll(key, properties.get(key));
        }
        if (context.getRemoveProps() != null) {
            context.getRemoveProps().forEach(newProperties::removeAll);
        }
        Properties addProperties = context.getAddProps();
        if (addProperties != null) {
            for (String key : addProperties.keySet()) {
                properties.putAll(key, addProperties.get(key));
            }
        }
        return newProperties;
    }

    private void overrideTargetFileIfExist(RepoRepoPath<LocalRepo> targetRrp, VfsItem targetItem, LocalRepo targetRepo,
            MoveMultiStatusHolder status) {
        MutableVfsItem newTargetItem = targetRepo.getMutableFsItem(targetRrp.getRepoPath());
        if (targetItem != null) {
            // target repository already contains file or folder with the same name, delete it
            log.debug("File {} already exists in target repository. Overriding.", targetRrp.getRepoPath());
            if (newTargetItem != null) {
                newTargetItem.delete(new DeleteContext(targetItem.getRepoPath()).triggeredByMove());
            } else {
                status.error(String.format("Failed to create mutable item for path: %s", targetRrp.getRepoPath()), log);
            }
            saveSession();
        }
    }

    private void saveSession() {
        StorageSessionHolder.getSession().save();
    }

    private MutableVfsFolder shallowCopyDirectory(VfsFolder sourceFolder, VfsItem targetItem, RepoRepoPath<LocalRepo> targetRrp) {
        LocalRepo targetRepo = targetRrp.getRepo();
        RepoPath targetRepoPath = InternalRepoPathFactory.create(targetRepo.getKey(),
                targetRrp.getRepoPath().getPath());

        if (targetItem == null) {
            log.debug("Creating target folder {}", targetRepoPath);
        } else {
            log.debug("Target folder {} already exist", targetRepoPath);
        }
        MutableVfsFolder targetFolder = targetRepo.createOrGetFolder(targetRepoPath);
        // copy relevant metadata from source to target
        log.debug("Copying folder metadata to {}", targetRepoPath);
        targetFolder.fillInfo(sourceFolder.getInfo());
        targetFolder.setProperties(sourceFolder.getProperties());
        return targetFolder;
    }
}
