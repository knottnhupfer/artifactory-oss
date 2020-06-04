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
 * Copies ssh public and private keys when upgrading to Artifactory 5
 * @author nadavy
 */
public class NoNfsSshConverter extends NoNfsBasicEnvironmentConverter {
    private static final Logger log = LoggerFactory.getLogger(NoNfsEnvironmentMimeTypeConverter.class);

    @Override
    public boolean isInterested(ArtifactoryHome home, CompoundVersionDetails source, CompoundVersionDetails target) {
        return isUpgradeTo5x(source, target);
    }

    @Override
    protected void doConvert(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        File sshDir;
        if (clusterHomeDir != null) {
            sshDir = new File(clusterHomeDir, "ha-etc/ssh");
        } else {
            sshDir = new File(artifactoryHome.getEtcDir(), "ssh");
        }
        if (sshDir.exists()) {
            File publicSshFile = new File(sshDir, "artifactory.ssh.public");
            File privateSshFile = new File(sshDir, "artifactory.ssh.private");
            moveFiles(artifactoryHome, publicSshFile, privateSshFile);
        } else {
            log.debug("SSH converter was set to run but source directory does not exist: {}", sshDir.getAbsolutePath());
        }
    }

    @Override
    protected void doAssertConversionPreconditions(ArtifactoryHome artifactoryHome, File clusterHomeDir) {
        File sshDir;
        if (clusterHomeDir != null) {
            sshDir = new File(clusterHomeDir, "ha-etc/ssh");
        } else {
            sshDir = new File(artifactoryHome.getEtcDir(), "ssh");
        }
        if (sshDir.exists()) {
            assertFilePermissions(new File(sshDir, "artifactory.ssh.public"));
            assertFilePermissions(new File(sshDir, "artifactory.ssh.private"));
        }
        assertTargetFilePermissions(new File(artifactoryHome.getSecurityDir(), "artifactory.ssh.public"));
        assertTargetFilePermissions(new File(artifactoryHome.getSecurityDir(), "artifactory.ssh.private"));
    }

    private void moveFiles(ArtifactoryHome artifactoryHome, File publicSshFile, File privateSshFile) {
        log.info("Starting ssh conversion: copy ssh files to security dir");
        if (publicSshFile.exists()) {
            safeCopyFile(publicSshFile, new File(artifactoryHome.getSecurityDir(), "artifactory.ssh.public"));
        }
        if (privateSshFile.exists()) {
            safeCopyFile(privateSshFile, new File(artifactoryHome.getSecurityDir(), "artifactory.ssh.private"));
        }
        log.info("Finished ssh conversion: copy ssh files to security dir");
    }
}