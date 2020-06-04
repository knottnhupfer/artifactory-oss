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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the ChecksumPolicyFail.
 *
 * @author Yossi Shaul
 */
@Test
public class ChecksumPolicyFailTest extends ChecksumPolicyBaseTest {

    private ChecksumPolicyFail policy;

    @BeforeClass
    public void createChecksumPolicy() {
        policy = new ChecksumPolicyFail();
    }

    @Override
    ChecksumPolicy getPolicy() {
        return policy;
    }

    @Override
    public void checksumsMatch() {
        boolean ok = policy.verifyChecksum(matchedChecksums) && policy.verifyChecksum(sha2);
        assertTrue(ok, "Policy should pass if checksums are same");
    }

    @Override
    public void checksumsDoesNotMatch() {
        boolean ok = policy.verifyChecksum(notMatchedChecksums);
        assertFalse(ok, "Policy should ignore any checksum errors");
    }

    @Override
    public void noOriginalChecksum() {
        boolean ok = policy.verifyChecksum(noOriginalChecksum);
        assertFalse(ok, "Policy should ignore any checksum errors");
    }

    @Override
    public void returnedChecksum() {
        String checksum = policy.getChecksum(matchedChecksums);
        assertEquals(checksum, matchedChecksums.getActual(), "Should always return the actual value");
        checksum = policy.getChecksum(notMatchedChecksums);
        assertEquals(checksum, notMatchedChecksums.getActual(), "Should always return the actual value");
        checksum = policy.getChecksum(noOriginalChecksum);
        assertEquals(checksum, noOriginalChecksum.getActual(), "Should always return the actual value");
        assertEquals(policy.getChecksum(sha2), sha2.getActual(), "Should always return the actual value");
    }

    @Override
    void checkShouldVerify() {
        assertTrue(policy.shouldVerifyBadClientChecksum(), "Fail policy requires verification");
    }

}