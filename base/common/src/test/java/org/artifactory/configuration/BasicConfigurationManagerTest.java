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

package org.artifactory.configuration;

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.configuration.helper.EnvContext;
import org.artifactory.configuration.helper.TestEnvContextBuilder;
import org.jfrog.config.ConfigurationManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Optional;

import static java.lang.Thread.sleep;
import static org.artifactory.configuration.helper.BasicConfigurationManagerTestHelper.*;
import static org.artifactory.configuration.helper.FileCreationStage.afterHome;
import static org.artifactory.configuration.helper.FileCreationStage.beforeHome;

/**
 * @author gidis
 */
@Test
public class BasicConfigurationManagerTest {

    private ConfigurationManager configurationManager;

    @AfterMethod
    public void cleanAfterCreateEnv() {
        System.setProperty(ConstantValues.masterKeyWaitingTimeout.getPropertyName(), "1000");
        Optional.ofNullable(configurationManager).ifPresent(ConfigurationManager::destroy);
        ArtifactoryContextThreadBinder.unbind();
        ArtifactoryHome.unbind();
    }

    @Test
    public void timeoutWaitingForMasterKey() {
        // Expecting BasicConfiguration to wait until timeout
        try {
            EnvContext envContext = TestEnvContextBuilder.create().build();
            configurationManager = createEnvironment(envContext);
            assertMasterKeyExist(envContext);
            defaultFileExists(envContext);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("master.key file is missing - timed out while waiting for master.key after"));
        }
    }

    @Test
    public void cleanStartWithMasterKey() throws IOException {
        // Expecting successful BasicConfiguration start (master.key exist)
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        // Setting master.key AFTER_HOME creation to simulate Access (providing the master.key file)
        builder.includeMasterKey(afterHome);
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        assertMasterKeyExist(envContext);
        defaultFileExists(envContext);
    }

    @Test
    public void masterKeyChecksumMatch() throws IOException {
        // Expecting BasicConfiguration to fail because of master key checksum mismatch
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        // Setting master.key AFTER_HOME creation to simulate Access (providing the master.key file)
        builder.includeMasterKeyInDb();
        builder.includeMasterKey(afterHome);
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        assertMasterKeyExist(envContext);
        defaultFileExists(envContext);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Master key checksum mismatch")
    public void masterKeyChecksumMismatch() throws IOException {
        // Expecting BasicConfiguration to fail because of master key checksum mismatch
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        // Setting master.key AFTER_HOME creation to simulate Access (providing the master.key file)
        builder.includeMasterKeyInDb();
        builder.includeMasterKey2(afterHome);
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        assertMasterKeyExist(envContext);
        defaultFileExists(envContext);
        Assert.fail();
    }

    @Test
    public void startExistingServerWithAllConfigFiles() throws IOException {
        // Expecting successful BasicConfiguration start (master.key exist and all other configs are valid)
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        builder.includeMasterKey(beforeHome);
        builder.includeDbProperties(beforeHome);
        builder.includeArtifactorySystemProperties(beforeHome);
        builder.includeArtifactoryLogback(beforeHome);
        builder.includeArtifactoryMimeTypes(beforeHome);
        builder.includeArtifactoryProperties(beforeHome);
        builder.includeArtifactoryKey(beforeHome);
        builder.includeArtifactoryServiceId(beforeHome);
        builder.includeArtifactoryRootCert(beforeHome);
        builder.includeArtifactoryBinarystore(beforeHome);
        builder.includeAccessDbProperties(beforeHome);
        builder.includeAccessLogback(beforeHome);
        builder.includeAccessPrivate(beforeHome);
        builder.includeAccessRootCrt(beforeHome);
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        assertMasterKeyExist(envContext);
        defaultFileExists(envContext);
    }

    @Test
    public void startServerWithCorruptedDbProperties() throws IOException {
        // Expecting BasicConfiguration to fail (master.key exist but db.properties is corrupted)
        try {
            TestEnvContextBuilder builder = TestEnvContextBuilder.create();
            // Setting corrupted db.properties
            builder.includeCorruptedDbProperties(beforeHome);
            // Setting valid configuration files
            builder.includeMasterKey(beforeHome);
            builder.includeArtifactorySystemProperties(beforeHome);
            builder.includeArtifactoryLogback(beforeHome);
            builder.includeArtifactoryMimeTypes(beforeHome);
            builder.includeArtifactoryProperties(beforeHome);
            builder.includeArtifactoryKey(beforeHome);
            builder.includeArtifactoryServiceId(beforeHome);
            builder.includeArtifactoryRootCert(beforeHome);
            builder.includeAccessDbProperties(beforeHome);
            builder.includeArtifactoryBinarystore(beforeHome);
            builder.includeAccessLogback(beforeHome);
            builder.includeAccessPrivate(beforeHome);
            builder.includeAccessRootCrt(beforeHome);
            EnvContext envContext = builder.build();
            configurationManager = createEnvironment(envContext);
            assertMasterKeyExist(envContext);
            defaultFileExists(envContext);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to load artifactory DB properties from"));
        }
    }

    @Test
    public void testProtectedUploadOnEmptyDb() throws IOException {
        // Expecting BasicConfiguration to delete artifactory.file since it is protected
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        // Setting corrupted root.crt
        builder.includeMasterKey(beforeHome);
        builder.includeCorruptedArtifactoryKey(beforeHome);
        builder.includeConfigTable();
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        assertMasterKeyExist(envContext);
        defaultFileExists(envContext);
        assertCorruptedArtifactoryKeyExist(envContext);
    }

    //TODO [by shayb]: replace with real test that test real protected file
    //@Test
    //public void testProtectedDownloaded() throws IOException {
    //    // Expecting BasicConfiguration to download artifactory.key from DB
    //    TestEnvContextBuilder builder = TestEnvContextBuilder.create();
    //    // Setting corrupted root.crt
    //    builder.includeMasterKey(beforeHome);
    //    builder.includeConfigTable();
    //    builder.includeArtifactoryKeyInDb();
    //    EnvContext envContext = builder.build();
    //    createEnvironment(envContext);
    //    assertMasterKeyExist(envContext);
    //    defaultFileExists(envContext);
    //    assertValidArtifactoryKeyExist(envContext);
    //}

    //@Test
    //public void testProtectedOverrided() throws IOException {
    //    // Expecting BasicConfiguration to download artifactory.key from DB
    //    TestEnvContextBuilder builder = TestEnvContextBuilder.create();
    //    // Setting corrupted root.crt
    //    builder.includeMasterKey(beforeHome);
    //    builder.includeConfigTable();
    //    builder.includeArtifactoryKeyInDb();
    //    builder.includeCorruptedArtifactoryKey(beforeHome);
    //    EnvContext envContext = builder.build();
    //    createEnvironment(envContext);
    //    assertMasterKeyExist(envContext);
    //    defaultFileExists(envContext);
    //    assertValidArtifactoryKeyExist(envContext);
    //}

    @Test
    public void testProtectedImport() throws IOException {
        // Expecting BasicConfiguration to download artifactory.key from DB
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        // Setting corrupted root.crt
        builder.includeMasterKey(beforeHome);
        builder.includeConfigTable();
        builder.includeArtifactoryKeyInDb();
        builder.includeImportArtifactoryKey(beforeHome);
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        assertMasterKeyExist(envContext);
        defaultFileExists(envContext);
    }

    @Test
    public void testNoPullWithoutMasterKey() throws IOException, InterruptedException {
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        builder.includeMasterKey(beforeHome);
        builder.includeConfigTable();
        builder.removeMasterEncryptionWrapper();
        builder.touchArtifactoryBinarystore();
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        // No master.key
        assertMasterKeyNotExists(envContext);
        // Our binarystore.xml file shouldn't have changed
        assertBinaryStoreFileNotModified(envContext, 0);
        assertValidBinaryStoreFileExists(envContext);
    }

    @Test
    public void testDeleteShouldHappenInDb() throws IOException, InterruptedException {
        TestEnvContextBuilder builder = TestEnvContextBuilder.create();
        builder.includeMasterKey(beforeHome);
        builder.includePluginGroovyFile(beforeHome);
        builder.includeConfigTable();
        builder.removePluginGroovyFile();
        EnvContext envContext = builder.build();
        configurationManager = createEnvironment(envContext);
        int pluginGroovyFileRowCount = tryToWaitForDeleteEventToPropagate(envContext);
        // Our binarystore.xml file be gone from the configs table
        Assert.assertTrue(pluginGroovyFileRowCount == 0);
    }

    private int tryToWaitForDeleteEventToPropagate(EnvContext envContext) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long now = startTime;
        int pluginGroovyFileRowCount = 1;
        while(now - startTime < 10000) {
            if((pluginGroovyFileRowCount = countPluginGroovyFileRowsInDb(envContext)) == 0 ) {
                return pluginGroovyFileRowCount;
            } else {
                now = System.currentTimeMillis();
                sleep(3000);
            }
        }
        return pluginGroovyFileRowCount;
    }
}