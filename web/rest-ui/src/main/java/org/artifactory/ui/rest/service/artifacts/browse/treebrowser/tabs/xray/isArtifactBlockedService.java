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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.xray;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * Service for checking if Xray is blocking an artifact for the UI Download button
 *
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class isArtifactBlockedService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(isArtifactBlockedService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        RepoPath repoPath = RequestUtils.getPathFromRequest(request);

        String errorMsg;
        if (!authorizationService.canRead(repoPath)) {
            errorMsg = "Forbidden UI REST call from user: '" + authorizationService.currentUsername() +"'";
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).error(errorMsg).buildResponse();
            log.error(errorMsg);
            return;
        }

        // Retrieving info from Xray client
        boolean downloadBlocked = xrayAddon.isDownloadBlocked(repoPath);
        if (downloadBlocked) {
            response.error("The artifact is blocked due to download blocking policy configured " +
                    "in Xray for " + repoPath.getRepoKey())
                    .responseCode(HttpServletResponse.SC_FORBIDDEN)
                    .buildResponse();
        }
    }
}
