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

package org.artifactory.ui.rest.service.admin.configuration.repositories.replication;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator.ReplicationConfigValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * Executes validation on the replication model - called by the save button in the local replication modal
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ValidateLocalReplicationService implements RestService<LocalReplicationConfigModel> {

    @Autowired
    private ReplicationConfigValidator replicationValidator;

    @Override
    public void execute(ArtifactoryRestRequest<LocalReplicationConfigModel> request, RestResponse response) {
        LocalReplicationConfigModel model = request.getImodel();
        if (model == null) {
            response.error("No repository configuration given to test replication with.").responseCode(SC_BAD_REQUEST);
            return;
        }
        try {
            model.setEnabled(true); //For the sake of validation mark model as enabled
            replicationValidator.validateLocalModels(Lists.newArrayList(model));
        } catch (RepoConfigException e) {
            response.error(e.getMessage()).responseCode(e.getStatusCode());
        }
    }
}