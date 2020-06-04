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
import org.artifactory.environment.converter.shared.MiscEtcFilesFilter;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Copies over leftover files from {cluster.home}/ha-etc
 *
 * @author Dan Feldman
 */
public class NoNfsMisEtcConverter extends NoNfsBasicEnvironmentConverter {
    private static final Logger log = LoggerFactory.getLogger(NoNfsMisEtcConverter.class);

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return isUpgradeTo5x(source, target);
    }

    @Override
    protected void doConvert(ArtifactoryHome home, File clusterHomeDir) {
        if (clusterHomeDir != null && clusterHomeDir.exists()) {
            File etc = new File(clusterHomeDir, "ha-etc");
            if (etc.exists()) {
                doCopy(home, etc);
            }
            File etcSecurity = new File(clusterHomeDir, "ha-etc/security");
            if (etcSecurity.exists()) {
                doCopy(home, etcSecurity);
            }
        }
    }

    @Override
    protected void doAssertConversionPreconditions(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        if (clusterHomeDir != null && clusterHomeDir.exists()) {
            File securityDir = artifactoryHome.getSecurityDir();
            doAssert(securityDir, new File(clusterHomeDir, "ha-etc"));
            doAssert(securityDir, new File(clusterHomeDir, "ha-etc/security"));
        }
    }

    private void doCopy(ArtifactoryHome home, File etcSecurity) {
        File[] miscFiles = etcSecurity.listFiles(new MiscEtcFilesFilter());
        if (miscFiles != null && miscFiles.length > 0) {
            log.info("Starting environment conversion: copy ha-etc files");
            for (File miscFile : miscFiles) {
                File securityDir = home.getSecurityDir();
                safeCopyFile(miscFile, new File(securityDir, miscFile.getName()));
            }
            log.info("Finished environment conversion: copy ha-etc files");
        }
    }

    private void doAssert(File securityDir, File etcSecurity) {
        if (etcSecurity.exists()) {
            File[] miscFiles = etcSecurity.listFiles(new MiscEtcFilesFilter());
            if (miscFiles != null) {
                for (File miscFile : miscFiles) {
                    assertFilePermissions(miscFile);
                    assertTargetFilePermissions(new File(securityDir, miscFile.getName()));
                }
            }
        }
    }
}
