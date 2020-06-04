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

package org.artifactory.ui.rest.service.admin.security.permissions;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.ui.rest.model.admin.security.permissions.DeletePermissionTargetModel;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.http.HttpStatus.*;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeletePermissionsTargetService implements RestService<DeletePermissionTargetModel> {
    private static final Logger log = LoggerFactory.getLogger(DeletePermissionsTargetService.class);

    private RestSecurityRequestHandlerV2 securityRequestHandler;
    private AuthorizationService authService;

    @Autowired
    public DeletePermissionsTargetService(
            RestSecurityRequestHandlerV2 securityRequestHandler, AuthorizationService authService) {
        this.securityRequestHandler = securityRequestHandler;
        this.authService = authService;
    }

    @Override
    public void execute(ArtifactoryRestRequest<DeletePermissionTargetModel> request, RestResponse response) {
        if (!authService.isAdmin()) {
            response.responseCode(SC_FORBIDDEN);
            return;
        }
        DeletePermissionTargetModel deleteRequest = request.getImodel();
        List<String> errors = Lists.newArrayList();
        deleteRequest.getPermissionTargetNames()
                .stream()
                .filter(StringUtils::isNotBlank)
                .forEach(permissionName -> deleteAcl(permissionName, response, errors));
        prepareResponse(deleteRequest, response, errors);
    }

    private void prepareResponse(DeletePermissionTargetModel deleteRequest, RestResponse response, List<String> errors) {
        if (CollectionUtils.notNullOrEmpty(errors)) {
            response.errors(errors);
            if (errors.size() > 1) {
                //consolidate all error codes
                response.responseCode(SC_BAD_REQUEST);
            }
        } else if (deleteRequest.getPermissionTargetNames().size() > 1) {
            response.info("Successfully removed " + deleteRequest.getPermissionTargetNames().size() + " permission targets");
        } else if (deleteRequest.getPermissionTargetNames().size() == 1) {
            response.info("Successfully removed permission target '" + deleteRequest.getPermissionTargetNames().get(0) + "'");
        }
    }

    private void deleteAcl(String permissionName, RestResponse response, List<String> errors) {
        try {
            securityRequestHandler.deletePermissionTarget(permissionName);
        } catch (NotFoundException nfe) {
            errors.add(nfe.getMessage());
            response.responseCode(SC_NOT_FOUND);
            log.debug("", nfe);
        } catch (BadRequestException bre) {
            errors.add(bre.getMessage());
            response.responseCode(SC_BAD_REQUEST);
            log.debug("", bre);
        } catch (Exception e) {
            errors.add("Unexpected error deleting permission target '" + permissionName + "' : " + e.getMessage());
            log.debug("", e);
        }
    }
}
