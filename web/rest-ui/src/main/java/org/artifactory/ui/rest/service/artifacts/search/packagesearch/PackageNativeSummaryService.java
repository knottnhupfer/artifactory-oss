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

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.NativeSummaryModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeSummaryHelper;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.TYPE;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PackageNativeSummaryService implements RestService {

    private NativeSummaryHelper nativeSummaryHelper;

    @Autowired
    public PackageNativeSummaryService(NativeSummaryHelper nativeSummaryHelper) {
        this.nativeSummaryHelper = nativeSummaryHelper;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .type(request.getPathParamByKey(TYPE))
                .searches((List<AqlUISearchModel>) request.getModels())
                .build();

        NativeSummaryModel summaryModel = nativeSummaryHelper
                .getSummary(searchControls, searchControls.getModelHandler().getNamePropKey());

        response.iModel(summaryModel);
    }
}
