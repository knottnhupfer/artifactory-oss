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

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.download.FolderDownloadInfo;
import org.artifactory.api.download.FolderDownloadResult;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.config.CentralConfigKey;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.fs.ItemInfo;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.version.CompoundVersionDetails;
import org.jfrog.common.archive.ArchiveType;
import org.jfrog.common.config.diff.DataDiff;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.descriptor.repo.SupportBundleRepoDescriptor.SUPPORT_BUNDLE_REPO_NAME;

/**
 * Serves requests for download of folders as archive
 *
 * @author Dan Feldman
 */
@Service
@Reloadable(beanClass = InternalFolderDownloadService.class, initAfter = {InternalRepositoryService.class},
        listenOn = CentralConfigKey.folderDownloadConfig)
public class FolderDownloadServiceImpl implements InternalFolderDownloadService {
    private static final Logger log = LoggerFactory.getLogger(FolderDownloadServiceImpl.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private AddonsManager addonsManager;

    private int maxDownloadSizeMb;
    private long maxFiles;
    private boolean serviceEnabled;
    private ConcurrentDownloadCounter concurrentDownloadCounter;

    @Override
    public void init() {
        FolderDownloadConfigDescriptor config = getFolderDownloadConfig();
        this.maxDownloadSizeMb = config.getMaxDownloadSizeMb();
        this.maxFiles = config.getMaxFiles();
        this.serviceEnabled = config.isEnabled();
        this.concurrentDownloadCounter = new ConcurrentDownloadCounter(config.getMaxConcurrentRequests());
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor, List<DataDiff<?>> configDiff) {
        FolderDownloadConfigDescriptor newConfig = getFolderDownloadConfig();
        FolderDownloadConfigDescriptor oldConfig = oldDescriptor.getFolderDownloadConfig();
        if (!newConfig.equals(oldConfig)) {
            this.maxDownloadSizeMb = newConfig.getMaxDownloadSizeMb();
            this.maxFiles = newConfig.getMaxFiles();
            this.serviceEnabled = newConfig.isEnabled();
            //Take the extra effort to compare old and new because resize is a synchronized method
            if (newConfig.getMaxConcurrentRequests() != oldConfig.getMaxConcurrentRequests()) {
                concurrentDownloadCounter.resize(newConfig.getMaxConcurrentRequests());
            }
        }
    }

    @Override
    public boolean getAvailableDownloadSlot() {
        if (!concurrentDownloadCounter.tryAcquire()) {
            log.debug("No available download slots, current available count in semaphore: {}",
                    concurrentDownloadCounter.availablePermits());
            return false;
        }
        return true;
    }

    @Override
    public void releaseDownloadSlot() {
        concurrentDownloadCounter.release();
    }

    @Override
    public FolderDownloadResult downloadFolder(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws FolderDownloadException {
        if (SUPPORT_BUNDLE_REPO_NAME.equals(pathToDownload.getRepoKey())) {
            return downloadFolderNoSizeLimit(pathToDownload, archiveType, includeChecksumFiles);
        }
        if (!serviceEnabled) {
            throw new FolderDownloadException("Download Folder functionality is disabled.", SC_FORBIDDEN);
        }
        assertPath(pathToDownload);
        FolderDownloadInfo info = collectFolderInfo(pathToDownload);
        assertXrayBlock(info, pathToDownload.toPath());
        assertLimitsNotExceeded(info, pathToDownload.toPath());
        if (!getAvailableDownloadSlot()) {
            throw new FolderDownloadException("There are too many folder download requests currently running. " +
                    "Try again later.", SC_BAD_REQUEST);
        }
        return new FolderDownloadResultImpl(this, pathToDownload, archiveType, maxDownloadSizeMb, maxFiles, includeChecksumFiles);
    }

    @Override
    public FolderDownloadResult downloadFolderNoLimit(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws FolderDownloadException {
        return new FolderDownloadResultImpl(this, pathToDownload, archiveType, maxDownloadSizeMb, maxFiles, includeChecksumFiles);
    }

    @Override
    public FolderDownloadResult downloadFolderNoSizeLimit(RepoPath pathToDownload, ArchiveType archiveType, boolean includeChecksumFiles) throws FolderDownloadException {
        if (!getAvailableDownloadSlot()) {
            throw new FolderDownloadException("There are too many folder download requests currently running. " +
                    "Try again later.", SC_BAD_REQUEST);
        }
        return new FolderDownloadResultImpl(this, pathToDownload, archiveType, 0, 0, includeChecksumFiles);
    }

    protected void process(OutputStream out, RepoPath pathToDownload, ArchiveType archiveType, int maxDownloadSizeMb,
            long maxFiles, boolean includeChecksumFiles) throws FolderDownloadException {
        ChecksumPolicy checksumPolicy = getChecksumPolicyIfNeeded(pathToDownload, includeChecksumFiles);
        FolderArchiveStreamer streamer = new FolderArchiveStreamer(pathToDownload, archiveType, maxDownloadSizeMb,
                maxFiles, checksumPolicy, includeChecksumFiles);
        streamer.go(out);
    }

    @Override
    public FolderDownloadInfo collectFolderInfo(RepoPath folder) {
        double sizeMb = StorageUnit.MB.fromBytes(fileService.getFilesTotalSize(folder));
        long artifactCount = repoService.getArtifactCount(folder);
        boolean isBlockedFolder = addonsManager.addonByType(XrayAddon.class).isBlockedFolder(folder);
        return new FolderDownloadInfo(sizeMb, artifactCount, isBlockedFolder);
    }

    @Override
    public FolderDownloadConfigDescriptor getFolderDownloadConfig() {
        return ContextHelper.get().beanForType(CentralConfigService.class).getDescriptor().getFolderDownloadConfig();
    }

    private void assertPath(RepoPath pathToDownload) throws FolderDownloadException {
        String folderPath = pathToDownload.toPath();
        ItemInfo itemInfo;
        Repo repo = repoService.repositoryByKey(pathToDownload.getRepoKey());
        if (repo == null) {
            throw new FolderDownloadException(pathToDownload.getRepoKey() + " is not a repository.", SC_NOT_FOUND);
        } else if (!repo.isLocal() && !repo.isCache()) {
            throw new FolderDownloadException("Downloading a folder or a repository's root is only available for local "
                    + "(or cache) repositories", SC_NOT_FOUND);
        }
        if (!pathToDownload.isRoot()) {
            //Not root, check folder status
            try {
                itemInfo = repoService.getItemInfo(pathToDownload);
            } catch (ItemNotFoundRuntimeException inf) {
                log.debug("", inf);
                throw new FolderDownloadException(
                        String.format("Path '%s' does not exist, aborting folder download", folderPath), SC_NOT_FOUND);
            }
            if (!itemInfo.isFolder()) {
                throw new FolderDownloadException(
                        String.format("Path '%s' is not a folder, aborting folder download", folderPath), SC_BAD_REQUEST);
            }
        }
        if (!authService.canRead(pathToDownload)) {
            throw new FolderDownloadException(
                    "You don't have the required permissions to download " + folderPath + ".", SC_FORBIDDEN);
        }
    }

    private void assertLimitsNotExceeded(FolderDownloadInfo info, String folderPath) throws FolderDownloadException {
        //We don't deduct xray blocked artifacts from total size for now - i'm not convinced the complexity is worth it.
        if (info.getSizeMb() > maxDownloadSizeMb) {
            throw new FolderDownloadException("Size of path '" + folderPath + "' ("
                    + String.format("%.2f", info.getSizeMb()) + "MB) exceeds the max allowed " +
                    "folder download size (" + maxDownloadSizeMb + "MB).", SC_BAD_REQUEST);
        } else if (info.getTotalFiles() > maxFiles) {
            throw new FolderDownloadException("Number of files under the path '" + folderPath + "' ("
                    + info.getTotalFiles() + ") exceeds the max allowed file count for folder download (" + maxFiles
                    + ").", SC_BAD_REQUEST);
        }
    }

    private void assertXrayBlock(FolderDownloadInfo info, String folderPath) throws FolderDownloadException {
        if (info.isBlockedByXray()) {
            throw new FolderDownloadException("Path '" + folderPath + "' contains blocked artifacts by Xray",
                    SC_FORBIDDEN);
        }
    }

    private ChecksumPolicy getChecksumPolicyIfNeeded(RepoPath pathToDownload, boolean includeChecksumFiles) {
        ChecksumPolicy checksumPolicy = null;
        if (includeChecksumFiles) {
            LocalRepo storingRepo = repoService.localOrCachedRepositoryByKey(pathToDownload.getRepoKey());
            if (storingRepo == null) {
                throw new FolderDownloadException("Can't get checksum policy " + pathToDownload.toPath()
                        + " from nonexistent repo " + pathToDownload.getRepoKey(), SC_NOT_FOUND);
            }
            checksumPolicy = storingRepo.getChecksumPolicy();
        }
        return checksumPolicy;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    /**
     * A resizable semaphore to act as the concurrent downloads counter for the service.
     */
    private static class ConcurrentDownloadCounter extends Semaphore {

        private int permits;

        ConcurrentDownloadCounter(int permits) {
            super(permits, true);
            this.permits = permits;
        }

        synchronized void resize(int newSize) {
            log.debug("Resizing download counter, old size: {}, new size: {}", permits, newSize);
            int delta = newSize - permits;
            if (delta == 0) {
                log.trace("Same size chosen - no need to resize");
                return;
            } else if (delta > 0) {
                log.trace("Adding {} permits", delta);
                this.release(delta);
            } else if (delta < 0) {
                log.trace("Reducing {} permits", Math.abs(delta));
                this.reducePermits(Math.abs(delta));
            }
            this.permits = newSize;
            log.debug("Current available permits in counter: {}", this.availablePermits());
        }
    }
}
