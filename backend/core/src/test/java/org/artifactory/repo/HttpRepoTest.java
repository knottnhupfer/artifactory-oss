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

package org.artifactory.repo;

import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.HaAddon;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.repo.ResearchService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.config.RepoConfigDefaultValues.DefaultUrl;
import org.artifactory.repo.http.GuaveCacheConnectionManagersHolder;
import org.artifactory.repo.http.IdleConnectionMonitorService;
import org.artifactory.repo.http.IdleConnectionMonitorServiceImpl;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Yoav Landman
 */
@Test
public class HttpRepoTest extends ArtifactoryHomeBoundTest {

    @Mock private InternalRepositoryService internalRepoService;
    @Mock private ResearchService researchService;
    @Mock private ArtifactoryContext contextMock;
    @Mock private AddonsManager addonsManager;
    @Mock private LayoutsCoreAddon layoutsCoreAddon;
    @Mock private HaAddon haAddon;

    private HttpRepoDescriptor descriptor;
    private HttpRepo httpRepo;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        bindArtifactoryHome();
        getBound().setProperty(ConstantValues.idleConnectionMonitorInterval, "10");
        getBound().setProperty(ConstantValues.disableIdleConnectionMonitoring, "false");
        IdleConnectionMonitorService idleConnectionMonitorService = new IdleConnectionMonitorServiceImpl(new GuaveCacheConnectionManagersHolder());
        when(addonsManager.addonByType(LayoutsCoreAddon.class)).thenReturn(layoutsCoreAddon);
        when(addonsManager.addonByType(HaAddon.class)).thenReturn(haAddon);
        when(contextMock.beanForType(AddonsManager.class)).thenReturn(addonsManager);
        when(contextMock.beanForType(IdleConnectionMonitorService.class)).thenReturn(idleConnectionMonitorService);
        when(researchService.isRepoConfiguredToSyncProperties(any(HttpRepoDescriptor.class))).thenReturn(true);
        ArtifactoryContextThreadBinder.bind(contextMock);
        descriptor = new HttpRepoDescriptor();
        httpRepo = new HttpRepo(descriptor, internalRepoService, addonsManager, researchService, false, null);
        httpRepo.initDefaultUrlsLists();
    }

    @AfterMethod
    public void tearDown() {
        bindArtifactoryHome();
        ArtifactoryContextThreadBinder.unbind();
        getBound().setProperty(ConstantValues.syncPropertiesBlacklistUrls, "");
    }

    public void syncPropsDisabledWhenRepoConfigDisabled() {
        when(researchService.isRepoConfiguredToSyncProperties(descriptor)).thenReturn(false);
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();
    }

    public void syncPropsEnabledForNonDefaultUrl() {
        descriptor.setUrl("http://13.11.201.9");//begin there
        descriptor.setType(RepoType.Maven);
        assertThat(httpRepo.isSynchronizeProperties()).isTrue();
    }

    public void syncPropsEnabledForBlankDefaultUrl() {
        descriptor.setUrl("http://7.10.201.9");//end here
        descriptor.setType(RepoType.Generic);
        assertThat(httpRepo.isSynchronizeProperties()).isTrue();

        descriptor.setUrl("");
        descriptor.setType(RepoType.GitLfs);
        assertThat(httpRepo.isSynchronizeProperties()).isTrue();
    }

    public void syncPropsDisabledForDefaultUrl() {
        descriptor.setUrl(DefaultUrl.COCOAPODS.getUrl());
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();

        descriptor.setUrl(DefaultUrl.DEBIAN.getUrl());
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();

        descriptor.setUrl(DefaultUrl.BOWER.getUrl());
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();

        descriptor.setUrl(DefaultUrl.MAVEN.getUrl());
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();

        descriptor.setUrl(DefaultUrl.OPKG.getUrl());
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();
    }

    public void syncPropertiesDisabledIfUrlInBlacklist() {
        String url = "http://19.8.201.9";//sign
        String anotherUrl = "http://5.10.201.4";//started here
        getBound().setProperty(ConstantValues.syncPropertiesBlacklistUrls, url);
        descriptor.setType(RepoType.BuildInfo);
        descriptor.setUrl(url);
        //Have to init repo again since const values are read on init method
        httpRepo.initDefaultUrlsLists();
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();

        getBound().setProperty(ConstantValues.syncPropertiesBlacklistUrls, url + "," + anotherUrl);
        //Have to init repo again since const values are read on init method
        httpRepo.initDefaultUrlsLists();
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();

        descriptor.setUrl(anotherUrl);
        assertThat(httpRepo.isSynchronizeProperties()).isFalse();
    }

    public void syncPropertiesEnabledIfUrlNotInBlacklist() {
        String url = "http://what.can.be.done.must.be/done";
        getBound().setProperty(ConstantValues.syncPropertiesBlacklistUrls, url);
        descriptor.setType(RepoType.Distribution);
        descriptor.setUrl("https://open.hands.to.catch.the.falling/chance");
        assertThat(httpRepo.isSynchronizeProperties()).isTrue();
    }

    public void testProxyRemoteAuthAndMultihome() throws IOException {
        ProxyDescriptor proxyDescriptor = new ProxyDescriptor();
        proxyDescriptor.setHost("proxyHost");
        proxyDescriptor.setPort(12345);
        proxyDescriptor.setUsername("proxy-username");
        proxyDescriptor.setPassword("proxy-password");

        HttpRepoDescriptor httpRepoDescriptor = new HttpRepoDescriptor();
        httpRepoDescriptor.setUrl("http://test");

        httpRepoDescriptor.setProxy(proxyDescriptor);

        httpRepoDescriptor.setUsername("repo-username");
        httpRepoDescriptor.setPassword("repo-password");

        httpRepoDescriptor.setLocalAddress("0.0.0.0");

        HttpRepo httpRepo = new HttpRepo(httpRepoDescriptor, internalRepoService, addonsManager, researchService, false, null);
        CloseableHttpClient client = httpRepo.createHttpClient(ConstantValues.httpClientMaxTotalConnections.getInt(), true);
        if (client != null) {
            client.close();
        }

        //TODO: [by YS] implement test on httpclient4
        /*Credentials proxyCredentials = client.getState().getProxyCredentials(AuthScope.ANY);
        Assert.assertNotNull(proxyCredentials);
        Assert.assertTrue(proxyCredentials instanceof UsernamePasswordCredentials,
                "proxyCredentials are of the wrong class");
        Assert.assertEquals(((UsernamePasswordCredentials) proxyCredentials).getUserName(), "proxy-username");
        Assert.assertEquals(proxyCredentials.getPassword(), "proxy-password");

        Credentials repoCredentials = client.getState().getCredentials(
                new AuthScope("test", AuthScope.ANY_PORT, AuthScope.ANY_REALM));
        Assert.assertNotNull(repoCredentials);
        Assert.assertTrue(repoCredentials instanceof UsernamePasswordCredentials,
                "repoCredentials are of the wrong class");
        Assert.assertEquals(((UsernamePasswordCredentials) repoCredentials).getUserName(), "repo-username");
        Assert.assertEquals(repoCredentials.getPassword(), "repo-password");

        Assert.assertEquals(client.getHostConfiguration().getLocalAddress().getHostAddress(), "0.0.0.0");*/
    }
}