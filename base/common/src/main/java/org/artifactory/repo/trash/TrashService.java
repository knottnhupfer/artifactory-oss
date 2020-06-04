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

package org.artifactory.repo.trash;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.common.StatusHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.spring.ReloadableBean;
import org.artifactory.storage.GCCandidate;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Shay Yaakov
 */
public interface TrashService extends ReloadableBean {

    String TRASH_KEY = "auto-trashcan";
    String PROP_TRASH_TIME = "trash.time";
    String PROP_DELETED_BY = "trash.deletedBy";
    String PROP_ORIGIN_REPO = "trash.originalRepository";
    String PROP_ORIGIN_REPO_TYPE = "trash.originalRepositoryType";
    String PROP_ORIGIN_PATH = "trash.originalPath";
    String PROP_RESTORED_TIME = "trash.restoredTime";

    /**
     * Copies the given repoPath to the trashcan if it's a file, overriding properties if it's a folder.
     * Usually this will be called after all beforeDelete events and before any afterDelete events.
     *
     * @param repoPath The repo path to trash
     */
    void copyToTrash(RepoPath repoPath);


    /**
     * Restores an bulk of items in single transaction to it's original repository path.
     *
     * @param repoPath    The repo path to restore
     * @param restoreRepo The restore repo key
     * @param restorePath The restore repo path (can be a file for renaming or a folder)
     */
    //TORE: [by YS] repo path repo key must be the trash can repo key - this should be the responsibility of the
    // service and not the calling method!
    MoveMultiStatusHolder restoreBulk(RepoPath repoPath, String restoreRepo, String restorePath);

    /**
     * Restores an bulk of items in single transaction to it's original repository path.
     *
     * @param repoPath        The repo path to restore
     * @param restoreRepo     The restore repo key
     * @param restorePath     The restore repo path (can be a file for renaming or a folder)
     * @param transactionSize Number of items to be committed in single transaction
     */
    MoveMultiStatusHolder restoreBulk(RepoPath repoPath, String restoreRepo, String restorePath, int transactionSize);

    /**
     * Restores an item from the trashcan to it's original repository path.
     *
     * @param repoPath    The repo path to restore
     * @param restoreRepo The restore repo key
     * @param restorePath The restore repo path (can be a file for renaming or a folder)
     */
    MoveMultiStatusHolder restore(RepoPath repoPath, String restoreRepo, String restorePath);

    /**
     * Removes all the item from the trashcan
     */
    StatusHolder empty();

    List<GCCandidate> getGCCandidatesFromTrash();

    boolean isTrashcanEnabled();

    void undeployFromTrash(GCCandidate gcCandidate);

    /**
     *
     * @param retentionDaysTimestamp
     * @return
     */
    void validateRetentionPeriodTimestamp(long retentionDaysTimestamp);
}