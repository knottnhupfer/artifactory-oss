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

import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonType;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.WatchArtifact;
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
public class WatchArtifactService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(WatchArtifactService.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        WatchArtifact watchArtifact = (WatchArtifact) request.getImodel();
        String repoKey = watchArtifact.getRepoKey();
        String path = watchArtifact.getPath();
        String userName = authorizationService.currentUsername();
        if (authorizationService.isTransientUser()) {
            response.error("User " + userName + " doesn't have an internal user created in Artifactory and can't " +
                    "use watches.").responseCode(HttpStatus.SC_FORBIDDEN);
            return;
        }
        if (authorizationService.isAnonymous()) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        if (addonsManager.isAddonSupported(AddonType.WATCH)) {
            String param = watchArtifact.getParam();
            if (param != null) {
                boolean isWatch = (param.equals("watch"));
                watchOrUnwatchArtifact(repoKey, path, userName, isWatch, response);
            }
        }
    }

    /**
     * perform watch or unwatch on artifact
     *
     * @param repoKey  - artifact repo key
     * @param path     - artifact path
     * @param userName - user name
     * @param isWatch  - if true - watch , false - unwatch
     */
    private void watchOrUnwatchArtifact(String repoKey, String path, String userName, boolean isWatch,
            RestResponse artifactoryResponse) {
        ArtifactWatchAddon artifactWatchAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                ArtifactWatchAddon.class);
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        ItemInfo itemInfo = repositoryService.getItemInfo(repoPath);
        RepoPath pathToWatch = (itemInfo).getRepoPath();
        try {
            if (isWatch) {
                addWatchMessage(userName, artifactoryResponse, artifactWatchAddon, repoPath, pathToWatch);
            } else {
                addUnwatchMessage(userName, artifactoryResponse, artifactWatchAddon, repoPath, pathToWatch);
            }
        } catch (Exception e) {
            artifactoryResponse
                    .error(String.format("Failed to add watch on '%s' by user: '%s'", pathToWatch.toString(), userName));
        }
    }

    /**
     * add watch response message
     *
     * @param userName            - user watching
     * @param artifactoryResponse - encapsulate data related to response
     * @param artifactWatchAddon  - watch addon
     * @param repoPath            - artifact / repository path
     * @param pathToWatch         - patch to watch
     */
    private void addWatchMessage(String userName, RestResponse artifactoryResponse,
            ArtifactWatchAddon artifactWatchAddon, RepoPath repoPath, RepoPath pathToWatch) {
        artifactWatchAddon.addWatcher(pathToWatch, userName);
        if (repoPath.getPath().length() > 0) {
            artifactoryResponse.info(
                    String.format("Successfully added watch on artifact '%s' by user: '%s'", pathToWatch.toString(),
                            userName));
        } else {
            artifactoryResponse.info(
                    String.format("Successfully added watch on repository '%s' by user: '%s'", pathToWatch.toString(),
                            userName));
        }
    }

    /**
     * add un-watch response message
     *
     * @param userName            - user watching
     * @param artifactoryResponse - encapsulate data related to response
     * @param artifactWatchAddon  - watch addon
     * @param repoPath            - artifact / repository path
     * @param pathToWatch         - patch to un-watch
     */
    private void addUnwatchMessage(String userName, RestResponse artifactoryResponse,
            ArtifactWatchAddon artifactWatchAddon, RepoPath repoPath, RepoPath pathToWatch) {
        if (repoPath.getPath().length() > 0) {
            artifactWatchAddon.removeWatcher(pathToWatch, userName);
            artifactoryResponse.info(
                    String.format("Successfully removed watch on artifact '%s' by user: '%s'", pathToWatch.toString(),
                            userName));
        } else {
            artifactWatchAddon.removeWatcher(pathToWatch, userName);
            artifactoryResponse.info(
                    String.format("Successfully removed watch on repository '%s' by user: '%s'", pathToWatch.toString(),
                            userName));
        }
    }
}
