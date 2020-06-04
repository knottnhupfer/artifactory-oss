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

package org.artifactory.ui.rest.service.artifacts.search;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.npm.NpmAddon;
import org.artifactory.addon.npm.NpmMetadataInfo;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeDependenciesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeRestConstants.PATH;

/**
 * @author Inbar Tal
 * @author Lior Gur
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VersionNativeDependenciesService implements RestService {

    private AuthorizationService authorizationService;
    private RepositoryService repositoryService;

    @Autowired
    public VersionNativeDependenciesService(AuthorizationService authorizationService, RepositoryService repositoryService) {
        this.authorizationService = authorizationService;
        this.repositoryService = repositoryService;
    }

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String path = request.getQueryParamByKey(PATH);
        RepoPath repoPath = RepoPathFactory.create(path);

        if (!authorizationService.canRead(repoPath)) {
            response.iModel(new VersionNativeDependenciesModel());
            return;
        }

        if (!repositoryService.exists(RepoPathFactory.create(path))) {
            throw new NotFoundException(path + " not found");
        }

        VersionNativeDependenciesModel model = getDependencies(repoPath);
        response.iModel(model);
    }

    private VersionNativeDependenciesModel getDependencies(RepoPath repoPath) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        NpmAddon npmAddon = addonsManager.addonByType(NpmAddon.class);
        NpmMetadataInfo npmMetaDataInfo = npmAddon.getNpmMetaDataInfo(repoPath);

        return npmMetaDataInfo != null ? new VersionNativeDependenciesModel(
                npmMetaDataInfo.getNpmDependencies(), npmMetaDataInfo.getNpmDevDependencies()) : null;
    }
}

