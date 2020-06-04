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

package org.artifactory.maven.snapshot;

import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.storage.fs.tree.ItemNode;

import java.util.Comparator;

/**
 * @author Gidi Shabat
 */
public interface SnapshotComparator extends Comparator<ItemNode> {

    int compare(Snapshot o1, Snapshot o2);

    int compare(SnapshotVersion o1, SnapshotVersion o2);

    int compare(ModuleInfo folderItemModuleInfo, ModuleInfo latestSnapshotVersion);
}
