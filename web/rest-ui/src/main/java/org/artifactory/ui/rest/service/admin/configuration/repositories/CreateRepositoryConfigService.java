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

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.repo.RepoDetailsType;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.RepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.distribution.DistributionRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.releasebundles.ReleaseBundlesRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualRepositoryConfigModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.CreateRepoConfigHelper;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.RepoConfigValidator;
import org.artifactory.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * @author Aviad Shikloshi
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateRepositoryConfigService implements RestService<RepositoryConfigModel> {
    private static final Logger log = LoggerFactory.getLogger(CreateRepositoryConfigService.class);

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private RepoConfigValidator repoValidator;

    @Autowired
    private CreateRepoConfigHelper creator;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepositoryConfigModel model = (RepositoryConfigModel) request.getImodel();
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        String repoKey = Optional.ofNullable(model.getGeneral()).orElse(new GeneralRepositoryConfigModel()).getRepoKey();
        try {
            configDescriptor = createRepo(response, model, configDescriptor);
            if (configDescriptor != null ) {
                configService.saveEditedDescriptorAndReload(configDescriptor);
                response.info("Successfully added repository '" + repoKey + "'");
            }
        } catch (Exception e) {
            log.error("Failed to create repository '" + repoKey + "': ", e.getMessage());
            log.debug("Failed to create repository '" + repoKey + "': ", e);
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            response.error("Failed to create repository " + repoKey + ": " +
                    (rootCause != null ? rootCause.getMessage() : e.getMessage()));
        }
    }

    public MutableCentralConfigDescriptor createRepo(RestResponse response, RepositoryConfigModel model,
            MutableCentralConfigDescriptor configDescriptor) throws RepoConfigException {
        String repoKey = model.getGeneral().getRepoKey();
        if (configDescriptor.isRepositoryExists(repoKey)) {
            response.error("Repository " + repoKey + " already exists").responseCode(HttpStatus.SC_BAD_REQUEST);
            return null;
        }
        log.info("Creating repository {}", repoKey);
        //Run repo name validation only on create
        repoValidator.validateRepoName(model.getGeneral().getRepoKey(), getRepoTypeByConfigModel(model));
        if (model instanceof LocalRepositoryConfigModel) {
            if (StringUtils.endsWithIgnoreCase(repoKey, "-cache")) {
                throw new RepoConfigException("Local repository '" + repoKey + "' with '-cache' suffix is not allowed",
                        SC_BAD_REQUEST);
            }
        }
        return model.createRepo(creator, configDescriptor);
    }

    private String getRepoTypeByConfigModel(RepositoryConfigModel model) throws RepoConfigException {
        if (model instanceof LocalRepositoryConfigModel) {
            return RepoDetailsType.LOCAL_REPO;
        }
        if (model instanceof RemoteRepositoryConfigModel) {
            return RepoDetailsType.REMOTE_REPO;
        }
        if (model instanceof VirtualRepositoryConfigModel) {
            return RepoDetailsType.VIRTUAL_REPO;
        }
        if (model instanceof DistributionRepositoryConfigModel) {
            return RepoDetailsType.DISTRIBUTION_REPO;
        }
        if (model instanceof ReleaseBundlesRepositoryConfigModel) {
            return RepoDetailsType.RELEASE_BUNDLE_REPO;
        }
        throw new RepoConfigException("Cannot extract repository type from repository config model", SC_BAD_REQUEST);
    }
}
