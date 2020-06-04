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

package org.artifactory.security;

import org.apache.commons.io.FileUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryConfigurationAdapter;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.config.db.ArtifactoryDbProperties;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.security.access.AccessService;
import org.jfrog.common.ResourceUtils;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.watch.FileWatchingManager;
import org.jfrog.config.wrappers.ConfigurationManagerImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author gidis
 */
public class DbPropertiesEncryptionTest {

    private ConfigurationManager configurationManager;

    @Mock
    private CentralConfigService centralConfigServiceMock;

    @Mock
    private AccessService accessServiceMock;

    @Mock
    private AddonsManager addonsManagerMock;

    private ArtifactoryEncryptionService encryptionService;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Emulates Artifactory environment
     */
    @BeforeMethod
    public void init() throws IOException {
        File home = new File("target/test/" + DbPropertiesEncryptionTest.class.getSimpleName());
        ArtifactoryHome artifactoryHome = new ArtifactoryHome(home);
        File masterKeyFile = artifactoryHome.getMasterKeyFile();
        Files.write(masterKeyFile.toPath(), "0c1a1554553d487466687b339cd85f3d".getBytes());
        FileWatchingManager fileWatchingManager = Mockito.mock(FileWatchingManager.class);
        configurationManager = ConfigurationManagerImpl.create(new ArtifactoryConfigurationAdapter(artifactoryHome),
                fileWatchingManager);
        configurationManager.initDbProperties();
        configurationManager.initDefaultFiles();
        artifactoryHome.initPropertiesAndReload();
        ArtifactoryHome.bind(artifactoryHome);

        // Create mock ArtifactoryContext using Proxy and invocation handler
        ArtifactoryContext context = Mockito.mock(ArtifactoryContext.class);
        when(context.getArtifactoryHome()).thenReturn(ArtifactoryHome.get());
        ArtifactoryContextThreadBinder.bind(context);

        encryptionService = new ArtifactoryEncryptionServiceImpl(addonsManagerMock, new ArtifactoryDbProperties(ArtifactoryHome.get()));

        ((ArtifactoryEncryptionServiceImpl) encryptionService).setCentralConfigService(centralConfigServiceMock);
        ((ArtifactoryEncryptionServiceImpl) encryptionService).setAccessService(accessServiceMock);
        FileUtils.copyFile(ResourceUtils.getResourceAsFile("/org/artifactory/security/db.properties"),
                ArtifactoryHome.get().getDBPropertiesFile());
        String keyFileLocation = ConstantValues.securityArtifactoryKeyLocation.getString();
        FileUtils.deleteQuietly(new File(ArtifactoryHome.get().getEtcDir(), keyFileLocation));
        CryptoHelper.createArtifactoryKeyFile(ArtifactoryHome.get());
    }

    @AfterMethod
    public void tearDown() {
        configurationManager.destroy();
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void encryptTest() throws IOException {
        init();
        encryptionService.encryptDecryptDbProperties(true);
        ArtifactoryHome home = ArtifactoryHome.get();
        ArtifactoryDbProperties dbProperties = new ArtifactoryDbProperties(home);
        // Check the password
        String password = dbProperties.getProperty("password", null);
        assertThat(password).isNotEqualTo("test1");
        password = dbProperties.getPassword();
        assertThat(password).isEqualTo("test1");
        // Check the url
        String url = dbProperties.getConnectionUrl();
        assertThat(url).isEqualTo("jdbc:derby:" + home.getDataDir().getAbsolutePath() + "/derby;create=true");
    }

    @Test
    public void decryptTest() throws IOException {
        encryptTest();
        encryptionService.encryptDecryptDbProperties(false);
        ArtifactoryHome home = ArtifactoryHome.get();
        ArtifactoryDbProperties dbProperties = new ArtifactoryDbProperties(home);
        // Check the password
        String password = dbProperties.getProperty("password", null);
        assertThat(password).isEqualTo("test1");
        // Check the url
        String url = dbProperties.getConnectionUrl();
        assertThat(url).isEqualTo("jdbc:derby:" + home.getDataDir().getAbsolutePath() + "/derby;create=true");
    }
}
