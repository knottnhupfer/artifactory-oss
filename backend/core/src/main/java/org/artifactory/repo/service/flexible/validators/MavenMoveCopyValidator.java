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

package org.artifactory.repo.service.flexible.validators;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.maven.PomTargetPathValidator;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.flexible.MoveCopyItemInfo;
import org.artifactory.repo.service.flexible.context.MoveCopyContext;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author gidis
 */
public class MavenMoveCopyValidator implements MoveCopyValidator {
    private static final Logger log = LoggerFactory.getLogger(MavenMoveCopyValidator.class);

    @Override
    public boolean validate(MoveCopyItemInfo element, MoveMultiStatusHolder status, MoveCopyContext context) {
        RepoPath sourceRepoPath = element.getSourceRepoPath();
        LocalRepo targetRepo = element.getTargetRrp().getRepo();
        RepoPath targetRepoPath = element.getTargetRepoPath();
        String targetPath = targetRepoPath.getPath();
        VfsItem sourceItem = element.getSourceItem();
        // Snapshot/release policy is enforced only on files since it only has a meaning on files
        if (sourceItem.isFile() && !targetRepo.handlesReleaseSnapshot(targetPath)) {
            status.error("The repository '" + targetRepo.getKey() + "' rejected the path '" + targetPath
                    + "' due to a conflict with its snapshot/release handling policy.", HttpStatus.SC_BAD_REQUEST, log);
            return false;
        }

        if (sourceItem.isFile() && NamingUtils.isPom(sourceRepoPath.getPath()) && NamingUtils.isPom(targetPath) &&
                !((RealRepoDescriptor) targetRepo.getDescriptor()).isSuppressPomConsistencyChecks()) {
            ModuleInfo moduleInfo = targetRepo.getItemModuleInfo(targetPath);
            InputStream stream = null;
            try {
                stream = ((VfsFile) sourceItem).getStream();
                new PomTargetPathValidator(targetPath, moduleInfo).validate(stream, false);
            } catch (Exception e) {
                status.error("Validation failure for target path of pom: " + targetPath, HttpStatus.SC_BAD_REQUEST, e, log);
                return false;
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return true;
    }

    @Override
    public boolean isInterested(MoveCopyItemInfo element, MoveCopyContext context) {
        return element.getTargetRrp().getRepo().getDescriptor().getType().isMavenGroup();
    }
}
