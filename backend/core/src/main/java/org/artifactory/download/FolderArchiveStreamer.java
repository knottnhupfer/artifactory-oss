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


import com.google.common.base.Charsets;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jfrog.common.archive.ArchiveType;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.FileInfo;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.security.AccessLogger;
import org.artifactory.storage.fs.service.StatsService;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.storage.fs.tree.TreeBrowsingCriteria;
import org.artifactory.storage.fs.tree.TreeBrowsingCriteriaBuilder;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.DownloadEntry;
import org.jfrog.common.ArchiveUtils;
import org.artifactory.util.HttpUtils;
import org.jfrog.client.util.PathUtils;
import org.jfrog.storage.common.StorageUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Traverses the tree under the requested path recursively and writes each file into the stream serially.
 * The stream itself is an {@link ArchiveOutputStream} based on the selected {@link ArchiveType}
 *
 * @author Dan Feldman
 * @author Yossi Shaul
 */
class FolderArchiveStreamer {
    private static final Logger log = LoggerFactory.getLogger(FolderArchiveStreamer.class);

    private final RepoPath rootFolder;
    private final ArchiveType archiveType;
    private final long maxDownloadSizeInMB;
    private final long maxDownloadSizeInBytes;
    private final long maxFiles;
    private final boolean includeChecksumFiles;
    private final ChecksumPolicy repoChecksumPolicy;
    private final RepositoryService repoService;
    private long filesCount;
    private long totalSizeInBytes;
    private ArchiveOutputStream archiveOutputStream = null;


    FolderArchiveStreamer(RepoPath pathToDownload, ArchiveType archiveType, int maxDownloadSizeMb, long maxFiles,
            ChecksumPolicy repoChecksumPolicy, boolean includeChecksumFiles) {
        //rounds towards zero but the overflow is negligible
        this.maxDownloadSizeInBytes = (long) StorageUnit.MB.toBytes(maxDownloadSizeMb);
        this.rootFolder = pathToDownload;
        this.archiveType = archiveType;
        this.maxFiles = maxFiles;
        this.maxDownloadSizeInMB = maxDownloadSizeMb;
        this.includeChecksumFiles = includeChecksumFiles;
        this.repoChecksumPolicy = repoChecksumPolicy;
        this.repoService = ContextHelper.get().beanForType(RepositoryService.class);
    }

    public void go(OutputStream out) {
        try {
            long start = System.currentTimeMillis();
            archiveOutputStream = ArchiveUtils.createArchiveOutputStream(out, archiveType);
            ItemTree tree = new ItemTree(rootFolder, getTreeCriteria());
            ItemNode rootNode = tree.getRootNode();
            writeRecursive(rootNode);
            archiveOutputStream.finish();
            archiveOutputStream.flush();
            String path = rootNode == null ? "" : rootNode.getRepoPath().toPath();
            log.trace("folder download of path {} finished successfully, took {} ms", path,
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Error executing folder download: {}", e.getMessage().contains("Broken pipe") ?
                    "IOException: Client closed the stream prematurely" : e.getMessage());
            log.debug("Caught exception while executing folder download: ", e);
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
        }
    }

    private TreeBrowsingCriteria getTreeCriteria() {
        return new TreeBrowsingCriteriaBuilder().applyRepoIncludeExclude().applySecurity().cacheChildren(false).build();
    }

    private void writeRecursive(ItemNode currentNode) throws IOException {
        if (limitsReached()) {
            return;
        }
        if (currentNode.isFolder()) {
            for (ItemNode child : currentNode.getChildren()) {
                if (!limitsReached()) {
                    writeRecursive(child);
                }
            }
        } else {
            FileInfo fileInfo = (FileInfo) currentNode.getItemInfo();
            totalSizeInBytes += fileInfo.getSize();
            filesCount++;
            if (!limitsReached()) {
                writeArtifact(fileInfo.getRepoPath(), fileInfo.getSize());
                if (includeChecksumFiles) {
                    findAndAddChecksumFiles(fileInfo);
                }
            }
        }
    }

    /**
     * Checksum artifacts (.sha1 & .md5) are not persisted as nodes, so we need an extra logic to add them
     */
    private void findAndAddChecksumFiles(FileInfo fileInfo) throws IOException {
        Set<ChecksumInfo> checksums = fileInfo.getChecksumsInfo().getChecksums();
        for (ChecksumType checksumType : ChecksumType.BASE_CHECKSUM_TYPES) {
            String checksumValue = repoChecksumPolicy.getChecksum(checksumType, checksums);
            if (StringUtils.isNotBlank(checksumValue)) {
                // if we got something, write it
                writeArtifactChecksum(checksumValue, checksumType, fileInfo.getRepoPath(), checksumValue.length());
            }
        }
    }

    //0 means no limit
    private boolean limitsReached() {
        if ((0 > maxFiles) && filesCount > maxFiles) {
            log.debug("Stopping folder download tree traversal, max file limit reached. current file count: {} " +
                    "limit is: {}", filesCount, maxFiles);
            return true;
        } else if ((0 > maxDownloadSizeInBytes) && totalSizeInBytes > maxDownloadSizeInBytes) {
            log.debug(
                    "Stopping folder download tree traversal, max size limit reached. current size count: {} limit is {}",
                    StorageUnit.toReadableString(totalSizeInBytes), maxDownloadSizeInMB);
            return true;
        }
        return false;
    }

    private void writeArtifact(RepoPath filePath, long size) throws IOException {
        String relativePath = PathUtils.getRelativePath(rootFolder.getPath(), filePath.getPath());
        log.debug("Writing path {} to output stream", filePath.toPath());
        //Ok to go through the non-strict getHandle(), the tree browsing criteria takes security, xray etc. into account
        try (ResourceStreamHandle handle = repoService.getResourceStreamHandle(filePath)) {
            writeToStream(relativePath, size, handle.getInputStream(), filePath);
        }
    }

    private void writeArtifactChecksum(String checksum, ChecksumType checksumType, RepoPath filePath, long size) throws IOException {
        String relativePath = PathUtils.getRelativePath(rootFolder.getPath(), filePath.getPath() + checksumType.ext());
        log.debug("Writing checksum of path {} as {} to output stream", filePath.toPath(), relativePath);
        RepoPath pathToLog = RepoPathFactory.create(filePath.getRepoKey(), filePath.getPath() + checksumType.ext());
        try (InputStream artifactStream = new ByteArrayInputStream(checksum.getBytes(Charsets.UTF_8))) {
            writeToStream(relativePath, size, artifactStream, pathToLog);
        }
    }

    /**
     * Common stream writer used by checksum and artifact writers, creates a {@link ArchiveEntry} based on
     * {@param relativePath}, {@param archiveType} and {@param size}.
     * Also logs the written path ({@param pathToLog}) to the access and traffic logs.
     */
    private void writeToStream(String relativePath, long size, InputStream streamToWrite, RepoPath pathToLog) throws IOException {
        long start = System.currentTimeMillis();
        ArchiveEntry archiveEntry = ArchiveUtils.createArchiveEntry(relativePath, archiveType, size);
        try {
            archiveOutputStream.putArchiveEntry(archiveEntry);
            IOUtils.copy(streamToWrite, archiveOutputStream);
        } finally {
            archiveOutputStream.closeArchiveEntry();
            archiveOutputStream.flush();
        }
        logAccessTrafficAndStatsForSinglePath(pathToLog, size, start);
    }

    private void logAccessTrafficAndStatsForSinglePath(RepoPath path, long size, long start) {
        AccessLogger.downloaded(path);
        DownloadEntry downloadEntry = new DownloadEntry(path.getId(), size, System.currentTimeMillis() - start,
                HttpUtils.getRemoteClientAddress());
        ContextHelper.get().beanForType(TrafficService.class).handleTrafficEntry(downloadEntry);
        if (ConstantValues.downloadStatsEnabled.getBoolean()) {
            ContextHelper.get().beanForType(StatsService.class).fileDownloaded(path,
                    SecurityContextHolder.getContext().getAuthentication().getName(), System.currentTimeMillis(),
                    false);
        }
    }
}