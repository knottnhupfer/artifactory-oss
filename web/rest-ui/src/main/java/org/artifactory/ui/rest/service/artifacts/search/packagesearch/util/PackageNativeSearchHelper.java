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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.strategy.AqlUISearchStrategy;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.SearchHelper.buildStrategiesFromSearchModel;
import static org.artifactory.util.CollectionUtils.notNullOrEmpty;

/**
 * @author Inbar Tal
 */
@Component
public class PackageNativeSearchHelper {

    private static final Logger log = LoggerFactory.getLogger(PackageNativeSearchHelper.class);

    private AqlService aqlService;

    @Autowired
    public PackageNativeSearchHelper(AqlService aqlService) {
        this.aqlService = aqlService;
    }

    /**
     * Execute search according to given search strategies used as filters, properties we want to get in results, sort,
     * order, limit and offset.
     *
     * @param propKeys - properties to include in the property result filter
     * @param offset   - add offset to query
     * @return query results as set of {@link PackageNativeSearchResult}
     */
    public SearchResult<PackageNativeSearchResult> search(NativeSearchControls searchControls, Set<String> propKeys, int offset) {
        AqlBase query = buildQuery(searchControls, true, propKeys, offset);
        addSortToQuery(query, searchControls.getSort());
        addOrderToQuery(query, searchControls.getOrder());

        String nativeQuery = query.toNative(0);
        log.debug("strategies resolved to query: {}", nativeQuery);

        Set<PackageNativeSearchResult> results = executeSearch(query, propKeys);

        return new SearchResult<>(results, nativeQuery, results.size(), true);
    }

    /**
     * Execute search according to given search strategies used as filters and properties we want to get in results.
     *
     * @param propKeys - properties to include in the property result filter
     * @return query results as set of {@link PackageNativeSearchResult}
     */
    public SearchResult<PackageNativeSearchResult> search(NativeSearchControls searchControls, Set<String> propKeys) {
        AqlBase queryWithResultFilter = buildQuery(searchControls, true,
                null, propKeys);

        String nativeQuery = queryWithResultFilter.toNative(0);
        log.debug("strategies resolved to query: {}", nativeQuery);

        Set<PackageNativeSearchResult> results = executeSearch(queryWithResultFilter, propKeys);
        //Property result filter will cause no results to return in case some of the props are missing, therefore we rerun the search
        //without the filter to get all the props.
        if (results.isEmpty()) {
            AqlBase queryWithoutResultFilter = buildQuery(searchControls, false, null, 0);
            results = executeSearch(queryWithoutResultFilter, propKeys);
        }
        // update response data
        return new SearchResult<>(results, nativeQuery, results.size(), true);
    }

    private AqlBase buildQuery(NativeSearchControls searchControls, boolean includePropertiesInResult,
            @Nullable Set<String> propKeysToInclude, int offset) {
        AqlBase.AndClause query = createQueryWithStrategies(searchControls.getSearches());
        searchControls.addFromDateToQuery(query);
        return addPropertiesToResultFilter(query, includePropertiesInResult, propKeysToInclude,
                offset, searchControls.limit());
    }

    public AqlBase buildQuery(NativeSearchControls searchControls, boolean includePropertiesInResult,
            @Nullable List<AqlApiDynamicFieldsDomains.AqlApiField> extraFieldsToInclude,
            @Nullable Set<String> propKeysToInclude) {
        AqlBase.AndClause query = createQueryWithStrategies(searchControls.getSearches());
        searchControls.addFromDateToQuery(query);
        AqlApiItem aql = addPropertiesToResultFilter(query, includePropertiesInResult,
                propKeysToInclude, 0, ConstantValues.searchMaxResults.getInt());

        if (notNullOrEmpty(extraFieldsToInclude)) {
            extraFieldsToInclude.forEach(aql::include);
        }
        return aql;
    }

    public AqlApiItem.AndClause createQueryWithStrategies(List<AqlUISearchModel> searches) {
        List<AqlUISearchStrategy> strategies = buildStrategiesFromSearchModel(searches);
        if (log.isDebugEnabled()) {
            log.debug("input searches resolved to the following strategies: {}", Arrays.toString(strategies.toArray()));
        }
        AqlApiItem.AndClause query = AqlApiItem.and();
        strategies.stream()
                .map(AqlUISearchStrategy::toQuery)
                .filter(orClause -> !orClause.isEmpty())
                .forEach(query::append);
        return query;
    }

    private AqlApiItem addPropertiesToResultFilter(AqlBase.AndClause query, boolean includePropertiesInResult,
            Set<String> propKeysToInclude, int offset, int limit) {
        AqlApiItem.OrClause propKeyIncluder = AqlApiItem.or();
        int propKeysNum = 1;
        if (includePropertiesInResult) {
            propKeysNum = populatePropKeysToInclude(propKeyIncluder, propKeysToInclude);
            AqlBase.PropertyResultFilterClause<AqlBase> resultFilter = AqlApiItem.propertyResultFilter();
            setResultFilter(query, propKeyIncluder, resultFilter);
        }
        AqlApiItem aql = AqlApiItem.create().filter(query);
        //Result set size limit is calculated by (max ui results) * no of property keys being searched
        // == UI result limit (i.e. 500)repo paths + all of the required properties that will be shown in UI.
        if (log.isDebugEnabled()) {
            log.debug(
                    "Total number of props for current package type being searched is {}, result set limit is set to {}",
                    propKeysNum, limit * propKeysNum);
        }

        setOffsetAndLimit(aql, offset, limit * propKeysNum);

        if (includePropertiesInResult && !propKeyIncluder.isEmpty()) {
            aql.include(AqlApiItem.property().key(), AqlApiItem.property().value());
        }
        return aql;
    }

    private void setResultFilter(AqlBase.AndClause query, AqlBase.OrClause propKeyIncluder,
            AqlBase.PropertyResultFilterClause<AqlBase> resultFilter) {
        if (!propKeyIncluder.isEmpty()) {
            resultFilter.append(propKeyIncluder);
            query.append(resultFilter);
        }
    }

    private int populatePropKeysToInclude(AqlBase.OrClause propKeyIncluder, Set<String> propKeys) {
        if (CollectionUtils.isNullOrEmpty(propKeys)) {
            return 1;
        }
        propKeys.forEach(propKey -> propKeyIncluder.append((AqlApiItem.property().key().matches(propKey))));
        return propKeys.size();
    }

    private void setOffsetAndLimit(AqlApiItem aql, int offset, int limit) {
        aql.offset(offset);
        if (limit != 0) {
            aql.limit(limit);
        }
    }

    private Set<PackageNativeSearchResult> executeSearch(AqlBase query, Set<String> propKeys) {
        long timer = System.currentTimeMillis();
        List<AqlBaseFullRowImpl> queryResults = aqlService.executeQueryEager(query).getResults();
        log.debug("Search found {} rows", queryResults.size());
        HashMultimap<RepoPath, AqlBaseFullRowImpl> resultsByPath = AqlUtils.aggregateResultsByPath(queryResults);
        Set<PackageNativeSearchResult> results = reduceAggregatedResultRows(resultsByPath, propKeys);
        log.trace("Search found {} results in {} milliseconds", results.size(), System.currentTimeMillis() - timer);
        return results;
    }

    private Set<PackageNativeSearchResult> reduceAggregatedResultRows(
            HashMultimap<RepoPath, AqlBaseFullRowImpl> resultsByPath, Set<String> propKeys) {
        Set<PackageNativeSearchResult> results = Sets.newHashSet();

        for (Map.Entry<RepoPath, AqlBaseFullRowImpl> entry : resultsByPath.entries()) {
            Set<AqlBaseFullRowImpl> rows = resultsByPath.get(entry.getKey());
            PackageNativeSearchResult packageNativeSearchResult = new PackageNativeSearchResult(rows.iterator().next());
            rows.forEach(row -> packageNativeSearchResult.aggregateRow(row, propKeys));
            results.add(packageNativeSearchResult);
        }
        return results;
    }

    private void addSortToQuery(AqlBase query, String sort) {
        if ("lastModified".equals(sort)) {
            query.addSortElement(AqlApiItem.modified());
        } else {
            query.addSortElement(AqlApiItem.name());
        }
    }

    private void addOrderToQuery(AqlBase query, String order) {
        if ("desc".equals(order)) {
            query.desc();
        } else {
            query.asc();
        }
    }
}
