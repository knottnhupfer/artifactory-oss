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

package org.artifactory.environment.converter.shared.version.v1;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Gidi Shabat
 */
public class NoNfsPluginsConverter extends NoNfsBasicEnvironmentConverter {
    private static final Logger log = LoggerFactory.getLogger(NoNfsPluginsConverter.class);

    @Override
    protected void doConvert(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        if (clusterHomeDir != null) {
            log.info("Starting environment conversion: copy plugins folder from cluster home to node home");
            safeMoveDirectory(clusterHomeDir, artifactoryHome.getPluginsDir());
            log.info("Finished environment conversion: copy plugins folder from cluster home to node home");
        }
    }

    @Override
    protected void doAssertConversionPreconditions(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        if (clusterHomeDir != null) {
            File oldHaFile = getOldHaFile(clusterHomeDir, artifactoryHome.getPluginsDir());
            File[] listFiles = oldHaFile.listFiles();
            if (listFiles != null) {
                for (File candidateFile : listFiles) {
                    assertFilePermissions(oldHaFile);
                    assertTargetFilePermissions(
                            new File(artifactoryHome.getPluginsDir(), candidateFile.getName()));
                }
            }
        }
    }

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return isUpgradeTo5x(source, target);
    }
}