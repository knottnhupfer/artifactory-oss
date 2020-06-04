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

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.CopyArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CopyArtifactService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(CopyArtifactService.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CopyArtifact copyArtifact = (CopyArtifact) request.getImodel();
        // copy artifact to another repo
        RepoPath repoPath = RepoPathFactory.create(copyArtifact.getRepoKey(), copyArtifact.getPath());
        // check if there's read permissions on source repo. deploy permission on target is checked later in deploy stages
        if (!authorizationService.canRead(repoPath)) {
            response.responseCode(HttpServletResponse.SC_FORBIDDEN).buildResponse();
            log.error("Forbidden UI REST call from user: '{}'", authorizationService.currentUsername());
            return;
        }
        copyArtifact(response, copyArtifact);

    }

    /**
     * copy artifact from one repo to another
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param copyArtifact        - copy action model
     */
    private void copyArtifact(RestResponse artifactoryResponse, CopyArtifact copyArtifact) {
        // copy artifact
        MoveMultiStatusHolder copyStatus = copy(copyArtifact);
        // update response data
        updateResponseData(artifactoryResponse, copyArtifact, copyStatus);

    }

    /**
     * update response feedback and http status code
     *
     * @param artifactoryResponse - encapsulate data require for reposne
     * @param copyArtifact        - copy action model
     * @param copyStatus          - copy status holder
     */
    private void updateResponseData(RestResponse artifactoryResponse, CopyArtifact copyArtifact,
            MoveMultiStatusHolder copyStatus) {
        try {
            if (copyStatus.hasErrors()) {
                artifactoryResponse.responseCode(HttpServletResponse.SC_CONFLICT);
                List<String> errors = new ArrayList<>();
                copyStatus.getErrors().forEach(error -> errors.add(error.getMessage()));
                artifactoryResponse.errors(errors);
            } else {
                artifactoryResponse.info(
                        "Artifacts successfully copied to: " + copyArtifact.getTargetRepoKey() + ":" +
                                copyArtifact.getTargetPath());
            }
        } catch (Exception e) {
            artifactoryResponse.responseCode(HttpServletResponse.SC_CONFLICT);
            List<String> errors = new ArrayList<>();
            copyStatus.getErrors().forEach(error -> errors.add(error.getMessage()));
            artifactoryResponse.errors(errors);
        }
    }

    /**
     * copy artifact from one repo path to another repo path
     *
     * @param copyArtifact - copy action model
     * @return - copy multi status holder
     */
    private MoveMultiStatusHolder copy(CopyArtifact copyArtifact) {

        MoveMultiStatusHolder status;
        RepoPath sourcePath = repositoryService
                .getItemInfo(InternalRepoPathFactory.create(copyArtifact.getRepoKey(), copyArtifact.getPath()))
                .getRepoPath();
        RepoPath targetPath = InternalRepoPathFactory.create(copyArtifact.getTargetRepoKey(),
                copyArtifact.getTargetPath(), sourcePath.isFolder());
        // Force suppressing layouts, we want to be able to copy stuff between layouts
        copyArtifact.setSuppressLayouts(true);
        status = repositoryService.copyMultiTx(sourcePath, targetPath, copyArtifact.isDryRun(),
                copyArtifact.isSuppressLayouts(), copyArtifact.isFailFast());
        if (!status.isError() && !status.hasWarnings()) {
            String opType = (copyArtifact.isDryRun()) ? "Dry run for " : "";
            status.status(
                    String.format("%s copying %s to %s completed successfully, %s artifacts and %s folders were " +
                                    "copied", opType, sourcePath, targetPath, status.getMovedArtifactsCount(),
                            status.getMovedFoldersCount()), log);
        }
        return status;
    }
}
