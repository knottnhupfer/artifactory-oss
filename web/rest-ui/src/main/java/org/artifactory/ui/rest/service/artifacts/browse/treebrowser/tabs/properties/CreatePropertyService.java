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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.exception.CancelException;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.PropertiesArtifactInfo;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreatePropertyService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(DeletePropertyService.class);

    @Autowired
    private PropertiesService propsService;
    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PropertiesArtifactInfo propertiesTab = (PropertiesArtifactInfo) request.getImodel();
        RepoPath repoPath = RequestUtils.getPathFromRequest(request);
        // read permission checks
        if (!authorizationService.canAnnotate(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        boolean recursive = Boolean.valueOf(request.getQueryParamByKey("recursive"));
        // save property to db
        if (propertiesTab.getProperty() == null) {
            response.error("Cannot create an empty property");
            return;
        }
        savePropertyAndUpdateResponse(response, propertiesTab, repoPath, recursive);
    }

    /**
     * save property and update response
     *
     * @param artifactoryResponse - encapsulate artifactory response data
     * @param propertiesTab       - properties tab data
     * @param repoPath            - repo path
     */
    private void savePropertyAndUpdateResponse(RestResponse artifactoryResponse, PropertiesArtifactInfo propertiesTab,
            RepoPath repoPath, boolean recursive) {
        try {
            if (recursive) {
                propsService.addPropertyRecursively(repoPath, propertiesTab.getParent(), propertiesTab.getProperty(),
                        true, propertiesTab.getSelectedValues());
            } else {
                propsService.addProperty(repoPath, propertiesTab.getParent(), propertiesTab.getProperty(),
                        true, propertiesTab.getSelectedValues());
            }
            artifactoryResponse.info("Successfully created property '" + propertiesTab.getProperty().getName() + "'");
            artifactoryResponse.responseCode(HttpServletResponse.SC_CREATED);
        } catch (CancelException e) {
            log.error("Failed to create property:" + propertiesTab.getProperty().getName());
            artifactoryResponse.error("Failed to created property '" + propertiesTab.getProperty().getName() + "'");
        }
    }
}
