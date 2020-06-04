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

package org.artifactory.io.checksum;

import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.jfrog.security.util.Pair;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the ChecksumCalculator.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumsTest {

    public void calculateSha1() throws IOException {
        byte[] bytes = "this is a test".getBytes();
        String result = ChecksumUtils.getChecksum(new ByteArrayInputStream(bytes), ChecksumType.sha1);
        assertEquals(result, "fa26be19de6bff93f70bc2308434e4a440bbad02", "Wrong SHA1 calculated");
    }

    public void calculateSha2() throws IOException {
        byte[] bytes = "this is a test".getBytes();
        String result = ChecksumUtils.getChecksum(new ByteArrayInputStream(bytes), ChecksumType.sha256);
        assertEquals(result, "2e99758548972a8e8822ad47fa1017ff72f06f3ff6a016851f45c398732bc50c", "Wrong SHA256 calculated");
    }

    public void calculateMd5() throws IOException {
        byte[] bytes = "this is a test".getBytes();
        String result = ChecksumUtils.getChecksum(new ByteArrayInputStream(bytes), ChecksumType.md5);
        assertEquals(result, "54b0c58c7ce9f2a8b551351102ee0938", "Wrong SHA1 calculated");
    }

    public void calculateSha1Sha2AndMd5() throws IOException {
        byte[] bytes = "and this is another test".getBytes();
        ChecksumsInfo results = ChecksumUtils.getChecksumsInfo(new ByteArrayInputStream(bytes));
        assertNotNull(results, "Results should not be null");
        assertEquals(results.size(), 3, "Expecting three calculated value");
        assertEquals(results.getSha1(), "5258d99970d60aed055c0056a467a0422acf7cb8", "Wrong SHA1 calculated");
        assertEquals(results.getSha256(), "888973e0d19ad6d2c63752bab2f7fbe614d11a68b7d1434a068fd4ff37c26db3", "Wrong SHA2 calculated");
        assertEquals(results.getMd5(), "72f1aea68f75f79889b99cd4ff7acc83", "Wrong MD5 calculated");
    }

    public void calculateAllKnownChecksums() throws IOException {
        byte[] bytes = "and this is yet another test".getBytes();
        Pair<Long, ChecksumsInfo> results = ChecksumUtils.getSizeAndChecksumsInfo(new ByteArrayInputStream(bytes), ChecksumType.values());
        ChecksumsInfo checksumsInfo = results.getSecond();
        assertNotNull(checksumsInfo, "Results should not be null");
        assertEquals(checksumsInfo.size(), ChecksumType.values().length, "Expecting all checksums calculated value");
        assertEquals(checksumsInfo.getSha1(), "4e125432334dc76048aab3132a1bbc03c79f27e9", "Wrong SHA1 calculated");
        assertEquals(checksumsInfo.getMd5(), "0df8861dcef78d35aae9c6c6c8c69506", "Wrong MD5 calculated");
        assertEquals(checksumsInfo.getSha256(), "02a5357cde4bf21ba850fce37721406475a0c311dbbdffc2c79148ece46049b3", "Wrong SHA256 calculated");
    }

}
