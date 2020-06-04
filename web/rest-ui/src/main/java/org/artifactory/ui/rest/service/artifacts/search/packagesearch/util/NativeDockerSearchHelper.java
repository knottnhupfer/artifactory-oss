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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.api.AqlApiElement;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.domain.sensitive.AqlApiProperty;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.common.ConstantValues;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.FieldSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUISearchStrategy;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.aql.api.domain.sensitive.AqlApiItem.statistic;
import static org.artifactory.aql.api.internal.AqlBase.propertyResultFilter;

/**
 * @author ortalh
 */
public class NativeDockerSearchHelper {
    private static final Logger log = LoggerFactory.getLogger(NativeDockerSearchHelper.class);

    public static AqlBase buildPackageItemQuery(List<AqlUISearchModel> searches, String type,
            String sort, String order) {
        AqlApiItem.AndClause query = AqlApiItem.and();

        addSearchStrategiesToQuery(searches, type, query);
        if (StringUtils.equals(type, "docker")) {
            query.append(propertyResultFilter(AqlApiProperty.key().equal("docker.repoName")));
        }
        AqlApiProperty aql = AqlApiProperty.create().filter(query)
                .include(AqlApiProperty.item().repo());

        int numOfResults = ConstantValues.packageNativeUiResults.getInt();
        log.debug("Total number for current native package type being searched is {}", numOfResults);
        AqlApiProperty sortedAql = sortPackageResultsBy(aql, sort, order);
        return sortedAql.limit(numOfResults);
    }

    public static AqlApiItem buildVersionItemQuery(NativeSearchControls searchControls, boolean includeDownloadInResult,
            AqlApiElement aqlApiElement) {
        AqlApiItem.AndClause query = AqlApiItem.and();
        addSearchStrategiesToQuery(searchControls.getSearches(), searchControls.getType(), query);
        query.append(aqlApiElement);
        searchControls.addFromDateToQuery(query);

        AqlApiItem aql = AqlApiItem.create().filter(query);
        aql.include(AqlApiItem.property().key(), AqlApiItem.property().value());
        if (includeDownloadInResult) {
            aql.include(statistic().downloads());
        }
        AqlApiItem sortedAql = sortVersionResultsBy(aql, searchControls.getSort(), searchControls.getOrder());
        // There's always gonna be a limit where we fall between 2 nodes (i.e. reached the limit on a row before the digest)
        // which will cause some results to have empty digests. I asked Ortal to use a mechanism that will always retrieve all
        // rows until the next nodeId is reached. I don't know why she didn't - now we will have to do that as well when we refactor here.

        //searchControls.setLimitModifier(2); //2 * to accommodate for the extra sha256 row (?)
        int numOfResults = searchControls.limit();
        log.debug("Total number for current native package type being searched is {}",  numOfResults);
        return sortedAql.limit(numOfResults);
    }

    public static AqlApiItem buildVersionItemQueryByPath(List<AqlUISearchModel> searches,
            boolean includeDownloadInResult) {
        int numOfResults = ConstantValues.packageNativeUiResults.getInt();
        AqlApiItem.AndClause query = AqlApiItem.and();
        addSearchStrategiesToQuery(searches, "", query);
        AqlApiItem aql = AqlApiItem.create().filter(query);
        if (includeDownloadInResult) {
            aql.include(statistic().downloads());
        }
        return aql.limit(numOfResults);
    }

    private static void addSearchStrategiesToQuery(List<AqlUISearchModel> searches, String type,
            AqlBase.AndClause query) {
        List<AqlUISearchStrategy> strategies = buildStrategiesFromSearchModel(searches, type);
        log.debug("input searches resolved to the following strategies: " + Arrays.toString(strategies.toArray()));
        strategies.stream()
                .map(AqlUISearchStrategy::toQuery)
                .filter(orClause -> !orClause.isEmpty())
                .forEach(query::append);
    }


    private static List<AqlUISearchStrategy> buildStrategiesFromSearchModel(List<AqlUISearchModel> searches,
            String type) {
        return searches.stream()
                .map(search -> getStrategyByTypeAndElementTypeOrByFieldId(search, type)
                        .comparator(search.getComparator())
                        .values(search.getValues()))
                .collect(Collectors.toList());
    }

    /**
     * Tries each of the criteria enums for the strategy of the given model
     */
    private static AqlUISearchStrategy getStrategyByTypeAndElementTypeOrByFieldId(AqlUISearchModel search,
            String type) {
        switch (search.getId()) {
            case "pkg":
                if (type.equals("docker")) {
                    return PackageSearchCriteria.getStrategyByTypeAndElementType(
                            PackageSearchCriteria.PackageSearchType.valueOf("dockerV2"), "Image");
                }
                //todo fix that it doesn't work
                return PackageSearchCriteria.getStrategyByTypeAndElementType(
                        PackageSearchCriteria.PackageSearchType.valueOf(search.getId()), "Name");
            case "version":
                if (type.equals("docker")) {
                    return PackageSearchCriteria.getStrategyByTypeAndElementType(
                            PackageSearchCriteria.PackageSearchType.valueOf("dockerV2"), "Tag");
                }
                //todo fix that it doesn't work
                return PackageSearchCriteria.getStrategyByTypeAndElementType(
                        PackageSearchCriteria.PackageSearchType.valueOf(search.getId()), "Version");
            case "path":
                return PackageSearchCriteria.getStrategyByTypeAndElementType(
                        PackageSearchCriteria.PackageSearchType.valueOf("dockerV2"), "TagPath");
            case "repo":
                return FieldSearchCriteria.getStrategyByFieldId(search.getId());
            default:
                throw new IllegalArgumentException("Not a valid id value :" + search.getId());
        }
    }


    private static AqlApiProperty sortPackageResultsBy(AqlApiProperty aql, String sort, String order) {
        if (sort.equals("name")) {
            aql.addSortElement(AqlApiProperty.value());

        }
        if (order.equals("desc")) {
            return aql.desc();
        } else {
            aql.asc();
        }
        return aql;
    }

    private static AqlApiItem sortVersionResultsBy(AqlApiItem aql, String sort, String order) {
        switch (sort) {
            case "name":
                aql.addSortElement(AqlApiItem.property().value());
                break;
            case "lastModified":
                aql = aql.addSortElement(AqlApiItem.modified());
                break;
            case "repoKey":
                aql = aql.addSortElement(AqlApiItem.repo());
                break;
            case "size":
                aql = aql.addSortElement(AqlApiItem.size());
                break;
            default: {
                aql.addSortElement(AqlApiItem.property().value());
            }
        }
        if (order.equals("desc")) {
            return aql.desc();
        } else {
            aql.asc();
        }
        return aql;
    }
}
