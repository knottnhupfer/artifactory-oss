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

package org.artifactory.ui.rest.service.admin.configuration.ha;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RemoveServerService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(RemoveServerService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("RemoveServer");
        String serverID = request.getPathParamByKey("id");
        // remove server and update response
        removeServer(serverID, response);
    }

    /**
     * remove server from cluster if not responding
     *
     * @param id - server id
     */
    private void removeServer(String id, RestResponse response) {

        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        HaCommonAddon haAddon = addonsManager.addonByType(HaCommonAddon.class);

        try {
            boolean success = haAddon.deleteArtifactoryServer(id);
            if (success) {
                response.info(String.format("Successfully removed '%s'.", id));
            } else {
                response.warn(String.format("Unable to remove '%s'", id));
            }
        } catch (Exception e) {
            String errorMessage = String.format("Exception occurred while removing '%s'", id);
            response.error(errorMessage);
            log.error(errorMessage, e);
        }
    }
}
