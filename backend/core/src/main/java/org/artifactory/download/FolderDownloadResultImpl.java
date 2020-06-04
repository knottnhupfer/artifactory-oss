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

package org.artifactory.download;

import org.jfrog.common.archive.ArchiveType;
import org.artifactory.api.download.FolderDownloadResult;
import org.artifactory.api.download.FolderDownloadService;
import org.artifactory.repo.RepoPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Represents a ready-to-go folder download stream. created ease the use of StreamingOutput with the validation process
 * (exceptions thrown inside the stream's write() method arrive to late to act upon with the Response).
 *
 * NOTE: Holding this object also means you are holding a download slot in the {@link FolderDownloadService}'s semaphore!
 * It is automatically released when this object finishes writing the stream.
 * If you are discarding this object without invoking the {@link this#accept(OutputStream)} method,
 * use {@link this#releaseDownloadSlot()} or else you will clog the service's download counter!
 *
 * @author Dan Feldman
 */
public class FolderDownloadResultImpl implements FolderDownloadResult {
    private static final Logger log = LoggerFactory.getLogger(FolderDownloadResult.class);

    private final FolderDownloadServiceImpl service;
    private final RepoPath pathToDownload;
    private final ArchiveType archiveType;
    private final int maxDownloadSizeMb;
    private final long maxFiles;
    private final boolean includeChecksumFiles;

    protected FolderDownloadResultImpl(FolderDownloadServiceImpl service, RepoPath pathToDownload,
            ArchiveType archiveType, int maxDownloadSizeMb, long maxFiles, boolean includeChecksumFiles) {
        this.service = service;
        this.pathToDownload = pathToDownload;
        this.archiveType = archiveType;
        this.maxDownloadSizeMb = maxDownloadSizeMb;
        this.maxFiles = maxFiles;
        this.includeChecksumFiles = includeChecksumFiles;
    }

    @Override
    public void accept(OutputStream out) {
        try {
            log.debug("Folder Download Result starts streaming contents of folder {}", pathToDownload.toPath());
            service.process(out, pathToDownload, archiveType, maxDownloadSizeMb, maxFiles, includeChecksumFiles);
        } finally {
            log.debug("Done streaming folder {} content to output stream - releasing download slot.", pathToDownload.toPath());
            // Validation phase acquires a download slot, must be released here.
            service.releaseDownloadSlot();
        }
    }

    /**
     * NOTE: Holding this object also means you are holding a download slot in the {@link FolderDownloadService}'s semaphore!
     * It is automatically released when this object finishes writing the stream.
     * If you are discarding this object without invoking the {@link this#accept(OutputStream)} method,
     * use this method or else you will clog the service's download counter!
     */
    public void releaseDownloadSlot() {
        service.releaseDownloadSlot();
    }
}
