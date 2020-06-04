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

package org.artifactory.util;

import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.trash.TrashService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the {@link org.artifactory.util.RepoPathUtils}.
 * This test is here because it has runtime dependency on {@link org.artifactory.model.common.RepoPathImpl}.
 *
 * @author Yossi Shaul
 */
@Test
public class RepoPathUtilsTest {

    public void getAncestorParent() {
        RepoPath path = RepoPathFactory.create("test", "1/2");
        assertEquals(RepoPathUtils.getAncestor(path, 1), path.getParent());
    }

    public void getAncestorGrandParent() {
        RepoPath path = RepoPathFactory.create("test", "1/2");
        assertEquals(RepoPathUtils.getAncestor(path, 2), path.getParent().getParent());
    }

    public void getAncestorOfRoot() {
        RepoPath path = RepoPathFactory.create("test", "");
        assertNull(RepoPathUtils.getAncestor(path, 1));
    }

    @Test(dataProvider = "pathsToTest")
    public void isAncestorOf(RepoPath repoPath, RepoPath otherRepoPath, boolean ancestor) {
        assertEquals(RepoPathUtils.isAncestorOf(repoPath, otherRepoPath), ancestor);
    }

    public void getAncestorBeyondRoot() {
        RepoPath path = RepoPathFactory.create("test", "1/2");
        assertNull(RepoPathUtils.getAncestor(path, 3));
    }

    public void isTrashOfTrash() {
        RepoPath path = RepoPathFactory.create(TrashService.TRASH_KEY, "1/2");
        assertTrue(RepoPathUtils.isTrash(path));
        assertTrue(RepoPathUtils.isTrash(path.getRepoKey()));
        assertTrue(RepoPathUtils.isTrash(RepoPathUtils.repoRootPath(TrashService.TRASH_KEY)));
        assertTrue(RepoPathUtils.isTrash(TrashService.TRASH_KEY));
    }

    public void isTrashRootRepo() {
        RepoPath path = RepoPathUtils.repoRootPath("repo");
        assertFalse(RepoPathUtils.isTrash(path));
        assertFalse(RepoPathUtils.isTrash(path.getRepoKey()));

    }

    public void isTrashNullRepoPath() {
        assertFalse(RepoPathUtils.isTrash((RepoPath) null));
        assertFalse(RepoPathUtils.isTrash((String) null));
    }

    public void fileRepoPathToFolderRepoPath() {
        RepoPath folder = RepoPathUtils.toFolderRepoPath(new RepoPathImpl("a", "b"));
        assertTrue(folder.isFolder());
        assertEquals(folder.toPath(), "a/b/");
    }

    public void folderRepoPathToFolderRepoPath() {
        RepoPath folderRepoPath = new RepoPathImpl("a", "b/");
        assertSame(folderRepoPath, RepoPathUtils.toFolderRepoPath(folderRepoPath));
    }

    public void folderRepoPathToFileRepoPath() {
        RepoPath file = RepoPathUtils.toFileRepoPath(new RepoPathImpl("a", "b", true));
        assertTrue(file.isFile());
        assertEquals(file.toPath(), "a/b");
        file = RepoPathUtils.toFileRepoPath(new RepoPathImpl("a", "b/"));
        assertTrue(file.isFile());
        assertEquals(file.toPath(), "a/b");
    }

    public void fileRepoPathToFileRepoPath() {
        RepoPath folderRepoPath = new RepoPathImpl("a", "b/");
        assertSame(folderRepoPath, RepoPathUtils.toFolderRepoPath(folderRepoPath));
    }

    @DataProvider
    public static Object[][] pathsToTest() {
        return new Object[][]{
                {new RepoPathImpl("myKey", "x/y/z/"), new RepoPathImpl("anotherKey", "x/y/z/file.jar"), false},
                {new RepoPathImpl("myKey", "x/y/z"), new RepoPathImpl("anotherKey", "x/y/z/myfile.jar"), false},
                {new RepoPathImpl("myKey", "x/y/z/"), new RepoPathImpl("myKey", "x/y/z/file.jar"), true},
                {new RepoPathImpl("myKey", "x/y/z"), new RepoPathImpl("myKey", "x/y/z/this.txt"), true},
                {new RepoPathImpl("myKey", "x/y/"), new RepoPathImpl("myKey", "x/y/z/file.exe"), true},
                {new RepoPathImpl("myKey", "x/y/"), new RepoPathImpl("myKey", "x/y/z/dir"), true},
                {new RepoPathImpl("myKey", "x/y/"), new RepoPathImpl("myKey", "x/y/z/dir/"), true},
                {new RepoPathImpl("myKey", "x/"), new RepoPathImpl("myKey", "x/y/z/file.jar"), true},
                {new RepoPathImpl("myKey", "x"), new RepoPathImpl("myKey", "x/y/z/file.jar"), true},
                {new RepoPathImpl("myKey", ""), new RepoPathImpl("myKey", "x/y/file.jar"), true},
                {new RepoPathImpl("myKey", null), new RepoPathImpl("myKey", "x/y/file.jar"), true},
                {new RepoPathImpl("myKey", "x/y/z/"), new RepoPathImpl("myKey", "x/y/file.jar"), false},
                {new RepoPathImpl("myKey", "x/y/z"), new RepoPathImpl("myKey", "x/y/file.jar"), false},
                {new RepoPathImpl("myKey", "x/z/"), new RepoPathImpl("myKey", "x/y/z/file.jar"), false},
                {new RepoPathImpl("myKey", "y/z/"), new RepoPathImpl("myKey", "x/y/z/file.jar"), false},
                {new RepoPathImpl("myKey", "x/y/z.jar"), new RepoPathImpl("myKey", "x/y/z/file.jar"), false},
                {new RepoPathImpl("myKey", "file.jar"), new RepoPathImpl("myKey", "x/y/z/file.jar"), false},
                {new RepoPathImpl("myKey", "z.txt"), new RepoPathImpl("myKey", "x/y/z/file.jar"), false},
                {new RepoPathImpl("myKey", "x/y/file.jar"), new RepoPathImpl("myKey", "x/y/z/"), false},
                {new RepoPathImpl("myKey", "x/y/file.jar"), new RepoPathImpl("myKey", "x/y/z"), false},
                {new RepoPathImpl("myKey", "x/y/file.jar"), new RepoPathImpl("myKey", "a/y"), false},
                {new RepoPathImpl("myKey", "x/y/file.jar"), new RepoPathImpl("myKey", "a/y.xyz"), false},
                {new RepoPathImpl("myKey", "x/y/file.jar"), new RepoPathImpl("myKey", ""), false},
                {new RepoPathImpl("myKey", "x/y/file.jar"), new RepoPathImpl("myKey", null), false},
                {new RepoPathImpl("myKey", "x/y/z"), new RepoPathImpl("myKey", "x/y/z"), false},
                {new RepoPathImpl("myKey", "x/y/z/"), new RepoPathImpl("myKey", "x/y/z"), false},
                {new RepoPathImpl("myKey", "x/y/z"), new RepoPathImpl("myKey", "x/y/z/"), false},
                {new RepoPathImpl("myKey", "x/y/z/"), new RepoPathImpl("myKey", "x/y/z/"), false},
        };
    }
}
