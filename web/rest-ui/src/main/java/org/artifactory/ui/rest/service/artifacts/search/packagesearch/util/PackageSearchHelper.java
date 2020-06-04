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

import com.google.common.collect.Sets;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.rows.FullRow;
import org.artifactory.common.ConstantValues;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria.PackageSearchCriterion;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria.PackageSearchType;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUIPropertySearchStrategy;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUISearchStrategy;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.SearchHelper.buildStrategiesFromSearchModel;

/**
 * @author Dan Feldman
 */
public class PackageSearchHelper {
    private static final Logger log = LoggerFactory.getLogger(PackageSearchHelper.class);

    /**
     * Builds an AQL query from the given search models which are constructed when a criteria is sent by the UI.
     */
    public static AqlBase buildItemQuery(List<AqlUISearchModel> searches, boolean includePropertiesInResult) {
        List<AqlUISearchStrategy> strategies = buildStrategiesFromSearchModel(searches);
        if (log.isDebugEnabled()) {
            log.debug("input searches resolved to the following strategies: {}", Arrays.toString(strategies.toArray()));
        }

        AqlApiItem.AndClause query = AqlApiItem.and();
        strategies.stream()
                .map(AqlUISearchStrategy::toQuery)
                .filter(orClause -> !orClause.isEmpty())
                .forEach(query::append);

        int noOfPropKeys = 1;
        AqlApiItem.OrClause propKeyIncluder = AqlApiItem.or();
        if (includePropertiesInResult) {
            noOfPropKeys = populatePropKeysToInclude(searches, propKeyIncluder);
            AqlBase.PropertyResultFilterClause<AqlBase> resultFilter = AqlApiItem.propertyResultFilter();
            if (!propKeyIncluder.isEmpty()) {
                resultFilter.append(propKeyIncluder);
                query.append(resultFilter);
            }
        }
        AqlApiItem aql = AqlApiItem.create().filter(query);
        //Result set size limit is calculated by (max ui results) * no of property keys being searched
        // == UI result limit (i.e. 500)repo paths + all of the required properties that will be shown in UI.
        log.debug("Total number of props for current package type being searched is {}, result set limit is set to {}",
                noOfPropKeys, ConstantValues.searchMaxResults.getInt() * noOfPropKeys);
        aql.limit(ConstantValues.searchMaxResults.getInt() * noOfPropKeys);
        if (includePropertiesInResult && !propKeyIncluder.isEmpty()) {
            aql.include(AqlApiItem.property().key(), AqlApiItem.property().value());
        }
        return aql;
    }

    private static int populatePropKeysToInclude(List<AqlUISearchModel> searches, AqlBase.OrClause propKeyIncluder) {
        int noOfPropKeys;
        Set<String> propKeysToInclude = getPropKeysToInclude(searches);
        //Count prop keys to modify search limit (because each aql row is one property)
        noOfPropKeys = propKeysToInclude.isEmpty() ? 1 : propKeysToInclude.size();
        //Transform prop keys into aql directives and append to query
        propKeysToInclude.stream()
                .map(key -> AqlApiItem.property().key().equal(key))
                .forEach(propKeyIncluder::append);
        return noOfPropKeys;
    }

    private static Set<String> getPropKeysToInclude(List<AqlUISearchModel> searches) {
        if (CollectionUtils.isNullOrEmpty(searches)) {
            return Sets.newHashSet();
        }
        Set<String> propKeysToInclude = searches.stream()
                .map(PackageSearchHelper::getPackageSearchTypeBySearchModel)
                .filter(Objects::nonNull)
                .map(PackageSearchHelper::getStrategiesForPackageType)
                .map(PackageSearchHelper::getPropKeysFromStrategies)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        //we never want to add this to the result filter because we do not need to display this as part of the
        //package search results - will be removed in the next package native refactor.
        return propKeysToInclude.stream()
                    .filter(propKey -> !"npm.keywords".equals(propKey))
                    .collect(Collectors.toSet());
    }

    private static PackageSearchType getPackageSearchTypeBySearchModel(AqlUISearchModel model) {
        return PackageSearchCriteria.getPackageTypeByFieldId(model.getId());
    }

    private static List<AqlUISearchStrategy> getStrategiesForPackageType(PackageSearchType type) {
        return PackageSearchCriteria.getStartegiesByPackageSearchType(type);
    }

    /**
     * Filters out field based strategies and returns all property keys that are being searched
     */
    private static List<String> getPropKeysFromStrategies(List<AqlUISearchStrategy> strategies) {
        return strategies.stream()
                .filter(strategy -> strategy instanceof AqlUIPropertySearchStrategy)
                .map(AqlUISearchStrategy::getSearchKey)
                .collect(Collectors.toList());
    }

    public static PackageSearchCriterion getMatchingPackageSearchCriteria(FullRow row) {
        PackageSearchCriterion criterion;
        //Special case to differentiate between docker v1 and v2 image property which is the same
        if ("docker.repoName".equalsIgnoreCase(row.getKey()) &&
                row.getPath().contains("repositories/" + row.getValue())) {
            criterion = PackageSearchCriteria.dockerV1Image.getCriterion();
        } else {
            criterion = PackageSearchCriteria.getCriterionByAqlFieldOrPropName(row.getKey());
        }
        return criterion;
    }
}