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
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateSumoLogicProxyService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(UpdateSumoLogicProxyService.class);

    @Autowired
    private CentralConfigService centralConfig;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String proxy = request.getQueryParamByKey("proxy");
            MutableCentralConfigDescriptor mutableDescriptor = centralConfig.getMutableDescriptor();
            SumoLogicConfigDescriptor sumoLogicConfig = mutableDescriptor.getSumoLogicConfig();
            sumoLogicConfig.setProxy(centralConfig.getDescriptor().getProxy(proxy));
            centralConfig.saveEditedDescriptorAndReload(mutableDescriptor);
        } catch (Exception e) {
            String msg = "Error updating proxy: " + e.getMessage();
            log.error(msg, e);
            response.iModel(msg).responseCode(500);
        }
    }
}
