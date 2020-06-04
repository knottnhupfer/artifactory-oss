/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
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

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.model.artifact.BaseArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Nadav Yogev
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RecalculateDebianCoordinatesService<T extends BaseArtifact> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(RecalculateDebianCoordinatesService.class);

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        BaseArtifact targetRepo = request.getImodel();
        String repoKey = targetRepo.getRepoKey();
        RepoPath repoPath = RepoPathFactory.create(repoKey);
        if (!authorizationService.canManage(repoPath)) {
            response.error("Forbidden").responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }

        try {
            DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
            LocalRepoDescriptor descriptor = ContextHelper.get().beanForType(RepositoryService.class)
                    .localOrCachedRepoDescriptorByKey(repoKey);
            if (debianAddon != null && descriptor.isCache()) {
                debianAddon.calculateCachedDebianCoordinates(repoKey);
            }
            String message = "Recalculating debian coordinates for repository " + repoKey + " scheduled to run";
            log.info(message);
            response.info(message);
        } catch (Exception e) {
            response.error("failed to schedule debian coordinates calculation");
            log.debug("", e);
        }
    }
}
