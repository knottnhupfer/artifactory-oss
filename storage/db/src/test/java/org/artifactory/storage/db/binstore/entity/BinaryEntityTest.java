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

package org.artifactory.storage.db.binstore.entity;

import org.artifactory.storage.db.binstore.service.BinaryEntityWithValidation;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Date: 12/10/12
 * Time: 9:42 PM
 *
 * @author freds
 */
@Test
public class BinaryEntityTest {

    public void simpleBinaryData() {
        BinaryEntityWithValidation bd = new BinaryEntityWithValidation(
                "8018634e43a47494119601b857356a5a1875f888",
                "9f58f055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280",
                "7c9703f5909d78ab0bf18147aee0a5b3", 13L);
        assertEquals(bd.getSha1(), "8018634e43a47494119601b857356a5a1875f888");
        assertEquals(bd.getSha2(), "9f58f055bc6d5477c35f82da16323efb884fc21a87fbf7ebda9d5848eee3e280");
        assertEquals(bd.getMd5(), "7c9703f5909d78ab0bf18147aee0a5b3");
        assertEquals(bd.getLength(), 13L);
        assertTrue(bd.isValid());
    }

    public void maxNullBinaryData() {
        // sha1, sha2 and md5 good length but invalid
        BinaryEntityWithValidation bd = new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! so i'm going to tell you about ",
                "length matters but not content!!", 0L);
        assertEquals(bd.getSha1(), "length matters but not content!!12345678");
        assertEquals(bd.getSha2(), "length matters but not content!! so i'm going to tell you about ");
        assertEquals(bd.getMd5(), "length matters but not content!!");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());

        // sha1, sha2 and md5 good length but md5 and sha2 invalid
        bd = new BinaryEntityWithValidation(
                "8018634e43a47494119601b857356a5a1875f888",
                "length matters but not content!! so i'm going to tell you about ",
                "length matters but not content!!", 0L);
        assertEquals(bd.getSha1(), "8018634e43a47494119601b857356a5a1875f888");
        assertEquals(bd.getSha2(), "length matters but not content!! so i'm going to tell you about ");
        assertEquals(bd.getMd5(), "length matters but not content!!");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());

        // sha1, sha2 and md5 good length but sha1 and sha2 invalid
        bd = new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! so i'm going to tell you about ",
                "7c9703f5909d78ab0bf18147aee0a5b3", 0L);
        assertEquals(bd.getSha1(), "length matters but not content!!12345678");
        assertEquals(bd.getSha2(), "length matters but not content!! so i'm going to tell you about ");
        assertEquals(bd.getMd5(), "7c9703f5909d78ab0bf18147aee0a5b3");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());

        // sha1, sha2 and md5 good length but sha1 and md5 invalid
        bd = new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "84495e734090fee6588f6c581567491790c67a9c7679457977ca72b872fe6d14",
                "length matters but not content!!", 0L);
        assertEquals(bd.getSha1(), "length matters but not content!!12345678");
        assertEquals(bd.getSha2(), "84495e734090fee6588f6c581567491790c67a9c7679457977ca72b872fe6d14");
        assertEquals(bd.getMd5(), "length matters but not content!!");
        assertEquals(bd.getLength(), 0L);
        assertFalse(bd.isValid());
    }


    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*SHA1.*not.*valid.*")
    public void nullSha1BinaryData() {
        new BinaryEntityWithValidation(
                null,
                "length matters but not content!! it is what it is, what to do***",
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*MD5.*not.*valid.*")
    public void nullMd5BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! it is what it is, what to do***",
                null, 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*SHA2.*not.*valid.*")
    public void nullSha2BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                null,
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*SHA1.*not.*valid.*")
    public void emptySha1BinaryData() {
        new BinaryEntityWithValidation(
                "  ",
                "length matters but not content!! it is what it is, what to do***",
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*SHA2.*not.*valid.*")
    public void emptySha2BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "  ",
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*MD5.*not.*valid.*")
    public void emptyMd5BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! it is what it is, what to do***",
                "  ", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*SHA1.*not.*valid.*")
    public void wrongSha1BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!123456789",
                "length matters but not content!! it is what it is, what to do***",
                "length matters but not content!!", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*SHA2.*not.*valid.*")
    public void wrongSha2BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! it is what it is, what to do***123",
                "length matters but not content!!1", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*MD5.*not.*valid.*")
    public void wrongMd5BinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! it is what it is, what to do***",
                "length matters but not content!!1", 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*Length.*not.*valid.*")
    public void wrongLengthBinaryData() {
        new BinaryEntityWithValidation(
                "length matters but not content!!12345678",
                "length matters but not content!! it is what it is, what to do***",
                "length matters but not content!!", -1L);
    }
}
