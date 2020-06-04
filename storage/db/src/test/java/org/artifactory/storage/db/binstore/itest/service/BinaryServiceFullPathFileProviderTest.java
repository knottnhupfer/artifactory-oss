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

import org.apache.commons.lang.SystemUtils;
import org.artifactory.storage.binstore.service.BinaryInfo;
import org.jfrog.common.ResourceUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Date: 12/10/12
 * Time: 9:54 PM
 *
 * @author freds
 */
@Test
public class BinaryServiceFullPathFileProviderTest extends BinaryServiceBaseTest {

    @Override
    protected String getBinaryStoreContent() {
        return
                "<config version=\"2\"> " +
                        "<chain template=\"full-db\"/>" +
                        "   <provider id=\"cache-fs\" type=\"cache-fs\">" +
                        "      <binariesDir>##baseDataDir##</binariesDir>" +
                        "   </provider>" +
                        "</config>";
    }

    @Override
    protected String getBinaryStoreDirName() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return SystemUtils.getJavaIoTmpDir().getAbsolutePath() + "\\fstest-" + hashCode();
        }
        return "//tmp/fstest-" + hashCode();
    }

    @Override
    protected void assertBinaryExistsEmpty(String sha1) {
        defaultAssertBinaryExistsEmpty(sha1);
    }

    @Override
    protected BinaryInfo addBinary(String resName, String sha1, String sha2, String md5, long length) throws IOException {
        try (InputStream resource = ResourceUtils.getResource("/binstore/" + resName)) {
            return binaryStore.addBinary(binaryStore.createBinaryStream(resource, null));
        }
    }

    @Override
    protected void testPruneAfterLoad() {
        testPrune(0, 0, 0);
    }

    @Override
    protected void assertPruneAfterOneGc() {
        testPrune(1, 0, 0);
    }
}
