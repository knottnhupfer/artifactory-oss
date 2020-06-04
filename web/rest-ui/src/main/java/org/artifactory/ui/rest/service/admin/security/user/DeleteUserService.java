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

package org.artifactory.ui.rest.service.admin.security.user;

import org.apache.commons.lang3.StringUtils;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.user.DeleteUsersModel;
import org.jfrog.access.client.AccessClientHttpException;
import org.jfrog.access.client.model.ErrorsModel;
import org.jfrog.access.client.model.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteUserService<T extends DeleteUsersModel> implements RestService<T> {
    @Autowired
    protected AuthorizationService authorizationService;
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        List<String> userNames = model.getUserNames();
        for (String userName : userNames) {
            if (StringUtils.isBlank(userName)) {
                response.responseCode(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (userTryToDeleteItsOwnUser(userName)) {
                setForbiddenResponseWithMessage(response, "You are logged-in as the user you have selected for removal");
                return;
            }
            try {
                userGroupService.deleteUser(userName);
            } catch (AccessClientHttpException e) {
                setForbiddenResponseWithMessage(response, extractMessage(e));
                return;
            }
        }
        if (model.getUserNames().size() > 1) {
            response.info("Successfully removed " + model.getUserNames().size() + " users");
        } else if (model.getUserNames().size() == 1) {
            response.info("Successfully removed user '" + model.getUserNames().get(0) + "'");
        }
    }

    /**
     * check if user Try To Delete Its Own User
     *
     * @param selectedUsername - user requested to be deleted
     * @return true if user Try To Delete Its Own User
     */
    private boolean userTryToDeleteItsOwnUser(String selectedUsername) {
        String currentUsername = authorizationService.currentUsername();
        return currentUsername.equals(selectedUsername);
    }

    private void setForbiddenResponseWithMessage(RestResponse restResponse, String message) {
        restResponse.responseCode(HttpServletResponse.SC_FORBIDDEN);
        restResponse.error("Action cancelled. " + message);
    }

    private String extractMessage(AccessClientHttpException e) {
        ErrorsModel errorsModel = e.getErrorsModel();
        if (errorsModel != null) {
            List<MessageModel> errors = errorsModel.getErrors();
            if (!errors.isEmpty()) {
                return errors.get(0).getMessage();
            }
        }
        return "";
    }
}
