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

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.CombinedPermissionTargetUIModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.security.PermissionTargetNaming.NAMING_UI;

/**
 * Used in specific Permission page
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetPermissionsTargetService implements RestService {

    @Autowired
    private RestSecurityRequestHandlerV2 securityRequestHandler;

    @Autowired
    private CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String permissionTargetName = request.getPathParamByKey("name");
            getPermissionTarget(response, permissionTargetName);
        } catch (ForbiddenWebAppException nope) {
            response.error(nope.getMessage()).responseCode(SC_FORBIDDEN);
        } catch (NotFoundException notThere) {
            response.error(notThere.getMessage()).responseCode(SC_NOT_FOUND);
        } catch (Exception wtf) {
            response.error(wtf.getMessage()).responseCode(SC_BAD_REQUEST);
        }
    }

    /**
     * Populates the model returned in {@param response} with the permission target {@param permissionName}
     */
    private void getPermissionTarget(RestResponse response, String permissionName) {
        PermissionTargetModel backendModel = securityRequestHandler.getPermissionTarget(permissionName, NAMING_UI);
        if (backendModel == null) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND).error("Permission target '" + permissionName + "' not found!");
            return;
        }
        boolean buildGlobalBasicReadAllowed = configService.getDescriptor().getSecurity()
                .isBuildGlobalBasicReadAllowed();
        CombinedPermissionTargetUIModel uiModel = new CombinedPermissionTargetUIModel(backendModel, buildGlobalBasicReadAllowed);
        response.iModel(uiModel);
    }
}
