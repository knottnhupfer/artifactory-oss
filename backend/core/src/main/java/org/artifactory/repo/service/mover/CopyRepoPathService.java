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
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
public class CopyRepoPathService extends RepoPathMover {

    @Override
    public void executeOperation(MoveMultiStatusHolder status, MoverConfig moverConfig) {
        moveOrCopy(status, moverConfig);
    }

    @Override
    public void handleMoveOrCopy(MoveMultiStatusHolder status, MoverConfig moverConfig,
                                 VfsItem sourceItem, RepoRepoPath<LocalRepo> targetRrp) {
        DefaultRepoPathCopier defaultRepoPathCopier = new DefaultRepoPathCopier(status, moverConfig);
        if (moverConfig.isAtomic()) {
            defaultRepoPathCopier.moveOrCopy(sourceItem, targetRrp);
        } else {
            defaultRepoPathCopier.moveOrCopyMultiTx(sourceItem, targetRrp);
        }
    }

    @Override
    protected String operationType() {
        return "copy";
    }
}
