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

package org.artifactory.converter.nonfs;

import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.converter.helpers.ConvertersManagerTestBase;
import org.artifactory.converter.helpers.MockArtifactoryHome;
import org.artifactory.environment.converter.local.PreInitConverter;
import org.artifactory.environment.converter.shared.SharedEnvironmentConverter;
import org.artifactory.test.TestUtils;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.artifactory.version.ArtifactoryVersionProvider.v4111;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author gidis
 */
@Test
public class NoNfsConverterTest extends ConvertersManagerTestBase {

    private File home;

    @BeforeClass
    public void init() throws IOException {
        home = TestUtils.createTempDir(getClass());
        createHomeEnvironment(home, v4111.get());
    }

    // Pre init converter should throw an exception if before version is not 5.4.6
    public void testV550VersionPrerequisite() {
        System.setProperty(ConstantValues.allowAnyUpgrade.getPropertyName(), "5.5.0-m005");
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome(home);
        runConverters(artifactoryHome, ArtifactoryVersionProvider.v4111.get());
        System.clearProperty(ConstantValues.allowAnyUpgrade.getPropertyName());
        artifactoryHome = new MockArtifactoryHome(home);
        try {
            runConverters(artifactoryHome, ArtifactoryVersionProvider.v4111.get());
        } catch (Exception iae) {
            assertTrue(iae.getMessage().contains("upgrade your cluster to Artifactory 5.4.6"));
            return;
        }
        assertFalse(true, "Expected to fail over v550 prerequisite check.");
    }

    public void convertDbPropertiesFromHa() throws IOException {
        System.setProperty(ConstantValues.allowAnyUpgrade.getPropertyName(), "5.5.0-m005");
        ArtifactoryHome artifactoryHome = new MockArtifactoryHome(home);
        File artDir = new File(home, ".artifactory");
        File etcDir = new File(artDir, "etc");
        File pluginsDir = new File(etcDir, "plugins");
        File pluginFile = new File(pluginsDir, "plugin.groovy");
        File uiFileDir = new File(etcDir, "ui");
        File uiFile = new File(uiFileDir, "ui.log");
        File artifactorySystemPropertiesFile = new File(etcDir, "artifactory.system.properties");
        File binaryStoreXml = new File(etcDir, "binarystore.xml");
        File mimeTypeXml = new File(etcDir, "mimetypes.xml");
        File dbPropertiesFile = new File(etcDir, "db.properties");
        File homePropertiesFile = new File(etcDir, "home.properties");
        File storageProperties = new File(etcDir, "storage.properties");
        File sshPrivateKey = new File(etcDir, "security/artifactory.ssh.private");
        File sshPublicKey = new File(etcDir, "security/artifactory.ssh.public");
        // Create Full Ha environment
        createHaEnvironment(home);
        // Assert configuration not exist in home directory
        cleanAndAssert(pluginFile);
        cleanAndAssert(uiFile);
        cleanAndAssert(artifactorySystemPropertiesFile);
        cleanAndAssert(binaryStoreXml);
        cleanAndAssert(mimeTypeXml);
        cleanAndAssert(dbPropertiesFile);
        cleanAndAssert(homePropertiesFile);
        cleanAndAssert(storageProperties);
        cleanAndAssert(sshPrivateKey);
        cleanAndAssert(sshPublicKey);
        // Run converters

        runConverters(artifactoryHome, ArtifactoryVersionProvider.v4111.get());
        // Assert configuration exist in home directory
        assertTrue(pluginFile.exists());
        assertTrue(artifactorySystemPropertiesFile.exists());
        assertTrue(binaryStoreXml.exists());
        assertTrue(mimeTypeXml.exists());
        assertTrue(dbPropertiesFile.exists());
        assertTrue(pluginFile.exists());
        Assert.assertFalse(storageProperties.exists());
        assertTrue(sshPrivateKey.exists());
        assertTrue(sshPublicKey.exists());
        // Assert db properties content
        ArtifactoryDbProperties dbProperties = new ArtifactoryDbProperties(artifactoryHome, dbPropertiesFile);
        // we don't get the password directly, because asking it will try to decrypt the password using the master key, which will throw exception because there is no such key yet.
        String password = dbProperties.getProperty(ArtifactoryDbProperties.Key.password);
        password = CryptoHelper.decryptIfNeeded(artifactoryHome, password);
        assertTrue(password.equals("password"));
        // Assert artifactory.system.properties content
        String content = FileUtils.readFileToString(artifactorySystemPropertiesFile);
        assertTrue(content.equals("test=just_test"));
    }

    private void runConverters(ArtifactoryHome artifactoryHome, ArtifactoryVersion from) {
        try {
            ArtifactoryHome.bind(artifactoryHome);
            PreInitConverter localEnvironmentConverter = new PreInitConverter(artifactoryHome);
            localEnvironmentConverter.convert(new CompoundVersionDetails(from,"TEST", 0L), artifactoryHome.getRunningArtifactoryVersion());
            SharedEnvironmentConverter sharedEnvironmentConverter = new SharedEnvironmentConverter(artifactoryHome);
            sharedEnvironmentConverter.convert(new CompoundVersionDetails(from,"TEST", 0L), artifactoryHome.getRunningArtifactoryVersion());

        } finally {
            ArtifactoryHome.unbind();
        }
    }

    private void createHaEnvironment(File home) throws IOException {
        createHaPluginsFile(home);
        createHaUiFile(home);
        createHaBackupFile(home);
        createArtifactorySystemPropertiesFile(home);
        createBinaryStoreXmlFile(home);
        createMimeTypeXmlFile(home);
        createStoragePropertiesXmlFile(home);
        createSshKeys(home);
    }

    static void cleanAndAssert(File file) {
        if (file.exists()) {
            boolean delete = file.delete();
            if (!delete) {
                Assert.fail();
            }
        }
        Assert.assertFalse(file.exists());
    }
}
