package org.artifactory.ui.rest.service.general;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.addon.license.ArtifactoryBaseLicenseDetails;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.artifactory.addon.OssAddonsManager.FREE_LICENSE_KEY_HASH;
import static org.artifactory.addon.OssAddonsManager.JCR_LICENSE_KEY_HASH;
import static org.mockito.Mockito.*;

/**
 * @author dudim
 */
public class GetFooterServiceTest {

    @Mock
    private ArtifactoryContext context;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(context);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testGetVersionID() {
        GetFooterService footerService = new GetFooterService();

        Assert.assertEquals(footerService.getVersionID(JCR_LICENSE_KEY_HASH), "JCR");
        Assert.assertEquals(footerService.getVersionID(JCR_LICENSE_KEY_HASH + " Online"), "JCR");
        Assert.assertEquals(footerService.getVersionID(FREE_LICENSE_KEY_HASH), "ConanCE");
    }

    @Test
    public void testGetVersionInfoJcrOnline() {
        GetFooterService footerServiceSpy = spy(new GetFooterService());
        AddonsManager addonsManagerMock = mock(AddonsManager.class);
        doReturn(addonsManagerMock).when(footerServiceSpy).getAddonManager();
        when(addonsManagerMock.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.AOL_JCR);
        String versionInfo = footerServiceSpy.getVersionInfo();
        Assert.assertEquals(versionInfo, JCR_LICENSE_KEY_HASH + " Online");
    }

    @Test
    public void testGetVersionInfoJcr() {
        GetFooterService footerServiceSpy = spy(new GetFooterService());
        OssAddonsManager addonsManagerMock = mock(OssAddonsManager.class);
        doReturn(addonsManagerMock).when(footerServiceSpy).getAddonManager();
        when(addonsManagerMock.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.JCR);
        when(addonsManagerMock.getLicenseKeyHash(false)).thenReturn(JCR_LICENSE_KEY_HASH);
        String versionInfo = footerServiceSpy.getVersionInfo();
        Assert.assertEquals(versionInfo, JCR_LICENSE_KEY_HASH);
    }

    @Test
    public void testGetVersionInfoConan() {
        GetFooterService footerServiceSpy = spy(new GetFooterService());
        OssAddonsManager addonsManagerMock = mock(OssAddonsManager.class);
        doReturn(addonsManagerMock).when(footerServiceSpy).getAddonManager();
        when(addonsManagerMock.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.CONAN);
        when(addonsManagerMock.getLicenseKeyHash(false)).thenReturn(FREE_LICENSE_KEY_HASH);
        String versionInfo = footerServiceSpy.getVersionInfo();
        Assert.assertEquals(versionInfo, FREE_LICENSE_KEY_HASH);
    }

    @Test(dataProvider = "provideLicenseTypes")
    public void testGetVersionInfoForOthers(String licenseType, String expectedVersionInfo) {
        GetFooterService footerServiceSpy = spy(new GetFooterService());
        AddonsManager addonsManagerMock = mock(AddonsManager.class);
        doReturn(addonsManagerMock).when(footerServiceSpy).getAddonManager();
        when(addonsManagerMock.getArtifactoryRunningMode()).thenReturn(ArtifactoryRunningMode.PRO);
        when(context.beanForType(AddonsManager.class)).thenReturn(addonsManagerMock);

        CoreAddons coreAddons = mock(CoreAddons.class);
        when(addonsManagerMock.addonByType(CoreAddons.class)).thenReturn(coreAddons);
        when(coreAddons.isAol()).thenReturn(false);
        when(addonsManagerMock.getLicenseKeyHash(false)).thenReturn("x");
        ArtifactoryBaseLicenseDetails licDetails = new ArtifactoryBaseLicenseDetails();
        licDetails.setType(licenseType);
        when(addonsManagerMock.getProAndAolLicenseDetails()).thenReturn(licDetails);

        String versionInfo = footerServiceSpy.getVersionInfo();
        Assert.assertEquals(versionInfo, expectedVersionInfo);
    }

    @DataProvider
    public Object[][] provideLicenseTypes() {
        return new Object[][]{
                {"Trial", "Artifactory Trial"},
                {"Edge", "Artifactory Edge"},
                {"Edge Trial", "Artifactory Edge Trial"},
                {"Enterprise Plus", "Artifactory Enterprise Plus"},
                {"Enterprise Plus Trial", "Artifactory Enterprise Plus Trial"},
                {"Commercial", "Artifactory Professional"}
        };
    }
}