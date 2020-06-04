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

package org.artifactory.checksum;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the ChecksumInfo class.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumInfoTest {

    private static final String DUMMY_UPPER_CASE_SHA1 = "1234567890ABCD56789012345678901234567890";
    static final String DUMMY_SHA1 = "1234567890123456789012345678901234567890";
    static final String DUMMY2_SHA1 = "3234567890123456789012345678901234567890";

    private static final String DUMMY_UPPER_CASE_MD5 = "1234567890ABCD567890123456789012";
    static final String DUMMY_MD5 = "12345678901234567890123456789012";
    static final String DUMMY2_MD5 = "32345678901234567890123456789012";

    private static final String DUMMY_UPPER_CASE_SHA2 = "1ADC23029162F3B2DC51F512CB64CBE8CB6913ED6E540F23EC567D898F60E3B5";
    static final String DUMMY_SHA2 = "1adc23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60e3b5";
    static final String DUMMY2_SHA2 = "cf6a23029162f3b2dc51f512cb64bce8cb6913ed6e540f23ec567d898f60d4ef";

    @Test(expectedExceptions = IllegalStateException.class)
    public void wrongSha1Actual() {
        new ChecksumInfo(ChecksumType.sha1, DUMMY_SHA1, "f");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void wrongMd5Actual() {
        new ChecksumInfo(ChecksumType.md5, DUMMY_MD5, "f");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void wrongSha2Actual() {
        new ChecksumInfo(ChecksumType.sha256, DUMMY_SHA2, "f");
    }

    public void matchSameOriginalAndActual() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, DUMMY_SHA1, DUMMY_SHA1);
        assertTrue(infoSha1.checksumsMatch(), "SHA1 Checksums should match");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, DUMMY2_MD5, DUMMY2_MD5);
        assertTrue(infoMd5.checksumsMatch(), "MD5 Checksums should match");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, DUMMY2_SHA2, DUMMY2_SHA2);
        assertTrue(infoSha2.checksumsMatch(), "SHA2 Checksums should match");
    }

    public void matchDifferentOriginalAndActual() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, DUMMY_SHA1, DUMMY2_SHA1);
        assertFalse(infoSha1.checksumsMatch(), "SHA1 Checksums shouldn't match");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, DUMMY_MD5, DUMMY2_MD5);
        assertFalse(infoMd5.checksumsMatch(), "MD5 Checksums shouldn't match");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, DUMMY_SHA2, DUMMY2_SHA2);
        assertFalse(infoSha2.checksumsMatch(), "SHA2 Checksums shouldn't match");
    }

    public void matchNullOriginal() {
        ChecksumInfo infoSha1 = new org.artifactory.checksum.ChecksumInfo(ChecksumType.sha1, null, DUMMY_SHA1);
        assertFalse(infoSha1.checksumsMatch(), "SHA1 Checksums shouldn't match if one is null");
        ChecksumInfo infoMd5 = new org.artifactory.checksum.ChecksumInfo(ChecksumType.md5, null, DUMMY_MD5);
        assertFalse(infoMd5.checksumsMatch(), "MD5 Checksums shouldn't match if one is null");
        //not applicable for sha2
    }

    public void matchNullActual() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, DUMMY_SHA1, null);
        assertFalse(infoSha1.checksumsMatch(), "SHA1 Checksums shouldn't match if one is null");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, DUMMY_MD5, null);
        assertFalse(infoMd5.checksumsMatch(), "MD5 Checksums shouldn't match if one is null");
        //not applicable for sha2
    }

    public void matchNullOriginalAndActual() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, null, null);
        assertFalse(infoSha1.checksumsMatch(), "SHA1 Checksums shouldn't match if both are null");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, null, null);
        assertFalse(infoMd5.checksumsMatch(), "MD5 Checksums shouldn't match if both are null");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, null, null);
        assertFalse(infoSha2.checksumsMatch(), "SHA2 Checksums shouldn't match if both are null");
    }

    public void trustedOriginalShouldReturnActual() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, ChecksumInfo.TRUSTED_FILE_MARKER, DUMMY_SHA1);
        assertTrue(infoSha1.isMarkedAsTrusted(), "SHA1 Should have been marked as trusted");
        assertEquals(infoSha1.getOriginal(), infoSha1.getActual(), "SHA1 Original should return actual if marked as trusted");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, ChecksumInfo.TRUSTED_FILE_MARKER, DUMMY_MD5);
        assertTrue(infoMd5.isMarkedAsTrusted(), "MD5 Should have been marked as trusted");
        assertEquals(infoMd5.getOriginal(), infoMd5.getActual(), "MD5 Original should return actual if marked as trusted");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, ChecksumInfo.TRUSTED_FILE_MARKER, DUMMY2_SHA2);
        assertTrue(infoSha2.isMarkedAsTrusted(), "SHA2 Should have been marked as trusted");
        assertEquals(infoSha2.getOriginal(), infoSha2.getActual(), "SHA2 Original should return actual if marked as trusted");
    }

    public void matchIfOriginalIsTruetedAndActualIsSet() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, ChecksumInfo.TRUSTED_FILE_MARKER, DUMMY_SHA1);
        assertTrue(infoSha1.checksumsMatch(), "SHA1 Checksums should match if marked as trusted and actual not null");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, ChecksumInfo.TRUSTED_FILE_MARKER, DUMMY_MD5);
        assertTrue(infoMd5.checksumsMatch(), "MD5 Checksums should match if marked as trusted and actual not null");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, ChecksumInfo.TRUSTED_FILE_MARKER, DUMMY2_SHA2);
        assertTrue(infoSha2.checksumsMatch(), "SHA2 Checksums should match if marked as trusted and actual not null");
    }

    /**
     * This test makes sure that if the  original checksum is valid  then the original checksum is being converted to lowercase.
     */
    public void validChecksumWithDifferentCaseNormalizationTest() {
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, DUMMY_UPPER_CASE_SHA1.toUpperCase(), DUMMY_UPPER_CASE_SHA1.toLowerCase());
        assertTrue(infoSha1.getOriginal().equals(DUMMY_UPPER_CASE_SHA1.toLowerCase()), "Expected lower case but fund upper case");
        assertTrue(infoSha1.checksumsMatch(), "SHA1 Checksums should match");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, DUMMY_UPPER_CASE_MD5.toUpperCase(), DUMMY_UPPER_CASE_MD5.toLowerCase());
        assertTrue(infoMd5.getOriginal().equals(DUMMY_UPPER_CASE_MD5.toLowerCase()), "Expected lower case but fund upper case");
        assertTrue(infoMd5.checksumsMatch(), "MD5 Checksums should match");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, DUMMY_UPPER_CASE_SHA2.toUpperCase(), DUMMY_UPPER_CASE_SHA2.toLowerCase());
        assertTrue(infoSha2.getOriginal().equals(DUMMY_UPPER_CASE_SHA2.toLowerCase()), "Expected lower case but fund upper case");
        assertTrue(infoSha2.checksumsMatch(), "SHA2 Checksums should match");
    }

    /**
     * This test makes sure that if the  original checksum is not valid  then the original checksum stay untouched.
     */
    public void notValidChecksumNormalizationTest() {
        String sha1Checksum = "ABC";
        String md5Checksum = "DEF";
        String Sha2Checksum = "ADF";
        ChecksumInfo infoSha1 = new ChecksumInfo(ChecksumType.sha1, sha1Checksum.toUpperCase(), DUMMY_UPPER_CASE_SHA1.toLowerCase());
        assertTrue(infoSha1.getOriginal().equals(sha1Checksum), "Expected upper case but fund lower case");
        ChecksumInfo infoMd5 = new ChecksumInfo(ChecksumType.md5, md5Checksum.toUpperCase(), DUMMY_UPPER_CASE_MD5.toLowerCase());
        assertTrue(infoMd5.getOriginal().equals(md5Checksum), "Expected upper case but fund lower case");
        ChecksumInfo infoSha2 = new ChecksumInfo(ChecksumType.sha256, Sha2Checksum.toUpperCase(), DUMMY_UPPER_CASE_SHA2.toLowerCase());
        assertTrue(infoSha2.getOriginal().equals(Sha2Checksum), "Expected upper case but fund lower case");
    }
}
