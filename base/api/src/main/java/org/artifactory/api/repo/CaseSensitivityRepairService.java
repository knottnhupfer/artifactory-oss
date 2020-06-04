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

package org.artifactory.api.repo;

import org.artifactory.api.rest.artifact.RepairPathConflictsResult;
import org.artifactory.fs.ItemInfo;

import java.util.List;

/**
 * @author Yoav Luft
 */
public interface CaseSensitivityRepairService {
    /**
     * Finds all paths rooted at basePath that have letter case conflicting names.
     *
     * @param path root path for search, only elements belonging to it's subtree will be searched
     * @return {@link RepairPathConflictsResult} element containing all found conflicts, their count and status message
     */
    RepairPathConflictsResult findPathConflicts(String path);

    /**
     * Attempts to automatically fix all parent-child path case conflicts for the given list of paths
     *
     * @param conflicts paths to be fixed
     * @return {@link RepairPathConflictsResult} element containing all found conflicts, their count, count of repaired
     * paths and a status message
     */
    RepairPathConflictsResult fixCaseConflicts(List<RepairPathConflictsResult.PathConflict> conflicts);

    List<ItemInfo> getOrphanItems(String path);
}
