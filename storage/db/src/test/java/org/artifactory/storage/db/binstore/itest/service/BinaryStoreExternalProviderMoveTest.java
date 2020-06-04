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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.checksum.ChecksumType;
import org.jfrog.storage.binstore.ifc.ProviderConnectMode;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Date: 12/10/12
 * Time: 9:54 PM
 *
 * @author freds
 */
@Test
public class BinaryStoreExternalProviderMoveTest extends BinaryStoreExternalProviderBaseTest {

    @Override
    protected String getBinaryStoreContent() {
        String externalDir = testExternal.getAbsolutePath();
        String mode = ProviderConnectMode.MOVE.propName;
        return
                "<config version=\"2\"> " +
                        "<chain>" +
                        "   <provider id=\"file-system\" type=\"file-system\">" +
                        "      <provider id=\"external-wrapper\" type=\"external-wrapper\">" +
                        "         <provider id=\"external-file\" type=\"external-file\"/>" +
                        "      </provider>" +
                        "   </provider>" +
                        "</chain>" +
                        "<provider id=\"file-system\" type=\"file-system\">" +
                        "   <binariesDir>##baseDataDir##</binariesDir>" +
                        "</provider>" +
                        "<provider id=\"external-wrapper\" type=\"external-wrapper\">" +
                        "   <binariesDir>##baseDataDir##</binariesDir>" +
                        "   <connectMode>" + mode + "</connectMode>" +
                        "</provider>" +
                        "<provider id=\"external-file\" type=\"external-file\">" +
                        "   <binariesDir>" + externalDir + "</binariesDir>" +
                        "</provider>" +
                        "</config>";
    }

    @Override
    protected void assertBinaryExistsEmpty(String sha1) throws IOException {
        File extFile = new File(testExternal, sha1.substring(0, 2) + "/" + sha1);
        assertTrue(extFile.exists());
        try (InputStream bis = binaryStore.getBinary(sha1)) {
            assertNotNull(bis);
        }
        // Test external removed
        assertFalse(extFile.exists());
    }

    @Override
    protected void checkBinariesDirAfterLoad(Map<String, Object[]> subFolders) {
        // In move the folder is full as soon as binary accessed
        super.checkBinariesDirAfterLoad(subFolders);
    }

    @Override
    protected void testPruneAfterLoad() {
        testPrune(0, 0, 0);
        // Test disconnection will do nothing
        BasicStatusHolder statusHolder = new BasicStatusHolder();
        binaryStore.disconnectExternalFilestore(testExternal, ProviderConnectMode.COPY_FIRST, statusHolder);
        String statusMsg = statusHolder.getStatusMsg();
        assertFalse(statusHolder.isError(), "Error during disconnecting external provider: " + statusMsg);
        assertTrue(statusMsg.contains("" + 4 + " files out"), "Wrong status message " + statusMsg);
        assertTrue(statusMsg.contains("" + 0 + " total"), "Wrong status message " + statusMsg);
        super.checkBinariesDirAfterLoad(getSubFoldersMap());
    }

    //[sha2]: add proper tests when binarystore switches to full sha2
    protected void checkSha1OnEmpty(String sha1) throws IOException {
        assertNull(binaryStore.findBinary(ChecksumType.sha1, sha1));
        boolean exist = binaryStore.isFileExist(sha1);
        assertTrue(exist);
        assertBinaryExistsEmpty(sha1);
    }

    @Override
    protected void assertPruneAfterOneGc() {
        testPrune(1, 0, 0);
    }
}
