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

package org.artifactory.ui.rest.service.artifacts.search.trashsearch;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchControls;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.artifact.ArtifactSearchControls;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.artifact.ChecksumSearchControls;
import org.artifactory.api.search.exception.InvalidChecksumException;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.trashsearch.TrashSearch;
import org.artifactory.ui.rest.model.artifacts.search.trashsearch.TrashSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.artifactory.ui.utils.SearchUtils.addChecksumCriteria;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TrashSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(TrashSearchService.class);

    @Autowired
    private SearchService searchService;

    @Autowired
    private PropertiesService propertiesService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        TrashSearch trashSearch = (TrashSearch) request.getImodel();
        if (trashSearch.isChecksum()) {
            performChecksumSearch(trashSearch, response);
        } else {
            performQuickSearch(trashSearch, response);
        }
    }

    private void performChecksumSearch(TrashSearch trashSearch, RestResponse response) {
        ChecksumSearchControls searchControl = getChecksumSearchControl(trashSearch);
        if (isSearchEmptyOrWildCardOnly(searchControl)) {
            response.error("Please enter a valid checksum to search for");
            return;
        }
        try {
            ItemSearchResults<ArtifactSearchResult> checksumResults =
                    searchService.getArtifactsByChecksumResults(searchControl);
            updateResponse(trashSearch, searchControl.isLimitSearchResults(), checksumResults, response);
        } catch (InvalidChecksumException ice) {
            log.error("Failed to execute checksum search");
            response.error(ice.getMessage());
        }
    }

    private void performQuickSearch(TrashSearch trashSearch, RestResponse response) {
        ArtifactSearchControls searchControl = getArtifactSearchControl(trashSearch);
        if (isSearchEmptyOrWildCardOnly(searchControl)) {
            response.error("Search term empty or containing only wildcards is not permitted");
            return;
        }
        ItemSearchResults<ArtifactSearchResult> checksumResults = searchService.searchArtifacts(searchControl);
        updateResponse(trashSearch, searchControl.isLimitSearchResults(), checksumResults, response);
    }

    private void updateResponse(TrashSearch trashSearch, boolean limit,
            ItemSearchResults<ArtifactSearchResult> searchResults, RestResponse response) {
        List<TrashSearchResult> trashSearchResults = Lists.newArrayList();
        for (ArtifactSearchResult artifactSearchResult : searchResults.getResults()) {
            trashSearchResults.add(new TrashSearchResult(artifactSearchResult,
                    propertiesService.getProperties(artifactSearchResult.getItemInfo().getRepoPath())));
        }
        long resultsCount;
        int maxResults = ConstantValues.searchMaxResults.getInt();
        if (limit && trashSearchResults.size() > maxResults) {
            trashSearchResults = trashSearchResults.subList(0, maxResults);
            resultsCount = trashSearchResults.size() == 0 ? 0 : searchResults.getFullResultsCount();
        } else {
            resultsCount = trashSearchResults.size();
        }
        SearchResult model = new SearchResult(trashSearchResults, trashSearch.getQuery(), resultsCount, limit);
        model.addNotifications(response);
        response.iModel(model);
    }

    private ChecksumSearchControls getChecksumSearchControl(TrashSearch trashSearch) {
        String query = trashSearch.getQuery();
        ChecksumSearchControls searchControls = new ChecksumSearchControls();
        if (StringUtils.isNotBlank(query)) {
            addChecksumCriteria(query, searchControls);
            searchControls.setSelectedRepoForSearch(Lists.newArrayList(TrashService.TRASH_KEY));
        }
        return searchControls;
    }

    private ArtifactSearchControls getArtifactSearchControl(TrashSearch trashSearch) {
        ArtifactSearchControls artifactSearchControls = new ArtifactSearchControls();
        artifactSearchControls.setSelectedRepoForSearch(Lists.newArrayList(TrashService.TRASH_KEY));
        artifactSearchControls.setQuery(trashSearch.getQuery());
        artifactSearchControls.setLimitSearchResults(true);
        return artifactSearchControls;
    }

    private boolean isSearchEmptyOrWildCardOnly(SearchControls artifactSearchControl) {
        return artifactSearchControl.isEmpty() || artifactSearchControl.isWildcardsOnly();
    }
}
