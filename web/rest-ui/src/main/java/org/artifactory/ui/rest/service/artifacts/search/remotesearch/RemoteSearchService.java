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

package org.artifactory.ui.rest.service.artifacts.search.remotesearch;

import org.artifactory.api.bintray.BintrayItemInfo;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.api.search.BintrayItemSearchResults;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.remotesearch.RemoteResult;
import org.artifactory.ui.rest.model.artifacts.search.remotesearch.RemoteSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoteSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RemoteSearchService.class);
    private static final int MINIMAL_QUERY_LENGTH = 3;

    @Autowired
    BintrayService bintrayService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RemoteSearch remoteSearch = (RemoteSearch) request.getImodel();
        Map<String, String> headersMap = getHeaders(request.getServletRequest());
        if (remoteSearch.getSearchKey().length() < MINIMAL_QUERY_LENGTH) {
            response.error("The search key must contain at least 3 letters");
        }
        // search in bintray
        BintrayItemSearchResults<BintrayItemInfo> bintraySearchResults = searchRemoteRepo(remoteSearch, headersMap);
        // update model list result
        List<RemoteResult> remoteResults = updateModelListResult(bintraySearchResults,request);
        // update response data
        boolean isLimitSearchResults = (bintraySearchResults != null) && (bintraySearchResults.getRangeLimitTotal() > 0);
        SearchResult model = new SearchResult(remoteResults, remoteSearch.getSearchKey(), remoteResults.size(),
                isLimitSearchResults);
        model.addNotifications(response);
        response.iModel(model);
    }


    /**
     * check if search is empty or contain wildcard only
     *
     * @param searchKey - search key
     * @return if true - search is empty or containing wild card only
     */
    private boolean isSearchEmptyOrWildCardOnly(String searchKey) {
        return searchKey.isEmpty() || searchKey.indexOf("*") == 0;
    }

    /**
     * update remote model list
     *
     * @param bintraySearchResults - results return from bintray
     * @return - remote results model list
     */
    private List<RemoteResult> updateModelListResult(BintrayItemSearchResults<BintrayItemInfo> bintraySearchResults,ArtifactoryRestRequest restRequest) {
        List<RemoteResult> remoteResults = new ArrayList<>();
        if (bintraySearchResults != null) {
            bintraySearchResults.getResults().forEach(remoteItem -> remoteResults.add(new RemoteResult(remoteItem,restRequest)));
        }
        return remoteResults;
    }

    /**
     * search remote repository
     *
     * @param remoteSearch - remote search model
     * @param headersMap   - request headers
     * @return Bintray search results
     */
    private BintrayItemSearchResults<BintrayItemInfo> searchRemoteRepo(RemoteSearch remoteSearch,
            Map<String, String> headersMap) {
        BintrayItemSearchResults<BintrayItemInfo> bintrayItemInfoBintrayItemSearchResults = null;
        try {
            bintrayItemInfoBintrayItemSearchResults = bintrayService.searchByName(
                    remoteSearch.getSearchKey(), headersMap);
        } catch (IOException e) {
            log.error(e.toString());
        } catch (BintrayException e) {
            log.error(e.toString());
        }
        return bintrayItemInfoBintrayItemSearchResults;
    }

    /**
     * return header from servlet request
     *
     * @param request - http servlet request
     * @return Map of headers
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();
        if (request != null) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                headersMap.put(headerName.toUpperCase(), request.getHeader(headerName));
            }
        }
        return headersMap;
    }
}
