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

package org.artifactory.environment.converter.shared;

import org.artifactory.common.ArtifactoryHome;

import java.io.File;
import java.io.FileFilter;

/**
 * A {@link FileFilter} that accepts files called:
 * artifactory.gpg.public
 * artifactory.gpg.private
 * artifactory.ssh.public
 * artifactory.ssh.private
 * artifactory.config.* (but not config.import)
 *
 * @author Dan Feldman
 */
public class MiscEtcFilesFilter implements FileFilter {

    private static final String CONFIG_XMLS = "artifactory.config.*";

    @Override
    public boolean accept(File pathname) {
        return pathname != null
                && (pathname.getAbsolutePath().contains(ArtifactoryHome.ARTIFACTORY_GPG_PUBLIC_KEY)
                || pathname.getAbsolutePath().contains(ArtifactoryHome.ARTIFACTORY_GPG_PRIVATE_KEY)
                || pathname.getAbsolutePath().contains(ArtifactoryHome.ARTIFACTORY_SSH_PUBLIC_KEY)
                || pathname.getAbsolutePath().contains(ArtifactoryHome.ARTIFACTORY_SSH_PRIVATE_KEY)
                // Don't copy over config.import files, don't want to accidentally overwrite stuff
                || (pathname.getAbsolutePath().contains(CONFIG_XMLS) && !pathname.getAbsolutePath().contains(".import")));
    }
}
