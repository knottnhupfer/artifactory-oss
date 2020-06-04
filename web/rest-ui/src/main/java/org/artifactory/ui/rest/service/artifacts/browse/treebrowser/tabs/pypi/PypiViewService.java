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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.pypi;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.pypi.PypiAddon;
import org.artifactory.addon.pypi.PypiPkgMetadata;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.pypi.PypiArtifactInfo;
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
public class PypiViewService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(PypiViewService.class);

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        PypiArtifactInfo pypiArtifactInfo = (PypiArtifactInfo) request.getImodel();
        String path = pypiArtifactInfo.getPath();
        String repoKey = pypiArtifactInfo.getRepoKey();
        RepoPath repoPath = RepoPathFactory.create(repoKey, path);
        if(repositoryService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repositoryService.getVirtualFileInfo(repoPath).getRepoPath();
        }
        // read permission checks
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        // get pypi meta data model
        PypiArtifactInfo pypiMetaData = getPypiArtifactInfo(repoPath.getPath(), repoPath.getRepoKey());
        response.iModel(pypiMetaData);

    }

    /**
     * get pypi meta data from pypi file meta data
     *
     * @param path    - artifact path
     * @param repoKey - repo path
     * @return pypi meta data model
     */
    private PypiArtifactInfo getPypiArtifactInfo(String path, String repoKey) {
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        PypiAddon pypiAddon = addonsManager.addonByType(PypiAddon.class);
        PypiPkgMetadata pypiPkgMetadata = pypiAddon.getPypiMetadata(repoPath);
        if (pypiPkgMetadata != null) {
            return new PypiArtifactInfo(pypiPkgMetadata);
        }
        return null;
    }
}
