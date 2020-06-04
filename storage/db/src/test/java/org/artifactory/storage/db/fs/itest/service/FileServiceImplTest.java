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

package org.artifactory.storage.db.fs.itest.service;

import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.api.repo.storage.FolderSummeryInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FolderInfoImpl;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.VfsItemNotFoundException;
import org.artifactory.storage.fs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Low level integration tests for the file service.
 *
 * @author Yossi Shaul
 */
@Test
public class FileServiceImplTest extends DbBaseTest {

    @Autowired
    private FileService fileService;

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes-for-service.sql");
    }

    public void countAllFiles() throws VfsException {
        int count = fileService.getFilesCount();
        assertEquals(count, 7);
    }

    public void countFilesUnderRepo() throws VfsException {
        int count = fileService.getFilesCount(new RepoPathImpl("repo1", ""));
        assertEquals(count, 1);
    }

    public void countAndSizeFilesUnderRepo() throws VfsException {
        FolderSummeryInfo folderSummeryInfo = fileService.getFilesCountAndSize(new RepoPathImpl("repo2", ""));
        assertEquals(folderSummeryInfo.getFileCount(), 2);
        assertEquals(folderSummeryInfo.getFolderSize(), 716460);
    }

    public void countFilesUnderFolder() throws VfsException {
        int count = fileService.getFilesCount(new RepoPathImpl("repo1", "ant"));
        assertEquals(count, 1);
    }

    public void countAndSizeFilesUnderFolder() throws VfsException {
        FolderSummeryInfo folderSummeryInfo = fileService
                .getFilesCountAndSize(new RepoPathImpl("repo-copy", "org/shayy"));
        assertEquals(folderSummeryInfo.getFileCount(), 3);
        assertEquals(folderSummeryInfo.getFolderSize(), 130302);
        folderSummeryInfo = fileService.getFilesCountAndSize(new RepoPathImpl("repo-copy", "org/shayy/trustme"));
        assertEquals(folderSummeryInfo.getFileCount(), 1, "expected org/shayy/trustme to contain only 1 file");
        assertEquals(folderSummeryInfo.getFolderSize(), 43434, "expected org/shayy/trustme total size to be 43434");
    }

    public void countRepoFilesUnderNonExistentRepo() throws VfsException {
        int count = fileService.getFilesCount(new RepoPathImpl("repoXXX", ""));
        assertEquals(count, 0, "Dummy repo name");
    }

    @Test(dependsOnMethods = "deleteFolderById")
    public void countFolderAndFilesUnderRepo() throws VfsException {
        int count = fileService.getNodesCount(new RepoPathImpl("repo1", ""));
        assertEquals(count, 7);
    }

    public void countFolderAndFilesUnderFolder() throws VfsException {
        int count = fileService.getNodesCount(new RepoPathImpl("repo1", "ant"));
        assertEquals(count, 3);
    }

    public void countRepoFilesAndFoldersUnderNonExistentRepo() throws VfsException {
        int count = fileService.getNodesCount(new RepoPathImpl("repoXXX", ""));
        assertEquals(count, 0, "Dummy repo name");
    }

    public void getSha1BadChecksums() {
        List<FileInfo> files = fileService.searchFilesWithBadChecksum(ChecksumType.sha1);
        assertNotNull(files);
        assertEquals(files.size(), 1, "Expected 1 bad SHA-1 checksum");
        FileInfo file = files.get(0);
        assertEquals(file.getName(), "badsha1.jar", "Expected file name to be badsha1.jar");
        assertFalse(file.getChecksumsInfo().getChecksumInfo(ChecksumType.sha1).checksumsMatch(),
                "SHA-1 checksums should not match");
    }

    public void getSha2BadChecksums() {
        List<FileInfo> files = fileService.searchFilesWithBadChecksum(ChecksumType.sha256);
        assertNotNull(files);
        assertEquals(files.size(), 1, "Expected 1 bad SHA-256 checksum");
        FileInfo file = files.get(0);
        assertEquals(file.getName(), "badsha2.jar", "Expected file name to be badsha2.jar");
        assertFalse(file.getChecksumsInfo().getChecksumInfo(ChecksumType.sha256).checksumsMatch(),
                "SHA-2 checksums should not match");
    }

    public void getMd5BadChecksums() {
        List<FileInfo> files = fileService.searchFilesWithBadChecksum(ChecksumType.md5);
        assertNotNull(files);
        assertEquals(files.size(), 1, "Expected 1 bad MD5 checksum");
        FileInfo file = files.get(0);
        assertEquals(file.getName(), "badmd5.jar", "Expected file name to be badmd5.jar");
        assertFalse(file.getChecksumsInfo().getChecksumInfo(ChecksumType.md5).checksumsMatch(),
                "MD5 checksums should not match");
    }

    public void loadFolderUnderRoot() {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant");
        FolderInfo folderInfo = (FolderInfo) fileService.loadItem(repoPath);
        assertNotNull(folderInfo);
        assertEquals(folderInfo.getId(), 2);
        assertEquals(folderInfo.getRepoPath(), repoPath);
        assertEquals(folderInfo.getCreated(), 1340283204448L);
        assertEquals(folderInfo.getCreatedBy(), "yossis-1");
        assertEquals(folderInfo.getLastModified(), 1340283205448L);
        assertEquals(folderInfo.getModifiedBy(), "yossis-2");
        assertEquals(folderInfo.getLastUpdated(), 1340283205448L);
    }

    public void loadFolder() {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant");
        FolderInfo folderInfo = (FolderInfo) fileService.loadItem(repoPath);
        assertNotNull(folderInfo);
        assertEquals(folderInfo.getId(), 3);
        assertEquals(folderInfo.getRepoPath(), repoPath);
        assertEquals(folderInfo.getCreated(), 1340283204450L);
        assertEquals(folderInfo.getCreatedBy(), "yossis-1");
        assertEquals(folderInfo.getLastModified(), 1340283204450L);
        assertEquals(folderInfo.getModifiedBy(), "yossis-3");
        assertEquals(folderInfo.getLastUpdated(), 1340283214450L);
    }

    public void loadItemById() {
        ItemInfo itemInfo = fileService.loadItem(4);
        assertNotNull(itemInfo);
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant/1.5");
        assertEquals(itemInfo.getId(), 4);
        assertEquals(itemInfo.getRepoPath(), repoPath);
    }

    @Test(expectedExceptions = VfsItemNotFoundException.class)
    public void loadNonExistentById() {
        fileService.loadItem(47483);
    }

    /*
    @Test(expectedExceptions = FolderExpectedException.class)
    public void loadFileWithFolderPath() throws Exception {
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "ant/ant/1.5");
        fileService.loadFile(repoPath);
    }
    */

    public void loadChildren() {
        List<ItemInfo> children = fileService.loadChildren(new RepoPathImpl("repo1", ""));
        assertNotNull(children);
        assertEquals(children.size(), 2, "Expected 2 direct children but got: " + children);
        assertTrue(children.get(0).getId() > 0, "Child loaded without id");
        assertTrue(children.get(1).getId() > 0, "Child loaded without id");
    }

    @Test(enabled = false, expectedExceptions = VfsItemNotFoundException.class)
    public void loadChildrenPathNotFound() {
        fileService.loadChildren(new RepoPathImpl("repoYYY", ""));
    }

    @Test(enabled = false, expectedExceptions = FolderExpectedException.class)
    public void loadChildrenOfFile() {
        fileService.loadChildren(new RepoPathImpl("repo1", "ant/ant/1.5/ant-1.5.jar"));
    }

    @Test(expectedExceptions = VfsItemNotFoundException.class)
    public void loadNonExistentFolder() {
        fileService.loadItem(new RepoPathImpl("repoXXX", "ant"));
    }

    public void createFolder() {
        FolderInfoImpl folderSave = new FolderInfoImpl(new RepoPathImpl("repo1", "new/folder"));
        long nodeId = fileService.createFolder(folderSave);
        RepoPathImpl repoPath = new RepoPathImpl("repo1", "new/folder");
        FolderInfo folderLoaded = (FolderInfo) fileService.loadItem(repoPath);
        assertNotNull(folderLoaded);
        assertEquals(folderSave, folderLoaded);
        assertEquals(folderLoaded.getId(), nodeId);
    }

    @Test(dependsOnMethods = "createFolder")
    public void deleteFolderById() {
        RepoPathImpl folderRepoPath = new RepoPathImpl("repo1", "new/folder");
        VfsItem item = fileService.loadVfsItem(null, folderRepoPath);
        boolean deleted = fileService.deleteItem(item.getId());
        assertTrue(deleted);
        assertFalse(fileService.exists(folderRepoPath));
    }

    public void deleteNonExistentItem() {
        boolean deleted = fileService.deleteItem(89894);
        assertFalse(deleted);
    }

    public void itemExists() throws VfsException {
        assertTrue(fileService.exists(new RepoPathImpl("repo1", "org")));
    }

    public void itemNotExists() throws VfsException {
        assertFalse(fileService.exists(new RepoPathImpl("repo1", "nosuchfile")));
    }

    public void itemExistsRepoRoot() throws VfsException {
        assertTrue(fileService.exists(new RepoPathImpl("repo1", "")));
    }

    public void nodeIdRoot() {
        assertEquals(fileService.getNodeId(new RepoPathImpl("repo2", "")), 500);
    }

    public void nodeIdNoSuchNode() {
        assertEquals(fileService.getNodeId(new RepoPathImpl("repo2", "no/folder")), DbService.NO_DB_ID);
    }

    public void searchGrandchildPoms() {
        List<FileInfo> poms = fileService.searchGrandchildPoms(new RepoPathImpl("repo2", "org/jfrog"));
        assertNotNull(poms);
        assertEquals(poms.size(), 1, "Expected 1 pom but got: " + poms);
    }
}
