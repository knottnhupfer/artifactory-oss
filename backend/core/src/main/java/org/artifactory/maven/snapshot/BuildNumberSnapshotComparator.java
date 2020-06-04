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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.storage.fs.tree.ItemNode;

/**
 * @author Gidi Shabat
 */
public class BuildNumberSnapshotComparator implements SnapshotComparator {

    private static final BuildNumberSnapshotComparator instance = new BuildNumberSnapshotComparator();

    /**
     * The more specific, strings only version comparator
     */
    public static BuildNumberSnapshotComparator get() {
        return instance;
    }

    @Override
    public int compare(ItemNode o1, ItemNode o2) {
        int buildNumber1 = MavenNaming.getUniqueSnapshotVersionBuildNumber(o1.getName());
        int buildNumber2 = MavenNaming.getUniqueSnapshotVersionBuildNumber(o2.getName());
        return buildNumber1 - buildNumber2;
    }

    @Override
    public int compare(Snapshot o1, Snapshot o2) {
        int buildNumber1 = o1.getBuildNumber();
        int buildNumber2 = o2.getBuildNumber();
        return buildNumber1 - buildNumber2;
    }

    @Override
    public int compare(SnapshotVersion o1, SnapshotVersion o2) {
        int buildNumber1 = Integer.valueOf(StringUtils.substringAfterLast(o1.getVersion(), "-"));
        int buildNumber2 = Integer.valueOf(StringUtils.substringAfterLast(o2.getVersion(), "-"));
        return buildNumber1 - buildNumber2;
    }

    @Override
    public int compare(ModuleInfo folderItemModuleInfo, ModuleInfo latestSnapshotVersion) {
        int folderItemBuildNumber = Integer.parseInt(StringUtils.substringAfter(
                folderItemModuleInfo.getFileIntegrationRevision(), "-"));
        int latestSnapshotVersionBuildNumber = Integer.parseInt(StringUtils.substringAfter(
                latestSnapshotVersion.getFileIntegrationRevision(), "-"));
        return folderItemBuildNumber - latestSnapshotVersionBuildNumber;
    }
}
