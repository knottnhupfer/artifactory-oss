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

package org.artifactory.storage.db.binstore.itest.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.artifactory.api.storage.BinariesInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.binstore.dao.BinariesDao;
import org.artifactory.storage.db.binstore.entity.BinaryEntity;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * Low level tests of the {@link org.artifactory.storage.db.binstore.dao.BinariesDao}.
 *
 * @author Yossi Shaul
 */
@Test
public class BinariesDaoTest extends DbBaseTest {

    @Autowired
    private BinariesDao binariesDao;

    @BeforeClass
    public void setup() {
        importSql("/sql/binaries.sql");
    }

    public void binaryExists() throws SQLException {
        assertTrue(binariesDao.exists(ChecksumType.sha1, "f0d381ab0e057d4f835d639f6330a7c3e81eb6af"));
    }

    public void binaryNotExists() throws SQLException {
        assertFalse(binariesDao.exists(ChecksumType.sha1, "699a458d52a1ca32cecb4d7c1ea50b89ea9ecaf9"));
    }

    public void loadExistingNode() throws SQLException {
        BinaryEntity binaryEntity = binariesDao.load(ChecksumType.sha1, "f0d381ab0e057d4f835d639f6330a7c3e81eb6af");
        assertNotNull(binaryEntity);
        assertEquals(binaryEntity.getSha1(), "f0d381ab0e057d4f835d639f6330a7c3e81eb6af");
        assertEquals(binaryEntity.getSha2(), "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd");
        assertEquals(binaryEntity.getMd5(), "902a360ecad98a34b59863c1e65bcf71");
        assertEquals(binaryEntity.getLength(), 2725);
    }

    public void loadNonExistingNode() throws SQLException {
        BinaryEntity binaryEntity = binariesDao.load(ChecksumType.sha1, "62540a41c0b21fd3739565f7d961db07b760bfb8");
        assertNull(binaryEntity);

        binaryEntity = binariesDao.load(ChecksumType.sha256, "fdec23029162f3b2dc51f1111b64bce8cb69abcd6e540f23ec567d898f60abcd");
        assertNull(binaryEntity);
    }

    @Test(dependsOnMethods = {"findPotentialDeletion"} )
    public void createBinary() throws SQLException {
        BinaryEntity binaryEntity = new BinaryEntity("1bae873f4a13f2919a4205aff0722b44ead4b190",
                "dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3yyyy",
                "666a360ecad98a34b59863c1e65bcf71", 20);
        assertFalse(binariesDao.exists(ChecksumType.sha1, binaryEntity.getSha1()));
        boolean created = binariesDao.create(binaryEntity);
        assertTrue(created);
        assertTrue(binariesDao.exists(ChecksumType.sha1, binaryEntity.getSha1()));
        BinaryEntity loadedData = binariesDao.load(ChecksumType.sha1, "1bae873f4a13f2919a4205aff0722b44ead4b190");
        assertNotNull(loadedData);
        assertEquals(loadedData.getSha1(), binaryEntity.getSha1());
        assertEquals(loadedData.getSha2(), binaryEntity.getSha2());
        assertEquals(loadedData.getMd5(), binaryEntity.getMd5());
        assertEquals(loadedData.getLength(), binaryEntity.getLength());
    }

    @Test(expectedExceptions = SQLException.class)
    public void createExistingBinary() throws SQLException {
        BinaryEntity binaryEntity = new BinaryEntity("f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd",
                "902a360ecad98a34b59863c1e65bcf71", 20);
        binariesDao.create(binaryEntity);
    }

    public void findPotentialDeletion() throws SQLException {
        Collection<BinaryEntity> potentialDeletion = binariesDao.findPotentialDeletion();
        assertEquals(potentialDeletion.size(), 3);
        verifyAllPotentialForDeletionReturned(potentialDeletion);
    }

    private void verifyAllPotentialForDeletionReturned(Collection<BinaryEntity> potentialDeletion) {
        Set<String> nodesSha1 = potentialDeletion.stream()
                .map(binaryEntity -> binaryEntity == null ? "" : binaryEntity.getSha1())
                .collect(Collectors.toSet());
        assertTrue(nodesSha1.contains("356a192b7913b04c54574d18c28d46e6395428ab"));
        assertTrue(nodesSha1.contains("74239116da1def240fe1d366eb535513efc1c40b"));
        assertTrue(nodesSha1.contains("da39a3ee5e6b4b0d3255bfef95601890afd80709"));

        Set<String> nodesSha2 = potentialDeletion.stream()
                .map(binaryEntity -> binaryEntity == null ? "" : binaryEntity.getSha2())
                .collect(Collectors.toSet());
        assertTrue(nodesSha2.contains("dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60ddac"));
        assertTrue(nodesSha2.contains("yyyy23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60yyyy"));
        assertTrue(nodesSha2.contains("dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280"));
    }

    public void AssertPotentialDeletionOrder() throws SQLException {
        Collection<BinaryEntity> potentialDeletion = binariesDao.findPotentialDeletion();
        assertEquals(potentialDeletion.size(), 3);
        List<String> nodes = potentialDeletion.stream()
                .map(binaryEntity -> binaryEntity == null ? "" : binaryEntity.getSha1())
                .collect(Collectors.toList());
        // verify candidates are ordered by size
        assertEquals(nodes.get(0), "74239116da1def240fe1d366eb535513efc1c40b");
        assertEquals(nodes.get(1), "356a192b7913b04c54574d18c28d46e6395428ab");
        assertEquals(nodes.get(2), "da39a3ee5e6b4b0d3255bfef95601890afd80709");
    }

    @Test(dependsOnMethods = "createBinary")
    public void testGetCountAndTotalSize() throws SQLException {
        BinariesInfo binariesInfo = binariesDao.getCountAndTotalSize();
        assertEquals(binariesInfo.getBinariesCount(), 6L);
        assertEquals(binariesInfo.getBinariesSize(), 3 + 2725 + 33670080 + 1 + 20);
    }

    @Test(dependsOnMethods = {
            "testGetCountAndTotalSize",
            "findChecksumsByMd5",
            "findChecksumsBySha1"
    })
    public void testDeleteEntries() throws SQLException {
        ImmutableSet<String> deleteTest = ImmutableSet.of(
                "74239116da1def240fe1d366eb535513efc1c40b",
                "f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                "da39a3ee5e6b4b0d3255bfef95601890afd80709"
        );
        int nbDeleted = 0;
        for (String sha1ToDelete : deleteTest) {
            nbDeleted += binariesDao.deleteEntry(sha1ToDelete);
        }
        assertEquals(nbDeleted, 2);
        BinariesInfo countAndTotalSize = binariesDao.getCountAndTotalSize();
        assertEquals(countAndTotalSize.getBinariesCount(), 4L);
        assertEquals(countAndTotalSize.getBinariesSize(), 3 + 2725 + 1 + 20);
    }

    public void findChecksumsBySha1() throws SQLException {
        Collection<BinaryEntity> nodes = binariesDao.search(ChecksumType.sha1, ImmutableList.of(
                "f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                "deaddeaddeaddeaddeaddeaddeaddeaddeaddead",
                "da39a3ee5e6b4b0d3255bfef95601890afd80709"
        ));
        assertFoundNodes(nodes);
    }

    public void findChecksumsBySha2() throws SQLException {
        Collection<BinaryEntity> nodes = binariesDao.search(ChecksumType.sha256, ImmutableList.of(
                "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd",
                "deaddeaddeaddeaddeaddeaddeaddeaddeaddeadxxxxxxxxxxxxxxxxxxxxxxxx",
                "dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280"
        ));
        assertFoundNodes(nodes);
    }

    public void findChecksumsByMd5() throws SQLException {
        Collection<BinaryEntity> nodes = binariesDao.search(ChecksumType.md5, ImmutableList.of(
                "902a360ecad98a34b59863c1e65bcf71",
                "602a360ecad98a34b59863c1e65bcf71",
                "402a360ecad98a34b59863c1e65bc222"
        ));
        assertFoundNodes(nodes);
    }

    // RTFACT-6364 - Oracle limits the number of elements in the IN clause to 1000
    public void findChecksumsBySha1ThousandLimit() throws SQLException {
        findChecksumsByThousandLimit(ChecksumType.sha1);
    }

    public void findChecksumsBySha2ThousandLimit() throws SQLException {
        findChecksumsByThousandLimit(ChecksumType.sha256);
    }

    private void findChecksumsByThousandLimit(ChecksumType checksumType) throws SQLException {
        List<String> checksums = Lists.newArrayListWithCapacity(2000);
        for (int i = 0; i < 999; i++) {
            checksums.add(getRandomChecksum(checksumType));
        }
        binariesDao.search(checksumType, checksums);

        // 1000
        checksums.add(getRandomChecksum(checksumType));
        binariesDao.search(checksumType, checksums);

        // 1001 (fails if not chunked in Oracle)
        checksums.add(getRandomChecksum(checksumType));
        binariesDao.search(ChecksumType.sha1, checksums);
    }

    private String getRandomChecksum(ChecksumType checksumType) {
        String randomChecksum = "";
        if (ChecksumType.sha1.equals(checksumType)) {
            randomChecksum = randomSha1();
        } else if (ChecksumType.sha256.equals(checksumType)) {
            randomChecksum = randomSha2();
        }
        return randomChecksum;
    }

    public void testFindEmptyChecksumsBySha1() throws SQLException {
        Collection<BinaryEntity> nodes = binariesDao.search(ChecksumType.sha1, new ArrayList<>(1));
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    public void testFindEmptyChecksumsBySha2() throws SQLException {
        Collection<BinaryEntity> nodes = binariesDao.search(ChecksumType.sha256, new ArrayList<>(1));
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    public void testFindEmptyChecksumsByMd5() throws SQLException {
        Collection<BinaryEntity> nodes = binariesDao.search(ChecksumType.md5, new ArrayList<>(1));
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }

    private void assertFoundNodes(Collection<BinaryEntity> nodes) {
        assertNotNull(nodes);
        assertEquals(nodes.size(), 2);
        for (BinaryEntity node : nodes) {
            BinaryEntity expected = null;
            switch ((int) node.getLength()) {
                case 2725:
                    expected = new BinaryEntity("f0d381ab0e057d4f835d639f6330a7c3e81eb6af",
                            "dddd23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60dddd",
                            "902a360ecad98a34b59863c1e65bcf71", 2725);
                    break;
                case 0:
                    expected = new BinaryEntity("da39a3ee5e6b4b0d3255bfef95601890afd80709",
                            "dcabf055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280",
                            "602a360ecad98a34b59863c1e65bcf71", 0);
                    break;
                default:
                    fail("Binary data " + node + " unexpected!");
            }
            assertEquals(node.getSha1(), expected.getSha1());
            assertEquals(node.getSha2(), expected.getSha2());
            assertEquals(node.getMd5(), expected.getMd5());
            assertEquals(node.getLength(), expected.getLength());
        }
    }
}
