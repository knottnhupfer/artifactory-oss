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
import org.artifactory.bundle.BundleType;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.distribution.BundleSummeryModel;
import org.artifactory.ui.rest.model.distribution.ReleaseBundlesIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Tomer Mayost
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllReleaseBundleService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetAllReleaseBundleService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ReleaseBundleSearchFilter filter = createFilter(request);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        Map<String, Set<BundleVersion>> bundles = releaseBundleAddon.getCompletedBundlesLastVersion(filter).getBundles();
        ReleaseBundlesIModel iModel = new ReleaseBundlesIModel();
        populateModel(bundles, iModel);
        try {
            response.iModel(JacksonWriter.serialize(iModel));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }

    private ReleaseBundleSearchFilter createFilter(ArtifactoryRestRequest request) {
        String type = request.getPathParamByKey("type");
        String beforeStr = request.getQueryParamByKey("before");
        String afterStr = request.getQueryParamByKey("after");
        String limitStr = request.getQueryParamByKey("num_of_rows");
        long before = StringUtils.isNotBlank(beforeStr) ? Long.parseLong(beforeStr) : 0;
        long after = StringUtils.isNotBlank(afterStr) ? Long.parseLong(afterStr) : 0;
        String name = request.getQueryParamByKey("name");
        String orderBy = request.getQueryParamByKey("order_by");
        String direction = request.getQueryParamByKey("direction");
        int limit = StringUtils.isNotBlank(limitStr) ? Integer.parseInt(limitStr) : ConstantValues.searchUserQueryLimit.getInt();
        limit = Math.min(limit,ConstantValues.searchUserQueryLimit.getInt());
        BundleType bundleType = BundleType.SOURCE.name().equalsIgnoreCase(type) ? BundleType.SOURCE : BundleType.TARGET;
        return ReleaseBundleSearchFilter.builder()
                .bundleType(bundleType)
                .before(before)
                .after(after)
                .name(name)
                .orderBy(orderBy)
                .direction(direction)
                .limit(limit)
                .daoLimit(ConstantValues.searchUserSqlQueryLimit.getInt())
                .build();
    }

    private void populateModel(Map<String, Set<BundleVersion>> bundles, ReleaseBundlesIModel iModel) {
        bundles.forEach((bundleName, versions) -> {
            BundleVersion latest = versions.iterator().next();
            if (latest != null) {
                BundleSummeryModel bundleSummeryModel = new BundleSummeryModel();
                bundleSummeryModel.setCreated(latest.getCreated());
                bundleSummeryModel.setLatestVersion(latest.getVersion());
                bundleSummeryModel.setName(bundleName);
                iModel.getBundles().add(bundleSummeryModel);
            }
        });
    }
}
