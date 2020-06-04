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

package org.artifactory.repo.service.flexible.validators;

import org.apache.http.HttpStatus;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.sapi.fs.VfsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gidis
 */
public class BasicMoveCopyValidator implements MoveCopyValidator {

    private static final Logger log = LoggerFactory.getLogger(BasicMoveCopyValidator.class);

    @Override
    public boolean validate(MoveCopyItemInfo element, MoveMultiStatusHolder status, MoveCopyContext context) {
        // RepoPath fromRepoPath = context.getFinalSourceRepoPath();
        String operation = context.isCopy() ? "copy" : "move";
        RepoPath sourceRepoPath = element.getSourceRepoPath();

        // Make sure that source exist if bot, stop avery thing
        if (element.getSourceItem() == null) {
            throw new IllegalArgumentException("Could not find item at " + sourceRepoPath);
        }

        // Do nothing if destination is equal to source
        if (sourceRepoPath.equals(element.getTargetRepoPath())) {
            status.error(String.format("Skipping %s %s: Destination and source are the same", operation, sourceRepoPath), log);
            return false;
        }

        // Make sure that the target is no cache
        RepoRepoPath<LocalRepo> targetRrp = element.getTargetRrp();
        if (targetRrp.getRepo().isCache()) {
            throw new IllegalArgumentException(String.format("Target repository %s is a cache repository. %s to cache" +
                    " repositories is not allowed.", element.getTargetRepoPath().getRepoKey(), operation));
        }

        // If target exist then don't allow moving/copying folder to file
        VfsItem targetItem = element.getTargetItem();
        if (targetItem != null) {
            VfsItem sourceItem = element.getSourceItem();
            if (targetItem.isFile() && sourceItem.isFolder()) {
                String msg = "Can't " + operation + " file to existing folder '" + element.getTargetRepoPath() + "'.";
                status.error(msg, HttpStatus.SC_BAD_REQUEST, log);
                return false;
            }
            if(! context.isUnixStyleBehavior()) {
                if (targetItem.isFolder() && sourceItem.isFile()) {
                    String msg = "Can't " + operation + " folder to existing file '" + element.getTargetRepoPath() + "'.";
                    status.error(msg, HttpStatus.SC_BAD_REQUEST, log);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isInterested(MoveCopyItemInfo itemInfo, MoveCopyContext config) {
        return true;
    }


}
