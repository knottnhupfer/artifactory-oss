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

import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.PackageNativeCountModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeSearchHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CountNativePackagesService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CountNativePackagesService.class);

    private AqlService aqlService;

    private PackageNativeSearchHelper packageNativeSearchHelper;

    @Autowired
    public CountNativePackagesService(AqlService aqlService, PackageNativeSearchHelper packageNativeSearchHelper) {
        this.aqlService = aqlService;
        this.packageNativeSearchHelper = packageNativeSearchHelper;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String type = request.getPathParamByKey(TYPE);
        List<AqlUISearchModel> searches = request.getModels();
        if (searches.isEmpty()) {
            response.iModel(new PackageNativeCountModel());
            return;
        }
        int numOfPackages = countPackages(searches, type);
        response.iModel(new PackageNativeCountModel(numOfPackages));
    }

    private int countPackages(List<AqlUISearchModel> searches, String packageType) {
        AqlApiItem.AndClause query = packageNativeSearchHelper.createQueryWithStrategies(searches);

        AqlApiItem.PropertyResultFilterClause<AqlApiItem> resultFilter = AqlApiItem.propertyResultFilter();
        resultFilter.append(AqlApiItem.property().key().matches(packageType + ".name"));
        query.append(resultFilter);
        AqlApiItem aql = AqlApiItem.createWithEmptyResults().filter(query);
        aql.include(AqlApiItem.property().value());

        String nativeQuery = aql.toNative(0);
        log.debug("strategies resolved to query: {}", nativeQuery);

        long timer = System.currentTimeMillis();
        List<AqlItem> queryResults = aqlService.executeQueryEager(aql).getResults();
        log.trace("Search found {} results in {} milliseconds", queryResults.size(), System.currentTimeMillis() - timer);

        return queryResults.size();
    }
}
