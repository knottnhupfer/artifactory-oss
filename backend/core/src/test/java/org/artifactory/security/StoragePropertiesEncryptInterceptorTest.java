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
import org.artifactory.storage.StorageProperties;
import org.jfrog.common.ResourceUtils;
import org.jfrog.config.ConfigurationManager;
import org.jfrog.config.wrappers.ConfigurationManagerImpl;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * @author Gidi Shabat
 */
@Test
public class StoragePropertiesEncryptInterceptorTest {

    private ConfigurationManager configurationManager;

    @Mock
    private CentralConfigService centralConfigServiceMock;

    @Mock
    private AccessService accessServiceMock;

    @Mock
    private AddonsManager addonsManagerMock;

    private ArtifactoryEncryptionService encryptionService;

    public static <T> T proxy(Class<T> interfaceClass, InvocationHandler handler) {
        Class[] interfaces = {interfaceClass};
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(classLoader, interfaces, handler);
    }

    /**
     * Emulates Artifactory environment
     */
    @BeforeClass(enabled = false)
    public void init() throws IOException {
        //TODO: [by YS] We need to add functionality to the ArtifactoryHomeBoundTest to support storage.properties
        // Create Artifactory home and bind it to the thread
        File home = new File("target/test/" + StoragePropertiesEncryptInterceptorTest.class.getSimpleName());
        ArtifactoryHome artifactoryHome = new ArtifactoryHome(home);
        configurationManager = ConfigurationManagerImpl
                .create(new ArtifactoryConfigurationAdapter(ArtifactoryHome.get()));
        configurationManager.initDbProperties();
        configurationManager.initDefaultFiles();
        artifactoryHome.initPropertiesAndReload();
        ArtifactoryHome.bind(artifactoryHome);
        // Create mock ArtifactoryContext using Proxy and invocation handler
        TestInvocationHandler handler = new TestInvocationHandler();
        ArtifactoryContext context = proxy(ArtifactoryContext.class, handler);
        ArtifactoryContextThreadBinder.bind(context);

        encryptionService = new ArtifactoryEncryptionServiceImpl(addonsManagerMock, new ArtifactoryDbProperties(ArtifactoryHome.get()));
        ((ArtifactoryEncryptionServiceImpl) encryptionService).setCentralConfigService(centralConfigServiceMock);
        ((ArtifactoryEncryptionServiceImpl) encryptionService).setAccessService(accessServiceMock);

        File storageProperties = ResourceUtils.getResourceAsFile("/org/artifactory/security/db.properties");
        FileUtils.copyFile(storageProperties, ArtifactoryHome.get().getDBPropertiesFile());
        String keyFileLocation = ConstantValues.securityArtifactoryKeyLocation.getString();
        File keyFile = new File(ArtifactoryHome.get().getEtcDir(), keyFileLocation);
        //noinspection ResultOfMethodCallIgnored
        keyFile.delete();
        CryptoHelper.createArtifactoryKeyFile(ArtifactoryHome.get());
    }

    @AfterClass
    public void tearDown() {
        Optional.ofNullable(configurationManager).ifPresent(ConfigurationManager::destroy);
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test(enabled = false)
    public void encryptTest() throws IOException {
        encryptionService.encryptDecryptDbProperties(true);
        StorageProperties storageProperties = new StorageProperties(ArtifactoryHome.get().getDBPropertiesFile());
        // Check the credentials
        String credentials = storageProperties.getProperty("binary.provider.s3.credential", null);
        Assert.assertNotEquals(credentials, "test2");
        credentials = storageProperties.getS3Credential();
        Assert.assertEquals(credentials, "test2");
        // Check proxy credential
        String proxyCredentials = storageProperties.getProperty("binary.provider.s3.proxy.credential", null);
        Assert.assertNotEquals(proxyCredentials, "test3");
        proxyCredentials = storageProperties.getS3ProxyCredential();
        Assert.assertEquals(proxyCredentials, "test3");
    }

    @Test(enabled = false, dependsOnMethods = "encryptTest")
    public void decryptTest() throws IOException {
        encryptionService.encryptDecryptDbProperties(true);
        StorageProperties storageProperties = new StorageProperties(ArtifactoryHome.get().getDBPropertiesFile());
        String credentials = storageProperties.getProperty("binary.provider.s3.credential", null);
        Assert.assertEquals(credentials, "test2");
        // Check the proxy credentials
        String proxyCredentials = storageProperties.getProperty("binary.provider.s3.proxy.credential", null);
        Assert.assertEquals(proxyCredentials, "test3");
    }

    public class TestInvocationHandler implements InvocationHandler {
        public int count = 0;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getArtifactoryHome".equals(method.getName())) {
                return ArtifactoryHome.get();
            }
            if ("beanForType".equals(method.getName()) &&
                    ((Class) args[0]).getName().equals(ArtifactoryDbProperties.class.getName())) {
                return new ArtifactoryDbProperties(ArtifactoryHome.get());
            }
            throw new IllegalStateException("The state is not expected in this test: " + method.getName());
        }
    }
}
