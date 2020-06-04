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

package org.artifactory.io.checksum.policy;

import com.google.common.collect.Sets;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Base class for the checksum policies tests. Mainly to enforce certain tests for all the policies.
 *
 * @author Yossi Shaul
 */
public abstract class ChecksumPolicyBaseTest {
    public static final String DUMMY_SHA1 = "1234567890123456789012345678901234567890";
    public static final String DUMMY2_SHA1 = "3234567890123456789012345678901234567890";
    public static final String DUMMY_MD5 = "12345678901234567890123456789012";
    public static final String DUMMY2_MD5 = "32345678901234567890123456789012";
    public static final String SHA2 = "3e3845ec5e9416ff770664b8555d29e4f08ee4e7dbfa0337fc147c6303eb494a";

    ChecksumInfo matchedChecksums;
    ChecksumInfo notMatchedChecksums;
    ChecksumInfo noOriginalChecksum;
    ChecksumInfo sha2;

    abstract ChecksumPolicy getPolicy();

    abstract void checksumsMatch();

    abstract void noOriginalChecksum();

    abstract void checksumsDoesNotMatch();

    abstract void returnedChecksum();

    abstract void checkShouldVerify();

    @BeforeMethod
    void generateTestData() {
        // Match checksum should be the only sha1
        matchedChecksums = new ChecksumInfo(ChecksumType.sha1, DUMMY_SHA1, DUMMY_SHA1);
        notMatchedChecksums = new ChecksumInfo(ChecksumType.md5, "thiswontmatch", DUMMY_MD5);
        noOriginalChecksum = new ChecksumInfo(ChecksumType.md5, null, DUMMY2_MD5);
        sha2 = new ChecksumInfo(ChecksumType.sha256, ChecksumInfo.TRUSTED_FILE_MARKER, SHA2); //sha2 is always trusted
    }

    @Test
    public void oneMatchedChecksumAllShouldPass() {
        ChecksumPolicyBase delegatingBasePolicy = new ChecksumPolicyBase() {
            @Override
            boolean verifyChecksum(ChecksumInfo checksumInfo) {
                return ((ChecksumPolicyBase) getPolicy()).verifyChecksum(checksumInfo);
            }

            @Override
            String getChecksum(ChecksumInfo checksumInfo) {
                return ((ChecksumPolicyBase) getPolicy()).getChecksum(checksumInfo);
            }

            @Override
            public boolean shouldVerifyBadClientChecksum() {
                return getPolicy().shouldVerifyBadClientChecksum();
            }

            @Override
            ChecksumPolicyType getChecksumPolicyType() {
                return null;
            }
        };

        // You can have only 2 checksums in the set (one for each type)
        Assert.assertTrue(delegatingBasePolicy.verify(Sets.newHashSet(notMatchedChecksums, matchedChecksums)),
                "All policies should pass because there is one matching checksum");
        Assert.assertTrue(delegatingBasePolicy.verify(Sets.newHashSet(noOriginalChecksum, matchedChecksums)),
                "All policies should pass because there is one matching checksum");
    }
}
