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

package org.artifactory.rest.common.service.trash;

import org.apache.http.HttpStatus;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.common.ConstantValues;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.rest.common.model.trash.RestoreArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Restore an artifact from the trashcan to it's original repository (or to the given destination)
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestoreArtifactService implements RestService {

    @Autowired
    TrashService trashService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RestoreArtifact restoreArtifact = (RestoreArtifact) request.getImodel();
        String repoKey = restoreArtifact.getRepoKey();
        String path = restoreArtifact.getPath();
        int transactionSize=restoreArtifact.getTransactionSize()!=null?restoreArtifact.getTransactionSize():
                ConstantValues.moveCopyDefaultTransactionSize.getInt();
        RepoPath source = InternalRepoPathFactory.create(repoKey, path);
        MoveMultiStatusHolder status = trashService.restoreBulk(source, restoreArtifact.getTargetRepoKey(),
                restoreArtifact.getTargetPath(),transactionSize);
        if (request.isUiRestCall()) {
            uiResponse(status, response);
        } else {
            apiResponse(status, response);
        }
    }

    private void uiResponse(MoveMultiStatusHolder status, RestResponse response) {
        if (status.isError()) {
            response.error(status.getLastError().getMessage());
        } else if (status.hasWarnings()) {
            response.warn(status.getLastWarning().getMessage());
        } else {
            response.info("Successfully restored trash items");
        }
    }

    private void apiResponse(MoveMultiStatusHolder statusHolder, RestResponse response) {
        if (statusHolder.isError()) {
            throw new BadRequestException(statusHolder.getLastError().getMessage());
        } else if (statusHolder.hasWarnings()) {
            throw new BadRequestException(statusHolder.getLastWarning().getMessage());
        } else {
            response.iModel("Successfully restored trash items");
            response.responseCode(HttpStatus.SC_ACCEPTED);
        }
    }
}
