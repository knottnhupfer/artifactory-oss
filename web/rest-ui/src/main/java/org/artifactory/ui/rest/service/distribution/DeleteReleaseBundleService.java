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

package org.artifactory.ui.rest.service.distribution;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.bundle.BundleType;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Tomer Mayost
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteReleaseBundleService implements RestService {

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        String name = request.getPathParamByKey("name");
        String version = request.getPathParamByKey("version");
        String type = request.getPathParamByKey("type");
        BundleType bundleType = BundleType.SOURCE.name().equalsIgnoreCase(type) ? BundleType.SOURCE : BundleType.TARGET;
        Boolean includeContent = BundleType.SOURCE.equals(bundleType) ? Boolean.TRUE :
                Boolean.valueOf(request.getQueryParamByKey("include_content"));
        releaseBundleAddon.deleteReleaseBundle(name, version, bundleType, includeContent);
        response.responseCode(HttpStatus.SC_OK);
    }
}
