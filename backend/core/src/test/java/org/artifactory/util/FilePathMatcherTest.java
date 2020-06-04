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

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * GlobalExcludes unit tests.
 *
 * @author Gidi Shabat
 */
@Test
public class FilePathMatcherTest extends ArtifactoryHomeBoundTest {

    @Test
    public void globalExcludes() {
        File file = new File("/work/tmp/t2/20080810.145407/repositories/repo1-cache/.DS_Store");
        assertTrue(GlobalExcludes.isInGlobalExcludes(file));
        assertFalse(GlobalExcludes.matches(file, null, GlobalExcludes.getGlobalExcludes()));
        file = new File("/repo1-cache/test~");
        assertTrue(GlobalExcludes.isInGlobalExcludes(file));
        assertFalse(GlobalExcludes.matches(file, null, GlobalExcludes.getGlobalExcludes()));
        // RTFACT-5394 -  Make sure global excludes doesn't check match start
        File shortFileName = new File("/t");
        assertFalse(GlobalExcludes.isInGlobalExcludes(shortFileName));
        // Make sure that global excludes, exclude CVS directories.
        File cvsFile = new File("/toto/CVS/should/be/excluded");
        assertTrue(GlobalExcludes.isInGlobalExcludes(cvsFile));
    }
}
