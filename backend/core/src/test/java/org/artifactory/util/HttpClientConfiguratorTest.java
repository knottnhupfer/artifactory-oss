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

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.http.GuaveCacheConnectionManagersHolder;
import org.artifactory.repo.http.IdleConnectionMonitorService;
import org.artifactory.repo.http.IdleConnectionMonitorServiceImpl;
import org.artifactory.spring.InternalArtifactoryContext;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.TestUtils;
import org.artifactory.util.bearer.RepoSpecificBearerSchemeFactory;
import org.easymock.EasyMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests the HTTP client configurator behaviors and conditions.
 * Implementation is package protected so most test must be done using reflection.
 *
 * @author Yossi Shaul
 */
@Test
public class HttpClientConfiguratorTest extends ArtifactoryHomeBoundTest {

    @BeforeMethod
    public void setUp() throws Exception {
        bindArtifactoryHome();

        getBound().setProperty(ConstantValues.idleConnectionMonitorInterval, "10");
        getBound().setProperty(ConstantValues.disableIdleConnectionMonitoring, "false");
        IdleConnectionMonitorService idleConnectionMonitorService = new IdleConnectionMonitorServiceImpl(new GuaveCacheConnectionManagersHolder());
        ArtifactoryContext contextMock = EasyMock.createMock(InternalArtifactoryContext.class);
        EasyMock.expect(contextMock.beanForType(IdleConnectionMonitorService.class))
                .andReturn(idleConnectionMonitorService).anyTimes();
        ArtifactoryContextThreadBinder.bind(contextMock);
        EasyMock.replay(contextMock);

        //Version might not have been initialized
        if (StringUtils.isBlank(ConstantValues.artifactoryVersion.getString())) {
            ArtifactoryHome.get().getArtifactoryProperties().
                    setProperty(ConstantValues.artifactoryVersion.getPropertyName(), "momo");
        }
    }

    @AfterMethod
    public void tearDown() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testHostFromInvalidUrl() {
        new HttpClientConfigurator().hostFromUrl("sttp:/.com");
    }

    public void testTokenAuthentication() throws IOException {
        try (CloseableHttpClient client = new HttpClientConfigurator()
                .host("bob").enableTokenAuthentication(true, null, null).build()) {
            Registry<AuthSchemeProvider> registry = getAuthSchemeRegistry(client);
            assertThat(registry.lookup("bearer")).isInstanceOf(RepoSpecificBearerSchemeFactory.class);
            RequestConfig defaultConfig = getDefaultConfig(client);
            assertThat(defaultConfig.getTargetPreferredAuthSchemes().size()).isEqualTo(1);
            assertThat(defaultConfig.getTargetPreferredAuthSchemes().iterator().next()).isEqualTo("Bearer");
        }
    }

    private Registry<AuthSchemeProvider> getAuthSchemeRegistry(HttpClient client) {
        return TestUtils.getField(getCloseableHttpClient(client), "authSchemeRegistry", Registry.class);
    }

    private RequestConfig getDefaultConfig(HttpClient client) {
        return TestUtils.getField(getCloseableHttpClient(client), "defaultConfig", RequestConfig.class);
    }

    private CloseableHttpClient getCloseableHttpClient(HttpClient client) {
        return TestUtils.getField(client, "closeableHttpClient", CloseableHttpClient.class);
    }
}
