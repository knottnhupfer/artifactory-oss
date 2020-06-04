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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.ui.rest.resource.artifacts.browse.treebrowser.tabs.generalinfo.FilteredResourceResource.SET_FILTERED_QUERY_PARAM;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SetFilteredResourceService implements RestService {

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseArtifactInfo artifact = (BaseArtifactInfo) request.getImodel();
        RepoPath path = RepoPathFactory.create(artifact.getRepoKey(), artifact.getPath());
        if (!addonsManager.isAddonSupported(AddonType.FILTERED_RESOURCES)) {
            response.error("The Filtered Resource addon is not enabled").responseCode(HttpStatus.SC_FORBIDDEN);
        } else if (!authService.canAnnotate(path)) {
            response.error("You do not have annotate permissions on this path").responseCode(HttpStatus.SC_FORBIDDEN);
        } else if (repoService.getItemInfo(path).isFolder()) {
            response.error(path.getPath() + " is a folder").responseCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            addonsManager.addonByType(FilteredResourcesAddon.class).toggleResourceFilterState(path,
                    Boolean.valueOf(request.getQueryParamByKey(SET_FILTERED_QUERY_PARAM)));
        }
    }
}
