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

package org.artifactory.ui.rest.service.admin.advanced.support;

import org.artifactory.addon.support.ArtifactorySupportBundleConfig;
import org.artifactory.support.config.bundle.BundleConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * A container for {@link BundleConfiguration} and
 * {@link HttpServletRequest}
 *
 * @author Michael Pasternak
 */
public class BundleConfigurationWrapper {
    private final ArtifactorySupportBundleConfig artifactorySupportBundleConfig;
    private final HttpServletRequest httpServletRequest;

    public BundleConfigurationWrapper(ArtifactorySupportBundleConfig bundleConfiguration,
            HttpServletRequest httpServletRequest) {
        this.artifactorySupportBundleConfig = bundleConfiguration;
        this.httpServletRequest = httpServletRequest;
    }

    public ArtifactorySupportBundleConfig getArtifactorySupportBundleConfig() {
        return artifactorySupportBundleConfig;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }
}
