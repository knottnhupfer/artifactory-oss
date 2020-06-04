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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch;

import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.FieldSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria.PackageSearchCriterion;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchPackageTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPackageSearchOptionsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetPackageSearchOptionsService.class);

    private static final String AVAILABLE_PACKAGES = "availablePackages";

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String pkgType = request.getPathParamByKey("type");
        pkgType = pkgType == null ? "" : pkgType;
        //Special call to get package types that are available to search by
        if (pkgType.equalsIgnoreCase(AVAILABLE_PACKAGES)) {
            response.iModelList(
                    Stream.of(PackageSearchCriteria.PackageSearchType.values()).map(AqlUISearchPackageTypeModel::new)
                            .collect(Collectors.toList()));

            response.responseCode(HttpStatus.SC_OK);
        } else {
            try {
                List<AqlUISearchModel> availableOptions = Lists.newArrayList();
                availableOptions.addAll(PackageSearchCriteria.getCriteriaByPackage(pkgType)
                                .stream()
                                .map(PackageSearchCriterion::getModel)
                                .collect(Collectors.toList())
                );
                availableOptions.addAll(Stream.of(FieldSearchCriteria.values())
                                .map(FieldSearchCriteria::getModel)
                                .collect(Collectors.toList())
                );
                response.iModel(availableOptions);
                response.responseCode(HttpStatus.SC_OK);
            } catch (UnsupportedOperationException uoe) {
                log.debug(uoe.getMessage(), uoe);
                response.error(uoe.getMessage());
            }
        }
    }
}
