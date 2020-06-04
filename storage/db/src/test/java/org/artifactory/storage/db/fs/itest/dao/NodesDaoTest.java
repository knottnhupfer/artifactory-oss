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

package org.artifactory.storage.db.fs.itest.dao;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodeBuilder;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.jfrog.storage.DbType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Low level tests of the {@link org.artifactory.storage.db.fs.dao.NodesDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class NodesDaoTest extends DbBaseTest {

    @Autowired
    private NodesDao nodesDao;

    private NodePath fileNodePath = new NodePath("repo1", "ant/ant/1.5", "ant-1.5.jar", true);

    @BeforeClass
    public void setup() {
        importSql("/sql/nodes.sql");
    }

    public void createDirectoryNode() throws SQLException {
        NodeBuilder b = new NodeBuilder().nodeId(800).file(false).repo("repo").path("path/to/dir").name("name")
                .createdBy("yossis").modifiedBy("yossis");

        nodesDao.create(b.build());
    }

    @Test(dependsOnMethods = "createDirectoryNode")
    public void loadDirectoryNodeByPath() throws SQLException {
        Node node = nodesDao.get(new NodePath("repo", "path/to/dir", "name", false));
        assertNotNull(node);
        assertEquals(node.getNodeId(), 800);
        assertFalse(node.isFile());
        assertEquals(node.getDepth(), 4);
        assertEquals(node.getRepo(), "repo");
        assertEquals(node.getPath(), "path/to/dir");
        assertEquals(node.getName(), "name");
        assertEquals(node.getCreatedBy(), "yossis");
        assertEquals(node.getModifiedBy(), "yossis");
    }

    @Test
    public void testGetFileCount() throws SQLException {
        int fileCount = nodesDao.getFileCount("repo1", "test.bin");
        Assert.assertEquals(fileCount, 1);
    }

    @Test(dependsOnMethods = "loadDirectoryNodeByPath")
    public void loadDirectoryNodeById() throws SQLException {
        Node node = nodesDao.get(800);
        EqualsBuilder.reflectionEquals(node, nodesDao.get(new NodePath("repo", "path/to/dir", "name", false)));
    }

    public void itemExists() throws SQLException {
        assertTrue(nodesDao.exists(new NodePath("repo1", "", "org", false)));
    }

    public void itemNotExists() throws SQLException {
        assertFalse(nodesDao.exists(new NodePath("repo1", "", "nosuchfile", false)));
    }

    public void deleteDirectoryNode() throws SQLException {
        NodeBuilder b = new NodeBuilder().nodeId(801).file(false).repo("repo").path("path/to/dir").name("todelete")
                .createdBy("yossis").modifiedBy("yossis");

        Node inserted = b.build();
        nodesDao.create(inserted);
        Node loaded = nodesDao.get(inserted.getNodePath());
        assertNotNull(loaded);
        boolean deleted = nodesDao.delete(loaded.getNodeId());
        assertTrue(deleted);
        assertFalse(nodesDao.exists(inserted.getNodePath()));
    }

    public void deleteNonExistent() throws SQLException {
        boolean deleted = nodesDao.delete(990);
        assertFalse(deleted);
    }

    public void getChildrenOfRoot() throws SQLException {
        NodePath path = new NodePath("repo1", "", "", false);
        List<? extends Node> children = nodesDao.getChildren(path);
        assertEquals(children.size(), 5);

        assertTrue(nodesDao.hasChildren(path));
    }

    public void getChildrenOfNodeDirectlyUnderRoot() throws SQLException {
        // nodes directly under root has name but no path - hence special test case
        NodePath path = new NodePath("repo1", "", "org", false);
        List<? extends Node> children = nodesDao.getChildren(path);
        assertEquals(children.size(), 1);

        assertTrue(nodesDao.hasChildren(path));
    }

    public void getChildrenOfNodeDirectlyUnderRootWithCousinStartingWithSamePrefix() throws SQLException {
        NodePath path = new NodePath("repo1", "", "ant", false);
        List<? extends Node> children = nodesDao.getChildren(path);
        assertEquals(children.size(), 1);

        assertTrue(nodesDao.hasChildren(path));
    }

    public void getChildrenOfLeafFolderNode() throws SQLException {
        NodePath leaf = new NodePath("repo1", "org/yossis/tools", "test.bin", true);
        assertTrue(nodesDao.exists(leaf));
        assertEquals(nodesDao.getChildren(leaf).size(), 0);
        assertFalse(nodesDao.hasChildren(leaf));
    }

    public void getChildrenOfLeafFileNode() throws SQLException {
        assertTrue(nodesDao.exists(fileNodePath));
        assertEquals(nodesDao.getChildren(fileNodePath).size(), 0);
        assertFalse(nodesDao.hasChildren(fileNodePath));
    }

    public void getChildrenOfFolderWithUnderscore() throws SQLException {
        NodePath path1 = new NodePath("repo1", "", "a_1.2", false);
        NodePath path2 = new NodePath("repo1", "", "ab1.2", false);
        NodePath file1 = new NodePath("repo1", "a_1.2", "tt.txt", true);
        NodePath file2 = new NodePath("repo1", "ab1.2", "tt.txt", true);
        assertTrue(nodesDao.exists(path1));
        assertTrue(nodesDao.exists(path2));
        assertFalse(nodesDao.exists(file1));
        assertTrue(nodesDao.exists(file2));
        assertEquals(nodesDao.getChildren(path1).size(), 0);
        assertFalse(nodesDao.hasChildren(path1));
        assertEquals(nodesDao.getChildren(path2).size(), 1);
        assertTrue(nodesDao.hasChildren(path2));
    }

    public void countRepositoryFiles() throws SQLException {
        assertEquals(nodesDao.getFilesCount("repo1"), 4);
    }


    public void countAndSizeRepositoryFiles() throws SQLException {
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "", "", false)).getFileCount(), 4);
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "", "", false)).getFolderSize(), 846441);
    }

    public void countFilesUnderFolder() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo1", "", "ant", false)), 1);
    }


    public void countAndSizeFilesUnderFolder() throws SQLException {
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "", "ant", false)).getFileCount(), 1);
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "", "ant", false)).getFolderSize(), 716139);
    }

    public void countFilesUnderFolderWithDirectChildren() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo1", "ant/ant", "1.5", false)), 1);
    }

    public void countAndSizeFilesUnderFolderWithDirectChildren() throws SQLException {
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "ant/ant", "1.5", false)).getFileCount(), 1);
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "ant/ant", "1.5", false)).getFolderSize(),
                716139);
    }

    public void countFilesUnderNonExistentFolder() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo1", "xxx", "boo", false)), 0);
    }

    public void countAndSizeFilesUnderNonExistentFolder() throws SQLException {
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "xxx", "boo", false)).getFileCount(), 0);
        assertEquals(nodesDao.getFilesCountAndSize(new NodePath("repo1", "xxx", "boo", false)).getFolderSize(), 0);
    }

    public void countFilesUnderFile() throws SQLException {
        assertEquals(nodesDao.getFilesCount(fileNodePath), 0);
    }

    public void getFilesTotalSize() throws SQLException {
        //hardcoded in nodes.sql
        final int antExpectedSize = 716139;
        final int totalExpectedSize = 846441;
        final int toolsExpectedSize = 130302;

        assertEquals(nodesDao.getFilesTotalSize(new NodePath("repo1", "", "ant", false)), antExpectedSize,
                "single file size should be " + antExpectedSize);

        assertEquals(nodesDao.getFilesTotalSize("repo1"), totalExpectedSize,
                "total size of repo1 should be " + totalExpectedSize);

        long filesTotalSize = 0;
        for (Node node : nodesDao.getChildren(new NodePath("repo1", "", "", false))) {
            filesTotalSize += nodesDao.getFilesTotalSize(node.getNodePath());
        }

        assertEquals(filesTotalSize, totalExpectedSize,
                "sum of children size in repo1 should be " + filesTotalSize);

        assertEquals(nodesDao.getFilesTotalSize(new NodePath("repo1", "org/yossis", "tools", false)), toolsExpectedSize,
                "total size of files under org/yossis should be " + toolsExpectedSize);
    }

    public void countRepositoryFilesAndFolders() throws SQLException {
        assertEquals(nodesDao.getNodesCount("repo1"), 17);
    }

    public void countRepositoryFilesAndFoldersUnderFolder() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo1", "", "ant", false)), 3);
    }

    public void countFilesAndFoldersUnderFolderWithDirectChildren() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo1", "ant/ant", "1.5", false)), 1);
    }

    public void countFilesAndFoldersUnderNonExistentFolder() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo1", "xxx", "boo", false)), 0);
    }

    public void countFilesAndFoldersUnderFile() throws SQLException {
        assertEquals(nodesDao.getNodesCount(fileNodePath), 0);
    }

    public void countFilesUnderFolderWithPrefix() throws SQLException {
        assertEquals(nodesDao.getFilesCount(new NodePath("repo2", "", "a", false)), 2, "Files from 'repo2:/aa' were counted");
    }

    public void countNodesUnderFolderWithPrefix() throws SQLException {
        assertEquals(nodesDao.getNodesCount(new NodePath("repo2", "", "a", false)), 3, "Nodes from 'repo2:/aa' were counted");
    }

    public void getTotalSizeUnderFolderWithPrefix() throws SQLException {
        assertEquals(nodesDao.getFilesTotalSize(new NodePath("repo2", "", "a", false)),  716139 * 2, "Files from 'repo2:/aa' were counted");
    }

    public void countConanV2Files() throws SQLException {
        assertEquals(nodesDao.getConanV1LayoutArtifactCount(), 0);
    }

    public void updateFolderNode() throws SQLException {
        NodeBuilder b = new NodeBuilder().nodeId(50).file(false).repo("repo").path("path/to/dir").name("toupdate")
                .createdBy("yossis").modifiedBy("yossis");

        nodesDao.create(b.build());

        Node nodeToUpdate = b.repo("repo2").path("new/path").name("updatedfolder")
                .created(1111).createdBy("updater-creator")
                .modified(2222).modifiedBy("updater").updated(3333).build();

        int updateCount = nodesDao.update(nodeToUpdate);
        assertEquals(updateCount, 1);

        Node updatedNode = nodesDao.get(nodeToUpdate.getNodePath());
        assertNotNull(updatedNode);
        assertEquals(updatedNode.getRepo(), nodeToUpdate.getRepo());
        assertEquals(updatedNode.getPath(), nodeToUpdate.getPath());
        assertEquals(updatedNode.getName(), nodeToUpdate.getName());
        assertEquals(updatedNode.getCreated(), nodeToUpdate.getCreated());
        assertEquals(updatedNode.getCreatedBy(), nodeToUpdate.getCreatedBy());
        assertEquals(updatedNode.getModified(), nodeToUpdate.getModified());
        assertEquals(updatedNode.getModifiedBy(), nodeToUpdate.getModifiedBy());
        assertEquals(updatedNode.getUpdated(), nodeToUpdate.getUpdated());
        assertEquals(updatedNode.getNodeId(), 50, "Node id shouldn't have been updated");
        assertEquals(updatedNode.isFile(), false, "Node type shouldn't have been updated");
    }


    public void nodeIdRoot() throws SQLException {
        assertEquals(nodesDao.getNodeId(new NodePath("repo1", "", "", false)), 1);
    }

    public void nodeIdNoSuchNode() throws SQLException {
        assertEquals(nodesDao.getNodeId(new NodePath("repo2", "no", "folder", false)), DbService.NO_DB_ID);
    }

    public void nodeSha1OfFile() throws SQLException {
        assertEquals(nodesDao.getNodeSha1(new NodePath("repo1", "org/yossis/tools", "test.bin", true)),
                "acab88fc2a043c2479a6de676a2f8179e9ea2167");
    }

    public void nodeSha1NotExist() throws SQLException {
        assertNull(nodesDao.getNodeSha1(new NodePath("repo2", "no", "folder", false)));
    }

    public void nodeSha1OfFolder() throws SQLException {
        assertTrue(nodesDao.exists(new NodePath("repo1", "org/yossis", "tools", false)));
        assertNull(nodesDao.getNodeSha1(new NodePath("repo1", "org/yossis", "tools", false)));
    }

    public void searchFilesByProperty() throws SQLException {
        List<Node> nodes = nodesDao.searchNodesByProperty("repo1", "build.number");
        assertNotNull(nodes);
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5);
    }

    public void searchFilesByPropertyNoMatch() throws SQLException {
        //TODO: [by YS] the property value is ignored. this method is re-evaluated
        List<Node> nodes = nodesDao.searchNodesByProperty("repo1", "build.number");
        assertNotNull(nodes);
        assertEquals(nodes.size(), 1);
    }

    public void searchGrandchildPoms() throws SQLException {
        List<Node> nodes = nodesDao.searchGrandchildPoms(new NodePath("repo1", "org", "yossis", false));
        assertNotNull(nodes);
        assertEquals(nodes.size(), 2);
        assertNotNull(getById(nodes, 12));
        assertNotNull(getById(nodes, 13));
    }

    public void searchGrandchildPomsNoMatch() throws SQLException {
        List<Node> nodes = nodesDao.searchGrandchildPoms(new NodePath("bu", "ba", "ga", false));
        assertNotNull(nodes);
        assertEquals(nodes.size(), 0);
    }

    public void findChecksumsBySha1() throws SQLException {
        List<Node> nodes;
        nodes = nodesDao.searchByChecksum(ChecksumType.sha1, "dcab88fc2a043c2479a6de676a2f8179e9ea2167");
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5L);
        assertEquals(nodes.get(0).getRepo(), "repo1");
        assertEquals(nodes.get(0).getName(), "ant-1.5.jar");
        nodes = nodesDao.searchByChecksum(ChecksumType.sha1, "dddd88fc2a043c2479a6de676a2f8179e9eadddd");
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 13);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file3.pom");
        node = getById(nodes, 15);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "file3.bin");
        nodes = nodesDao.searchByChecksum(ChecksumType.sha1, "acab88fc2a043c2479a6de676a2f8179e9ea2222");
        assertTrue(nodes.isEmpty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindEmptyChecksumsBySha1() throws SQLException {
        nodesDao.searchByChecksum(ChecksumType.sha1, "wrong");
    }

    public void findChecksumsByMd5() throws SQLException {
        List<Node> nodes;
        nodes = nodesDao.searchByChecksum(ChecksumType.md5, "902a360ecad98a34b59863c1e65bcf71");
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5L);
        assertEquals(nodes.get(0).getRepo(), "repo1");
        assertEquals(nodes.get(0).getName(), "ant-1.5.jar");
        nodes = nodesDao.searchByChecksum(ChecksumType.md5, "502a360ecad98a34b59863c1e65bcf71");
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 13);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file3.pom");
        node = getById(nodes, 15);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "file3.bin");
        nodes = nodesDao.searchByChecksum(ChecksumType.md5, "902a360ecad98a34b59863c1e65b2222");
        assertTrue(nodes.isEmpty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindEmptyChecksumsByMd5() throws SQLException {
        nodesDao.searchByChecksum(ChecksumType.md5, "wrong");
    }

    public void findChecksumsBySha2() throws SQLException {
        List<Node> nodes;

        nodes = nodesDao.searchByChecksum(ChecksumType.sha256, "bbbb23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60bbbb");
        assertEquals(nodes.size(), 1);
        assertEquals(nodes.get(0).getNodeId(), 5L);
        assertEquals(nodes.get(0).getRepo(), "repo1");
        assertEquals(nodes.get(0).getName(), "ant-1.5.jar");
        nodes = nodesDao.searchByChecksum(ChecksumType.sha256, "acdc23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60febd");
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 16);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "trustme.jar");
        node = getById(nodes, 17);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "badmd5.jar");
        nodes = nodesDao.searchByChecksum(ChecksumType.sha256, "ABCD23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ACDC");
        assertTrue(nodes.isEmpty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindEmptyChecksumsBySha2() throws SQLException {
        nodesDao.searchByChecksum(ChecksumType.sha256, "wrong");
    }

    public void searchBadSha1Checksums() throws Exception {
        List<Node> nodes = nodesDao.searchBadChecksums(ChecksumType.sha1);
        assertBadChecksumNodes(nodes);
    }

    public void searchBadSha2Checksums() throws Exception {
        List<Node> nodes = nodesDao.searchBadChecksums(ChecksumType.sha256);
        assertBadChecksumNodes(nodes);
    }

    private void assertBadChecksumNodes(List<Node> nodes) {
        assertEquals(nodes.size(), 3);
        Node node = getById(nodes, 12);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file2.pom");
        node = getById(nodes, 13);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo1");
        assertEquals(node.getName(), "file3.pom");
        node = getById(nodes, 15);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "file3.bin");
    }

    public void findMissingDirectories() throws Exception {
        if (dbProperties.getDbType() == DbType.DERBY) {
            return; // CONCAT doesn't work on Derby + it's irrelevant
        }
        List<Node> nodes = nodesDao.getOrphanNodes(new NodePath("repo3", null, null, false));
        assertEquals(nodes.size(), 2);
        Node node = getById(nodes, 33);
        assertNotNull(node);
        assertEquals(node.getPath(), "a/B");
        assertEquals(node.getName(), "C");
        node = getById(nodes, 34);
        assertNotNull(node);
        assertEquals(node.getPath(), "B");
        assertEquals(node.getName(), "test.txt");
    }

    public void findMissingDirectoriesUnderPath() throws Exception {
        if (dbProperties.getDbType() == DbType.DERBY) {
            return; // CONCAT doesn't work on Derby + it's irrelevant
        }
        List<Node> nodes = nodesDao.getOrphanNodes(new NodePath("repo3", "a", "B", false));
        assertEquals(nodes.size(), 1);
        Node node = getById(nodes, 33);
        assertNotNull(node);
        assertEquals(node.getPath(), "a/B");
        assertEquals(node.getName(), "C");
    }

    public void searchBadMd5Checksums() throws Exception {
        List<Node> nodes = nodesDao.searchBadChecksums(ChecksumType.md5);
        assertEquals(nodes.size(), 1);
        Node node = getById(nodes, 17);
        assertNotNull(node);
        assertEquals(node.getRepo(), "repo-copy");
        assertEquals(node.getName(), "badmd5.jar");
    }

    public void constantValuesGetQuery() throws SQLException, InvocationTargetException, IllegalAccessException {
        setArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeByPath,
                "SELECT * FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?");
        Node node = nodesDao.get(new NodePath("repo1", "", "org", false));
        assertTrue(node != null);
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeByPath, "SELECT * from not_nodes");
            nodesDao.get(new NodePath("repo1", "", "org", false));
        } catch (SQLException e) {
            return; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeByPath);
        }
        fail();
    }

    public void constantValuesExistsQuery() throws SQLException, InvocationTargetException, IllegalAccessException {
        setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeExists,
                "SELECT count(*) FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?");
        boolean exists = nodesDao.exists(new NodePath("repo1", "", "org", false));
        assertTrue(exists);
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeExists, "SELECT * from not_nodes");
            nodesDao.exists(new NodePath("repo1", "", "org", false));
        } catch (SQLException e) {
            return; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeByPath);
        }
        fail();
    }

    public void constantValuesGetNodeIdQuery() throws SQLException, InvocationTargetException, IllegalAccessException {
        setArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeIdByPath,
                "SELECT node_id FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?");
        long nodeId = nodesDao.getNodeId(new NodePath("repo1", "", "org", false));
        assertTrue(nodeId == 8);
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeIdByPath, "SELECT * from not_nodes");
            nodesDao.getNodeId(new NodePath("repo1", "", "org", false));
        } catch (SQLException e) {
            return; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlGetNodeIdByPath);
        }
        fail();
    }

    public void constantValuesSearchNodeByPropQuery()
            throws SQLException, InvocationTargetException, IllegalAccessException {
        setArtifactoryProperty(ConstantValues.nodesDaoSqlSearchFilesByProperty,
                "SELECT n.* FROM nodes n JOIN node_props p ON n.node_id = p.node_id WHERE repo = ? AND p.prop_key = ?");
        List<Node> repo1 = nodesDao.searchNodesByProperty("repo1", "build.number");
        assertNotNull(repo1);
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlSearchFilesByProperty,"SELECT * from not_nodes");
            nodesDao.searchNodesByProperty("repo1", "build.number");
        } catch (SQLException e) {
            return; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlSearchFilesByProperty);
        }
        fail();
    }

    public void constantValuesNodeItemTypeQuery() throws SQLException, InvocationTargetException, IllegalAccessException {
        setArtifactoryProperty(ConstantValues.nodesDaoSqlGetItemType,
                "SELECT node_type FROM nodes WHERE repo = ? AND node_path = ? AND node_name = ?");
        boolean exists = nodesDao.exists(new NodePath("repo1", "", "org", false));
        assertTrue(exists);
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlGetItemType, "SELECT * from not_nodes");
            nodesDao.getItemType(new NodePath("repo1", "", "org", false));
        } catch (SQLException e) {
            return; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlGetItemType);
        }
        fail();
    }

    public void constantValuesNodeHasChildren() throws SQLException, InvocationTargetException, IllegalAccessException {
        boolean hasChildren = nodesDao.hasChildren(new NodePath("non-existing", "path", "dir", false));
        assertFalse(hasChildren);

        hasChildren = nodesDao.hasChildren(new NodePath("repo3", "a", "b", false));
        assertTrue(hasChildren);

        setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeHasChildren,
                "SELECT COUNT(1) FROM nodes WHERE sha256 = 'yop' AND repo = ? AND node_path = ? AND depth = ?");
        hasChildren = nodesDao.hasChildren(new NodePath("repo3", "a", "b", false));
        assertFalse(hasChildren);
        boolean errorOccurred = false;
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeHasChildren, "SELECT repo from nodes");
            nodesDao.hasChildren(new NodePath("repo3", "a", "b", false));
        } catch (SQLException e) {
            errorOccurred = true; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlNodeHasChildren);
        }
        if (!errorOccurred) {
            fail();
        }
    }

    public void constantValuesNodeGetChildren() throws SQLException, InvocationTargetException, IllegalAccessException {
        List<Node> children = nodesDao.getChildren(new NodePath("non-existing", "path", "dir", false));
        assertTrue(children.isEmpty());

        children = nodesDao.getChildren(new NodePath("repo3", "a", "b", false));
        assertEquals(children.size(), 1);

        setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeGetChildren, "SELECT * FROM nodes WHERE " +
                "sha256 = 'acab23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy' AND repo = ? " +
                "AND node_path = ? AND depth = ?");
        children = nodesDao.getChildren(new NodePath("repo3", "a", "b", false));
        assertEquals(children.size(), 1);

        setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeGetChildren, "SELECT * FROM nodes WHERE " +
                "sha256 = 'empty' AND repo = ? " +
                "AND node_path = ? AND depth = ?");
        children = nodesDao.getChildren(new NodePath("repo3", "a", "b", false));
        assertTrue(children.isEmpty());
        boolean errorOccurred = false;
        try {
            setArtifactoryProperty(ConstantValues.nodesDaoSqlNodeGetChildren, "SELECT repo from nodes");
            nodesDao.getChildren(new NodePath("repo3", "a", "b", false));
        } catch (SQLException e) {
            errorOccurred = true; // expected
        } finally {
            removeArtifactoryProperty(ConstantValues.nodesDaoSqlNodeGetChildren);
        }
        if (!errorOccurred) {
            fail();
        }
    }

    public void testGetLastNodeId() throws SQLException {
        assertEquals(nodesDao.findLastNodeId(), 10001);
    }

    private Node getById(List<Node> nodes, long id) {
        for (Node node : nodes) {
            if (node.getNodeId() == id) {
                return node;
            }
        }
        return null;
    }

    private void setArtifactoryProperty(ConstantValues property, String query)
            throws InvocationTargetException, IllegalAccessException {
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(property.getPropertyName(), query);
        Method initExternalQueries = ReflectionUtils.findMethod(nodesDao.getClass(), "initExternalQueries");
        initExternalQueries.setAccessible(true);
        initExternalQueries.invoke(nodesDao);
    }

    private void removeArtifactoryProperty(ConstantValues property)
            throws InvocationTargetException, IllegalAccessException {
        ArtifactoryHome.get().getArtifactoryProperties().removeProperty(property);
        Method initExternalQueries = ReflectionUtils.findMethod(nodesDao.getClass(), "initExternalQueries");
        initExternalQueries.setAccessible(true);
        initExternalQueries.invoke(nodesDao);
    }
}
