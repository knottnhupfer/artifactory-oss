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

import org.apache.http.HttpStatus;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.NativeExtraInfoModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeExtraInfoHelper;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandler;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandlersFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.TYPE;

/**
 * @author Lior Gur
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PackageNativeExtraInfoService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(PackageNativeExtraInfoService.class);

    private NativeExtraInfoHelper nativeExtraInfoHelper;

    @Autowired
    public PackageNativeExtraInfoService(NativeExtraInfoHelper nativeExtraInfoHelper) {
        this.nativeExtraInfoHelper = nativeExtraInfoHelper;
    }

    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String type = request.getPathParamByKey(TYPE);
        List<AqlUISearchModel> searches = request.getModels();

        if (!isValidRequest(searches, type)) {
            log.debug("Package name is missing");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            response.error("Package name is missing");
            return;
        }
        NativeExtraInfoModel model = new NativeExtraInfoModel(nativeExtraInfoHelper.getTotalDownloads(searches));
        response.iModel(model);
    }

    private boolean isValidRequest(List<AqlUISearchModel> searchModels, String type) {
        PackageNativeModelHandler modelHandler = PackageNativeModelHandlersFactory.getModelHandler(type);
        String namePropKey = modelHandler.getNamePropKey();
        return searchModels.stream()
                .anyMatch(model -> namePropKey.equals(model.getId()));
    }
}