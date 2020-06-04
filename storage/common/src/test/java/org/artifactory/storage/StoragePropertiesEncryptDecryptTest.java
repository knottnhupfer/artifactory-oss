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
 * Unit tests for the storage properties encrypt decrypt
 *
 * @author Chen Keinan
 */
@Test(enabled = false)
public class StoragePropertiesEncryptDecryptTest extends ArtifactoryHomeBoundTest {
    StorageProperties sp;

    @BeforeMethod
    public void loadProperties() throws IOException {
        String filePath = "/storage/storagepostgres.properties";
        sp = new StorageProperties(ResourceUtils.getResourceAsFile(filePath));
    }

    @Test(enabled = false)
    public void propertiesS3CredentialEncryptionTest() throws IOException {
        String filePath = "/storage/storagepostgres.properties";
        if (!CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get())) {
            CryptoHelper.createArtifactoryKeyFile(ArtifactoryHome.get());
        }
        String pass = sp.getProperty(StorageProperties.Key.binaryProviderS3Credential);
        assertTrue(!CryptoHelper.isEncryptedUserPassword(pass));
        pass = CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), pass);
        int numOfLineBeforeEncryptAndSaving = StorageTestHelper.getFileNumOfLines(filePath);
        int passwordLinePositionBeforeEncryptAndSave = StorageTestHelper.getKeyPositionLine(filePath, "binary.provider.s3.credential");
        sp.setS3Credential(pass);
        sp.updateStoragePropertiesFile(getPropertiesStorageFile(filePath));
        int numOfLineAfterEncryptAndSaving = StorageTestHelper.getFileNumOfLines(filePath);
        int passwordLinePositionAfterEncryptAndSave = StorageTestHelper.getKeyPositionLine(filePath, "binary.provider.s3.credential");
        // check that comments are maintain
        assertEquals(numOfLineBeforeEncryptAndSaving, numOfLineAfterEncryptAndSaving);
        // check that order is maintain
        assertEquals(passwordLinePositionBeforeEncryptAndSave, passwordLinePositionAfterEncryptAndSave);
    }

    @Test(enabled = false)
    public void propertiesS3ProxyCredentialEncryptionTest() throws IOException {
        String filePath = "/storage/storagepostgres.properties";
        if (!CryptoHelper.hasArtifactoryKey(ArtifactoryHome.get())) {
            CryptoHelper.createArtifactoryKeyFile(ArtifactoryHome.get());
        }
        String pass = sp.getProperty(StorageProperties.Key.binaryProviderS3ProxyCredential);
        assertTrue(!CryptoHelper.isEncryptedUserPassword(pass));
        pass = CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), pass);
        int numOfLineBeforeEncryptAndSaving = StorageTestHelper.getFileNumOfLines(filePath);
        int passwordLinePositionBeforeEncryptAndSave = StorageTestHelper.getKeyPositionLine(filePath, "binary.provider.s3.proxy.credential");
        sp.setS3ProxyCredential(pass);
        sp.updateStoragePropertiesFile(getPropertiesStorageFile(filePath));
        int numOfLineAfterEncryptAndSaving = StorageTestHelper.getFileNumOfLines(filePath);
        int passwordLinePositionAfterEncryptAndSave = StorageTestHelper.getKeyPositionLine(filePath, "binary.provider.s3.proxy.credential");
        // check that comments are maintain
        assertEquals(numOfLineBeforeEncryptAndSaving, numOfLineAfterEncryptAndSaving);
        // check that order is maintain
        assertEquals(passwordLinePositionBeforeEncryptAndSave, passwordLinePositionAfterEncryptAndSave);
    }

    private File getPropertiesStorageFile(String filePath) {
        return ResourceUtils.getResourceAsFile(filePath);
    }

}
