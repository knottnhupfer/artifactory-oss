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
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationException;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.accesstoken.AccessClientSettings;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.test.TestUtils;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.AccessClientException;
import org.jfrog.access.client.cert.CertClient;
import org.jfrog.access.client.config.ConfigClient;
import org.jfrog.access.client.system.SystemClient;
import org.jfrog.access.client.token.TokenClient;
import org.jfrog.access.client.token.TokenRequest;
import org.jfrog.access.client.token.TokenResponseImpl;
import org.jfrog.client.http.RestResponse;
import org.jfrog.security.crypto.JFrogCryptoHelper;
import org.jfrog.security.ssl.CertificateGenerationException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.security.cert.CertificateEncodingException;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.artifactory.test.TestUtils.createCertificate;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.*;

/**
 * @author Yinon Avraham
 */
public class AccessServiceImplTest {

    private static final CharSequence TEST_JFROG_JOIN_KEY = "cc949ef041b726994a225dc20e018f2310101010101010101010101010101010";
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private UserGroupStoreService userGroupStore;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private SecurityService securityService;
    @Mock
    private InternalCentralConfigService centralConfigService;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private RepositoryService repositoryService;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private ConfigsService configsService;
    @Mock
    private AccessClient accessClient;

    private AccessServiceImpl accessServiceSpy;

    private static long constValueDefaultMaxExpiresIn = TimeUnit.HOURS.toSeconds(1);//default value of ConstantValues.accessTokenNonAdminMaxExpiresIn
    private static long USER_TOKEN_MAX_EXPIRES_IN_MINUTES_DEFAULT = TimeUnit.SECONDS.toMinutes(constValueDefaultMaxExpiresIn);

    @BeforeMethod
    public void resetAll()
            throws CertificateException, CertificateGenerationException, CertificateEncodingException, IOException {
        initMocks(this);

        ArtifactoryHomeStub artifactory = new ArtifactoryHomeStub(
                "./target/test/" + AccessServiceImplTest.class.getSimpleName());
        ArtifactoryHome.bind(artifactory);

        AccessServiceImpl accessService = new AccessServiceImpl(addonsManager, publisher, configsService);
        accessService.setSecurityService(securityService);
        accessService.setAuthorizationService(authorizationService);
        accessService.setCentralConfigService(centralConfigService);
        accessService.setUserGroupService(userGroupService);
        accessService.setUserGroupStore(userGroupStore);
        accessService.setRepositoryService(repositoryService);


        accessServiceSpy = Mockito.spy(accessService);
        when(accessClient.useAuth(argThat(i -> true))).thenReturn(accessClient);
        TokenClient tokenClient = mock(TokenClient.class);
        //noinspection ConstantConditions
        when(tokenClient.create(argThat(i -> true)))
                .thenReturn(new TokenResponseImpl("blah", "blah", "blah", Long.MAX_VALUE));
        when(accessClient.token()).thenReturn(tokenClient);
        SystemClient systemClient = mock(SystemClient.class);
        when(systemClient.getAccessServerName()).thenReturn("blah");
        when(accessClient.system()).thenReturn(systemClient);
        RestResponse restResponse = mock(RestResponse.class);
        when(restResponse.isSuccessful()).thenReturn(true);
        when(restResponse.getBodyAsString()).thenReturn("{\"token\":\"aToken\"}");
        when(accessClient.restCall(argThat(i -> true))).thenReturn(restResponse);
        CertClient certClient = mock(CertClient.class);

        Certificate certificate = createCertificate(JFrogCryptoHelper.generateKeyPair());
        when(certClient.getRootCertificate()).thenReturn(certificate);
        when(accessClient.cert()).thenReturn(certClient);

        //newClientBuilder()->getAccessServerSettings()->detectBundledAccessServerUrl() is hard to mock, so we stub
        doReturn(accessClient).when(accessServiceSpy).buildAccessClient();
        doReturn(true).when(accessServiceSpy).isUsingBundledAccessServer();

        HaCommonAddon haCommonAddon = mock(HaCommonAddon.class);
        when(addonsManager.addonByType(HaCommonAddon.class)).thenReturn(haCommonAddon);
        CentralConfigDescriptorImpl configDescriptor = new CentralConfigDescriptorImpl();
        when(centralConfigService.getDescriptor()).thenReturn(configDescriptor);
        ConfigClient configClient = mock(ConfigClient.class);
        when(accessClient.config()).thenReturn(configClient);

        File joinKeyFile = ArtifactoryHome.get().getJoinKeyFile();
        FileUtils.write(joinKeyFile, TEST_JFROG_JOIN_KEY);
        accessServiceSpy.init();
    }

    @Test(dataProvider = "provideNormalizeInstanceId")
    public void testNormalizeInstanceId(String original, String expected) {
        assertEquals(AccessServiceImpl.normalizeInstanceId(original), expected);
    }

    @DataProvider
    private static Object[][] provideNormalizeInstanceId() {
        String uuid = UUID.randomUUID().toString();
        return new Object[][]{
                {uuid, uuid},
                {"b0902ad17dfa28f5:7c169c4a:15849ae7faa:-8000", "b0902ad17dfa28f5_7c169c4a_15849ae7faa_-8000"},
                {" asdf 1234-erty_dfg5+sfg:sdf65=ghtr54 ", "_asdf_1234-erty_dfg5_sfg_sdf65_ghtr54"}
        };
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNormalizeInstanceIdTooShort() {
        AccessServiceImpl.normalizeInstanceId("foo");
    }

    @Test(dataProvider = "provideValidExpiresInForNonAdmin")
    public void testAssertExpiresInForNonAdminUsers(boolean nonnull, Long maxExpiresIn, Long expiresIn,
            Long maxExpiresInConstValue) {
        CentralConfigDescriptorImpl centralConfig = new CentralConfigDescriptorImpl();
        centralConfig.setSecurity(new SecurityDescriptor());
        AccessClientSettings accessClientSettings = null;
        if (nonnull) {
            accessClientSettings = new AccessClientSettings();
            accessClientSettings.setUserTokenMaxExpiresInMinutes(maxExpiresIn);
        }
        centralConfig.getSecurity().setAccessClientSettings(accessClientSettings);
        when(centralConfigService.getDescriptor()).thenReturn(centralConfig);
        TestUtils.setField(accessServiceSpy, "centralConfigService", centralConfigService);
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.accessTokenNonAdminMaxExpiresIn.getPropertyName(),
                        maxExpiresInConstValue.toString());

        TokenRequest tokenRequest = TokenRequest.scopes("api:*").nonRefreshable().expiresIn(expiresIn).build();
        accessServiceSpy.assertValidExpiresInForNonAdmin(tokenRequest, "testuser", false);
    }

    @DataProvider
    public static Object[][] provideValidExpiresInForNonAdmin() {
        long validDefaultExpiresIn = USER_TOKEN_MAX_EXPIRES_IN_MINUTES_DEFAULT * 60 / 2;
        return new Object[][]{
                //{nonnull, constValueDefaultMaxExpiresIn, expiresIn, maxExpiresInConstValue}
                {false, null, validDefaultExpiresIn, constValueDefaultMaxExpiresIn}, //expects default max expires in
                {true, null, validDefaultExpiresIn, constValueDefaultMaxExpiresIn}, //expects default max expires in
                {true, null, USER_TOKEN_MAX_EXPIRES_IN_MINUTES_DEFAULT, constValueDefaultMaxExpiresIn}, //expects default max expires in
                {true, 10L, 5L * 60, constValueDefaultMaxExpiresIn},
                {true, 10L, 10L * 60, constValueDefaultMaxExpiresIn},
                {true, 0L, 10L, constValueDefaultMaxExpiresIn},
                {true, 0L, null, constValueDefaultMaxExpiresIn},
                {true, 0L, Long.MAX_VALUE, constValueDefaultMaxExpiresIn},
                {true, null, constValueDefaultMaxExpiresIn - 60, constValueDefaultMaxExpiresIn - 60},
                {true, null, constValueDefaultMaxExpiresIn, constValueDefaultMaxExpiresIn},
                {true, null, constValueDefaultMaxExpiresIn + 60, constValueDefaultMaxExpiresIn + 60},
        };
    }

    @Test(dataProvider = "provideInvalidExpiresInForNonAdmin", expectedExceptions = AuthorizationException.class)
    public void failAssertExpiresInForNonAdminUsers(boolean nonnull, Long maxExpiresIn, Long expiresIn,
            Long maxExpiresInConstValue) {
        testAssertExpiresInForNonAdminUsers(nonnull, maxExpiresIn, expiresIn, maxExpiresInConstValue);
    }

    @DataProvider
    public static Object[][] provideInvalidExpiresInForNonAdmin() {
        long invalidDefaultExpiresIn = USER_TOKEN_MAX_EXPIRES_IN_MINUTES_DEFAULT * 60 + 1;
        return new Object[][]{
                //{nonnull, constValueDefaultMaxExpiresIn, expiresIn, maxExpiresInConstValue}
                {false, null, invalidDefaultExpiresIn, constValueDefaultMaxExpiresIn}, //expects default max expires in
                {true, null, invalidDefaultExpiresIn, constValueDefaultMaxExpiresIn}, //expects default max expires in
                {true, null, 0L, constValueDefaultMaxExpiresIn},
                {true, 10L, 10L * 60 + 1, constValueDefaultMaxExpiresIn},
                {true, 10L, null, constValueDefaultMaxExpiresIn},
                {true, null, Long.MAX_VALUE, constValueDefaultMaxExpiresIn},
                {true, null, constValueDefaultMaxExpiresIn, constValueDefaultMaxExpiresIn - 60},
                {true, null, constValueDefaultMaxExpiresIn + 60, constValueDefaultMaxExpiresIn},
                {true, null, constValueDefaultMaxExpiresIn + 60 + 60, constValueDefaultMaxExpiresIn + 60},
        };
    }

    @Test
    public void testEncryptOrDecrypt() {
        ArtifactoryAccessClientConfigStore configStore = mock(ArtifactoryAccessClientConfigStore.class);
        TestUtils.setField(accessServiceSpy, "configStore", configStore);
        accessServiceSpy.encryptOrDecrypt(true);
        verify(configStore, times(1)).encryptOrDecryptAccessCreds(true);
        reset(configStore);
        accessServiceSpy.encryptOrDecrypt(false);
        verify(configStore, times(1)).encryptOrDecryptAccessCreds(false);
    }

    @Test(dataProvider = "provideAssertionVariants")
    public void testInitAccessServiceAssertsClientServerMatch(String verAssert, String dev, boolean shouldThrow)
            throws IOException {
        //arrange
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(ConstantValues.dev.getPropertyName(), dev);
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.accessClientIgnoreServerVersionAssertion.getPropertyName(), verAssert);
        doThrow(new AccessClientException("message")).when(accessClient).assertClientServerVersionsMatch();

        // refresh join key
        File joinKeyFile = ArtifactoryHome.get().getJoinKeyFile();
        FileUtils.write(joinKeyFile, TEST_JFROG_JOIN_KEY);
        try {
            //act
            accessServiceSpy.initAccessService(null);
            //assert
            assertFalse(shouldThrow);
        } catch (Exception e) {
            assertTrue(shouldThrow);
        }
    }

    @Test
    public void testAccessTimeOutDuringBootstrap() throws Exception {
        ArtifactoryHome.get().getArtifactoryProperties()
                .setProperty(ConstantValues.accessClientWaitForServer.getPropertyName(), "2");
        doThrow(new AccessClientException("message")).when(this.accessClient).assertClientServerVersionsMatch();
        try {
            accessServiceSpy.initAccessService("service-id");
        } catch (Exception e) {
        }
        verify(this.accessClient, times(0)).close();
    }

    @DataProvider
    public Object[][] provideAssertionVariants() {
        return new Object[][]{
                //{ ConstantValues.accessClientIgnoreServerVersionAssertion, ConstantValues.dev, shouldThrow}
                {"true", "true", false},
                {"false", "true", false},
                {"true", "false", false},
                {"false", "false", true},
        };
    }
}