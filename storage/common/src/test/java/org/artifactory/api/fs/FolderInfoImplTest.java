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

import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FolderInfoImpl;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the {@link FolderInfoImpl}.
 *
 * @author Yossi Shaul
 */
@Test
public class FolderInfoImplTest extends ArtifactoryHomeBoundTest {

    public void folderInfoNoId() {
        FolderInfoImpl f = new FolderInfoImpl(new RepoPathImpl("repo", "path"));
        assertEquals(f.getId(), -1);
    }

    public void folderInfoWithId() {
        FolderInfoImpl f = new FolderInfoImpl(new RepoPathImpl("repo", "path"), 888);
        assertEquals(f.getId(), 888);
    }

    public void folderInfoCopyWithId() {
        FolderInfoImpl f1 = new FolderInfoImpl(new RepoPathImpl("repo", "path"), 999);
        FolderInfoImpl f2 = new FolderInfoImpl(f1);
        assertEquals(f2.getId(), f1.getId());
    }

    public void buildFolderInfoWithFileRepoPath() {
        FolderInfoImpl fi = new FolderInfoImpl(new RepoPathImpl("repo", "path", false));
        assertTrue(fi.getRepoPath().isFolder());
        assertEquals(fi.getRepoPath().toPath(), "repo/path/");
    }
}
