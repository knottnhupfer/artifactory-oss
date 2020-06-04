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

package org.artifactory.storage;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.common.ResourceUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author gidis
 */
@Test
public class DbPropertiesEncryptDecryptTest extends ArtifactoryHomeBoundTest {

    ArtifactoryDbProperties sp;

    @BeforeMethod
    public void loadProperties() throws IOException {
        String filePath = "/storage/dbpostgres.properties";
        sp = new ArtifactoryDbProperties(ArtifactoryHome.get(), ResourceUtils.getResourceAsFile(filePath));
    }

    @Test()
    public void propertiesPasswordEncryptionTest() throws IOException {
        String filePath = "/storage/dbpostgres.properties";
        if (!CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get())) {
            CryptoHelper.createArtifactoryKeyFile(ArtifactoryHome.get());
        }
        String pass = sp.getProperty(ArtifactoryDbProperties.Key.password);
        assertTrue(!CryptoHelper.isEncryptedUserPassword(pass));
        pass = CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), pass);
        int numOfLineBeforeEncryptAndSaving = StorageTestHelper.getFileNumOfLines(filePath);
        int passwordLinePositionBeforeEncryptAndSave = StorageTestHelper.getKeyPositionLine(filePath, "password");
        sp.setPassword(pass);
        sp.updateDbPropertiesFile(getPropertiesStorageFile(filePath));
        int numOfLineAfterEncryptAndSaving = StorageTestHelper.getFileNumOfLines(filePath);
        int passwordLinePositionAfterEncryptAndSave = StorageTestHelper.getKeyPositionLine(filePath, "password");
        // check that comments are maintain
        assertEquals(numOfLineBeforeEncryptAndSaving, numOfLineAfterEncryptAndSaving);
        // check that order is maintain
        assertEquals(passwordLinePositionBeforeEncryptAndSave, passwordLinePositionAfterEncryptAndSave);
    }

    private File getPropertiesStorageFile(String filePath) {
        return ResourceUtils.getResourceAsFile(filePath);
    }


}
