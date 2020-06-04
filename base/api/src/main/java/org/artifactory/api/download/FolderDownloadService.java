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

package org.artifactory.api.download;

import org.jfrog.common.archive.ArchiveType;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * A service for downloading entire directory contents as an archive.
 *
 * @author Dan Feldman
 */
public interface FolderDownloadService {

    /**
     * Tries to acquire an available download slot from the pool (which is limited by the amount specified in the config)
     *
     * @return true if slot acquired.
     */
    boolean getAvailableDownloadSlot();

    /**
     * Releases a download slot to the pool
     */
    void releaseDownloadSlot();

    /**
     * Collects all files under the current folder and wraps the given {@param out} with an ArchiveOutputStream of type
     * as required by {@param archiveType}. Files that the user doesn't have permission to read are filtered and not
     * included in the archive.
     *
     * NOTE: It is the callers responsibility to acquire a download slot with {@link this#getAvailableDownloadSlot()}
     * when starting the operation and release it with {@link this#releaseDownloadSlot()} when done (and closing)
     * <b>writing the stream</b>
     *
     * @param pathToDownload           - Folder or Repo to download
     * @param archiveType              - Type of archive to stream files in
     * @param includeChecksumFiles     - Whether to include .sha1 and .md5 files
     * @throws FolderExpectedException - On any error.
     */
    FolderDownloadResult downloadFolder(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws FolderExpectedException;

    /**
     * Same as process, but ignores the limits set by the download slots config but  take into consideration max size and max file count
     * This method is meant to be used by internal services and runs no validations, use with caution!
     * CAUTION: Also does not check if the service was enabled in the config and serves the request in any case.
     *
     * @param pathToDownload           - Folder or Repo to download
     * @param archiveType              - Type of archive to stream files in
     * @param includeChecksumFiles     - Whether to include .sha1 and .md5 files
     * @throws FolderExpectedException - On any error.
     */
    FolderDownloadResult downloadFolderNoLimit(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws FolderExpectedException;

    /**
     * Same as process, but ignores max size and max file count, but still take into considerations download slots
     * This method is meant to be used by internal services and runs no validations, use with caution!
     * CAUTION: Also does not check if the service was enabled in the config and serves the request in any case.
     *
     * @param pathToDownload           - Folder or Repo to download
     * @param archiveType              - Type of archive to stream files in
     * @param includeChecksumFiles     - Whether to include .sha1 and .md5 files
     * @throws FolderExpectedException - On any error.
     */
    FolderDownloadResult downloadFolderNoSizeLimit(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws FolderExpectedException;

    /**
     * Collects file count and total size for the requested folder for the UI to show
     *
     * @param folder folder to get info about
     * @return FolderDownloadInfo model with the info
     */
    FolderDownloadInfo collectFolderInfo(RepoPath folder);

    /**
     * @return the {@link FolderDownloadConfigDescriptor}
     */
    FolderDownloadConfigDescriptor getFolderDownloadConfig();
}
