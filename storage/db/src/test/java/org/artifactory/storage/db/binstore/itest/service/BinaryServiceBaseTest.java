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

package org.artifactory.storage.db.binstore.itest.service;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.storage.binstore.service.GarbageCollectorInfo;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.artifactory.storage.db.binstore.service.BinaryInfoImpl;
import org.artifactory.storage.db.binstore.service.BinaryServiceImpl;
import org.artifactory.storage.db.fs.dao.NodesDao;
import org.artifactory.storage.db.fs.entity.Node;
import org.artifactory.storage.db.fs.entity.NodeBuilder;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.jfrog.common.ResourceUtils;
import org.jfrog.storage.binstore.exceptions.BinaryNotFoundException;
import org.jfrog.storage.common.StorageUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

/**
 * Date: 12/10/12
 * Time: 9:54 PM
 *
 * @author freds
 */
public abstract class BinaryServiceBaseTest extends DbBaseTest {

    @Autowired
    protected BinaryServiceImpl binaryStore;

    @Autowired
    protected NodesDao nodesDao;

    @Autowired
    protected BinariesDao binariesDao;

    @Override
    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        ArtifactoryHomeBoundTest artifactoryHomeTest = super.createArtifactoryHomeTest();
        artifactoryHomeTest.bindArtifactoryHome();
        ArtifactoryHome artifactoryHome = ArtifactoryHome.get();

        File workDir = new File("target", "binstoretest").getAbsoluteFile();
        // Inject the new home dir and data dir to Artifactory home
        TestUtils.setField(artifactoryHome, "homeDir", workDir);
        TestUtils.setField(artifactoryHome, "dataDir", artifactoryHome.getOrCreateSubDir("data"));
        // find the filestore dir
        File filestoreDir = getFilestoreFolder(artifactoryHome);
        if (filestoreDir.exists()) {
            // cleanup the filestore dir
            FileUtils.deleteDirectory(filestoreDir);
            assertFalse(filestoreDir.exists(), "Could not clean filestore " + filestoreDir.getAbsolutePath());
        }
        return artifactoryHomeTest;
    }

    private File getFilestoreFolder(ArtifactoryHome artifactoryHome) {
        String binaryStoreDirName = getBinaryStoreDirName();
        File filestoreDir;
        if (new File(binaryStoreDirName).isAbsolute()) {
            filestoreDir = new File(binaryStoreDirName);
        } else {
            filestoreDir = new File(artifactoryHome.getDataDir(), binaryStoreDirName);
        }
        return filestoreDir;
    }

    @BeforeClass
    public void initBinaryStore() throws IOException {
        try {
            // Bind artifactory context
            bindDummyContext();
            // update BinaryProviderProperties
            updateBinaryStoreXml();
            binaryStore.initialize();
        } finally {
            unbindDummyContext();
        }
    }

    protected void updateBinaryStoreXml() throws IOException {
        File binaryStoreXmlFile = ArtifactoryHome.get().getBinaryStoreXmlFile();
        FileUtils.writeStringToFile(binaryStoreXmlFile, getBinaryStoreContent());
        // customize the storage properties before the binary store initialization
        String binaryStoreDirName = getBinaryStoreDirName();
        if (binaryStoreDirName.startsWith("cache")) {
            updateBinaryStoreXml("##baseDataDir##", binaryStoreDirName);
        } else {
            updateBinaryStoreXml("##baseDataDir##", binaryStoreDirName);
        }
    }

    private void updateBinaryStoreXml(String key, String value) throws IOException {
        File binaryStoreXmlFile = ArtifactoryHome.get().getBinaryStoreXmlFile();
        String binaryStoreXmlContent = FileUtils.readFileToString(binaryStoreXmlFile);
        binaryStoreXmlContent = binaryStoreXmlContent.replace(key, value);
        FileUtils.writeStringToFile(binaryStoreXmlFile, binaryStoreXmlContent);
    }

    protected abstract String getBinaryStoreContent();

    protected abstract String getBinaryStoreDirName();

    @DataProvider(name = "testBinFiles")
    protected static Object[][] getBinFileData() {
        // File Name, SHA1, SHA2, MD5, size, nodeId
        return new Object[][]{
                {"100c.bin", "8018634e43a47494119601b857356a5a1875f888", "0eba69759662ff669ba681e4432d8836b3200f9afb632f59af2d16338d95776c", "7c9703f5909d78ab0bf18147aee0a5b3", 100L, 71L},
                {"300c.bin", "e5dc83f4c8d6f5f23c00b61ee40dfcbf18c0a7ba", "593a23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f604ba4", "270a150e83246818c8524cd04514aa67", 300L, 72L},
                {"256w.bin", "b397ec1546ff6ada8c937cc8f8d988be57324f5e", "84495e734090fee6588f6c581567491790c67a9c7679457977ca72b872fe6d14", "1a8e102c605bbb502d36848d48989498", 512L, 73L},
                {"2k.bin", "195573fd008c06ea08fb66c2bbe76d4995b3f40a", "9f58f055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280", "76632b91e81884b19b88ff86850b8f2e", 2048L, 74L},
        };
    }

    @Test
    public void testEmpty() throws IOException {
        // Check initialized with folders correctly
        File filestoreDir = getFilestoreFolder(ArtifactoryHome.get());
        assertEquals(binaryStore.getBinariesDir().getAbsolutePath(), filestoreDir.getAbsolutePath());
        File[] files = filestoreDir.listFiles();
        assertNotNull(files);
        assertEquals(files.length, 1);
        File preFolder = files[0];
        assertTrue(preFolder.isDirectory(), "File " + preFolder.getAbsolutePath() + " should be a folder");
        assertEquals(preFolder.getName(), "_pre");

        // Ping all OK => Ping throws exception if something wrong
        binaryStore.ping();

        // Check all is empty
        assertTrue(binaryStore.findAllBinaries().isEmpty());
        assertEquals(binaryStore.getStorageSize(), 0L);

        // Finder should returns null and empty collections
        Set<String> allChecksums = Sets.newHashSet();
        Object[][] binFileData = getBinFileData();
        for (Object[] binFile : binFileData) {
            String sha1 = (String) binFile[1];
            String sha2 = (String) binFile[2];
            String md5 = (String) binFile[3];
            assertNull(binaryStore.findBinary(ChecksumType.sha1, sha1));
            assertNull(binaryStore.findBinary(ChecksumType.sha256, sha2));
            assertNull(binaryStore.findBinary(ChecksumType.md5, md5));
            allChecksums.add(sha1);
            allChecksums.add(sha2);
            allChecksums.add(md5);
        }
        assertTrue(binaryStore.findBinaries(allChecksums).isEmpty());

        // Garbage collection should do nothing
        GarbageCollectorInfo collectorInfo = binaryStore.runFullGarbageCollect();
        assertEquals(collectorInfo.initialSize, 0L);
        assertEquals(collectorInfo.initialCount, 0);
        assertEquals(collectorInfo.candidatesForDeletion, 0);
        assertEquals(collectorInfo.binariesCleaned.get(), 0);
        assertEquals(collectorInfo.checksumsCleaned.get(), 0);
        assertEquals(collectorInfo.totalSizeCleaned.get(), 0L);

        testPrune(0, 0, 0);

        for (Object[] binData : binFileData) {
            checkSha1OnEmpty((String) binData[1]);
        }
    }

    //[sha2]: add proper tests when binarystore switches to full sha2
    protected void checkSha1OnEmpty(String sha1) throws IOException {
        assertNull(binaryStore.findBinary(ChecksumType.sha1, sha1));
        boolean exist = binaryStore.isFileExist(sha1);
        assertFalse(exist);
        assertBinaryExistsEmpty(sha1);
    }

    //[sha2]: add proper tests when binarystore switches to full sha2
    protected abstract void assertBinaryExistsEmpty(String sha1) throws IOException;

    protected void defaultAssertBinaryExistsEmpty(String sha1) {
        InputStream bis = null;
        try {
            bis = binaryStore.getBinary(sha1);
            fail("Should have sent " + BinaryNotFoundException.class + " exception!");
        } catch (BinaryNotFoundException e) {
            // Message should be "Couldn't find content for '" + sha1 + "'"
            String message = e.getMessage();
            assertTrue(message.contains("content for '" + sha1 + "'"), "Wrong exception message " + message);
        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    @Test(dependsOnMethods = "testEmpty", dataProvider = "testBinFiles")
    public void testLoadResources(final String resName, final String sha1, final String sha2, final String md5,
            final long length, long nodeId) throws IOException, SQLException {
        BinaryInfo binaryInfo = dbService.invokeInTransaction("testLoadResources",
                () -> addBinary(resName, sha1, sha2, md5, length));
        assertNotNull(binaryInfo);
        assertEquals(binaryInfo.getSha1(), sha1);
        assertEquals(binaryInfo.getSha2(), sha2);
        assertEquals(binaryInfo.getMd5(), md5);
        assertEquals(binaryInfo.getLength(), length);
        // Add a dummy node to enforce usage...
        createDummyNode(resName, sha1, sha2, md5, length, nodeId);
    }

    @Test(dependsOnMethods = "testLoadedNothingToDelete")
    public void testReloadResourceWithoutSha2() throws IOException, SQLException {
        String resourceName = "cat.bin";
        String os = System.getProperty("os.name");
        String sha1 = os.contains("Win") ? "f0aedf295071ed34ab8c6a7692223d22b6a19841" :
                "e5fa44f2b31c1fb553b6021e7360d07d5d91ff5e";
        String sha2 = os.contains("Win") ? "f1b2f662800122bed0ff255693df89c4487fbdcf453d3524a42d4ec20c3d9c04" :
                "4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865";
        String md5 = os.contains("Win") ? "a5ea0ad9260b1550a14cc58d2c39b03d" : "b026324c6904b2a9cb4b88d6d61c81d1";
        long length = 4L;

        BinaryInfo binaryInfo = dbService.invokeInTransaction("testLoadResources",
                () -> {
                    logger.info("Adding binary ");
                    BinaryInfo binaryInfo1 = addBinary(resourceName, sha1, sha2, md5, length);
                    if (binaryInfo1 != null) {
                        logger.info("Binary sha1: " + binaryInfo1.getSha1());
                        logger.info("Binary sha2: " + binaryInfo1.getSha2());
                        logger.info("Binary md5: " + binaryInfo1.getMd5());
                    }
                    binaryStore.updateSha2ForSha1(sha1, " "); // put blank sha2 to simulate an old resource
                    Collection<BinaryInfo> allBinaries = binaryStore.findAllBinaries();
                    allBinaries.forEach(info -> logger.info(info.getSha1()));
                    logger.info("Searching for binary: " + sha1);
                    return binaryStore.findBinary(ChecksumType.sha1, sha1);
                });
        assertNotNull(binaryInfo, "After first insert ");
        assertEquals(binaryInfo.getSha1(), sha1);
        assertEquals(binaryInfo.getSha2().trim(), ""); // sha2 value is padded with spaces

        binaryInfo = dbService.invokeInTransaction("testLoadResources",
                () -> addBinary(resourceName, sha1, sha2, md5, length));
        assertNotNull(binaryInfo);
        assertEquals(binaryInfo.getSha1(), sha1);
        assertEquals(binaryInfo.getSha2(), sha2); // sha2 value has been updated even though this binary already existed
    }

    private void createDummyNode(String resName, String sha1, String sha2, String md5, long length, long nodeId)
            throws SQLException {
        Node node = new NodeBuilder().nodeId(nodeId).repo("repo1").name(resName).file(true)
                .sha1Actual(sha1).sha2(sha2).md5Actual(md5).length(length).build();
        nodesDao.create(node);
    }

    protected abstract BinaryInfo addBinary(String resName, String sha1, String sha2, String md5, long length)
            throws IOException;

    @Test(dependsOnMethods = "testLoadResources")
    public void testLoadedNothingToDelete() throws IOException, SQLException {
        Object[][] binFileData = getBinFileData();
        Map<String, Object[]> subFolders = getSubFoldersMap();

        // Check initialized with folders correctly
        checkBinariesDirAfterLoad(subFolders);

        // Ping all OK => Ping throws exception if something wrong
        binaryStore.ping();

        // Check store size match
        Collection<BinaryInfo> allBinaries = binaryStore.findAllBinaries();
        assertEquals(allBinaries.size(), 4);
        long totSize = 0L;
        for (BinaryInfo allBinary : allBinaries) {
            long expected = assertBinData(allBinary, subFolders);
            totSize += expected;
        }
        assertEquals(binaryStore.getStorageSize(), totSize);

        // Finder should returns null and empty collections
        Set<String> allSha1Checksums = Sets.newHashSet();
        Set<String> allSha2Checksums = Sets.newHashSet();
        Set<String> allMd5Checksums = Sets.newHashSet();
        for (Object[] binFile : binFileData) {
            String sha1 = (String) binFile[1];
            BinaryInfo bd = binaryStore.findBinary(ChecksumType.sha1, sha1);
            Assert.assertNotNull(bd);
            assertBinData(bd, binFile);
            allSha1Checksums.add(sha1);
            allSha2Checksums.add((String) binFile[2]);
            allMd5Checksums.add((String) binFile[3]);
        }
        Set<BinaryInfo> binaries = binaryStore.findBinaries(allSha1Checksums);
        assertEquals(binaries.size(), 4);
        for (BinaryInfo binary : binaries) {
            assertBinData(binary, subFolders);
        }
        binaries = binaryStore.findBinaries(allSha2Checksums);
        assertEquals(binaries.size(), 4);
        for (BinaryInfo binary : binaries) {
            assertBinData(binary, subFolders);
        }
        binaries = binaryStore.findBinaries(allMd5Checksums);
        assertEquals(binaries.size(), 4);
        for (BinaryInfo binary : binaries) {
            assertBinData(binary, subFolders);
        }

        for (Object[] binData : binFileData) {
            String sha1 = (String) binData[1];
            String sha2 = (String) binData[2];
            String md5 = (String) binData[3];
            // Should NOT print warning => TODO: How to test for that?
            long size = (Long) binData[4];
            long nodeId = (long) binData[5];
            nodesDao.delete(nodeId);
            Collection<BinaryEntity> potentialDeletion = binariesDao.findPotentialDeletion();
            assertEquals(potentialDeletion.size(), 1);
            assertBinData(potentialDeletion.iterator().next(), binData);
            BinaryInfo binaryInfo = binaryStore.findBinary(ChecksumType.sha1, sha1);
            assertNotNull(binaryInfo);
            assertBinData(binaryInfo, binData);
            createDummyNode("trans-" + binData[0], binaryInfo.getSha1(), binaryInfo.getSha2(),
                    binaryInfo.getMd5(), binaryInfo.getLength(), nodeId);
            assertTrue(binariesDao.findPotentialDeletion().isEmpty());

            try (InputStream bis = binaryStore.getBinary(sha1)) {
                assertEquals(IOUtils.toByteArray(bis),
                        IOUtils.toByteArray(ResourceUtils.getResource("/binstore/" + binData[0])));
            }

            BinaryInfo info = binaryStore.findBinary(ChecksumType.sha256, sha2);
            assertNotNull(info);
            assertEquals(info.getSha2(), sha2);
            assertEquals(info.getSha1(), sha1);
            try (InputStream bis = binaryStore.getBinary(info.getSha1())) {
                assertEquals(IOUtils.toByteArray(bis),
                        IOUtils.toByteArray(ResourceUtils.getResource("/binstore/" + binData[0])));
            }
            assertFileExistsAfterLoad(sha1, size);
        }

        // Garbage collection should do nothing
        GarbageCollectorInfo collectorInfo = binaryStore.runFullGarbageCollect();
        assertEquals(collectorInfo.initialSize, totSize);
        assertEquals(collectorInfo.initialCount, 4);
        assertEquals(collectorInfo.candidatesForDeletion, 0);
        assertEquals(collectorInfo.binariesCleaned.get(), 0);
        assertEquals(collectorInfo.checksumsCleaned.get(), 0);
        assertEquals(collectorInfo.totalSizeCleaned.get(), 0L);

        testPruneAfterLoad();
    }

    public static Map<String, Object[]> getSubFoldersMap() {
        Object[][] binFileData = getBinFileData();
        Map<String, Object[]> subFolders = Maps.newHashMap();
        subFolders.put("_pre", new Object[0]);
        for (Object[] binFile : binFileData) {
            String folderName = ((String) binFile[1]).substring(0, 2);
            subFolders.put(folderName, binFile);
        }
        return subFolders;
    }

    protected void assertFileExistsAfterLoad(String sha1, long size) {
        boolean binaryStoreFileExist = binaryStore.isFileExist(sha1);
        assertTrue(binaryStoreFileExist);
    }

    protected void checkBinariesDirAfterLoad(Map<String, Object[]> subFolders) {
        File filestoreDir = binaryStore.getBinariesDir();
        checkFilestoreDirIsFull(subFolders, filestoreDir);
    }

    protected void checkFilestoreDirIsFull(Map<String, Object[]> subFolders, File filestoreDir) {
        File[] files = filestoreDir.listFiles();
        assertNotNull(files);
        assertEquals(files.length, 5);
        checkFilesAreValid(subFolders, files);
    }

    protected void checkFilesAreValid(Map<String, Object[]> subFolders, File[] files) {
        for (File file : files) {
            assertTrue(file.isDirectory(), "File " + file.getAbsolutePath() + " should be a folder");
            String fileName = file.getName();
            assertTrue(subFolders.containsKey(fileName), "File " + file + " should be part of " + subFolders.keySet());
            File[] list = file.listFiles();
            assertNotNull(list);
            Object[] binData = subFolders.get(fileName);
            if (binData.length == 6) {
                // Real data one file matching dataProvider
                assertEquals(list.length, 1);
                assertEquals(list[0].getName(), (String) binData[1]);
                assertEquals(list[0].length(), ((Long) binData[4]).longValue());
            } else {
                // Make sure pre is empty
                assertEquals(list.length, 0);
            }
        }
    }

    protected abstract void testPruneAfterLoad();

    protected BasicStatusHolder testPrune(int folders, int files, int bytes) {
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        binaryStore.prune(statusHolder);
        String statusMsg = statusHolder.getStatusMsg();
        assertFalse(statusHolder.isError(), "Error during empty pruning: " + statusMsg);
        assertMessageContains(statusMsg, folders, files, bytes);
        return statusHolder;
    }

    protected void assertMessageContains(String statusMsg, int folders, int files, int bytes) {
        String expected = "" + files + " files";
        assertTrue(statusMsg.contains(expected), "Expected '" + expected + "' got status message '" + statusMsg + "'");
        expected = "size of " + StorageUnit.toReadableString(bytes);
        assertTrue(statusMsg.contains(expected), "Expected '" + expected + "' got status message '" + statusMsg + "'");
    }

    @Test(dependsOnMethods = "testLoadedNothingToDelete", dataProvider = "testBinFiles")
    public void testGarbageOneByOne(String resName, String sha1, String sha2, String md5, long length, long nodeId)
            throws IOException, SQLException {
        // Read the stream to lock reader
        InputStream bis = null;
        try {
            bis = binaryStore.getBinary(sha1);
            nodesDao.delete(nodeId);

            // Verify node ready for deletion
            Collection<BinaryEntity> potentialDeletion = binariesDao.findPotentialDeletion();
            assertEquals(potentialDeletion.size(), 1);
            assertBinData(potentialDeletion.iterator().next(), new Object[]{resName, sha1, sha2, md5, length});

            // No GC since file is being read
            GarbageCollectorInfo collectorInfo = binaryStore.runFullGarbageCollect();
            assertEquals(collectorInfo.candidatesForDeletion, 1);
            assertEquals(collectorInfo.binariesCleaned.get(), 0);
            assertEquals(collectorInfo.checksumsCleaned.get(), 0);
            assertEquals(collectorInfo.totalSizeCleaned.get(), 0L);

            bis.close();

            // Now GC works
            collectorInfo = binaryStore.runFullGarbageCollect();
            assertEquals(collectorInfo.candidatesForDeletion, 1);
            assertEquals(collectorInfo.binariesCleaned.get(), 1);
            assertEquals(collectorInfo.checksumsCleaned.get(), 1);
            assertEquals(collectorInfo.totalSizeCleaned.get(), length);

            assertPruneAfterOneGc();
        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    protected abstract void assertPruneAfterOneGc();

    public static long assertBinData(BinaryInfo bd, Map<String, Object[]> binDataMap) {
        Object[] binData = binDataMap.get(bd.getSha1().substring(0, 2));
        return assertBinData(bd, binData);
    }

    public static long assertBinData(BinaryInfo binaryInfo, Object[] binData) {
        assertNotNull(binData);
        assertEquals(binaryInfo.getSha1(), (String) binData[1]);
        assertEquals(binaryInfo.getSha2(), (String) binData[2]);
        assertEquals(binaryInfo.getMd5(), (String) binData[3]);
        long expected = (Long) binData[4];
        assertEquals(binaryInfo.getLength(), expected);
        return expected;
    }

    public static long assertBinData(BinaryEntity binaryEntity, Object[] binData) {
        assertNotNull(binData);
        BinaryInfo info = new BinaryInfoImpl(binaryEntity.getSha1(), binaryEntity.getSha2(), binaryEntity.getMd5(),
                binaryEntity.getLength());
        return assertBinData(info, binData);
    }
}
