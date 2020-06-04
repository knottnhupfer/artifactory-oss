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
import org.artifactory.storage.db.servers.model.ArtifactoryServer;
import org.artifactory.ui.rest.model.admin.configuration.ha.HaModel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * THIS WAS REPLACED WITH GetServersStatusService FOR THE UNIFIED UI
 *
 * @author Chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetHighAvailabilityMembersService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetHighAvailabilityMembers");
        List<HaModel> artifactoryServers = getArtifactoryServers();
        response.iModelList(artifactoryServers);
    }

    /**
     * return artifactory server list if has HA license and configure
     * @return - list of artifactory servers
     */
    private List<HaModel> getArtifactoryServers() {
        List<HaModel> haModels = new ArrayList<>();
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        HaCommonAddon haCommonAddon = addonsManager.addonByType(HaCommonAddon.class);
        List<ArtifactoryServer> allArtifactoryServers = haCommonAddon.getAllArtifactoryServers();
        if (allArtifactoryServers != null && !allArtifactoryServers.isEmpty()) {
            allArtifactoryServers.forEach(server -> {
                boolean hasHeartbeat = haCommonAddon.artifactoryServerHasHeartbeat(server);
                // This hash means that no license is installed
                boolean hasLicense = !server.getLicenseKeyHash().equals(AddonsManager.NO_LICENSE_HASH);
                HaModel model = new HaModel(server, !hasHeartbeat, hasLicense);
                haModels.add(model);
            });
        }
        return haModels;
    }
}
