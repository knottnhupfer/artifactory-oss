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

package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.crowd.CrowdAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestCrowdConnectionService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(TestCrowdConnectionService.class);

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        CrowdSettings crowdSettings = (CrowdSettings) request.getImodel();
        // test crowd connection
        testCrowdConnection(response, crowdSettings);
    }

    /**
     * test crowd connection to server
     * @param artifactoryResponse - encapsulate data require to response
     * @param crowdSettings       - crowd settings
     */
    private void testCrowdConnection(RestResponse artifactoryResponse, CrowdSettings crowdSettings) {
        crowdSettings.setPassword(CryptoHelper.encryptIfNeeded(ArtifactoryHome.get(), crowdSettings.getPassword()));
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CrowdAddon crowdAddon = addonsManager.addonByType(CrowdAddon.class);
        try {
            if (crowdSettings.isEnableIntegration()) {
                crowdAddon.testCrowdConnection(crowdSettings);
                artifactoryResponse.info("Successfully connected to Crowd / Jira users management server");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("java.net.UnknownHostException")) {
                artifactoryResponse.error("Could not resolve Crowd / Jira server URL: \"" + crowdSettings.getServerUrl() +
                        "\". Verify that the host is available");
                log.error("Could not resolve Crowd / Jira server URL address of {}. Verify that the host is available",
                        crowdSettings.getServerUrl(), e);
            }
            else {
                artifactoryResponse.error("An error occurred while testing the settings. View logs for more details");
                log.error("An error occurred while testing the new Crowd / Jira settings", e);
            }
        }
    }
}
