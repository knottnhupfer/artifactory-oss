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

package org.artifactory.configuration.helper;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.CompoundVersionDetails;

import java.io.File;
import java.util.Date;

/**
 * Author: gidis
 */
public class MockArtifactoryHome extends ArtifactoryHome {
    private boolean isHA = false;
    private File home = null;

    public MockArtifactoryHome(File home) {
        super(home);
    }

    @Override
    public CompoundVersionDetails getRunningArtifactoryVersion() {
        return new CompoundVersionDetails(ArtifactoryVersion.getCurrent(),"MOCK", new Date().getTime());
    }

    @Override
    public File getArtifactoryHaNodePropertiesFile() {
        return isHA ? new File(home, ".artifactory-ha/homes/pom/etc/" + ARTIFACTORY_HA_NODE_PROPERTIES_FILE) : super.getArtifactoryHaNodePropertiesFile();
    }
}
