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

package org.artifactory.ui.rest.service.admin.advanced.configDescriptor;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.security.AccessLogger;
import org.artifactory.ui.rest.model.admin.advanced.configdescriptor.ConfigDescriptorModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateConfigDescriptorService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateConfigDescriptorService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("UpdateConfigDescriptor");
        /// save updated config descriptor
        updateConfigXml(request, response);
    }

    /**
     * save updated config xml and update feedback response
     *
     * @param artifactoryRequest  encapsulate data related to request
     * @param artifactoryResponse - encapsulate data related to response
     */
    private void updateConfigXml(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        String configXml = ((ConfigDescriptorModel) artifactoryRequest.getImodel()).getConfigXml();
        if (StringUtils.isEmpty(configXml)) {
            artifactoryResponse.error("Cannot save null or empty central configuration");
        } else if (ContextHelper.get().isOffline()) {
            artifactoryResponse.error("Cannot save config descriptor during offline state");
        } else {
            try {
                centralConfigService.setConfigXml(configXml);
                AccessLogger.configurationChanged();
                artifactoryResponse.info("Central configuration successfully saved");
            } catch (Exception e) {
                log.error("Error while manually saving the central configuration.", e);
                artifactoryResponse.error("Unable to save configuration, Please verify the validity of your input");
            }
        }
    }
}
