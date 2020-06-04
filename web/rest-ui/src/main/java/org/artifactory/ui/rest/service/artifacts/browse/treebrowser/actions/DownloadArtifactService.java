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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadArtifact;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DownloadArtifactService implements RestService {

    @Autowired
    AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DownloadArtifact downloadArtifact = (DownloadArtifact) request.getImodel();
        String repoKey = downloadArtifact.getRepoKey();
        String path = downloadArtifact.getPath();
        RestModel artifactDownloadModel = getArtifactDownloadModel(request, repoKey, path);
        response.iModel(artifactDownloadModel);
    }

    /**
     * build download url and update download model
     * @param artifactoryRequest - encapsulate data related to request
      * @param repoKey - repo key
     * @param path - path
     */
    private RestModel getArtifactDownloadModel(ArtifactoryRestRequest artifactoryRequest,
            String repoKey, String path) {
        HttpServletRequest httpRequest =artifactoryRequest.getServletRequest();
        DownloadArtifact downloadArtifact = new DownloadArtifact();
        String downloadPath = HttpUtils.getServletContextUrl(httpRequest) + "/" + repoKey + "/" + path;
        downloadArtifact.setPath(downloadPath);
        updateXrayValidation(downloadArtifact, repoKey, path);
        return downloadArtifact;
    }

    private void updateXrayValidation(DownloadArtifact downloadArtifact, String repoKey, String path) {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        if (xrayAddon.isXrayConfPerRepoAndIntegrationEnabled(repoKey)
                && xrayAddon.isHandledByXray(RepoPathFactory.create(repoKey, path))) {
            downloadArtifact.setXrayValidation(true);
        }
    }
}
