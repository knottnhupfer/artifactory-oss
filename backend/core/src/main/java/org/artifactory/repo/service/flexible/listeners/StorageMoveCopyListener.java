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

package org.artifactory.repo.service.flexible.listeners;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.interceptor.StorageInterceptors;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;

/**
 * @author gidis
 */
public class StorageMoveCopyListener implements MoveCopyListeners {
    private StorageInterceptors storageInterceptors;

    public StorageMoveCopyListener(StorageInterceptors storageInterceptors) {
        this.storageInterceptors = storageInterceptors;
    }

    @Override
    public void notifyAfterMoveCopy(MoveCopyItemInfo itemInfo, MoveMultiStatusHolder status, MoveCopyContext context) {
        if (!context.isDryRun()) {
            if (itemInfo.getMutableTargetItem().isFile()) {
                InterceptorMoveCopyContext ctx = new InterceptorMoveCopyContext(itemInfo.getTargetOriginalFileInfo());
                if (context.isCopy()) {
                    storageInterceptors.afterCopy(itemInfo.getSourceItem(), itemInfo.getMutableTargetItem(), status, new PropertiesImpl(), ctx);
                } else {
                    storageInterceptors.afterMove(itemInfo.getSourceItem(), itemInfo.getMutableTargetItem(), status, new PropertiesImpl(), ctx);
                }
            } else {
                if (shouldRemoveSourceFolder((VfsFolder) itemInfo.getSourceItem(), context, status)) {
                    if (context.isCopy()) {
                        storageInterceptors.afterCopy(itemInfo.getSourceItem(), itemInfo.getMutableTargetItem(), status, new PropertiesImpl(), new InterceptorMoveCopyContext());
                    } else {
                        storageInterceptors.afterMove(itemInfo.getSourceItem(), itemInfo.getMutableTargetItem(), status, new PropertiesImpl(), new InterceptorMoveCopyContext());
                    }
                }
            }
        }
    }

    /**
     * If not in a dry run, If not pruning empty folders (if true it will happen at a later stage),
     * If not copying (no source removal when copying), If not on the root item (a repo),
     * If not containing any children and folders or artifacts were moved.
     */
    protected boolean shouldRemoveSourceFolder(VfsFolder sourceFolder, MoveCopyContext context, MoveMultiStatusHolder status) {
        return !context.isDryRun() && !context.isCopy() && !sourceFolder.getRepoPath().isRoot() && !sourceFolder.hasChildren()
                && !context.isPruneEmptyFolders() && (status.getMovedFoldersCount() != 0 || status.getMovedArtifactsCount() != 0);
    }

    @Override
    public void notifyBeforeMoveCopy(MoveCopyItemInfo itemInfo, MoveMultiStatusHolder status, MoveCopyContext context) {
        if (context.isCopy()) {
            storageInterceptors.beforeCopy(itemInfo.getSourceItem(),
                    itemInfo.getTargetRepoPath(), status, itemInfo.getSourceItem().getProperties());
        } else {
            storageInterceptors.beforeMove(itemInfo.getSourceItem(), itemInfo.getTargetRepoPath(), status,
                    itemInfo.getSourceItem().getProperties());
        }
    }

    @Override
    public boolean isInterested(MoveCopyItemInfo itemInfo, MoveCopyContext context) {
        return true;
    }
}
