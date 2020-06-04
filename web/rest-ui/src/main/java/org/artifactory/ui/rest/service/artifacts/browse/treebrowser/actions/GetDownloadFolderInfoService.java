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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.api.download.FolderDownloadInfo;
import org.artifactory.api.download.FolderDownloadService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadFolder;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;

/**
 * Called when user chooses 'download folder' from UI - initial check that request does not exceed allowed
 * file size limit and max files download limit.
 * Also returns info about any artifacts that will be blocked due to xray restrictions.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetDownloadFolderInfoService implements RestService {

    @Autowired
    private RepositoryService repoService;

    @Autowired
    private FolderDownloadService folderDownloadService;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepoPath folderToDownload = RequestUtils.getPathFromRequest(request);
        FolderDownloadConfigDescriptor config = folderDownloadService.getFolderDownloadConfig();
        if (!repoService.getItemInfo(folderToDownload).isFolder()) {
            response.error("Path " + folderToDownload.toPath() + " is not a folder.").responseCode(SC_BAD_REQUEST);
        } else if (!config.isEnabled()) {
            response.error("Downloading folders as archive was disabled by your system admin.");
        } else if (!authService.canRead(folderToDownload)) {
            response.error("You don't have the required permissions to download this folder.").responseCode(SC_FORBIDDEN);
        } else {
            respond(folderToDownload, response, config);
        }
    }

    private void respond(RepoPath pathToDownload, RestResponse response, FolderDownloadConfigDescriptor config) {
        FolderDownloadInfo folderInfo = folderDownloadService.collectFolderInfo(pathToDownload);
        double folderSize = folderInfo.getSizeMb();
        int maxDownloadSize = config.getMaxDownloadSizeMb();
        long folderFileCount = folderInfo.getTotalFiles();
        boolean blockedByXray = folderInfo.isBlockedByXray();
        long maxFileCount = config.getMaxFiles();
        if (folderSize > maxDownloadSize) {
            //We don't deduct xray blocked artifacts from total size for now - i'm not convinced the complexity is worth it.
            response.error("Folder " + pathToDownload.getPath() + "'s size(" + folderSize + "MB) exceeds the max " +
                    "allowed download size(" + maxDownloadSize + "MB).").responseCode(SC_FORBIDDEN);
        } else if (folderFileCount> maxFileCount) {
            response.error("Folder " + pathToDownload.getPath() + "'s artifact count(" + folderFileCount + ") exceeds" +
                    " the max allowed artifact count(" + maxFileCount + ").").responseCode(SC_FORBIDDEN);
        } else if (blockedByXray) {
            response.error("Folder '" + pathToDownload.getPath() + "' contains blocked artifacts by Xray")
                    .responseCode(SC_FORBIDDEN);
        } else {
            response.iModel(new DownloadFolder(folderInfo));
        }
    }
}