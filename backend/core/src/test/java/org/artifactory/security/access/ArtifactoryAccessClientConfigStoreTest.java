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

package org.artifactory.security.access;

import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.util.DirectoryScanner;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.property.ArtifactorySystemProperties;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.accesstoken.AccessClientSettings;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.easymock.EasyMock;
import org.jfrog.access.client.AccessAuth;
import org.jfrog.access.client.AccessAuthToken;
import org.jfrog.access.client.AccessClientBuilder;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.token.JwtAccessTokenImpl;
import org.jfrog.access.version.AccessVersion;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.jfrog.security.crypto.EncryptionWrapperFactory;
import org.jfrog.security.crypto.JFrogCryptoHelper;
import org.jfrog.security.crypto.PlainTextEncryptionWrapper;
import org.jfrog.security.file.PemHelper;
import org.jfrog.security.file.SecurityFolderHelper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.Properties;

import static org.artifactory.test.TestUtils.assertCausedBy;
import static org.artifactory.test.TestUtils.createCertificate;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * @author Yinon Avraham.
 */
public class ArtifactoryAccessClientConfigStoreTest extends ArtifactoryHomeBoundTest {

    public static final ServiceId TEST_SERVICE_ID = ServiceId.generateUniqueId("test");
    private File testDir;
    private File rootCrtFile;
    private File bootstrapCredsFile;
    private File accessCredsFile;
    private File accessAdminTokenFile;
    private File masterKeyFile;
    private EncryptionWrapper masterEncryption;
    private ArtifactorySystemProperties artSysProperties;
    private AccessClientSettings accessClientSettings;

    @BeforeMethod
    public void setup() throws Exception {
        testDir = Files.createTempDirectory(ArtifactoryAccessClientConfigStoreTest.class.getSimpleName()).toFile();
        rootCrtFile = new File(testDir, "keys/root.crt");
        bootstrapCredsFile = new File(testDir, "bootstrap.creds");
        accessCredsFile = new File(testDir, "keys/access.creds");
        accessAdminTokenFile = new File(testDir, "access.admin.token");
        masterKeyFile = new File(testDir, "master.key");
        SecurityFolderHelper.createKeyFile(masterKeyFile);
        removeMasterKey();
        artSysProperties = new ArtifactorySystemProperties();
        accessClientSettings = new AccessClientSettings();
    }

    @AfterMethod
    public void cleanup() throws Exception {
        FileUtils.forceDelete(testDir);
    }

    private void removeMasterKey() {
        masterEncryption = new PlainTextEncryptionWrapper();
    }

    private void initMasterKey() {
        masterEncryption = EncryptionWrapperFactory.createArtifactoryKeyWrapper(masterKeyFile,
                masterKeyFile.getParentFile(), 0, f -> true);
    }

    @Test
    public void testServiceId() throws Exception {
        ArtifactoryAccessClientConfigStore configStore = createConfigStore();
        assertNotNull(configStore.getServiceId());
        ServiceId serviceId = ServiceId.generateUniqueId("foo");
        configStore.setServiceId(serviceId);
        assertEquals(configStore.getServiceId(), serviceId);
    }

    private File getOldAccessCredsFile() {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**/*access.creds.*.bak"});
        scanner.setBasedir(accessCredsFile.getParent());
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        return new File(accessCredsFile.getParent() + "/" + files[0]);
    }

    @Test
    public void testAccessClientVersion() throws Exception {
        ArtifactoryAccessClientConfigStore configStore = createConfigStore();
        //No access.version.properties file
        assertNull(configStore.getAccessClientVersion());
        //Store the version
        AccessVersion newVersion = createAccessVersion("ver", "time", "rev");
        configStore.storeAccessClientVersion(newVersion);
        //Read the version from the file
        File versionFile = new File(testDir, "data/access.version.properties");
        try (InputStream input = new FileInputStream(versionFile)) {
            Properties properties = new Properties();
            properties.load(input);
            AccessVersion storedVersion = AccessVersion.read(properties);
            assertEqualVersions(storedVersion, newVersion);
        }
        //Get the version from the config store
        AccessVersion storedVersion = configStore.getAccessClientVersion();
        assertEqualVersions(storedVersion, newVersion);
    }

    @Test
    public void testGetAdminToken() throws Exception {
        ArtifactoryAccessClientConfigStore configStore = createConfigStore();
        //get throws if there is no token
        assertFalse(configStore.isAdminTokenExists());
        assertThrows(IllegalStateException.class, configStore::getAdminToken);
        //get returns the token in clear text
        String adminToken = "the-token";
        configStore.storeAdminToken(adminToken);
        assertTrue(configStore.isAdminTokenExists());
        assertEquals(configStore.getAdminToken(), adminToken);
        //get returns the encrypted token in clear text
        initMasterKey();
        accessClientSettings.setAdminToken(masterEncryption.encryptIfNeeded(adminToken));
        assertTrue(configStore.isAdminTokenExists());
        assertEquals(configStore.getAdminToken(), adminToken);
    }

    @Test(dataProvider = "provideStoreAdminToken")
    public void testStoreAdminToken(boolean saveDescriptorAllowed) throws Exception {
        ArtifactoryAccessClientConfigStore configStore = createConfigStore(saveDescriptorAllowed);
        //store admin token, no master key
        assertFalse(configStore.isAdminTokenExists());
        String adminToken = "the-token";
        configStore.storeAdminToken(adminToken);
        assertTrue(configStore.isAdminTokenExists());
        assertEquals(configStore.getAdminToken(), adminToken);
        //store admin token, master key exists
        initMasterKey();
        configStore.storeAdminToken(adminToken);
        assertTrue(configStore.isAdminTokenExists());
        assertEquals(configStore.getAdminToken(), adminToken);
        if (saveDescriptorAllowed) {
            assertNotEquals(accessClientSettings.getAdminToken(), adminToken);
        }
        //revoke the admin token
        configStore.revokeAdminToken();
        assertFalse(configStore.isAdminTokenExists());
    }

    @DataProvider
    public static Object[][] provideStoreAdminToken() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @Test
    public void testRootCertificate() throws Exception {
        ArtifactoryAccessClientConfigStore configStore = createConfigStore();
        //get non-existing root certificate throws
        assertFalse(configStore.isRootCertificateExists());
        assertFalse(rootCrtFile.exists());
        assertThrows(RuntimeException.class, configStore::getRootCertificate);
        //create certificate and store it
        KeyPair keyPair = JFrogCryptoHelper.generateKeyPair();
        Certificate certificate = createCertificate(keyPair);
        configStore.storeRootCertificate(certificate);
        assertTrue(configStore.isRootCertificateExists());
        assertEquals(configStore.getRootCertificate(), certificate);
        assertTrue(rootCrtFile.exists());
        assertEquals(PemHelper.readCertificate(rootCrtFile).getEncoded(), certificate.getEncoded());
    }

    @Test
    public void testConvertClientConfigFromEmbeddedServer() throws Exception {
        //Create the obsolete files
        File keysDir = new File(testDir, "keys");
        FileUtils.forceMkdir(keysDir);
        File adminTokenFile = new File(keysDir, TEST_SERVICE_ID + ".token");
        File keyFile = new File(keysDir, TEST_SERVICE_ID + ".key");
        File crtFile = new File(keysDir, TEST_SERVICE_ID + ".crt");
        File keystoreFile = new File(keysDir, "keystore.jks");
        FileUtils.write(adminTokenFile, "the-token");
        FileUtils.write(keyFile, "the-key");
        FileUtils.write(crtFile, "the-crt");
        FileUtils.write(keystoreFile, "the-keystore");
        assertTrue(adminTokenFile.exists());
        assertTrue(keyFile.exists());
        assertTrue(crtFile.exists());
        assertTrue(keystoreFile.exists());

        ArtifactoryAccessClientConfigStore configStore = createConfigStore();
        assertFalse(adminTokenFile.exists());
        assertThrows(configStore::getAdminToken);   // the converter should discard the old admin token
        assertFalse(keyFile.exists());
        assertFalse(crtFile.exists());
        assertFalse(keystoreFile.exists());
    }

    @Test(dataProvider = "provideNewClientBuilder")
    public void testNewClientBuilder(String configServerUrl, String sysPropServerUrl, String adminToken,
            Long configCacheSize, Long sysPropCacheSize,
            Long configCacheExpiry, Long sysPropCacheExpiry,
            Integer maxConnections, Integer connectionTimeout,
            Integer socketTimeout, boolean forceRest, boolean forceGrpc) throws Exception {
        ArtifactoryAccessClientConfigStore configStore = createConfigStore();
        accessClientSettings.setServerUrl(configServerUrl);
        artSysProperties.setProperty(ConstantValues.accessClientServerUrlOverride.getPropertyName(), sysPropServerUrl);
        accessClientSettings.setTokenVerifyResultCacheSize(configCacheSize);
        artSysProperties.setProperty(ConstantValues.accessClientTokenVerifyResultCacheSize.getPropertyName(),
                str(sysPropCacheSize));
        accessClientSettings.setTokenVerifyResultCacheExpirySeconds(configCacheExpiry);
        artSysProperties.setProperty(ConstantValues.accessClientTokenVerifyResultCacheExpiry.getPropertyName(),
                str(sysPropCacheExpiry));

        accessClientSettings.setMaxConnections(maxConnections);
        artSysProperties.setProperty(ConstantValues.accessClientMaxConnections.getPropertyName(), str(maxConnections));
        accessClientSettings.setConnectionTimeout(connectionTimeout);
        artSysProperties
                .setProperty(ConstantValues.accessClientConnectionTimeout.getPropertyName(), str(connectionTimeout));
        accessClientSettings.setSocketTimeout(socketTimeout);
        artSysProperties.setProperty(ConstantValues.accessClientSocketTimeout.getPropertyName(), str(socketTimeout));
        artSysProperties.setProperty(ConstantValues.accessClientForceRest.getPropertyName(), String.valueOf(forceRest));
        artSysProperties.setProperty(ConstantValues.accessClientForceGrpc.getPropertyName(), String.valueOf(forceGrpc));

        Certificate certificate = createCertificate(JFrogCryptoHelper.generateKeyPair());
        configStore.storeRootCertificate(certificate);

        if (adminToken == null) {
            configStore.revokeAdminToken();
        } else {
            configStore.storeAdminToken(adminToken);
        }

        String expectedServerUrl = sysPropServerUrl != null ? sysPropServerUrl : configServerUrl;
        AccessAuth expectedAuth = adminToken == null ? null : new AccessAuthToken(adminToken);
        long expectedCacheSize = sysPropCacheSize != null && sysPropCacheSize >= 0 ? sysPropCacheSize :
                configCacheSize != null && configCacheSize >= 0 ? configCacheSize :
                        1000; //client default
        long expectedCacheExpiry = sysPropCacheExpiry != null && sysPropCacheExpiry >= 0 ? sysPropCacheExpiry :
                configCacheSize != null && configCacheExpiry >= 0 ? configCacheExpiry :
                        60; //client default
        int expectedMaxConnections;
        if (maxConnections == null) {
            expectedMaxConnections = ConstantValues.accessClientMaxConnections.getInt();
        } else if (maxConnections <= 0) {
            expectedMaxConnections = 3;
        } else {
            expectedMaxConnections = maxConnections;
        }
        int expectedConnectionTimeout = connectionTimeout != null && connectionTimeout > 0 ? connectionTimeout : 10000;
        int expectedSocketTimeout = socketTimeout != null && socketTimeout > 0 ? socketTimeout : 60000;

        AccessClientBuilder builder = configStore.newClientBuilder();

        assertEquals(builder.getServiceId(), TEST_SERVICE_ID);
        assertEquals(builder.getServerUrl(), expectedServerUrl);
        assertEquals(builder.getRootCertificateHolder().get(), certificate);
        assertEquals(builder.getDefaultAuth(), expectedAuth);
        assertEquals(builder.getTokenVerificationResultCacheSize(), expectedCacheSize);
        assertEquals(builder.getTokenVerificationResultCacheExpiry(), expectedCacheExpiry);
        assertEquals(builder.getMaxConnections(), expectedMaxConnections);
        assertEquals(builder.getConnectionTimeout(), expectedConnectionTimeout);
        assertEquals(builder.getSocketTimeout(), expectedSocketTimeout);
        assertEquals(builder.isForceRest(), forceRest);
        assertEquals(builder.isForceGrpc(), forceGrpc);
    }

    @DataProvider
    public static Object[][] provideNewClientBuilder() {
        return new Object[][]{
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 40L, 50, 60, 70, false, false},
                {"config-url", null, null, 10L, null, 30L, null, 50, 60, 70, false, false},
                {null, "sys-prop-url", "auth-token", null, 20L, null, 40L, 50, 60, 70, false, false},
                {null, "sys-prop-url", "auth-token", -1L, 20L, -1L, 40L, 50, 60, 70, false, false},
                {null, "sys-prop-url", "auth-token", null, null, null, null, 50, 60, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", -1L, -1L, -1L, -1L, 50, 60, 70, false, false},
                {"config-url", null, null, 0L, 20L, 0L, 40L, 50, 60, 70, false, false},
                {null, "sys-prop-url", "auth-token", -1L, 0L, -1L, 0L, 50, 60, 70, false, false},
                {null, "sys-prop-url", "auth-token", null, 0L, null, 0L, 50, 60, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, null, 60, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, null, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 60, null, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 0, 60, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 0, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 60, 0, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, -1, 60, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, -1, 70, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 60, -1, false, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 60, -1, true, false},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 60, -1, true, true},
                {"config-url", "sys-prop-url", "auth-token", 10L, 20L, 30L, 0L, 50, 60, -1, false, true},
        };
    }

    @Test(description =
            "This test tries to check the case when the server url is not specified in neither the system " +
                    "properties and the config xml. In such case it is expected that the listening port is detected. The " +
                    "detection is expected to fail (the test is not running in tomcat) with a specific exception and message. " +
                    "The test is running under the assumption that creating a new client builder from the config store first " +
                    "tries to get the server url.")
    public void testGetServerUrlTriesToDetectPortIfUrlIsNotSpecified() throws Exception {
        try {
            createConfigStore().newClientBuilder();
            fail("Expected to fail here...");
        } catch (Exception e) {
            assertCausedBy(e, IllegalStateException.class, ".*Could not detect listening port.*");
        }
    }

    private static String str(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private void assertEqualVersions(AccessVersion actual, AccessVersion expected) {
        assertEquals(actual.getName(), expected.getName());
        assertEquals(actual.getTimestamp(), expected.getTimestamp());
        assertEquals(actual.getRevision(), expected.getRevision());
    }

    private AccessVersion createAccessVersion(String version, String timestamp, String revision) {
        Properties properties = new Properties();
        properties.setProperty("access.version", version);
        properties.setProperty("access.timestamp", timestamp);
        properties.setProperty("access.revision", revision);
        return AccessVersion.read(properties);
    }

    private ArtifactoryAccessClientConfigStore createConfigStore() {
        return createConfigStore(true);
    }

    private ArtifactoryAccessClientConfigStore createConfigStore(boolean SaveDescriptorAllowed) {
        AccessServiceImpl accessService = createMock(AccessServiceImpl.class);
        accessService.runAfterContextCreated(anyObject(Runnable.class));
        expectLastCall().andAnswer(() -> {
            Runnable runnable = (Runnable) getCurrentArguments()[0];
            runnable.run();
            return null;
        }).anyTimes();
        //Artifactory home
        ArtifactoryHome artHome = createMock(ArtifactoryHome.class);
        expect(accessService.artifactoryHome()).andReturn(artHome).anyTimes();
        expect(artHome.getAccessClientDir()).andReturn(testDir).anyTimes();
        expect(artHome.getAccessAdminCredsFile()).andReturn(accessCredsFile).anyTimes();
        expect(artHome.getAccessAdminTokenFile()).andReturn(accessAdminTokenFile).anyTimes();
        expect(artHome.getArtifactoryEncryptionWrapper()).andAnswer(() -> masterEncryption).anyTimes();
        expect(artHome.getArtifactoryProperties()).andReturn(artSysProperties).anyTimes();
        //Config service
        InternalCentralConfigService configService = createNiceMock(InternalCentralConfigService.class);
        expect(accessService.centralConfigService()).andReturn(configService).anyTimes();
        CentralConfigDescriptorImpl descriptor = new CentralConfigDescriptorImpl();
        descriptor.setSecurity(new SecurityDescriptor());
        descriptor.getSecurity().setAccessClientSettings(accessClientSettings);
        expect(configService.getDescriptor()).andReturn(descriptor).anyTimes();
        expect(configService.getMutableDescriptor()).andReturn(descriptor).anyTimes();
        expect(configService.isSaveDescriptorAllowed()).andReturn(SaveDescriptorAllowed).anyTimes();
        configService.saveEditedDescriptorAndReload(anyObject(MutableCentralConfigDescriptor.class));
        expectLastCall().anyTimes();
        //Security service
        expect(accessService.securityService()).andReturn(createNiceMock(SecurityService.class)).anyTimes();

        // pass validation with existing token
        expect(accessService.parseToken(EasyMock.anyString()))
                .andAnswer(() -> {
                    return EasyMock.mock(JwtAccessTokenImpl.class);
                })
                .anyTimes();

        replay(accessService, artHome, configService);
        return new ArtifactoryAccessClientConfigStore(accessService, TEST_SERVICE_ID);
    }
}