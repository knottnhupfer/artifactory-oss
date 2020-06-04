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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.composer;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.composer.ComposerAddon;
import org.artifactory.addon.composer.ComposerMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.composer.ComposerArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Bagants
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComposerViewService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(ComposerViewService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ComposerArtifactInfo composerArtifactInfo = (ComposerArtifactInfo) request.getImodel();
        // Fetch npm meta data
        fetchComposerMetadata(response, composerArtifactInfo);
    }

    private void fetchComposerMetadata(RestResponse artifactoryResponse,
            ComposerArtifactInfo composerArtifactInfo) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        ComposerAddon composerAddon = addonsManager.addonByType(ComposerAddon.class);
        RepoPath repoPath = InternalRepoPathFactory
                .create(composerArtifactInfo.getRepoKey(), composerArtifactInfo.getPath());
        if (!authorizationService.canRead(repoPath)) {
            artifactoryResponse.responseCode(HttpStatus.SC_FORBIDDEN);
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        ComposerMetadataInfo composerMetadataInfo = composerAddon.getComposerMetadataInfo(repoPath);
        artifactoryResponse.iModel(composerMetadataInfo);

    }
}
