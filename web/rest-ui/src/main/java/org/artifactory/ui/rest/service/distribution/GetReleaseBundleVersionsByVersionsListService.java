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

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.release.bundle.ReleaseBundleSearchFilter;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersion;
import org.artifactory.api.rest.distribution.bundle.models.BundleVersionsResponse;
import org.artifactory.api.rest.distribution.bundle.models.ReleaseBundleSearchModel;
import org.artifactory.bundle.BundleType;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lior Gur
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReleaseBundleVersionsByVersionsListService implements RestService {

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        ReleaseBundleSearchModel releaseBundleSearchModel = (ReleaseBundleSearchModel) request.getImodel();
        ReleaseBundleSearchFilter versionFilter = createVersionFilter(request);
        if(releaseBundleSearchModel.getVersions().size() > ConstantValues.searchUserMaxListSize.getInt()) {
            throw new IllegalArgumentException("Versions list size must be less then " + ConstantValues.searchUserMaxListSize.getInt());
        }
        versionFilter.setVersions(releaseBundleSearchModel.getVersions());
        BundleVersionsResponse bundles = releaseBundleAddon.getBundleVersions(versionFilter);
        List<BundleVersion> completedVersions = bundles.getVersions().stream().filter(ver -> "complete".equalsIgnoreCase(ver.getStatus()))
                .collect(Collectors.toList());
        bundles.setVersions(completedVersions);
        response.iModel(bundles);
    }

    private ReleaseBundleSearchFilter createVersionFilter(ArtifactoryRestRequest request) {
        String name = request.getPathParamByKey("name");
        String type = request.getPathParamByKey("type");
        BundleType bundleType = BundleType.SOURCE.name().equalsIgnoreCase(type) ? BundleType.SOURCE : BundleType.TARGET;
        String orderBy = request.getQueryParamByKey("order_by");
        String direction = request.getQueryParamByKey("direction");
        String limitStr = request.getQueryParamByKey("num_of_rows");

        int limit = StringUtils.isNotBlank(limitStr) ? Integer.parseInt(limitStr) : ConstantValues.searchUserQueryLimit.getInt();
        limit = Math.min(limit,ConstantValues.searchUserQueryLimit.getInt());
        return ReleaseBundleSearchFilter.builder()
                .name(name)
                .bundleType(bundleType)
                .orderBy(orderBy)
                .direction(direction)
                .limit(limit)
                .daoLimit(ConstantValues.searchUserSqlQueryLimit.getInt())
                .build();
    }
}
