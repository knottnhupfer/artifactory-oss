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

package org.artifactory.maven.versioning;

import org.artifactory.maven.MavenMetaDataInfo;

/**
 * A {@link MavenMetadataVersionComparator} that determines the latest and release versions based on a versions comparator.
 *
 * @author Yossi Shaul
 */
public class VersionNameMavenMetadataVersionComparator implements MavenMetadataVersionComparator {

    private static final VersionNameMavenMetadataVersionComparator instance =
            new VersionNameMavenMetadataVersionComparator();

    /**
     * The more specific, strings only version comparator
     */
    private final MavenVersionComparator comparator = new MavenVersionComparator();

    public static VersionNameMavenMetadataVersionComparator get() {
        return instance;
    }

    @Override
    public int compare(MavenMetaDataInfo o1, MavenMetaDataInfo o2) {
        return comparator.compare(o1.getVersion(), o2.getVersion());
    }
}
