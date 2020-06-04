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

package org.artifactory.ui.rest.common;

import org.artifactory.api.license.LicenseInfo;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.configuration.licenses.License;
import org.artifactory.ui.rest.model.admin.configuration.proxy.Proxy;

import javax.annotation.Nonnull;

/**
 * @author Chen Keinan
 */
public class ConfigModelPopulator {

    /**
     * populate proxy descriptor data to Proxy model
     *
     * @param proxyDescriptor - proxy descriptor
     * @return proxy model
     */
    @Nonnull
    public static Proxy populateProxyConfiguration(@Nonnull ProxyDescriptor proxyDescriptor) {
        Proxy proxy = null;
        if (proxyDescriptor != null) {
            proxy = new Proxy(proxyDescriptor);
        }
        return proxy;
    }

    /**
     * populate licenseInfo descriptor data to licenseInfo model
     *
     * @param licenseInfo - licenseInfo descriptor
     * @return licenseInfo model
     */
    @Nonnull
    public static License populateLicenseInfo(@Nonnull LicenseInfo licenseInfo) {
        License license = null;
        if (licenseInfo != null) {
            license = new License(licenseInfo);
        }
        return license;
    }

    /**
     * populate bintrayConfigDescriptor descriptor data to bintray  model
     *
     * @param bintrayConfigDescriptor - bintray  descriptor
     * @return licenseInfo model
     */
    @Nonnull
    public static RestModel populateBintrayInfo(BintrayConfigDescriptor bintrayConfigDescriptor, String bintrayUrl) {
        BintrayUIModel bintrayUIModel = new BintrayUIModel(bintrayConfigDescriptor);
        bintrayUIModel.setBintrayConfigUrl(bintrayUrl);
        return bintrayUIModel;
    }
}
