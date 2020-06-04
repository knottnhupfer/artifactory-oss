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

package org.artifactory.version;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Base class for tests shared by all SubConfigElementVersions.
 *
 * @author Yossi Shaul
 */
public abstract class VersionTest {

    @Test
    public void versionsCoverage() {
        // Check that all Artifactory versions are covered by a DB version
        VersionWrapper[] versions = getEnums();
        Assert.assertTrue(versions.length > 0);
        assertEquals(versions[0].getVersion(), getFirstSupportedArtifactoryVersion(),
                "First version should start at first supported Artifactory version");
        for (int i = 0; i < versions.length; i++) {
            VersionWrapper version = versions[i];
            if (i + 1 < versions.length) {
                assertTrue(version.getVersion().before(versions[i + 1].getVersion()),
                        "Versions should have full coverage but a hole between " + version + " and " + versions[i + 1] +
                                " exists in the list of "+version.getClass());
            }
        }
    }

    private ArtifactoryVersion getFirstSupportedArtifactoryVersion() {
        return ArtifactoryVersionProvider.v122rc0.get();
    }

    protected abstract VersionWrapper[] getEnums();
}
