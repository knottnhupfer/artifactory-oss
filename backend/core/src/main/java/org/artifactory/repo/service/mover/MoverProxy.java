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

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.common.StatusEntry;
import org.artifactory.md.Properties;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;
import org.artifactory.storage.fs.MutableVfsFolder;
import org.artifactory.storage.fs.lock.LockingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Chen Keinan
 */
@Service
public class MoverProxy implements LockableMover {
    private static final Logger log = LoggerFactory.getLogger(BaseRepoPathMover.class);

    @Override
    public void moveCopyFile(VfsItem source, RepoRepoPath<LocalRepo> targetRrp, BaseRepoPathMover repoPathMover, MoveMultiStatusHolder status) {

        RepoPath targetRepoPath = targetRrp.getRepoPath();
        StatusEntry lastError = status.getLastError();
        // call interceptors before operation
        repoPathMover.beforeOperationOnFile(source, targetRepoPath);
        if (status.getCancelException(lastError) != null) {
            return;
        }
        // Creating move/copy context before re-writing the target info
        InterceptorMoveCopyContext ctx = repoPathMover.createMoveCopyContext(targetRrp);
        repoPathMover.overrideTargetFileIfExist(targetRrp, targetRrp.getRepo());
        // copy or move file
        repoPathMover.operationOnFile((VfsFile) source, targetRrp, ctx);
        repoPathMover.saveSession();
        LockingHelper.removeLockEntry(source.getRepoPath());
    }

    @Override
    public VfsFolderRepo moveCopyFolder(VfsItem source, RepoRepoPath<LocalRepo> targetRrp, BaseRepoPathMover repoPathMove,
                                           MoveMultiStatusHolder status) {
        MutableVfsFolder mutableVfsFolder = null;
        if (repoPathMove.canMove(source, targetRrp)) {
            if (!repoPathMove.dryRun) {
                source = repoPathMove.getSourceItem(source);
                log.debug("start move/copy single tx on source {} and target {} ", source.getName(), targetRrp.getRepoPath().getPath());
                RepoPath targetRepoPath = targetRrp.getRepoPath();
                if (repoPathMove.validateDryRun(targetRrp)) return null;
                StatusEntry lastError = status.getLastError();
                //call interceptors before move or copy operation
                repoPathMove.beforeOperationOnFolder(source, targetRepoPath);
                if (status.getCancelException(lastError) != null) {
                    return null;
                }
                mutableVfsFolder = repoPathMove.shallowCopyDirectory((VfsFolder) source, targetRrp);
            }
            } else if (!repoPathMove.contains(targetRrp) ||
                    targetRrp.getRepo().getImmutableFsItem(targetRrp.getRepoPath()).isFile()) {
                // target repo doesn't accept this path and it doesn't already contain it OR the target is a file
                // so there is no point to continue to the children
                status.error("Cannot create/override the path '" + targetRrp.getRepoPath() + "'. " +
                        "Skipping this path and all its children.", log);
                return null;

            }
            repoPathMove.saveSession();
        return new VfsFolderRepo(mutableVfsFolder,targetRrp);
    }

    @Override
    public void postFolderProcessing(VfsItem sourceItem, VfsFolder targetFolder,RepoRepoPath<LocalRepo> targetRrp, BaseRepoPathMover repoPathMover, Properties properties,int numOfChildren) {
        repoPathMover.afterOperationOnFolder(sourceItem,targetRrp,targetFolder);
        repoPathMover.deleteAndReplicateAfterMoveEvent((MutableVfsFolder)targetFolder,numOfChildren);
    }
}
