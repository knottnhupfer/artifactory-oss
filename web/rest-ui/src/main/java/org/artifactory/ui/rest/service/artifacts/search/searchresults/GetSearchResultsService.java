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
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.artifacts.search.StashResult;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GetSearchResultsService extends BaseSearchResultService {

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String searchName = request.getQueryParamByKey("name");
        SavedSearchResults resultsToRequest = RequestUtils.getResultsFromRequest(searchName,
                request.getServletRequest());
        if (resultsToRequest != null) {
            // remove items which has been deleted from save search result
            removeNonValidDataFromResult(resultsToRequest);
            List<StashResult> stashResults = new ArrayList<>();
            resultsToRequest.getResults().forEach(result ->
                    {
                        RepoType repoPkgType = getRepoPkgType(result.getRepoKey());
                        MimeType mimeType = NamingUtils.getMimeType(result.getRelPath());
                        stashResults
                                .add(new StashResult(result.getName(), result.getRelPath(), result.getRepoKey(), repoPkgType,
                                        mimeType.isArchive()));
                    }
            );
            response.iModelList(stashResults);
        }
    }

    private RepoType getRepoPkgType(String repoKey) {
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        return repoDescriptor.getType();
    }

    /**
     * remove items which has been deleted from save search result
     *
     * @param resultsToRequest - results from session
     */
    private void removeNonValidDataFromResult(SavedSearchResults resultsToRequest) {
        ImmutableList.Builder<FileInfo> builder = ImmutableList.builder();
        resultsToRequest.getResults().forEach(result -> {
            try {
                repositoryService.getItemInfo(result.getRepoPath());
            } catch (ItemNotFoundRuntimeException e) {
                builder.add(result);
            }
        });
        ImmutableList<FileInfo> infos = builder.build();
        resultsToRequest.removeAll(infos);
    }
}
