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

import org.jfrog.common.archive.ArchiveType;
import org.artifactory.api.download.FolderDownloadResult;
import org.artifactory.api.download.FolderDownloadService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.download.FolderDownloadException;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadFolder;
import org.jfrog.client.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.StreamingOutput;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;

/**
 * Downloads a folder in the requested format.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DownloadFolderArchiveService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DownloadFolderArchiveService.class);

    @Autowired
    private FolderDownloadService folderDownloadService;

    @Autowired
    private AuthorizationService authService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (authService.isAnonymous() && !folderDownloadService.getFolderDownloadConfig().isEnabledForAnonymous()) {
            response.error("You must be logged in to download a folder or repository.").responseCode(SC_FORBIDDEN);
        } else {
            DownloadFolder folderToDownload = new DownloadFolder(request);
            String repoKey = folderToDownload.getRepoKey();
            String path = folderToDownload.getPath();
            RepoPath pathToDownload = RepoPathFactory.create(repoKey, path);
            respondWithArchiveStream((StreamRestResponse) response, getArchiveType(folderToDownload.getArchiveType()),
                    pathToDownload, folderToDownload.getIncludeChecksumFiles());
        }
    }

    private void respondWithArchiveStream(StreamRestResponse response, ArchiveType archiveType, RepoPath folder, Boolean includeChecksumFiles) {
        try {
            FolderDownloadResult result = folderDownloadService.downloadFolder(folder, archiveType, includeChecksumFiles);
            response.setDownloadFile(createArchiveFileName(folder, archiveType));
            response.setDownload(true);
            response.iModel((StreamingOutput) result::accept);
        } catch (FolderDownloadException fde) {
            response.responseCode(fde.getCode()).error(fde.getMessage());
            log.error("Error while executing folder download on path: {} --> {}", folder.toPath(), fde.getMessage());
            log.debug("Caught exception in folder download execution on path " + folder.toPath(), fde);
        } catch (Exception e) {
            response.error(e.getMessage());
            log.error("Error while executing folder download on path: {} --> {}", folder.toPath(), e.getMessage());
            log.debug("Caught exception in folder download execution on path " + folder.toPath(), e);
        }
    }

    //Defaults to zip if no input from user
    private ArchiveType getArchiveType(ArchiveType archiveType) {
        return archiveType != null ? archiveType : ArchiveType.ZIP;
    }

    /**
     * <Folder name>.<archive extension>
     */
    private String createArchiveFileName(RepoPath path, ArchiveType archiveType) {
        return (path.isRoot() ? path.getRepoKey() : PathUtils.getLastPathElement(path.getPath())) + "." + archiveType.value();
    }
}
