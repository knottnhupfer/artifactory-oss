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

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.logging.sumologic.SumoLogicException;
import org.artifactory.logging.sumologic.SumoLogicService;
import org.artifactory.rest.common.model.sumologic.SumoLogicModel;
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
public class RefreshSumoLogicTokenService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(RefreshSumoLogicTokenService.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private SumoLogicService sumoLogicService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String username = userGroupService.currentUser().getUsername();
            String accessToken = sumoLogicService.refreshToken(username);
            CentralConfigDescriptor configDescriptor = centralConfigService.getDescriptor();
            SumoLogicConfigDescriptor sumoLogicConfig = configDescriptor.getSumoLogicConfig();
            if (StringUtils.isNotBlank(sumoLogicConfig.getDashboardUrl()) && StringUtils.isNotBlank(accessToken)) {
                SumoLogicModel model = new SumoLogicModel();
                model.setDashboardUrl(sumoLogicConfig.getDashboardUrl() + "?access_token=" + accessToken);
                response.iModel(model);
            } else {
                log.error("Sumo Logic access token was refreshed for user: '{}' but is either missing or " +
                        "the dashboard URL is not configured ", username);
                response.iModel("Unable to refresh token").responseCode(500);
            }
        } catch (SumoLogicException e) {
            String msg = "Error refreshing token: " + e.getMessage();
            log.error(msg, e);
            response.iModel(msg).responseCode(e.getRelaxedStatus());
        } catch (Exception e) {
            String msg = "Error refreshing token: " + e.getMessage();
            log.error(msg, e);
            response.iModel(msg).responseCode(500);
        }
    }
}
