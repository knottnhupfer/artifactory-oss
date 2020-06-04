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

package org.artifactory.ui.rest.service.artifacts.search.checksumsearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.checksumsearch.ChecksumSearch;
import org.artifactory.ui.rest.model.artifacts.search.quicksearch.QuickSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.ui.utils.SearchUtils.addChecksumCriteria;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChecksumSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ChecksumSearchService.class);

    @Autowired
    private SearchService searchService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // search checksum artifact
        try {
            ChecksumSearch checksumSearch = (ChecksumSearch) request.getImodel();
            // Getting checksum type by length. No wildcards allowed in short strings, only exact length
            ChecksumSearchControls checksumSearchControl = getChecksumSearchControl(checksumSearch);
            if (isSearchEmptyOrWildCardOnly(checksumSearchControl)) {
                response.error("Invalid Checksum length");
                return;
            }
            ItemSearchResults<ArtifactSearchResult> checksumResults = searchService.getArtifactsByChecksumResults(
                    checksumSearchControl);
            // update model search
            List<QuickSearchResult> checksumResultList = updateSearchModels(checksumResults, request);
            int maxResults = ConstantValues.searchMaxResults.getInt();
            long resultsCount;
            if (checksumSearchControl.isLimitSearchResults() && checksumResultList.size() > maxResults) {
                checksumResultList = checksumResultList.subList(0, maxResults);
                resultsCount = checksumResultList.size() == 0 ? 0 : checksumResults.getFullResultsCount();
            } else {
                resultsCount = checksumResultList.size();
            }
            // update response
            SearchResult model = new SearchResult(checksumResultList, checksumSearch.getChecksum(), resultsCount,
                    checksumSearchControl.isLimitSearchResults());
            model.addNotifications(response);
            //noinspection unchecked
            response.iModel(model);
        } catch (Exception e) {
            log.error("Failed to execute checksum search", e);
            response.error(e.getMessage());
        }
    }

    /**
     * update search model with results
     *
     * @param checksumResults - checksum search result
     * @return list of search models
     */
    private List<QuickSearchResult> updateSearchModels(ItemSearchResults<ArtifactSearchResult> checksumResults,
            ArtifactoryRestRequest request) {
        List<QuickSearchResult> checksumResultList = new ArrayList<>();
        checksumResults.getResults().stream()
                .filter(this::filterNoReadResults)
                .forEach(checksumResult -> checksumResultList.add(new QuickSearchResult(checksumResult, request)));
        return checksumResultList;
    }

    private boolean filterNoReadResults(ArtifactSearchResult checksumResult) {
        RepoPath repoPath = RepoPathFactory.create(checksumResult.getRepoKey(), checksumResult.getRelativePath());
        return authorizationService.canRead(repoPath);
    }

    /**
     * check if search is empty or contain wildcard only
     *
     * @param checksumSearchControls Use the checksumSearchControls to conclude whether checksum is empty or
     *                               wildcards only
     * @return if true - search is empty or containing wild card only
     */
    private boolean isSearchEmptyOrWildCardOnly(ChecksumSearchControls checksumSearchControls) {
        return checksumSearchControls.isEmpty() || checksumSearchControls.isWildcardsOnly();
    }

    /**
     * create checksum search control
     *
     * @return Checksum search controls
     */
    private ChecksumSearchControls getChecksumSearchControl(ChecksumSearch checksumSearch) {
        String query = checksumSearch.getChecksum();
        ChecksumSearchControls searchControls = new ChecksumSearchControls();
        if (StringUtils.isNotBlank(query)) {
            addChecksumCriteria(query, searchControls);
            searchControls.setSelectedRepoForSearch(checksumSearch.getSelectedRepositories());
        }
        return searchControls;
    }
}
