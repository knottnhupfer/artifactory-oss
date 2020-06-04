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

package org.artifactory.layout;

import org.apache.commons.lang3.SerializationUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.webstart.ArtifactWebstartAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.request.UrlVerifier;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Objects;

import static org.artifactory.addon.AddonType.WEBSTART;
import static org.artifactory.addon.build.BuildAddon.BUILD_INFO_REPO_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * @author nadavy
 */
@Test
public class RepositoriesConfigurationChangesInterceptorTest extends ArtifactoryHomeBoundTest {

    private RepositoriesConfigurationChangesInterceptor repoConfigInterceptor;
    private CentralConfigService centralConfigService;
    private AddonsManager addonsManager;
    private CoreAddons coreAddons;
    private CentralConfigDescriptorImpl desc;

    @BeforeClass
    public void setup() {
        addonsManager = mock(AddonsManager.class);
        centralConfigService = mock(CentralConfigService.class);
        coreAddons = mock(CoreAddons.class);
        when(coreAddons.isAol()).thenReturn(true);
        when(addonsManager.addonByType(CoreAddons.class)).thenReturn(coreAddons);
    }

    @BeforeMethod
    public void beforeMethod() {
        when(addonsManager.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.PRO);
    }

    public void testHandleReverseProxySettingsOssRunningMode() {
        when(addonsManager.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.OSS);
        RepositoriesConfigurationChangesInterceptor repoConfChangesInterceptor =
                new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService, new UrlVerifier(addonsManager));
        CentralConfigDescriptorImpl newDescriptor = new CentralConfigDescriptorImpl();
        newDescriptor.addReverseProxy(new ReverseProxyDescriptor());
        repoConfChangesInterceptor.handleReverseProxySettings(newDescriptor);
        Assert.assertEquals(newDescriptor.getReverseProxies().size(), 0);
    }

    public void testHandleReverseProxySettingsProRunningMode() {
        when(addonsManager.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.PRO);
        RepositoriesConfigurationChangesInterceptor repoConfChangesInterceptor =
                new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService, new UrlVerifier(addonsManager));
        CentralConfigDescriptorImpl newDescriptor = new CentralConfigDescriptorImpl();
        newDescriptor.addReverseProxy(new ReverseProxyDescriptor());
        repoConfChangesInterceptor.handleReverseProxySettings(newDescriptor);
        Assert.assertEquals(newDescriptor.getReverseProxies().size(), 1);
    }

    public void testHandleReverseProxySettingsJcrRunningMode() {
        when(addonsManager.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.JCR);
        RepositoriesConfigurationChangesInterceptor repoConfChangesInterceptor =
                new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService, new UrlVerifier(addonsManager));
        CentralConfigDescriptorImpl newDescriptor = new CentralConfigDescriptorImpl();
        newDescriptor.addReverseProxy(new ReverseProxyDescriptor());
        repoConfChangesInterceptor.handleReverseProxySettings(newDescriptor);
        Assert.assertEquals(newDescriptor.getReverseProxies().size(), 1);
    }

    public void testReloadWithAddons() {
        enableAddons(true);
        repoConfigInterceptor.onBeforeSave(desc);
        assertEquals(countValidCertificates(), 1);
    }

    public void testReloadWithoutAddons() {
        enableAddons(false);
        repoConfigInterceptor.onBeforeSave(desc);
        assertEquals(countValidCertificates(), 2);
    }

    public void testBlacklistedUrls() {
        enableAddons(true);
        getBound().setProperty(ConstantValues.whitelistRemoteRepoUrls, "http://localhost");
        repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                new UrlVerifier(addonsManager));
        CentralConfigDescriptorImpl clone = SerializationUtils.clone(desc);
        clone.addRemoteRepository(addRemoteRepo("http://localhost:80/artifactory"));
        repoConfigInterceptor.onBeforeSave(clone);
        boolean blackedOut = clone.getRemoteRepositoriesMap().get("Remote-http://localhost:80/artifactory").isBlackedOut();
        assertFalse(blackedOut);
        // save will be ok

        getBound().setProperty(ConstantValues.whitelistRemoteRepoUrls, null);
        repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                new UrlVerifier(addonsManager));
        desc.addRemoteRepository(addRemoteRepo("http://localhost:8080/artifactory"));
        clone = SerializationUtils.clone(desc);
        clone.addRemoteRepository(addRemoteRepo("http://okurl/artifactory"));
        repoConfigInterceptor.onBeforeSave(clone);
        blackedOut = clone.getRemoteRepositoriesMap().get("Remote-http://localhost:8080/artifactory").isBlackedOut();
        assertTrue(blackedOut);
        // save will be ok - but repository should be blacked out

        clone = SerializationUtils.clone(desc);
        clone.addRemoteRepository(addRemoteRepo("http://localhost/artifactory"));
        try {
            repoConfigInterceptor.onBeforeSave(clone);
        } catch (IllegalArgumentException expected) {
            // this is good!
            assertEquals(expected.getMessage(), "Found a remote repository(ies) containing blacklisted URLs");
            return;
        }
        fail("Should have found a remote repository(ies) containing blacklisted URLs");
    }

    public void testNotAol() {
        try {
            enableAddons(true);
            when(coreAddons.isAol()).thenReturn(false);
            repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                    new UrlVerifier(addonsManager));
            CentralConfigDescriptorImpl clone = SerializationUtils.clone(desc);
            clone.getRemoteRepositoriesMap().get("Repo-Valid-Cert-true").setUrl("http://localhost:8080/artifactory");
            repoConfigInterceptor.onBeforeSave(clone);
        } finally {
            when(coreAddons.isAol()).thenReturn(true);
        }
    }

    public void testBlockedUrlsChangeUrl() {
        enableAddons(true);
        repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                new UrlVerifier(addonsManager));
        CentralConfigDescriptorImpl clone = SerializationUtils.clone(desc);
        clone.getRemoteRepositoriesMap().get("Repo-Valid-Cert-true").setUrl("http://okurl:8080/artifactory");
        repoConfigInterceptor.onBeforeSave(clone);

        enableAddons(true);
        repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                new UrlVerifier(addonsManager));
        clone = SerializationUtils.clone(desc);
        clone.getRemoteRepositoriesMap().get("Repo-Valid-Cert-true").setUrl("http://localhost:8080/artifactory");
        try {
            repoConfigInterceptor.onBeforeSave(clone);
        } catch (IllegalArgumentException expected) {
            // this is good!
            assertEquals(expected.getMessage(), "Found a remote repository(ies) containing blacklisted URLs");
            return;
        }
        fail("Update URL should have been blocked!");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStrictPolicy() {
        // strict policy will fail by default, in cases of unresolvable URLs
        enableAddons(true);
        getBound().setProperty(ConstantValues.remoteRepoBlockUrlStrictPolicy, Boolean.toString(true));
        repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                new UrlVerifier(addonsManager));
        CentralConfigDescriptorImpl clone = SerializationUtils.clone(desc);
        clone.addRemoteRepository(addRemoteRepo("\\o//"));
        repoConfigInterceptor.onBeforeSave(clone);
    }

    @BeforeMethod
    private void resetDescriptor() {
        desc = new CentralConfigDescriptorImpl();
        desc.addRemoteRepository(addRemoteRepo(true));
        desc.addRemoteRepository(addRemoteRepo(false));
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();
        localRepoDescriptor.setKey(BUILD_INFO_REPO_NAME);
        localRepoDescriptor.setType(RepoType.BuildInfo);
        desc.addLocalRepository(localRepoDescriptor);
        when(centralConfigService.getDescriptor()).thenReturn(desc);
        repoConfigInterceptor = new RepositoriesConfigurationChangesInterceptor(addonsManager, centralConfigService,
                new UrlVerifier(addonsManager));
    }

    /**
     * Count the number of remote repositories with a client TLS certificates
     */
    private long countValidCertificates() {
        return desc.getRemoteRepositoriesMap().values().stream()
                .filter(r -> r instanceof HttpRepoDescriptor)
                .map(r -> (HttpRepoDescriptor) r)
                .map(HttpRepoDescriptor::getClientTlsCertificate)
                .filter(Objects::nonNull)
                .count();
    }

    /**
     * Add remote repository with a tls certificate. the certificate may or may not be in the keystore
     */
    private HttpRepoDescriptor addRemoteRepo(boolean withValidCert) {
        HttpRepoDescriptor remoteRepoDescriptor = new HttpRepoDescriptor();
        remoteRepoDescriptor.setKey("Repo-Valid-Cert-" + withValidCert);
        remoteRepoDescriptor.setUrl("http://someurl.com");
        remoteRepoDescriptor.setClientTlsCertificate(withValidCert ? "ValidTls" : "NotValidTLS");
        return remoteRepoDescriptor;
    }

    /**
     * Add remote repository with a URL
     */
    private HttpRepoDescriptor addRemoteRepo(String url) {
        HttpRepoDescriptor remoteRepoDescriptor = new HttpRepoDescriptor();
        remoteRepoDescriptor.setKey("Remote-" + url);
        remoteRepoDescriptor.setUrl(url);
        return remoteRepoDescriptor;
    }

    /**
     * Enabled or disable the addons. if enabled returns a list with 1 valid certificate in the keystore, return null otherwise (like in CoreAddonsImpl)
     */
    private void enableAddons(boolean enabled) {
        when(addonsManager.isEdgeLicensed()).thenReturn(false);
        when(addonsManager.isClusterEnterprisePlus()).thenReturn(true);
        when(addonsManager.isAddonSupported(WEBSTART)).thenReturn(enabled);
        ArtifactWebstartAddon webstartAddon = mock(ArtifactWebstartAddon.class);
        when(addonsManager.addonByType(ArtifactWebstartAddon.class)).thenReturn(webstartAddon);
        when(webstartAddon.getSslCertNames()).thenReturn(enabled ? Lists.newArrayList("ValidTls") : null);
    }
}