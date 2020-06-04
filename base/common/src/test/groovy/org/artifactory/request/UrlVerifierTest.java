package org.artifactory.request;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.common.ConstantValues;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Nadav Yogev
 */
public class UrlVerifierTest extends ArtifactoryHomeBoundTest {

    @Test
    public void testLoopBack() {
        UrlVerifier urlVerifier = new UrlVerifier(getAddonsManager());
        boolean loopback = urlVerifier.isRemoteRepoBlocked("http://localhost:8080/artifactory/dangerZone", "test");
        assertTrue(loopback);

        loopback = urlVerifier.isRemoteRepoBlocked("http://127.0.0.1:8080/artifactory/dangerZone", "test");
        assertTrue(loopback);

        loopback = urlVerifier.isRemoteRepoBlocked("http://[::1]:8080/artifactory/dangerZone", "test");
        assertTrue(loopback);

        loopback = urlVerifier.isRemoteRepoBlocked("http://jfrog.com/artifactory/dangerZone", "test");
        assertFalse(loopback);

        loopback = urlVerifier.isRemoteRepoBlocked("http://jfrog.com/artifactory/dangerZone", "test");
        assertFalse(loopback);

        // test whitelist (localhost with /)
        getBound().setProperty(ConstantValues.whitelistRemoteRepoUrls, "https://127.0.0.1,http://localhost/");
        urlVerifier = new UrlVerifier(getAddonsManager());
        loopback = urlVerifier.isRemoteRepoBlocked("http://localhost:8080/artifactory/dangerZone", "test");
        assertTrue(loopback);
        loopback = urlVerifier.isRemoteRepoBlocked("http://localhost/artifactory/dangerZone", "test");
        assertFalse(loopback);
        loopback = urlVerifier.isRemoteRepoBlocked("http://127.0.0.1:8080/artifactory/dangerZone", "test");
        assertTrue(loopback);
        loopback = urlVerifier.isRemoteRepoBlocked("https://127.0.0.1:8080/artifactory/dangerZone", "test");
        assertFalse(loopback);

    }

    private AddonsManager getAddonsManager() {
        AddonsManager addonsManager = mock(AddonsManager.class);
        CoreAddons coreAddons = mock(CoreAddons.class);
        when (coreAddons.isAol()).thenReturn(true);
        when(addonsManager.addonByType(CoreAddons.class)).thenReturn(coreAddons);
        return addonsManager;
    }

    @Test
    public void testSiteAndLinkLocal() {
        getBound().setProperty(ConstantValues.remoteRepoBlockUrlStrictPolicy, "true");
        UrlVerifier urlVerifier = new UrlVerifier(getAddonsManager());
        boolean loopback = urlVerifier.isRemoteRepoBlocked("http://10.0.0.1:8080/internal/dangerZone", "test");
        assertTrue(loopback);

        loopback = urlVerifier.isRemoteRepoBlocked("http://169.254.169.254/latest/meta-data/dangerZone", "test");
        assertTrue(loopback);

        getBound().setProperty(ConstantValues.remoteRepoBlockUrlStrictPolicy, "false");
        loopback = urlVerifier.isRemoteRepoBlocked("http://10.0.0.1:8080/internal/dangerZone", "test");
        assertFalse(loopback);

        loopback = urlVerifier.isRemoteRepoBlocked("http://169.254.169.254/latest/meta-data/dangerZone", "test");
        assertFalse(loopback);
    }

}