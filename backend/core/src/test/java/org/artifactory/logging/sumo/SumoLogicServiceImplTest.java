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

package org.artifactory.logging.sumo;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.config.CentralConfigServiceImpl;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.storage.db.security.service.VersioningCacheImpl;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.util.StringInputStream;
import org.joda.time.format.DateTimeFormat;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.artifactory.test.TestUtils.*;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * <p>Created on 18/07/16
 *
 * @author Yinon Avraham
 */
@Test
public class SumoLogicServiceImplTest {

    private SumoLogicServiceImpl sumoLogicService = new SumoLogicServiceImpl();
    private CentralConfigService configService = new CentralConfigServiceImpl();

    @BeforeMethod
    public void setupTest() {
        sumoLogicService = new SumoLogicServiceImpl();
        configService = new CentralConfigServiceImpl();
        setDescriptor(configService, new CentralConfigDescriptorImpl());
        setField(sumoLogicService, "centralConfigService", configService);
    }

    @Test(dataProvider = "provideNeedToUpdateLogbackAppenderAccordingToSumoConfig")
    public void testNeedToUpdateLogbackAppenderAccordingToSumoConfig(
            SumoLogicConfigDescriptor oldSumoConfig, SumoLogicConfigDescriptor newSumoConfig, boolean expected) {
        System.out.println("testNeedToUpdateLogbackAppenderAccordingToSumoConfig: old=" + toString(oldSumoConfig) + ", new=" + toString(newSumoConfig));
        MutableCentralConfigDescriptor oldConfig = new CentralConfigDescriptorImpl();
        MutableCentralConfigDescriptor newConfig = new CentralConfigDescriptorImpl();
        assertNotNull(oldConfig.getSumoLogicConfig());

        oldConfig.setSumoLogicConfig(oldSumoConfig);
        newConfig.setSumoLogicConfig(newSumoConfig);
        assertEquals(needToUpdateLogbackAppender(oldConfig, newConfig), expected);
    }

    private boolean needToUpdateLogbackAppender(CentralConfigDescriptor oldConfig, CentralConfigDescriptor newConfig) {
        return (boolean) invokeMethod(sumoLogicService, "needToUpdateLogbackAppender",
                arrayOf(CentralConfigDescriptor.class, CentralConfigDescriptor.class), arrayOf(oldConfig, newConfig));
    }

    private String toString(SumoLogicConfigDescriptor sumoConfig) {
        if (sumoConfig == null) return "null";
        return "[" +
                "enabled=" + sumoConfig.isEnabled() +
                ", collectorUrl=" + sumoConfig.getCollectorUrl() +
                ", proxy=" + toString(sumoConfig.getProxy()) +
                "]";
    }

    private String toString(ProxyDescriptor proxy) {
        if (proxy == null) return "null";
        return "[" +
                "key=" + proxy.getKey() +
                ", host=" + proxy.getHost() +
                ", port=" + proxy.getPort() +
                ", default=" + proxy.isDefaultProxy() +
                "]";
    }

    @DataProvider
    private Object[][] provideNeedToUpdateLogbackAppenderAccordingToSumoConfig() {
        return new Object[][] {
                { null, null, false },
                { null, createSumoConfig(false, null, null), false },
                { null, createSumoConfig(true, null, null), true },
                { createSumoConfig(false, null, null), createSumoConfig(false, null, null), false },
                { createSumoConfig(false, null, null), createSumoConfig(true, null, null), true },
                { createSumoConfig(true, null, null), createSumoConfig(false, null, null), true },
                { createSumoConfig(true, null, null), createSumoConfig(true, "collector-url", null), true },
                { createSumoConfig(true, "collector-url", null), createSumoConfig(true, "collector-url", null), false },
                { createSumoConfig(true, "collector-url", null), createSumoConfig(true, "other-collector-url", null), true },
                { createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(true, "collector-url", null), true },
                { createSumoConfig(true, "collector-url", null), createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), true },
                { createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(true, "collector-url", createProxy("proxy2", "host1", 7777, false)), true },
                { createSumoConfig(true, "collector-url", createProxy("proxy1", "host2", 7777, false)), createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), true },
                { createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), false },
                { createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(true, "collector-url", createProxy("proxy1", "host1", 7777, true)), true },
                { createSumoConfig(false, "collector-url", null), createSumoConfig(false, "collector-url", null), false },
                { createSumoConfig(false, "collector-url", null), createSumoConfig(false, "other-collector-url", null), false },
                { createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(false, "collector-url", null), false },
                { createSumoConfig(false, "collector-url", null), createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), false },
                { createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(false, "collector-url", createProxy("proxy2", "host1", 7777, false)), false },
                { createSumoConfig(false, "collector-url", createProxy("proxy1", "host2", 7777, false)), createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), false },
                { createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), false },
                { createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, false)), createSumoConfig(false, "collector-url", createProxy("proxy1", "host1", 7777, true)), false }
        };
    }

    @Test(dataProvider = "provideNeedToUpdateLogbackAppenderAccordingToArtifactoryHost")
    public void testNeedToUpdateLogbackAppenderAccordingToArtifactoryHost(
            boolean oldEnabled, String oldUrlBase, String oldServerName,
            boolean newEnabled, String newUrlBase, String newServerName, boolean expected) {
        System.out.println("testNeedToUpdateLogbackAppenderAccordingToArtifactoryHost: " +
                "oldUrlBase=" + oldUrlBase + ", oldServerName=" + oldServerName +
                ", newUrlBase=" + newUrlBase + ", newServerName=" + newServerName);
        MutableCentralConfigDescriptor oldConfig = new CentralConfigDescriptorImpl();
        MutableCentralConfigDescriptor newConfig = new CentralConfigDescriptorImpl();

        oldConfig.getSumoLogicConfig().setEnabled(oldEnabled);
        oldConfig.setUrlBase(oldUrlBase);
        oldConfig.setServerName(oldServerName);

        newConfig.getSumoLogicConfig().setEnabled(newEnabled);
        newConfig.setUrlBase(newUrlBase);
        newConfig.setServerName(newServerName);

        assertEquals(needToUpdateLogbackAppender(oldConfig, newConfig), expected);
    }

    @DataProvider
    private Object[][] provideNeedToUpdateLogbackAppenderAccordingToArtifactoryHost() {
        return new Object[][] {
                { false, null, null, false, null, null, false },
                { false, null, null, false, "the-url", null, false },
                { false, null, null, false, null, "the-name", false },
                { false, "the-url", null, false, null, null, false },
                { false, null, "the-name", false, null, null, false },
                { false, "the-url", "the-name", true, "the-url", "the-name", true },
                { true, "the-url", "the-name", false, "the-url", "the-name", true },
                { true, null, null, true, "the-url", null, true },
                { true, null, null, true, null, "the-name", true },
                { true, "the-url", null, true, null, null, true },
                { true, null, "the-name", true, null, null, true },
                { true, "the-url", null, true, "the-new-url", null, true },
                { true, null, "the-name", true, null, "the-new-name", true },
                { true, "the-url", "the-name", true, "the-new-url", "the-name", true },
                { true, "the-url", "the-name", true, "the-url", "the-new-name", false }, // url base is used when it is set, so server name change does not matter
                { true, "the-url", "the-name", true, "the-url", "the-name", false }
        };
    }

    @Test
    public void testGetArtifactoryHost() {
        MutableCentralConfigDescriptor config = new CentralConfigDescriptorImpl();
        setDescriptor(configService, config);

        assertNotNull(getArtifactoryHost());

        config.setServerName("the-server");
        setDescriptor(configService, config);
        assertEquals(getArtifactoryHost(), "the-server");

        config.setUrlBase("http://the-host");
        assertEquals(getArtifactoryHost(), "the-host");

        config.setUrlBase("http://the-host:1234");
        assertEquals(getArtifactoryHost(), "the-host:1234");

        config.setUrlBase("http://the-host/foo/bar");
        assertEquals(getArtifactoryHost(), "the-host");

        config.setUrlBase("http://the-host/artifactory");
        assertEquals(getArtifactoryHost(), "the-host");

        config.setUrlBase("http://the-host:1234/foo/bar");
        assertEquals(getArtifactoryHost(), "the-host:1234");
    }

    @Test
    public void testBuildNewTokenRequest() {
        MutableCentralConfigDescriptor config = configService.getMutableDescriptor();
        SumoLogicConfigDescriptor sumoConfig = config.getSumoLogicConfig();
        sumoConfig.setClientId("the-client-id");
        sumoConfig.setSecret("the-secret");
        setDescriptor(configService, config);
        String requestBody = (String) invokeMethod(sumoLogicService, "buildNewTokenRequest", arrayOf(String.class), arrayOf("the-code"));
        assertEquals(requestBody, "{\"grant_type\":\"authorization_code\",\"code\":\"the-code\",\"client_id\":\"the-client-id\",\"client_secret\":\"the-secret\"}");
    }

    @Test
    public void testBuildRefreshTokenRequest() {
        MutableCentralConfigDescriptor config = configService.getMutableDescriptor();
        SumoLogicConfigDescriptor sumoConfig = config.getSumoLogicConfig();
        sumoConfig.setClientId("the-client-id");
        sumoConfig.setSecret("the-secret");
        setDescriptor(configService, config);
        String requestBody = (String) invokeMethod(sumoLogicService, "buildRefreshTokenRequest", arrayOf(String.class), arrayOf("the-refresh-token"));
        assertEquals(requestBody, "{\"grant_type\":\"refresh_token\",\"refresh_token\":\"the-refresh-token\",\"client_id\":\"the-client-id\",\"client_secret\":\"the-secret\"}");
    }

    @Test
    public void testBuildAppIntegrationRequestForNewSetup() {
        MutableCentralConfigDescriptor config = configService.getMutableDescriptor();
        SumoLogicConfigDescriptor sumoConfig = config.getSumoLogicConfig();
        sumoConfig.setClientId("the-client-id");
        sumoConfig.setSecret("the-secret");
        setDescriptor(configService, config);
        String requestBody = buildAppIntegrationRequest(true);
        assertEquals(requestBody, "{\"request_type\":\"setup_integration\",\"client_id\":\"the-client-id\",\"client_secret\":\"the-secret\"}");
    }

    @Test
    public void testBuildAppIntegrationRequestForExistingSetup() {
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        MutableCentralConfigDescriptor config = configService.getMutableDescriptor();
        SumoLogicConfigDescriptor sumoConfig = config.getSumoLogicConfig();
        sumoConfig.setClientId("the-client-id");
        sumoConfig.setSecret("the-secret");
        setDescriptor(configService, config);
        String requestBody = buildAppIntegrationRequest(false);
        assertEquals(requestBody, "{\"request_type\":\"retrieve_endpoints\",\"client_id\":\"the-client-id\",\"client_secret\":\"the-secret\"}");
    }

    @Test(dataProvider = "provideTryExtractErrorMessage")
    public void testTryExtractErrorMessage(String responseBody, String expectedMessage) throws Exception {
        String message = (String) invokeMethod(sumoLogicService, "tryExtractErrorMessage", arrayOf(String.class), arrayOf(responseBody));
        assertEquals(message, expectedMessage);
    }

    @DataProvider
    private Object[][] provideTryExtractErrorMessage() {
        return new Object[][] {
                { null, null },
                { "", null },
                { "not a real json", null },
                { "{\"unexpected_field\":\"value\"}", null },
                { "{\"error\":\"the-error-code\",\"error_description\":\"the error description\"}", "the error description" },
                { "{\"error_description\":\"the error description\"}", "the error description" },
                { "{\"error\":\"the-error-code\",\"error_description\":\"\"}", "the-error-code" },
                { "{\"error\":\"the-error-code\",\"error_description\":null}", "the-error-code" },
                { "{\"error\":\"the-error-code\"}", "the-error-code" },
                { "{\"error\":\"\"}", null },
                { "{\"error\":null}", null }
        };
    }

    @Test(dataProvider = "provideTryReadResponseBodyAsString")
    public void testTryReadResponseBodyAsString(String responseBody, boolean nullEntity, boolean getContentThrows, String expectedBody) throws Exception {
        HttpEntity entity = null;
        if (!nullEntity) {
            entity = createMock(HttpEntity.class);
            if (getContentThrows) {
                expect(entity.getContent()).andThrow(new RuntimeException("Expected exception")).anyTimes();
            } else {
                expect(entity.getContent()).andReturn(responseBody == null ? null : new StringInputStream(responseBody)).anyTimes();
            }
            replay(entity);
        }
        CloseableHttpResponse response = createMock(CloseableHttpResponse.class);
        expect(response.getEntity()).andReturn(entity).anyTimes();
        replay(response);

        String bodyAsString = (String) invokeMethod(sumoLogicService, "tryReadResponseBodyAsString", arrayOf(CloseableHttpResponse.class), arrayOf(response));
        assertEquals(bodyAsString, expectedBody);
    }

    @DataProvider
    private Object[][] provideTryReadResponseBodyAsString() {
        return new Object[][] {
                { null, false, false, null },
                { "", false, false, "" },
                { "this is the body", false, false, "this is the body" },
                { "", true, false, null },
                { "", false, true, null }
        };
    }

    private String buildAppIntegrationRequest(boolean newSetup) {
        return (String) invokeMethod(sumoLogicService, "buildAppIntegrationRequest", arrayOf(Boolean.TYPE), arrayOf(newSetup));
    }

    private void setDescriptor(CentralConfigService configService, MutableCentralConfigDescriptor config) {
        CentralConfigServiceImpl.CentralConfigDescriptorCache cache = new CentralConfigServiceImpl.CentralConfigDescriptorCache(
                config, "the-server", DateTimeFormat.forPattern(config.getDateFormat()));
        VersioningCacheImpl<CentralConfigServiceImpl.CentralConfigDescriptorCache> versioningCacheImpl =
                new VersioningCacheImpl<>(3000, () -> cache);
        ReflectionTestUtils.setField(configService, "descriptorCache", versioningCacheImpl);
    }

    private String getArtifactoryHost() {
        return (String) invokeMethod(sumoLogicService, "getArtifactoryHost", arrayOf(), arrayOf());
    }

    private ProxyDescriptor createProxy(String key, String host, int port, boolean isDefault) {
        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setKey(key);
        proxy.setHost(host);
        proxy.setPort(port);
        proxy.setDefaultProxy(isDefault);
        return proxy;
    }

    private SumoLogicConfigDescriptor createSumoConfig(boolean enabled, String collectorUrl, ProxyDescriptor proxy) {
        SumoLogicConfigDescriptor sumoConfig = new SumoLogicConfigDescriptor();
        sumoConfig.setEnabled(enabled);
        sumoConfig.setCollectorUrl(collectorUrl);
        sumoConfig.setProxy(proxy);
        return sumoConfig;
    }

}