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

package org.artifactory.ui.rest.model.artifacts.deploy;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link UploadArtifactInfo}.
 *
 * @author Yossi Shaul
 */
@Test
public class UploadArtifactInfoTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void failSetFileNameWithRelativePath() {
        new UploadArtifactInfo().setFileName("../hack");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void failGetFileNameWithRelativePath() throws Exception {
        UploadArtifactInfo i = new UploadArtifactInfo();
        ReflectionTestUtils.setField(i, "fileName", "../hack");
        i.getFileName(); // should throw IllegalArgumentException
    }

    public void validSetGetFileName() {
        UploadArtifactInfo i = new UploadArtifactInfo();
        i.setFileName("ok");
        assertEquals(i.getFileName(), "ok");
    }
}