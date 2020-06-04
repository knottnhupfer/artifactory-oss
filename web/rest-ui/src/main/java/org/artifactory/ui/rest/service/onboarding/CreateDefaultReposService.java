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

package org.artifactory.ui.rest.service.onboarding;

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.onboarding.YamlConfigCreator;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.onboarding.CreateDefaultReposModel;
import org.artifactory.ui.rest.service.admin.configuration.repositories.CreateRepositoryConfigService;
import org.artifactory.ui.utils.DefaultRepoCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import static org.artifactory.repo.config.RepoConfigDefaultValues.EXAMPLE_REPO_KEY;

/**
 * Service for creating default repositories for repo types stated in CreateDefaultReposModel
 *
 * @author nadavy
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateDefaultReposService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(CreateDefaultReposService.class);

    @Autowired
    private CreateRepositoryConfigService createRepoService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AddonsManager addonsManager;

    private DefaultRepoCreator defaultRepoCreator = new DefaultRepoCreator();

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CreateDefaultReposModel reposToCreate = (CreateDefaultReposModel) request.getImodel();
        createDefaultRepositoriesFromModel(response, reposToCreate, reposToCreate.isFromOnboarding());
    }

    private void deleteExampleRepository(MutableCentralConfigDescriptor configDescriptor) {
        RepoPath exampleRepoPath = RepoPathFactory.create(EXAMPLE_REPO_KEY, "");
        if (repositoryService.getArtifactCount(exampleRepoPath) == 0) {
            configDescriptor.removeRepository(EXAMPLE_REPO_KEY);
        }
    }

    /**
     * Create default repositories as defined in defaultRepository.json
     */
    private void createDefaultRepositoriesFromModel(RestResponse response, CreateDefaultReposModel reposToCreate,
            boolean fromOnboarding) {
        CreateDefaultReposResponseModel createDefaultReposResponseModel = new CreateDefaultReposResponseModel();
        ArtifactoryContext artifactoryContext = ContextHelper.get();
        List<RepoType> packageTypesToCreate = reposToCreate.getRepoTypeList();
        if (packageTypesToCreate.isEmpty()) {
            exportYamlConfigurationToFile(packageTypesToCreate, artifactoryContext);
            response.responseCode(HttpStatus.SC_OK);
            return;
        }
        boolean edgeLicensed = addonsManager.isEdgeLicensed();
        packageTypesToCreate.
                forEach(repoType -> defaultRepoCreator
                        .createDefaultRepos(createRepoService, response, repoType, createDefaultReposResponseModel, edgeLicensed));
        if (createDefaultReposResponseModel.isValid()) {
            handleValidRepoCreation(fromOnboarding, artifactoryContext, packageTypesToCreate);
            response.iModel(createDefaultReposResponseModel).responseCode(HttpStatus.SC_CREATED);
        } else {
            response.responseCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles valid repo creation from REST UI -
     * delete example repo (if needed)
     * save and reload config descriptor
     * and export settings to yaml (if from onboarding)
     */
    private void handleValidRepoCreation(boolean fromOnboarding, ArtifactoryContext artifactoryContext,
            List<RepoType> packageTypesToCreate) {
        deleteExampleRepository(defaultRepoCreator.getConfigDescriptor());
        artifactoryContext.getCentralConfig()
                .saveEditedDescriptorAndReload(defaultRepoCreator.getConfigDescriptor());
        if (fromOnboarding) {
            exportYamlConfigurationToFile(packageTypesToCreate, artifactoryContext);
        }
    }

    /**
     * export settings to YAML files
     */
    private void exportYamlConfigurationToFile(List<RepoType> repoTypeList, ArtifactoryContext artifactoryContext) {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String timestamp = formatter.format(System.currentTimeMillis());
        File yamlOutput = new File(
                artifactoryContext.getArtifactoryHome().getEtcDir() + "/artifactory.config." + timestamp
                        + ".yml");
        try {
            List<String> repoTypes = repoTypeList.stream()
                    .map(RepoType::getType)
                    .collect(Collectors.toList());
            new YamlConfigCreator()
                    .saveBootstrapYaml(repoTypes, artifactoryContext.getCentralConfig().getMutableDescriptor(),
                            yamlOutput);
        } catch (IOException e) {
            String message = "artifactory can't export settings to " + yamlOutput.getName();
            log.error(message);
            log.debug(message, e);
        }
    }
}
