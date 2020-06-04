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

package org.artifactory.rest.common.service.admin.advance.sumologic;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.rest.common.model.sumologic.SumoLogicModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.MaskedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateSumoLogicConfigService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(UpdateSumoLogicConfigService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        SumoLogicModel model = (SumoLogicModel) request.getImodel();

        MutableCentralConfigDescriptor centralConfig = centralConfigService.getMutableDescriptor();
        SumoLogicConfigDescriptor sumoLogicConfigDescriptor = centralConfig.getSumoLogicConfig();
        sumoLogicConfigDescriptor.setEnabled(model.getEnabled());
        sumoLogicConfigDescriptor.setProxy(centralConfig.getProxy(model.getProxy()));
        sumoLogicConfigDescriptor.setClientId(model.getClientId());
        sumoLogicConfigDescriptor.setSecret(model.getSecret());
        logSumoConfigInDebug(sumoLogicConfigDescriptor);
        centralConfig.setSumoLogicConfig(sumoLogicConfigDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(centralConfig);
    }

    private void logSumoConfigInDebug(SumoLogicConfigDescriptor sumoConfig) {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Enabled: ").append(sumoConfig.isEnabled()).append("\n");
            sb.append("Client ID: ").append(MaskedValue.of(sumoConfig.getClientId())).append("\n");
            sb.append("Secret: ").append(MaskedValue.of(sumoConfig.getSecret())).append("\n");
            sb.append("Dashboard URL: ").append(sumoConfig.getDashboardUrl()).append("\n");
            sb.append("Proxy: ").append(Optional.ofNullable(sumoConfig.getProxy()).map(ProxyDescriptor::getKey).orElse(null));
            log.debug("Saving SumoLogic configuration:\n" + sb.toString());
        }
    }
}
