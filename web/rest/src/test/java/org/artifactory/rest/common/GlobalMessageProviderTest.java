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



package org.artifactory.rest.common;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.FooterMessage;
import org.artifactory.addon.FooterMessage.FooterMessageVisibility;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.access.AccessService;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.db.conversion.version.v213.V213ConversionFailFunction;
import org.artifactory.storage.fs.service.ConfigsService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.glassfish.jersey.server.ContainerResponse;
import org.jfrog.common.JsonUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

import static org.artifactory.addon.FooterMessage.createError;
import static org.artifactory.addon.FooterMessage.createWarning;
import static org.artifactory.rest.common.GlobalMessageProvider.UI_MESSAGES_TAG;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;


/**
 * @author Dan Feldman
 */

@Test
public class GlobalMessageProviderTest extends ArtifactoryHomeBoundTest {

    private static final FooterMessage WARN = createWarning("I'm a teapot!", FooterMessageVisibility.admin);
    private static final FooterMessage ERR = createError("I am putting myself to the fullest possible use, which is all I think that any conscious entity can ever hope to do.", FooterMessageVisibility.admin);
    private static final String EMPTY_MESSAGES = "[]";

    @Mock
    private AddonsManager addonsManager;
    @Mock
    private StorageService storageService;
    @Mock
    private AuthorizationService authService;
    @Mock
    private XrayAddon xrayAddon;
    @Mock
    private ContainerResponse containerRes;
    @Mock
    private Response res;
    @Mock
    private ConfigsService configsService;
    @Mock
    private AccessService accessService;

    private GlobalMessageProvider msgProvider;
    private MultivaluedMap<String, Object> responseMetadata;
    private File tempFile;

    @BeforeClass
    public void setupMocks() throws IOException {
        MockitoAnnotations.initMocks(this);
        responseMetadata = new StringKeyIgnoreCaseMultivaluedMap<>();
        when(res.getMetadata()).thenReturn(responseMetadata);
        when(containerRes.getHeaders()).thenReturn(responseMetadata);
        when(accessService.isAdminUsingOldDefaultPassword()).thenReturn(false);
        tempFile = File.createTempFile("msg", ".tmp");
    }

    //Current default messages use the addon manager and storage service, apart from their own specific tests they should
    //not return their respective messages.
    @BeforeMethod
    public void setupDefaultMockResponses() {
        //Need a new message provider each time to clear its cache.
        msgProvider = new GlobalMessageProvider();
        when(addonsManager.getLicenseFooterMessage()).thenReturn(null);
        when(addonsManager.getRemoteRepUnsupportedUrls()).thenReturn(null);
        when(storageService.getStorageQuotaInfo(0)).thenReturn(null);
        when(addonsManager.addonByType(XrayAddon.class)).thenReturn(xrayAddon);
        when(xrayAddon.isXrayEnabled()).thenReturn(false);
        when(authService.isAdmin()).thenReturn(false);
        when(authService.isAnonymous()).thenReturn(false);
        when(configsService.hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER)).thenReturn(false);
    }

    @AfterMethod
    public void clean() {
        responseMetadata.clear();
        homeStub.setProperty(ConstantValues.uiCustomFooterMessageFile, null);
        reset(addonsManager, storageService, authService, xrayAddon);
    }

    public void noMessagesByDefault() {
        decorateWithGlobalMessages();
        assertNoMessages();
    }

    //Anonymous should not see admin messages, this test sets all admin messages (xray, pypi and quota)
    public void testNoAdminMessagesShownToAnon() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(false);
        when(authService.isAnonymous()).thenReturn(true);

        StorageQuotaInfo quota = mock(StorageQuotaInfo.class);
        when(quota.isLimitReached()).thenReturn(true);
        when(storageService.getStorageQuotaInfo(0)).thenReturn(quota);
        when(addonsManager.getRemoteRepUnsupportedUrls()).thenReturn(ERR);
        when(xrayAddon.isXrayEnabled()).thenReturn(true);
        when(xrayAddon.isXrayAlive()).thenReturn(false);

        decorateWithGlobalMessages();
        assertNoMessages();
    }


    @Test(dataProvider = "providePsqlMessageValues")
    public void testDecorateWithPsqlIndexMismatch(boolean isAdmin, boolean configExists, boolean messageExpected) {
        reset(authService);
        when(authService.isAdmin()).thenReturn(isAdmin);
        when(authService.isAnonymous()).thenReturn(!isAdmin);
        when(configsService.hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER)).thenReturn(configExists);
        decorateWithGlobalMessages();
        if (messageExpected) {
            assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains(
                    "An incompatible index has been found for the Artifactory ‘node_props’ database table. " +
                            "This may be caused by certain property values that exceed the allowed length limit, " +
                            "causing creation of the database index to fail during a version upgrade. <br/>" +
                            "For instructions on how to fix this error <a href='https://www.jfrog.com/confluence/display/RTF/Troubleshooting#Troubleshooting-RecoveringfromError:AnincompatibleindexhasbeenfoundfortheArtifactory%E2%80%98node_props%E2%80%99databasetable'>click here</a>");
        } else {
            assertNoMessages();
        }
    }

    @Test
    public void needToChangeAccessAdminPassword() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        when(authService.isAnonymous()).thenReturn(false);
        when(configsService.hasConfig(V213ConversionFailFunction.PSQL_NODE_PROPS_INDEX_MISSING_MARKER)).thenReturn(true);
        when(accessService.isAdminUsingOldDefaultPassword()).thenReturn(true);
        decorateWithGlobalMessages();
        assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains("Your Artifactory instance is set up with " +
                "default credentials to connect to the security datastore via localhost. It is highly recommended to change the default password.");
        reset(accessService);
    }

    @DataProvider
    private static Object[][] providePsqlMessageValues() {
        return new Object[][]{
                // isAdminUser, configExists, messageExcepted
                { true, true, true },
                { false, true, false },
                { true, false, false },
                { false, false, false },
        };
    }

    public void testLicenseMsg() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        when(addonsManager.getLicenseFooterMessage()).thenReturn(WARN);

        decorateWithGlobalMessages();
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(toJson(WARN));
    }

    public void testPypiMsg() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        when(addonsManager.getRemoteRepUnsupportedUrls()).thenReturn(ERR);

        decorateWithGlobalMessages();
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(toJson(ERR));
    }

    public void testQuotaErr() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        StorageQuotaInfo quota = mock(StorageQuotaInfo.class);
        when(quota.isLimitReached()).thenReturn(true);
        when(quota.getErrorMessage()).thenReturn("Quota limit reached!");
        when(storageService.getStorageQuotaInfo(0)).thenReturn(quota);

        decorateWithGlobalMessages();
        assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains("Quota limit reached!");
    }

    public void testQuotaWarn() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        StorageQuotaInfo quota = mock(StorageQuotaInfo.class);
        when(quota.isLimitReached()).thenReturn(false);
        when(quota.isWarningLimitReached()).thenReturn(true);
        when(quota.getWarningMessage()).thenReturn("Quota warn!");
        when(storageService.getStorageQuotaInfo(0)).thenReturn(quota);

        decorateWithGlobalMessages();
        assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains("Quota warn!");
    }

    public void testXrayLiveWarn() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        when(xrayAddon.isXrayEnabled()).thenReturn(true);
        when(xrayAddon.isXrayAlive()).thenReturn(false);

        decorateWithGlobalMessages();
        assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains("JFrog Xray is unavailable");
    }

    public void testXrayVersionWarn() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(true);
        when(xrayAddon.isXrayEnabled()).thenReturn(true);
        when(xrayAddon.isXrayAlive()).thenReturn(true);
        when(xrayAddon.isXrayVersionValid()).thenReturn(false);

        decorateWithGlobalMessages();
        assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains("is incompatible with this");
    }

    //FooterMessageVisibility.all should be shown to anon
    public void testAnonymousCanSeeAllVisibility() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(false);
        when(authService.isAnonymous()).thenReturn(true);

        FooterMessage anonVisibleMsg = createWarning("Anonymous can see me", FooterMessageVisibility.all);
        when(addonsManager.getLicenseFooterMessage()).thenReturn(anonVisibleMsg);

        decorateWithGlobalMessages();
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(toJson(anonVisibleMsg));
    }

    //FooterMessageVisibility.user should be shown to non admin, non anon
    public void testUserCanSeeUserVisibility() {
        reset(authService);
        when(authService.isAdmin()).thenReturn(false);
        when(authService.isAnonymous()).thenReturn(false);

        FooterMessage anonVisibleMsg = createWarning("user can see me", FooterMessageVisibility.all);
        when(addonsManager.getLicenseFooterMessage()).thenReturn(anonVisibleMsg);

        decorateWithGlobalMessages();
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(toJson(anonVisibleMsg));
    }

    public void testMessageWithoutRange() throws IOException {
        FooterMessage msg = FooterMessage.builder()
                .message("meow")
                .type("info")
                .visibility("user")
                .build();
        setCustomMessage(msg);

        decorateWithGlobalMessages();
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(toJson(msg));
    }

    public void testCustomMessageCache() throws IOException {
        decorateWithGlobalMessages();
        assertNoMessages();
        FooterMessage msg = FooterMessage.builder()
                .message("meow")
                .type("info")
                .visibility("user")
                .build();
        setCustomMessage(msg);
        // message will not be updated for an hour
        decorateWithGlobalMessages();
        assertNoMessages();
    }

    public void testMessageInRange() throws IOException {
        FooterMessage msg = FooterMessage.builder()
                .message("how")
                .type("info")
                .visibility("user")
                .showFrom(System.currentTimeMillis() - 1L)
                .showUntil(System.currentTimeMillis() + 1000000L)
                .build();
        setCustomMessage(msg);

        decorateWithGlobalMessages();
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(toJson(msg));
    }

    //A message with a range that has already passed (should not show)
    public void testMessageWithPastRange() throws IOException {
        FooterMessage msg = FooterMessage.builder()
                .message("This is a test of the emergency broadcast system, please stand by.")
                .type("info")
                .visibility("user")
                .showFrom(System.currentTimeMillis() - 10000000L)
                .showUntil(System.currentTimeMillis() - 1000L)
                .build();
        setCustomMessage(msg);

        decorateWithGlobalMessages();
        assertNoMessages();
    }

    //A message with a range that was not yet reached (should not show)
    public void testMessageWithFutureRange() throws IOException {
        FooterMessage msg = FooterMessage.builder()
                .message("I feel good today, Silent Bob. We're gonna make some money...")
                .type("info")
                .visibility("user")
                .showFrom(System.currentTimeMillis() + 10000000L)
                .showUntil(System.currentTimeMillis() + 10000000000L)
                .build();
        setCustomMessage(msg);

        decorateWithGlobalMessages();
        assertNoMessages();
    }

    public void testDismissCode() throws IOException {
        FooterMessage msg = FooterMessage.builder()
                .message("My dismiss code bring all the bots to the yard")
                .type("info")
                .visibility("user")
                .dismissible(true)
                .build();

        setCustomMessage(msg);
        decorateWithGlobalMessages();
        assertThat((String) responseMetadata.getFirst(UI_MESSAGES_TAG)).contains("\"dismissCode\":\"331d7e580da8973a49b2c4565eb7323f\"");
    }

    private void decorateWithGlobalMessages() {
        msgProvider.decorateWithGlobalMessages(containerRes, addonsManager, storageService, authService, configsService,
                accessService);
    }

    private void setCustomMessage(FooterMessage msg) throws IOException {
        Files.write(toJson(msg).getBytes(), tempFile);
        homeStub.setProperty(ConstantValues.uiCustomFooterMessageFile, tempFile.getAbsolutePath());
    }

    private void assertNoMessages() {
        assertThat(responseMetadata.getFirst(UI_MESSAGES_TAG)).isEqualTo(EMPTY_MESSAGES);
    }

    private String toJson(FooterMessage msg) {
        return JsonUtils.getInstance().valueToString(ImmutableList.of(msg));
    }
}