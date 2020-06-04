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

package org.artifactory.api.repo;

import org.artifactory.repo.RepoPath;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * @author Gal Ben Ami
 */
@Test
public class BrowsableItemTest {

    public void testTransitivityFolders() {
        RepoPath repoPath = mock(RepoPath.class);
        when(repoPath.getPath()).thenReturn("a");

        BrowsableItem a = new BrowsableItem("..", true, 0L, 0L, 0L, repoPath);
        BrowsableItem b = new BrowsableItem("..", true, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);

        a = new BrowsableItem("a", true, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("b", true, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        a = new BrowsableItem("a", true, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("b", false, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);


        a = new BrowsableItem("a", true, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("b", true, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        a = new BrowsableItem("a", true, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("a", true, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);


        a = new BrowsableItem("a", false, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("a", false, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);


        a = new BrowsableItem("..", false, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("a", false, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        a = new BrowsableItem("..", true, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("a", false, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        a = new BrowsableItem("a", true, 0L, 0L, 0L, repoPath);
        b = new BrowsableItem("a", false, 0L, 0L, 0L, repoPath);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

    }

}