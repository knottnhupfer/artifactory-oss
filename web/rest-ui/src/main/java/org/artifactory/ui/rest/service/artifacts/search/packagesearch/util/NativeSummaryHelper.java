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

import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.search.AqlUISearchModel;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.NativeSummaryModel;
import org.artifactory.ui.rest.service.artifacts.search.versionsearch.NativeSearchControls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author Inbar Tal
 */
@Component
public class NativeSummaryHelper {

    private PackageNativeSearchHelper packageNativeSearchHelper;

    @Autowired
    public NativeSummaryHelper(PackageNativeSearchHelper packageNativeSearchHelper) {
        this.packageNativeSearchHelper = packageNativeSearchHelper;
    }

    public NativeSummaryModel getSummary(NativeSearchControls searchControls, String propKey) {

        PackageNativeModelHandler modelHandler = searchControls.getModelHandler();
        List<AqlUISearchModel> searches = searchControls.getSearches();
        SearchResult<PackageNativeSearchResult> searchResults = packageNativeSearchHelper
                .search(searchControls, modelHandler.getSummaryPropKeys());
        Collection<PackageNativeSearchResult> packageSearchResults = searchResults.getResults();

        NativeSummaryModel packageNativeSummary = new NativeSummaryModel();
        packageSearchResults
                .forEach(pkgResult -> modelHandler.mergeSummaryFields(packageNativeSummary, pkgResult));
        setName(searches, packageNativeSummary, propKey);

        return packageNativeSummary;
    }

    private void setName(List<AqlUISearchModel> searches, NativeSummaryModel packageNativeSummary, String propKey) {
        for (AqlUISearchModel searchModel : searches) {
            if (propKey.equals(searchModel.getId())) {
                packageNativeSummary.setName(searchModel.getValues().get(0));
                break;
            }
        }
    }
}
