package org.artifactory.api.release.bundle;

import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
import org.artifactory.api.rest.distribution.bundle.models.BundlesResponse;
import org.artifactory.bundle.BundleType;

/**
 * @author Lior Gur
 */
public interface ReleaseBundleSearchService {

    BundlesResponse getFilteredBundles(ReleaseBundleSearchFilter filter);

    BundleVersionsResponse getFilterBundleVersions(ReleaseBundleSearchFilter filter);

    BundleVersionsResponse getBundleVersions(String bundleName, BundleType bundleType);

    BundlesResponse getBundles(BundleType bundleType);

}
