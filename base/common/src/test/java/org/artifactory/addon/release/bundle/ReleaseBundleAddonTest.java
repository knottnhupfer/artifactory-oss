package org.artifactory.addon.release.bundle;

import org.artifactory.api.release.bundle.ReleaseBundleSearchFilter;
import org.artifactory.api.rest.release.ReleaseBundleRequest;
import org.artifactory.api.rest.release.SourceReleaseBundleRequest;
import org.artifactory.bundle.BundleType;
import org.artifactory.sapi.common.ExportSettings;
import org.artifactory.sapi.common.ImportSettings;
import org.artifactory.util.UnsupportedByLicenseException;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertThrows;

/**
 * @author Eli Skoran 20/05/2020
 */
@Test
public class ReleaseBundleAddonTest {
    private ReleaseBundleAddon addon = new ReleaseBundleAddon() {
        @Override
        public void exportTo(ExportSettings exportSettings) {

        }

        @Override
        public void importFrom(ImportSettings importSettings) {

        }

        @Override
        public boolean isDefault() {
            return false;
        }
    };

    @Test
    public void testDefaults() {
        assertThrows(UnsupportedByLicenseException.class,() -> addon.executeReleaseBundleRequest(mock(ReleaseBundleRequest.class), false));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.verifyReleaseBundleSignature("",""));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.createBundleTransaction(""));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.closeBundleTransaction(""));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.closeBundleTransactionAsync("",0));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.checkCloseTransactionStatus(""));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getAllBundles(BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getCompletedBundlesLastVersion(mock(ReleaseBundleSearchFilter.class)));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundlesByReposAndPatterns(new ArrayList<>(),new ArrayList<>(),new ArrayList<>()));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundleVersions(mock(ReleaseBundleSearchFilter.class)));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundleJson("","",BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundleSignedJws("","",BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundleVersions("",BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.deleteReleaseBundle("","",BundleType.TARGET,true));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundleModel("","",BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.storeBundle(mock(SourceReleaseBundleRequest.class)));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getStoringRepo("","",BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getBundleStatus("","",BundleType.SOURCE));
        assertThrows(UnsupportedByLicenseException.class,() -> addon.getReleaseArtifactsUsingAql("","",BundleType.SOURCE));

        assertNull(addon.getReleaseBundlesConfig());
    }
}