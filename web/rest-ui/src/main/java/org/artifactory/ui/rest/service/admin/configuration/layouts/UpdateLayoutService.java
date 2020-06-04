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

package org.artifactory.ui.rest.service.admin.configuration.layouts;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.exception.ValidationException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.admin.configuration.layouts.validation.LayoutFieldRequiredTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */
@Component
public class UpdateLayoutService implements RestService<RepoLayout> {
    private static final Logger log = LoggerFactory.getLogger(UpdateLayoutService.class);
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest<RepoLayout> request, RestResponse response) {
        RepoLayout repoLayout = request.getImodel();
        try {
            validation(repoLayout);
            MutableCentralConfigDescriptor configDescriptor = getMutableDescriptor();
            RepoLayout savedRepoLayout = configDescriptor.getRepoLayout(repoLayout.getName());
            if (savedRepoLayout != null) {
                savedRepoLayout.setArtifactPathPattern(repoLayout.getArtifactPathPattern());
                savedRepoLayout.setDescriptorPathPattern(repoLayout.getDescriptorPathPattern());
                savedRepoLayout.setDistinctiveDescriptorPathPattern(repoLayout.isDistinctiveDescriptorPathPattern());
                savedRepoLayout.setFileIntegrationRevisionRegExp(repoLayout.getFileIntegrationRevisionRegExp());
                savedRepoLayout.setFolderIntegrationRevisionRegExp(repoLayout.getFolderIntegrationRevisionRegExp());
                centralConfigService.saveEditedDescriptorAndReload(configDescriptor);
            }

            String message = "Successfully updated layout '" + repoLayout.getName() + "'";
            response.info(message);
        } catch (ValidationException e) {
            response.error(e.getMessage());
            log.debug(e.getMessage());
        }
    }

    /**
    * @see LayoutFieldRequiredTokenValidator
    */
    private void validation(RepoLayout repoLayout) throws ValidationException {
            LayoutFieldRequiredTokenValidator.onValidate(repoLayout.getArtifactPathPattern());
            LayoutFieldRequiredTokenValidator.onValidate(repoLayout.getDescriptorPathPattern());
    }

    private MutableCentralConfigDescriptor getMutableDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }
}
