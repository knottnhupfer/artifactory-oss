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
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsFolder;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;

/**
 * @author Chen Keinan
 */
public class DefaultRepoPathCopier extends BaseRepoPathMover {

    protected DefaultRepoPathCopier(MoveMultiStatusHolder status, MoverConfig moverConfig) {
        super(status, moverConfig);
    }

    @Override
    protected void beforeOperationOnFolder(VfsItem sourceItem, RepoPath targetRepoPath) {
        storageInterceptors.beforeCopy(sourceItem, targetRepoPath, status, properties);
    }

    @Override
    public void beforeOperationOnFile(VfsItem sourceItem, RepoPath targetRepoPath) {
        storageInterceptors.beforeCopy(sourceItem, targetRepoPath, status, properties);
    }

    @Override
    protected void afterOperationOnFolder(VfsItem sourceItem, RepoRepoPath<LocalRepo> targetRrp,
                                          VfsFolder targetFolder) {
        afterFolderCopy(sourceItem, targetRrp, targetFolder, properties);
    }

    @Override
    public void operationOnFile(VfsFile sourceItem, RepoRepoPath<LocalRepo> targetRrp, InterceptorMoveCopyContext ctx) {
        copyFile(sourceItem, targetRrp, properties, ctx);
    }

    @Override
    public VfsItem getSourceItem(VfsItem sourceItem) {
        return sourceItem;
    }
}
