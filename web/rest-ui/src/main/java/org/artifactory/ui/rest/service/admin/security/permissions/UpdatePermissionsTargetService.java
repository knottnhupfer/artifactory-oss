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

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.CombinedPermissionTargetUIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.security.PermissionTargetNaming.NAMING_UI;
import static org.artifactory.ui.rest.service.admin.security.permissions.util.UIPermissionServiceUtil.backendModelFromUIModel;


/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdatePermissionsTargetService implements RestService<CombinedPermissionTargetUIModel> {
    private static final Logger log = LoggerFactory.getLogger(UpdatePermissionsTargetService.class);

    @Autowired
    private RestSecurityRequestHandlerV2 securityRequestHandler;

    @Override
    public void execute(ArtifactoryRestRequest<CombinedPermissionTargetUIModel> request, RestResponse response) {
        PermissionTargetModel backendModel = backendModelFromUIModel(request);
        String permissionName = backendModel.getName();
        try {
            //The underlying implementation protects against calling this without manage permissions.
            securityRequestHandler.updatePermissionTarget(permissionName, backendModel, NAMING_UI);
        } catch (ForbiddenWebAppException fbe) {
            response.error(fbe.getMessage());
            response.responseCode(SC_FORBIDDEN);
        } catch (NotFoundException nfe) {
            response.error(nfe.getMessage());
            response.responseCode(SC_NOT_FOUND);
        } catch (BadRequestException bre) {
            response.error(bre.getMessage());
            response.responseCode(SC_BAD_REQUEST);
        } catch (Exception e) {
            response.error("An unexpected error has occurred please review the logs: " + e.getMessage());
            log.error("Failed to create Permission target '" + permissionName + "': ", e);
        }
    }
}
