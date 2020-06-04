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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action;

import org.apache.commons.lang.StringUtils;
import org.jfrog.common.archive.ArchiveType;
import org.artifactory.api.download.FolderDownloadInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.artifact.BaseArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.ui.utils.RequestUtils;

/**
 * @author Dan Feldman
 */
public class DownloadFolder extends BaseArtifact {

    private static final String ARCHIVE_TYPE_QUERY_PARAM = "archiveType";
    private static final String INCLUDE_CHECKSUM_FILES_QUERY_PARAM = "includeChecksumFiles";

    private String sizeMB;
    private long filesUnderFolder;
    private ArchiveType archiveType;
    private Boolean includeChecksumFiles;

    public DownloadFolder(FolderDownloadInfo folderDownloadInfo) {
        this.sizeMB = String.format("%.2f", folderDownloadInfo.getSizeMb());
        this.filesUnderFolder = folderDownloadInfo.getTotalFiles();
    }

    public DownloadFolder(ArtifactoryRestRequest request) {
        RepoPath path = RequestUtils.getPathFromRequest(request);
        setPath(path.getPath());
        setRepoKey(path.getRepoKey());
        String archiveTypeParam = request.getQueryParamByKey(ARCHIVE_TYPE_QUERY_PARAM);
        if(StringUtils.isNotBlank(archiveTypeParam)) {
            this.archiveType = ArchiveType.fromValue(archiveTypeParam);
        } else {
            this.archiveType = ArchiveType.TARGZ;
        }
        includeChecksumFiles = Boolean.valueOf(request.getQueryParamByKey(INCLUDE_CHECKSUM_FILES_QUERY_PARAM));
    }

    public String getSizeMB() {
        return sizeMB;
    }

    public void setSizeMB(String sizeMB) {
        this.sizeMB = sizeMB;
    }

    public long getTotalFiles() {
        return filesUnderFolder;
    }

    public void setTotalFiles(long totalFiles) {
        this.filesUnderFolder = totalFiles;
    }

    public ArchiveType getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(ArchiveType archiveType) {
        this.archiveType = archiveType;
    }

    public Boolean getIncludeChecksumFiles() {
        return includeChecksumFiles;
    }
}