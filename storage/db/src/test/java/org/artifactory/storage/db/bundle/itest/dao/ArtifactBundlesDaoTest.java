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

package org.artifactory.storage.db.bundle.itest.dao;

import com.google.common.collect.Sets;
import org.artifactory.api.release.bundle.ReleaseBundleSearchFilter;
import org.artifactory.bundle.BundleTransactionStatus;
import org.artifactory.bundle.BundleType;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.artifactory.storage.db.bundle.dao.ArtifactBundlesDao;
import org.artifactory.storage.db.bundle.model.BundleNode;
import org.artifactory.storage.db.bundle.model.DBArtifactsBundle;
import org.artifactory.storage.db.bundle.model.DBBundleResult;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO;
import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * @author Rotem Kfir
 */
@Test
public class ArtifactBundlesDaoTest extends DbBaseTest {

    @Autowired
    protected ArtifactBundlesDao artifactBundlesDao;

    @Autowired
    protected NodesDao nodesDao;

    @Autowired
    protected BinariesDao binariesDao;

    private long nextId = 1;

    @AfterMethod
    public void cleanup() throws SQLException {
        artifactBundlesDao.deleteAllBundleNodes();
        artifactBundlesDao.deleteAllArtifactsBundles();
    }

    @Test
    public void test() throws SQLException {
        List<DBArtifactsBundle> bundles = artifactBundlesDao.getCompletedBundlesLastVersion(BundleType.TARGET);
        assertThat(bundles).hasSize(0);

        DBArtifactsBundle bundle = new DBArtifactsBundle();
        for (int i = 0; i < 3; i++) {
            bundle.setId(++nextId);
            bundle.setName("RB");
            bundle.setStatus(BundleTransactionStatus.COMPLETE);
            bundle.setDateCreated(DateTime.now().plusMillis((int) nextId));
            bundle.setVersion(String.valueOf(nextId));
            bundle.setType(BundleType.TARGET);
            bundle.setSignature("Signature" + nextId);
            artifactBundlesDao.create(bundle);
        }

        // Same creation date, higher version
        bundle.setId(++nextId);
        bundle.setVersion(String.valueOf(nextId));
        artifactBundlesDao.create(bundle);
        // Same creation date, even higher version, but INPROGRESS
        bundle.setId(++nextId);
        bundle.setVersion(String.valueOf(nextId));
        bundle.setStatus(BundleTransactionStatus.INPROGRESS);
        artifactBundlesDao.create(bundle);

        bundles = artifactBundlesDao.getCompletedBundlesLastVersion(BundleType.TARGET);
        assertThat(bundles).hasSize(1);
        assertThat(bundles.get(0).getVersion()).isEqualTo(String.valueOf(nextId - 1)); // the completed bundle with the latest date_created and highest version name

        bundle.setId(++nextId);
        bundle.setName("RB2");
        bundle.setStatus(BundleTransactionStatus.COMPLETE);
        artifactBundlesDao.create(bundle);
        bundles = artifactBundlesDao.getCompletedBundlesLastVersion(BundleType.TARGET);
        assertThat(bundles).hasSize(2);
    }

    @Test
    public void testCreateBundleFile() throws SQLException {
        DBArtifactsBundle dbArtifactsBundle = new DBArtifactsBundle();
        dbArtifactsBundle.setId(100);
        dbArtifactsBundle.setStatus(BundleTransactionStatus.INPROGRESS);
        dbArtifactsBundle.setName("yoazRB");
        dbArtifactsBundle.setDateCreated(new DateTime(System.currentTimeMillis()));
        dbArtifactsBundle.setSignature("MySig");
        dbArtifactsBundle.setStoringRepo("release-bundles");
        dbArtifactsBundle.setType(BundleType.SOURCE);
        dbArtifactsBundle.setVersion("1.x.0");
        artifactBundlesDao.create(dbArtifactsBundle);
        BinaryEntity binaryEntity = new BinaryEntity("mySha1", "mySha2", "myMd5", 100);
        binariesDao.create(binaryEntity);
        Node node = new Node(3, true, "repo2", "path/to", "file", (short) 2, 543L, "yossis",
                123L, "modifier", 213L, 22, "mySha1",
                "mySha1", "myMd5", "myMd5", "mySha2");
        nodesDao.create(node);
        String componentDetails = "{\"name\":\"yoaz.jar\",\"version\":\"1.0.x\",\"componentType\":\"Maven\",\"extension\":\"jar\",\"mimeType\":\"application/java-archive\"}";
        BundleNode bundleNode = createBundleNode(node.getNodeId(), dbArtifactsBundle.getId(), componentDetails, "my/repo/path", 1231);
        artifactBundlesDao.create(bundleNode);
        bundleNode = createBundleNode(node.getNodeId(), dbArtifactsBundle.getId(), null, "my/repo/path2", 1232);
        artifactBundlesDao.create(bundleNode);
        String details = artifactBundlesDao.getComponentDetails("my/repo/path");
        assertEquals(componentDetails, details);
        details = artifactBundlesDao.getComponentDetails("my/repo/path2");
        assertNull(details);
    }


    @Test
    public void testDeleteBundleNodes() throws SQLException {
        DBArtifactsBundle dbArtifactsBundle = new DBArtifactsBundle();
        dbArtifactsBundle.setId(100);
        dbArtifactsBundle.setStatus(BundleTransactionStatus.INPROGRESS);
        dbArtifactsBundle.setName("yoazRB");
        dbArtifactsBundle.setDateCreated(new DateTime(System.currentTimeMillis()));
        dbArtifactsBundle.setSignature("MySig");
        dbArtifactsBundle.setStoringRepo("release-bundles");
        dbArtifactsBundle.setType(BundleType.SOURCE);
        dbArtifactsBundle.setVersion("1.x.0");
        artifactBundlesDao.create(dbArtifactsBundle);
        BinaryEntity binaryEntity = new BinaryEntity("mySha1_123" + System.currentTimeMillis(), "mySha2_123"+ System.currentTimeMillis(), "myMd5_123"+ System.currentTimeMillis(), 100);
        binariesDao.create(binaryEntity);
        Node node = new Node(9001, true, "repo2", "path/to", "file" + System.currentTimeMillis(), (short) 2, 543L, "yossis",
                123L, "modifier", 213L, 22, "mySha1",
                "mySha1", "myMd5", "myMd5", "mySha2");
        nodesDao.create(node);
        BundleNode bundleNode = createBundleNode(node.getNodeId(), dbArtifactsBundle.getId(), null, "my/repo/path",
                1000);
        deleteAndAssertDeleted(bundleNode, 2000);

        deleteAndAssertDeleted(bundleNode, 2189);

        deleteAndAssertDeleted(bundleNode, 3);
    }

    private void deleteAndAssertDeleted(BundleNode bundleNode, int numberOfRecords) throws SQLException {
        List<Long> idsToDelete = new ArrayList<>();
        for (int i = 0; i < numberOfRecords; i++) {
            artifactBundlesDao.create(bundleNode);
            idsToDelete.add(bundleNode.getId());
            bundleNode.setId(bundleNode.getId() + 1);
        }
        artifactBundlesDao.deleteBundleNodes(bundleNode.getBundleId());
        assertEquals(0, artifactBundlesDao.getBundleNodes(bundleNode.getBundleId()).size());
    }

    @Test
    public void testIsPathRelatedToBundle() throws SQLException {
        DBArtifactsBundle dbArtifactsBundle = new DBArtifactsBundle();
        dbArtifactsBundle.setId(100);
        dbArtifactsBundle.setStatus(BundleTransactionStatus.INPROGRESS);
        dbArtifactsBundle.setName("yoazRB2");
        dbArtifactsBundle.setDateCreated(new DateTime(System.currentTimeMillis()));
        dbArtifactsBundle.setSignature("MySig2");
        dbArtifactsBundle.setStoringRepo("release-bundles");
        dbArtifactsBundle.setType(BundleType.SOURCE);
        dbArtifactsBundle.setVersion("1.x.1");
        artifactBundlesDao.create(dbArtifactsBundle);
        BinaryEntity binaryEntity = new BinaryEntity("mySha1_123" + System.currentTimeMillis(), "mySha2_123"+ System.currentTimeMillis(), "myMd5_123"+ System.currentTimeMillis(), 100);
        binariesDao.create(binaryEntity);
        Node node = new Node(9002, true, "repo2", "path/to", "file" + System.currentTimeMillis(), (short) 2, 543L, "yossis",
                123L, "modifier", 213L, 22, "mySha1",
                "mySha1", "myMd5", "myMd5", "mySha2");
        nodesDao.create(node);
        BundleNode bundleNode = createBundleNode(node.getNodeId(), dbArtifactsBundle.getId(), null, "repo2/path/to/file",
                1000);
        artifactBundlesDao.create(bundleNode);
        assertTrue(artifactBundlesDao.isDirectoryRelatedToBundle("repo2"));
        assertTrue(artifactBundlesDao.isDirectoryRelatedToBundle("repo2/path"));
        assertTrue(artifactBundlesDao.isDirectoryRelatedToBundle("repo2/path/to"));
        assertTrue(artifactBundlesDao.isDirectoryRelatedToBundle("repo2/path/to/"));
        assertTrue(artifactBundlesDao.isRepoPathRelatedToBundle("repo2/path/to/file"));

    }


    @Test
    public void testSource() throws SQLException {
        DBArtifactsBundle bundle = new DBArtifactsBundle();
        bundle.setId(++nextId);
        bundle.setName("RBS");
        bundle.setStatus(BundleTransactionStatus.INPROGRESS);
        bundle.setDateCreated(DateTime.now());
        bundle.setVersion("1.0");
        bundle.setType(BundleType.SOURCE);
        bundle.setSignature("Signature");
        artifactBundlesDao.create(bundle);

        DBArtifactsBundle rb = artifactBundlesDao.getArtifactsBundle("RBS", "1.0", BundleType.SOURCE);
        assertThat(rb).isNotNull();
        artifactBundlesDao.failBundle("RBS", "1.0", BundleType.SOURCE);
        rb = artifactBundlesDao.getArtifactsBundle("RBS", "1.0", BundleType.SOURCE);
        assertThat(rb).isNotNull();
        assertThat(rb.getStatus()).isEqualTo(BundleTransactionStatus.FAILED);

    }

    @Test
    public void getCompletedBundles() throws SQLException {
        createBundle("bundle1", BundleTransactionStatus.COMPLETE, "1.0.0", BundleType.SOURCE, "release-bundles");
        createBundle("bundle1", BundleTransactionStatus.COMPLETE, "2.0.0", BundleType.SOURCE, "release-bundles-1");
        createBundle("bundle1", BundleTransactionStatus.INPROGRESS, "3.0.0", BundleType.SOURCE, "release-bundles-2");

        createBundle("bundle2", BundleTransactionStatus.COMPLETE, "3.0.0", BundleType.TARGET, null);
        createBundle("bundle2", BundleTransactionStatus.COMPLETE, "4.0.0", BundleType.SOURCE, "release-bundles");

        createBundle("bundle3", BundleTransactionStatus.COMPLETE, "1.0.0", BundleType.SOURCE, "release-bundles-2");

        List<DBBundleResult> completedBundles = artifactBundlesDao.getAllCompletedBundles();
        assertThat(completedBundles.size()).isEqualTo(4);
        List<DBBundleResult> expectedBundles = Lists.newArrayList(new DBBundleResult("bundle1", "release-bundles"),
                new DBBundleResult("bundle1", "release-bundles-1"),
                new DBBundleResult("bundle2", "release-bundles"),
                new DBBundleResult("bundle3", "release-bundles-2"));
        assertEquals(expectedBundles, completedBundles);
    }

    @Test
    public void getCompletedBundlesNotFound() throws SQLException {
        createBundle("bundle2", BundleTransactionStatus.INPROGRESS, "3.0.0", BundleType.TARGET, null);
        createBundle("bundle2", BundleTransactionStatus.FAILED, "4.0.0", BundleType.SOURCE, "release-bundles");
        createBundle("bundle3", BundleTransactionStatus.FAILED, "1.0.0", BundleType.SOURCE, "release-bundles-2");

        List<DBBundleResult> completedBundles = artifactBundlesDao.getAllCompletedBundles();
        assertThat(completedBundles.size()).isEqualTo(0);
    }

    @Test
    public void testGetAllBundlesRelatedToNode() throws SQLException {
        createBundle(4001, "rb1", "1.0.0", BundleTransactionStatus.COMPLETE, BundleType.TARGET,
                RELEASE_BUNDLE_DEFAULT_REPO);
        createBinary("12345", "67891", "myMd53");
        createNode(6011, "generic-target-1", "my_path_1", "12345", "67891", "myMd53");
        createBundleNode(6011, 4001, "generic-target-1/my_path_1/org/artifact/1.0/artifact-1.0.jar");

        createBundle(4002, "rb2", "1.0.0", BundleTransactionStatus.COMPLETE, BundleType.TARGET,
                RELEASE_BUNDLE_DEFAULT_REPO);
        createBundleNode(6011, 4002, "generic-target-1/my_path_1/org/artifact/1.0/artifact-1.0.jar");

        List<Long> allBundlesRelatedToNode = artifactBundlesDao.getAllBundlesRelatedToNode(6011);
        assertThat(allBundlesRelatedToNode.size()).isEqualTo(2);
        assertEquals(Sets.newHashSet(Lists.newArrayList(4001L, 4002L)), new HashSet<>(allBundlesRelatedToNode));
    }

    private BundleNode createBundleNode(long nodeID, long bundleID, String componentDetails, String bundleNodeRepoPath, int bundleNodeID) {
        BundleNode bundleNode = new BundleNode();
        bundleNode.setBundleId(bundleID);
        bundleNode.setId(bundleNodeID);
        bundleNode.setOriginalFileDetails(componentDetails);
        bundleNode.setRepoPath(bundleNodeRepoPath);
        bundleNode.setNodeId(nodeID);
        return bundleNode;
    }

    private void createBinary(String sha1, String sha2, String md5) throws SQLException {
        BinaryEntity binaryEntity = new BinaryEntity(sha1, sha2, md5, 100);
        binariesDao.create(binaryEntity);
    }

    private void createNode(long id, String repoKey, String path, String sha1, String sha2, String md5)
            throws SQLException {
        Node node = new Node(id, false, repoKey, ".", path, (short) 1, 543L, "admin",
                123L, "modifier", 213L, 22, sha1,
                sha1, md5, md5, sha2);
        nodesDao.create(node);
    }

    private void createBundleNode(long nodeId, long bundleId, String path) throws SQLException {
        BundleNode bundleNode = new BundleNode();
        bundleNode.setNodeId(nodeId);
        bundleNode.setBundleId(bundleId);
        bundleNode.setRepoPath(path);
        artifactBundlesDao.create(bundleNode);
    }

    private void createBundle(long id, String name, String version, BundleTransactionStatus status, BundleType type, String storingRepo )
            throws SQLException {
        DBArtifactsBundle bundle = new DBArtifactsBundle();
        bundle.setId(id);
        bundle.setName(name);
        bundle.setStatus(status);
        bundle.setDateCreated(DateTime.now());
        bundle.setVersion(version);
        bundle.setType(type);
        bundle.setStoringRepo(storingRepo);
        bundle.setSignature("Signature" + id);
        artifactBundlesDao.create(bundle);
    }

    private void createBundle(String name, BundleTransactionStatus status, String version, BundleType type, String storingRepo)
            throws SQLException {
        DBArtifactsBundle bundle = new DBArtifactsBundle();
        bundle.setId(++nextId);
        bundle.setName(name);
        bundle.setStatus(status);
        bundle.setDateCreated(DateTime.now());
        bundle.setVersion(version);
        bundle.setType(type);
        bundle.setStoringRepo(storingRepo);
        bundle.setSignature("Signature" + nextId);
        artifactBundlesDao.create(bundle);
    }

    public void testBundleFilters() throws SQLException {
        DBArtifactsBundle bundle = new DBArtifactsBundle();
        for (int i = 1; i <= 4; i++) {
            for (int j = 0; j <2 ; j++) {
                bundle.setId(++nextId);
                bundle.setName("myRBS" + i);
                bundle.setStatus(BundleTransactionStatus.COMPLETE);
                bundle.setDateCreated(DateTime.now());
                bundle.setVersion("1." + j);
                bundle.setType(BundleType.TARGET);
                bundle.setSignature("Signature");
                artifactBundlesDao.create(bundle);
            }
        }
        testFilterName();
        testFilterLimit();
        testFilterNameAndOrderBy();
        testFilterStarNameAndOrderBy();
        testFilterNameDefaultOrderBy();
        testFilterDates();
        testFilterUnderScoreNameAndOrderBy();

    }

    private void testFilterNameAndOrderBy() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("myRBS*")
                .orderBy("name")
                .direction("asc")
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 4);
        Assert.assertEquals(filteredArtifactsBundles.get(0).getName(), "myRBS1");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getName(), "myRBS2");
        Assert.assertEquals(filteredArtifactsBundles.get(0).getVersion(), "1.1");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getVersion(), "1.1");
    }

    private void testFilterName() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("myRBS1")
                .limit(3) //should not affect, limit implemented outside of the dao
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 1);
        Assert.assertEquals(filteredArtifactsBundles.get(0).getName(), "myRBS1");
        Assert.assertEquals(filteredArtifactsBundles.get(0).getVersion(), "1.1");
    }

    private void testFilterLimit() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("?")
                .daoLimit(3) //should not affect, limit implemented outside of the dao
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 3);
    }

    private void testFilterNameDefaultOrderBy() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("myRBS")
                .limit(3) //should not affect, limit implemented outside of the dao
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 4);
        Assert.assertEquals(filteredArtifactsBundles.get(0).getName(), "myRBS4");
        Assert.assertEquals(filteredArtifactsBundles.get(0).getVersion(), "1.1");
    }

    private void testFilterStarNameAndOrderBy() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("my*")
                .orderBy("name")
                .direction("desc")
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 4);
        Assert.assertEquals(filteredArtifactsBundles.get(0).getName(), "myRBS4");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getName(), "myRBS3");
        Assert.assertEquals(filteredArtifactsBundles.get(0).getVersion(), "1.1");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getVersion(), "1.1");
    }

    private void testFilterDates() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("my*")
                .after(1013572468190L)
                .before(1913572468190L)
                .orderBy("name")
                .direction("desc")
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 4);
        Assert.assertEquals(filteredArtifactsBundles.get(0).getName(), "myRBS4");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getName(), "myRBS3");
        Assert.assertEquals(filteredArtifactsBundles.get(0).getVersion(), "1.1");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getVersion(), "1.1");
    }

    private void testFilterUnderScoreNameAndOrderBy() throws SQLException {
        ReleaseBundleSearchFilter filter = ReleaseBundleSearchFilter.builder()
                .bundleType(BundleType.TARGET)
                .name("m_")
                .orderBy("name")
                .direction("desc")
                .build();
        List<DBArtifactsBundle> filteredArtifactsBundles = artifactBundlesDao.getFilteredBundlesLastVersion(filter);

        Assert.assertEquals(filteredArtifactsBundles.size(), 4);
        Assert.assertEquals(filteredArtifactsBundles.get(0).getName(), "myRBS4");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getName(), "myRBS3");
        Assert.assertEquals(filteredArtifactsBundles.get(0).getVersion(), "1.1");
        Assert.assertEquals(filteredArtifactsBundles.get(1).getVersion(), "1.1");
    }
}
