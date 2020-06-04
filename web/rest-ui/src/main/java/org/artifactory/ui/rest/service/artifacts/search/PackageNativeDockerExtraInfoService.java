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

package org.artifactory.ui.rest.service.artifacts.search;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.ExtraInfoNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.aql.util.AqlUtils.aggregateRowByPermission;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeDockerSearchHelper.buildVersionItemQuery;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeDockerSearchHelper.buildVersionItemQueryByPath;

/**
 * @author ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class  PackageNativeDockerExtraInfoService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(PackageNativeDockerExtraInfoService.class);

    private static final String PACKAGE_NAME = "packageName";

    @Autowired
    private AqlService aqlService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .searches(new ArrayList<>())
                .packageName(request.getQueryParamByKey(PACKAGE_NAME))
                .type("docker")
                .build();

        String packageName = searchControls.getPackageName();
        if (StringUtils.isEmpty(packageName)) {
            response.error("Package name is missing");
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            log.error("Package name is missing");
            return;
        }
        boolean searchByPath = ConstantValues.packageNativeUiSearchByPath.getBoolean();
        if (searchByPath) {
            List<String> pathValues = new ArrayList<>();
            pathValues.add(packageName + "/*");
            searchControls.getSearches().add(new AqlUISearchModel("path", AqlComparatorEnum.matches, pathValues));
        } else {
            List<String> packageValues = new ArrayList<>();
            packageValues.add(packageName);
            searchControls.getSearches().add(new AqlUISearchModel("pkg", AqlComparatorEnum.matches, packageValues));
        }

        ExtraInfoNativeModel model = search(searchControls, searchByPath);
        response.iModel(model);
    }

    public ExtraInfoNativeModel search(NativeSearchControls searchControls, boolean searchByPath) {
        boolean includeDownloadInResult = ConstantValues.packageNativeUiIncludeTotalDownload.getBoolean();
        AqlApiItem query;
        if (!searchByPath) {
            AqlBase.PropertyResultFilterClause<AqlApiItem> aqlApiElement = AqlApiItem
                    .propertyResultFilter(AqlApiItem.property().key().equal("docker.manifest"));
            query = buildVersionItemQuery(searchControls, includeDownloadInResult, aqlApiElement);
        } else {
            query = buildVersionItemQueryByPath(searchControls.getSearches(), includeDownloadInResult);
        }
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: {}", query.toNative(0));
        }
        return executeSearch(query, searchControls.getPackageName(), includeDownloadInResult);
    }

    private ExtraInfoNativeModel executeSearch(/*AqlDomainEnum domain,*/ AqlApiItem query, String packageName,
                                               boolean includeTotalDownloads) {
        long timer = System.currentTimeMillis();
        List<AqlItem> results = aqlService.executeQueryEager(query).getResults();
        results = aggregateRowByPermission(results);
        ExtraInfoNativeModel extraInfoNativeModel = aggregateDownloadsResultsPackage(results, includeTotalDownloads);
        log.debug("Search total downloads for package named {} in {} milliseconds total {}", packageName,
                System.currentTimeMillis() - timer, extraInfoNativeModel);
        return extraInfoNativeModel;
    }

    private ExtraInfoNativeModel aggregateDownloadsResultsPackage(List<AqlItem> queryResults,
                                                                  boolean includeDownloadInResult) {
        ExtraInfoNativeModel extraInfoNativeModel = new ExtraInfoNativeModel();
        if (includeDownloadInResult) {
            extraInfoNativeModel.setTotalDownloads(0);
        }
        queryResults
                .forEach(queryResult -> extraInfoNativeModel.setExtraInfoByRow(queryResult, includeDownloadInResult));
        return extraInfoNativeModel;
    }
}
