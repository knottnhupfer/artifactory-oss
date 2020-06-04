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

package org.artifactory.storage.db.binstore.itest.service;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.ResourceUtils;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Date: 12/17/12
 * Time: 9:22 AM
 *
 * @author freds
 */
public abstract class BinaryStoreExternalProviderBaseTest extends BinaryServiceBaseTest {

    protected File testExternal;

    @Override
    protected ArtifactoryHomeBoundTest createArtifactoryHomeTest() throws IOException {
        ArtifactoryHomeBoundTest artifactoryHomeTest = super.createArtifactoryHomeTest();
        testExternal = new File(ArtifactoryHome.get().getDataDir(), "testExternal");
        Object[][] binFileData = getBinFileData();
        for (Object[] binData : binFileData) {
            String resName = (String) binData[0];
            String sha1 = (String) binData[1];
            File destFile = new File(testExternal, sha1.substring(0, 2) + "/" + sha1);
            File parentFile = destFile.getParentFile();
            if (!parentFile.exists()) {
                Assert.assertTrue(parentFile.mkdirs(), "Error creating " + parentFile.getAbsolutePath());
            }
            FileUtils.copyInputStreamToFile(ResourceUtils.getResource("/binstore/" + resName), destFile);
        }
        return artifactoryHomeTest;
    }

    @Override
    protected BinaryInfo addBinary(String resName, String sha1, String sha2, String md5, long length) throws IOException {
        return binaryStore.addBinaryRecord(sha1, sha2, md5, length);
    }

    @Override
    protected String getBinaryStoreDirName() {
        return new File(ArtifactoryHome.get().getDataDir(), "filestore_test").getAbsolutePath();
    }

    @Override
    public void testReloadResourceWithoutSha2() throws IOException, SQLException {
        // Do nothing. This test is not for external providers
    }
}
