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

package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import com.google.common.collect.ImmutableList;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearchResult;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DiscardFromResultsService extends BaseSearchResultService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        String path = request.getQueryParamByKey("path");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        String resultName = request.getQueryParamByKey("name");
        // get save search results from session
        SavedSearchResults savedSearchResults = RequestUtils.getResultsFromRequest(resultName,
                request.getServletRequest());
        SavedSearchResults resultsToDiscard = getResultsToDiscard(repoPath, resultName, savedSearchResults);
        //discard results from session
        savedSearchResults.discardFromResult(resultsToDiscard);
    }

    /**
     * get save search results to discard
     *
     * @param repoPath           - repo path
     * @param resultName         - result name
     * @param savedSearchResults - saved search result instance
     * @return
     */
    private SavedSearchResults getResultsToDiscard(RepoPath repoPath, String resultName,
            SavedSearchResults savedSearchResults) {
        ImmutableList<FileInfo> results = savedSearchResults.getResults();
        List<BaseSearchResult> baseSearchResults = getResultsToDiscard(repoPath, results);
        List<ItemSearchResult> newResults = new ArrayList<>();
        return getSavedSearchResults(resultName, newResults, baseSearchResults, false);
    }

    /**
     * get results to discard
     *
     * @param repoPath - repo path
     * @param results  - results
     * @return - list of saved search result to discard
     */
    private List<BaseSearchResult> getResultsToDiscard(RepoPath repoPath, ImmutableList<FileInfo> results) {
        List<BaseSearchResult> baseSearchResults = new ArrayList<>();
        results.forEach(result -> {
            if (result.getRepoPath().toString().startsWith(repoPath.toString())) {
                QuickSearchResult quickSearchResult = new QuickSearchResult();
                quickSearchResult.setRepoKey(result.getRepoKey());
                quickSearchResult.setRelativePath(result.getRelPath());
                baseSearchResults.add(quickSearchResult);
            }
        });
        return baseSearchResults;
    }
}
