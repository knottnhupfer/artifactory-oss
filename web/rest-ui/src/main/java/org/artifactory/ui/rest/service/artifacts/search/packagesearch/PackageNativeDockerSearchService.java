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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.PackagesNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.NativeDockerSearchHelper.buildPackageItemQuery;

/**
 * @author ortalh
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PackageNativeDockerSearchService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(PackageNativeDockerSearchService.class);
    private static final String TYPE = "type";
    private static final String SORT_BY = "sort_by";
    private static final String ORDER = "order";

    @Autowired
    private AqlService aqlService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String type = request.getPathParamByKey(TYPE);
        String sort = request.getQueryParamByKey(SORT_BY);
        String order = request.getQueryParamByKey(ORDER);

        List<AqlUISearchModel> searches = (List<AqlUISearchModel>) request.getModels();
        if (type.isEmpty()) {
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        if (CollectionUtils.isNullOrEmpty(searches)) {
            Set<PackageNativeModel> results = Sets.newHashSet();
            response.iModel(new PackagesNativeModel(results, results.size()));
            return;
        }
        if (validateSearchIdQuery(searches)) {
            response.responseCode(HttpStatus.SC_BAD_REQUEST);
            response.error("Search with no id param is not valid");
            log.error("Search with no id param is not valid");
            return;
        }
        if (!containsPkgFieldSearch(searches)) {
            List<String> values = new ArrayList<>();
            values.add("*");
            AqlUISearchModel aqlUISearchModel = new AqlUISearchModel("pkg", AqlComparatorEnum.matches, values);
            searches.add(aqlUISearchModel);
        }
        PackagesNativeModel model = search(searches, type, sort, order);
        response.iModel(model);
    }

    public PackagesNativeModel search(List<AqlUISearchModel> searches, String type, String sort, String order) {
        AqlBase query = buildPackageItemQuery(searches, type, sort, order);
        if (log.isDebugEnabled()) {
            log.debug("strategies resolved to query: " + query.toNative(0));
        }
        Set<PackageNativeModel> results = executeSearch(query);

        if (StringUtils.isNotEmpty(sort)) {
            List<PackageNativeModel> sortedResult = sortBy(results, sort, order);
            return new PackagesNativeModel(sortedResult, results.size());
        }
        // update response data
        return new PackagesNativeModel(results, results.size());
    }

    private Set<PackageNativeModel> executeSearch(/*AqlDomainEnum domain,*/ AqlBase query) {
        long timer = System.currentTimeMillis();
        List<AqlBaseFullRowImpl> queryResults = aqlService.executeQueryEager(query).getResults();
        HashMultimap<String, AqlBaseFullRowImpl> resultsByPath = AqlUtils.aggregateResultsPackage(queryResults);
        Set<PackageNativeModel> results = reduceAggregatedResultRows(resultsByPath);
        log.trace("Search found {} results in {} milliseconds", results.size(), System.currentTimeMillis() - timer);
        return results;
    }

    private Set<PackageNativeModel> reduceAggregatedResultRows(
            HashMultimap<String, AqlBaseFullRowImpl> resultsByPath) {
        Set<PackageNativeModel> concurrentSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        resultsByPath.keySet().parallelStream()
                .forEach(pkgName -> concurrentSet.add(
                        resultsByPath.get(pkgName).stream()
                                .reduce(new PackageNativeModel(pkgName, resultsByPath.get(pkgName).iterator().next()),
                                        PackageNativeModel::aggregateRow,
                                        PackageNativeModel::merge)
                ));
        return concurrentSet;
    }

    private boolean validateSearchIdQuery(List<AqlUISearchModel> searches) {
        return searches.parallelStream().anyMatch(aqlUISearchModel -> aqlUISearchModel.getId() == null);
    }

    private boolean containsPkgFieldSearch(List<AqlUISearchModel> searches) {
        return searches.parallelStream().anyMatch(aqlUISearchModel -> aqlUISearchModel.getId().equals("pkg")); //todo change parallelStream to stream
    }

    private List<PackageNativeModel> sortBy(Set<PackageNativeModel> packageNatives, String sort,
                                            String order) {
        List<PackageNativeModel> sortedSet = Lists.newArrayList();
        if (sort.equals("name")) {
            if (order.equals("desc")) {
                sortedSet = packageNatives.stream().sorted(
                        Comparator.comparing(PackageNativeModel::getName).reversed()).collect(
                        Collectors.toList());
            } else {
                sortedSet = packageNatives.stream().sorted(
                        Comparator.comparing(PackageNativeModel::getName)).collect(
                        Collectors.toList());
            }
        } else if (sort.equals("numOfRepos")) {
            if (order.equals("desc")) {
                sortedSet = packageNatives.stream().sorted(
                        Comparator.comparing(PackageNativeModel::getNumOfRepos).reversed()).collect(
                        Collectors.toList());
            } else {
                sortedSet = packageNatives.stream().sorted(
                        Comparator.comparing(PackageNativeModel::getNumOfRepos)).collect(
                        Collectors.toList());
            }
        }
        return sortedSet;
    }
}
