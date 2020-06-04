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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.PackagesNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeModel;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeSearchResult;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeModelHandler;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeSearchHelper;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.artifactory.util.CollectionUtils;
import org.jfrog.storage.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeConstants.ORDER_DESC;
import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.*;

/**
 * @author Lior Gur
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PackageNativeSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(PackageNativeSearchService.class);

    private static final int MINIMAL_PACKAGES_NUMBER = 21;

    private PackageNativeSearchHelper packageNativeSearchHelper;

    @Autowired
    public PackageNativeSearchService(PackageNativeSearchHelper packageNativeSearchHelper) {
        this.packageNativeSearchHelper = packageNativeSearchHelper;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoType = request.getPathParamByKey(TYPE);
        String sort = request.getQueryParamByKey(SORT_BY);
        String order = request.getQueryParamByKey(ORDER);
        NativeSearchControls searchControls = NativeSearchControls.builder()
                .searches(request.getModels())
                .type(request.getPathParamByKey(TYPE))
                .sort(request.getQueryParamByKey(SORT_BY))
                .order(request.getQueryParamByKey(ORDER))
                .build();

        if (CollectionUtils.isNullOrEmpty(searchControls.getSearches())) {
            Set<PackageNativeModel> results = Sets.newHashSet();
            response.iModel(new PackagesNativeModel(results, results.size()));
            return;
        }
        PackagesNativeModel model = search(searchControls);
        response.iModel(model);
    }

    private PackagesNativeModel search(NativeSearchControls searchControls) {
        PackageNativeModelHandler modelHandler = searchControls.getModelHandler();
        int offset = 0;
        searchControls.setLimitModifier(ConstantValues.searchMaxResults.getInt());

        SearchResult<PackageNativeSearchResult> searchResults = executeSearch(searchControls);

        long start = System.currentTimeMillis();
        int currentRows = searchResults.getResults().size();
        Map<String, PackageNativeModel> packageNativeResults = Maps.newHashMap();
        Map<String, Set<String>> allVersions = Maps.newHashMap();

        mergeResults(searchResults, packageNativeResults, modelHandler, allVersions);

        log.trace("first search return {} results, number of packages after merge {}",
                searchResults.getResults().size(), packageNativeResults.size());

        while (currentRows >= ConstantValues.searchMaxResults.getInt()
                && (System.currentTimeMillis() - start) < ConstantValues.nativeUiSearchMaxTimeMillis.getLong()
                && packageNativeResults.size() <= MINIMAL_PACKAGES_NUMBER) {

            searchControls.addOffset();
            searchResults = executeSearch(searchControls);
            currentRows = searchResults.getResults().size();
            mergeResults(searchResults, packageNativeResults, modelHandler, allVersions);

            log.trace("search number {} return {} results, number of packages after merge {}", offset / searchControls.limit(),
                    searchResults.getResults().size(), packageNativeResults.size());
        }

        List<PackageNativeModel> nativeResults = new ArrayList<>(packageNativeResults.values());
        List<PackageNativeModel> sortedResults = sortResults(nativeResults, searchControls.getOrder());
        removeLastPackageIfNeeded(currentRows, sortedResults);

        log.trace("Search found {} results in total", sortedResults.size());
        return new PackagesNativeModel(sortedResults, sortedResults.size());
    }

    private void removeLastPackageIfNeeded(int currentRows, List<PackageNativeModel> sortedResults) {
        //remove the last one to get full number of versions per package
        if (!sortedResults.isEmpty() && currentRows >= ConstantValues.searchMaxResults.getInt()) {
            PackageNativeModel pkgToRemove = sortedResults.get(sortedResults.size() - 1);
            String name = pkgToRemove.getName();
            int numOfVersions = pkgToRemove.getNumOfVersions();
            log.trace("Remove {} package with {} versions", name, numOfVersions);
            sortedResults.remove(sortedResults.size() - 1);
        }
    }

    private SearchResult<PackageNativeSearchResult> executeSearch(NativeSearchControls searchControls) {
        Set<String> propKeys = getPropKeys(searchControls.getModelHandler());
        return packageNativeSearchHelper.search(searchControls, propKeys, searchControls.getOffset());
    }

    private Set<String> getPropKeys(PackageNativeModelHandler modelHandler) {
        if (ArtifactoryHome.get().getDBProperties().getDbType().equals(DbType.DERBY)) {
            return modelHandler.getPackagePropKeysForDerby();
        }
        return modelHandler.getPackagePropKeys();
    }

    private void mergeResults(SearchResult<PackageNativeSearchResult> searchResults,
            Map<String, PackageNativeModel> packageNativeResults,
            PackageNativeModelHandler modelHandler, Map<String, Set<String>> allVersions) {

        Collection<PackageNativeSearchResult> packageSearchResults = searchResults.getResults();

        for (PackageNativeSearchResult packageResult : packageSearchResults) {
            String pkgName = modelHandler.getPackageName(packageResult);
            PackageNativeModel currentPackage = packageNativeResults.get(pkgName);

            if (currentPackage != null) {
                modelHandler.mergeFields(currentPackage, packageResult);
            } else {
                currentPackage = new PackageNativeModel();
                modelHandler.mergeFields(currentPackage, packageResult);
                packageNativeResults.put(pkgName, currentPackage);
            }
            mergeVersions(allVersions, pkgName, modelHandler.getPackageVersion(packageResult), currentPackage);
        }
    }

    private void mergeVersions(Map<String, Set<String>> allVersions, String pkgName, String resultVersion,
            PackageNativeModel packageNativeModel) {
        if (allVersions.get(pkgName) == null) {
            allVersions.put(pkgName, Sets.newHashSet(resultVersion));
        } else {
            allVersions.get(pkgName).add(resultVersion);
        }
        packageNativeModel.setNumOfVersions(allVersions.get(pkgName).size());
    }

    private List<PackageNativeModel> sortResults(List<PackageNativeModel> nativeResults, String orderType) {
        Comparator<PackageNativeModel> comparator = new CompareByName();
        if (ORDER_DESC.equals(orderType)) {
            comparator = comparator.reversed();
        }
        return nativeResults.stream().filter(pkg -> (pkg.getName() != null)).sorted(comparator)
                .collect(Collectors.toList());
    }

    public static class CompareByName implements Comparator<PackageNativeModel> {
        private static final String SCOPE_REGEX = "@.*[/]";

        @Override
        public int compare(PackageNativeModel pkg1, PackageNativeModel pkg2) {
            String namePkg1 =
                    pkg1.getName().startsWith("@") ? pkg1.getName().replaceFirst(SCOPE_REGEX, "") : pkg1.getName();
            String namePkg2 =
                    pkg2.getName().startsWith("@") ? pkg2.getName().replaceFirst(SCOPE_REGEX, "") : pkg2.getName();
            return namePkg1.compareToIgnoreCase(namePkg2);
        }
    }
}