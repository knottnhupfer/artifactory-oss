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

package org.artifactory.api.fs;

import com.google.common.collect.Sets;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for the FileInfoImpl.
 *
 * @author Yossi Shaul
 */
@Test
public class FileInfoImplTest extends ArtifactoryHomeBoundTest {
    private static final String DUMMY_SHA1 = "1234567890123456789012345678901234567890";
    private static final String DUMMY2_SHA1 = "3234567890123456789012345678901234567890";

    public void differentChecksumNotIdentical() {
        RepoPath path = new RepoPathImpl("repo", "test.jar");

        FileInfoImpl fileInfo1 = new FileInfoImpl(path);
        fileInfo1.setChecksums(
                Sets.newHashSet(new org.artifactory.checksum.ChecksumInfo(ChecksumType.sha1, null, DUMMY_SHA1)));

        FileInfoImpl fileInfo2 = new FileInfoImpl(path);
        fileInfo2.setChecksums(Sets.newHashSet(new ChecksumInfo(ChecksumType.sha1, "originalchecksum", DUMMY2_SHA1)));

        assertFalse(fileInfo1.isIdentical(fileInfo2), "Should not be identical - checksum info is not");
    }

    public void fileInfoNoId() {
        FileInfoImpl f = new FileInfoImpl(new RepoPathImpl("repo", "path"));
        assertEquals(f.getId(), -1);
    }

    public void fileInfoWithId() {
        FileInfoImpl f = new FileInfoImpl(new RepoPathImpl("repo", "path"), 8989);
        assertEquals(f.getId(), 8989);
    }

    public void fileInfoCopyWithId() {
        FileInfoImpl f1 = new FileInfoImpl(new RepoPathImpl("repo", "path"), 8989);
        FileInfoImpl f2 = new FileInfoImpl(f1);
        assertEquals(f2.getId(), f1.getId());
    }

    public void buildFileInfoWithFolderRepoPath() {
        FileInfoImpl fi = new FileInfoImpl(new RepoPathImpl("repo", "path", true), 8989);
        assertTrue(fi.getRepoPath().isFile());
        assertEquals(fi.getRepoPath().toPath(), "repo/path");
    }
}
