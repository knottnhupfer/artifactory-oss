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

package org.artifactory.storage.fs.service;

import org.artifactory.api.repo.storage.FolderSummeryInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.RepoStorageSummary;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.VfsItemNotFoundException;
import org.artifactory.storage.fs.repo.StoringRepo;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * @author Yossi Shaul
 */
@SuppressWarnings("DuplicateThrows")
public interface FileService {

    boolean exists(RepoPath repoPath) throws VfsException;

    boolean existsBySha1(String sha1) throws VfsException;

    @Nonnull
    ItemInfo loadItem(RepoPath repoPath) throws VfsItemNotFoundException, VfsException;

    ItemInfo loadItem(long id);

    int getFileCount(String repoKey, String fileName);

    int getFilesCount() throws VfsException;

    int getFilesCount(RepoPath repoPath) throws VfsException;

    FolderSummeryInfo getFilesCountAndSize(@Nonnull RepoPath repoPath) throws VfsException;

    int getNodesCount(RepoPath repoPath) throws VfsException;

    List<ItemInfo> loadChildren(RepoPath repoPath) throws VfsException;

    VfsItem loadVfsItem(StoringRepo storingRepo, RepoPath repoPath) throws VfsItemNotFoundException, VfsException;

    long createFolder(FolderInfo folder) throws VfsException;

    int updateFolder(long id, FolderInfo folder);

    long createFile(FileInfo file);

    int updateFile(long id, FileInfo file);

    boolean deleteItem(long id);

    /**
     * @param repoPath Repo path to check for children
     * @return True if the repo path exists and has children. False otherwise.
     */
    boolean hasChildren(RepoPath repoPath);

    Boolean isFolder(RepoPath repoPath);

    /**
     * Prints the hierarchy of the given repo path, including the full nodes data to the log.
     * Use for dev debugging purposes only.
     *
     * @param repoPath Root of the sub tree to print
     */
    @SuppressWarnings("unused")
    void debugNodeStructure(RepoPath repoPath);

    /**
     * Prints the nodes table to the console. For dev use only.
     */
    @SuppressWarnings("unused")
    void printNodesTable();

    long getNodeId(RepoPath repoPath);

    List<FileInfo> searchFilesByProperty(String repo, String propKey, String propValue);

    /**
     * Searches for pom files two levels deep (grandchild) from the input path.
     *
     * @param repoPath Path of the node to search grandchild poms
     * @return List of grandchild poms
     */
    List<FileInfo> searchGrandchildPoms(RepoPath repoPath);

    /**
     * Search for all files with bad checksums of the given checksum type (SHA-1 or MD5)
     *
     * @param type The checksum type to search for, we support SHA-1 or MD5
     */
    List<FileInfo> searchFilesWithBadChecksum(ChecksumType type);

    long getFilesTotalSize(RepoPath repoPath);

    Set<RepoStorageSummary> getRepositoriesStorageSummary();

    List<ItemInfo> getOrphanItems(RepoPath repoPath);

    Boolean isFile(RepoPath repoPath);

}
