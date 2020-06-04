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

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.NativeSummaryExtraInfoModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeExtraInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.PATH;

/**
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeSummaryExtraInfoService implements RestService {

    private NativeExtraInfoHelper nativeSummaryExtraInfoHelper;

    @Autowired
    public VersionNativeSummaryExtraInfoService(NativeExtraInfoHelper nativeSummaryExtraInfoHelper) {
        this.nativeSummaryExtraInfoHelper = nativeSummaryExtraInfoHelper;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String path = request.getQueryParamByKey(PATH);
        List<AqlUISearchModel> searches = (List<AqlUISearchModel>) request.getModels();
        NativeSummaryExtraInfoModel summaryExtraInfo = nativeSummaryExtraInfoHelper.getSummaryExtraInfo(path, searches);
        response.iModel(summaryExtraInfo);
    }
}
