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

package org.artifactory.ui.rest.service.admin.configuration.repositories;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusEntry;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.util.distribution.DistributionConstants.EDGE_UPLOADS_REPO_KEY;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteRepositoryConfigService<T extends RepositoryConfigModel> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(DeleteRepositoryConfigService.class);

    private CentralConfigService centralConfigService;
    private RepositoryService repositoryService;
    private AddonsManager addonsManager;
    private BuildService buildService;

    @Autowired
    public DeleteRepositoryConfigService(CentralConfigService centralConfigService, RepositoryService repositoryService,
            AddonsManager addonsManager, BuildService buildService) {
        this.centralConfigService = centralConfigService;
        this.repositoryService = repositoryService;
        this.addonsManager = addonsManager;
        this.buildService = buildService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MutableCentralConfigDescriptor configDescriptor = centralConfigService.getMutableDescriptor();
        String repoKey = request.getPathParamByKey("repoKey");
        if (TrashService.TRASH_KEY.equals(repoKey) || buildService.getBuildInfoRepoKey().equals(repoKey)) {
            response.error("Deleting a system repo '" + repoKey + "' is not allowed.").responseCode(SC_FORBIDDEN);
        }
        if (!configDescriptor.isRepositoryExists(repoKey)) {
            response.error("Repository '" + repoKey + "' does not exist").responseCode(SC_NOT_FOUND);
            return;
        }
        LocalRepoDescriptor localRepoDescriptor = configDescriptor.getLocalRepositoriesMap().get(repoKey);
        if (localRepoDescriptor != null) {
            if (configDescriptor.getLocalRepositoriesMap().size() == 1) {
                response.error("Deleting the last local repository is not allowed").responseCode(SC_FORBIDDEN);
                return;
            }
            if (EDGE_UPLOADS_REPO_KEY.equals(repoKey) && RepoType.Generic.equals(localRepoDescriptor.getType())
                    && addonsManager.isEdgeLicensed()) {
                response.error("Deleting '" + EDGE_UPLOADS_REPO_KEY + "' repository is not allowed").responseCode(SC_FORBIDDEN);
                return;
            }
        }
        try {
            log.info("Deleting repository {}", repoKey);
            BasicStatusHolder statusHolder = repositoryService.removeRepository(repoKey);
            if (statusHolder.hasErrors()) {
                StatusEntry statusEntry = statusHolder.getMostImportantErrorStatusCode();
                response.error(statusEntry.getMessage()).responseCode(statusEntry.getStatusCode());
            } else {
                response.info("Successfully deleted '" + repoKey + "' repository").responseCode(SC_OK);
            }
        } catch (Exception e) {
            log.error("Deleting repo '{}' failed: {}", repoKey, e.getMessage(), e);
            response.error("Deleting repo '" + repoKey + "' failed: " + e.getMessage());
        }
    }
}
