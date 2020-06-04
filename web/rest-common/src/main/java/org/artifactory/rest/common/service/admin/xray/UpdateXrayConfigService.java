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

package org.artifactory.rest.common.service.admin.xray;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.xray.XrayAuthState;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.rest.common.model.xray.XrayConfigModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.AuthenticationHelper;
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
public class UpdateXrayConfigService implements RestService<XrayConfigModel> {
    private static final Logger log = LoggerFactory.getLogger(UpdateXrayConfigService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private XrayAuthState xrayAuthState;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        if (!addonsManager.isXrayLicensed()) {
            response.error("Invalid xray license for Xray").responseCode(HttpStatus.SC_BAD_REQUEST);
        } else {
            // create xray Config
            if (!updateXrayConfig(request)) {
                response.error("Could not create Xray config - check Artifactory logs").responseCode(400);
            }
        }
    }

    /**
     * update xray config
     */
    private boolean updateXrayConfig(ArtifactoryRestRequest request) {
        String remoteAddress = AuthenticationHelper.getRemoteAddress(AuthenticationHelper.getAuthentication());
        log.info("Updating Xray config, request received from instance at: {}", remoteAddress);
        XrayConfigModel xrayModel = (XrayConfigModel) request.getImodel();
        boolean xrayAccessTokenAuthProviderEnabled = xrayAuthState.isXrayAccessTokenAuthProviderEnabled();
        if (xrayAccessTokenAuthProviderEnabled || xrayModel.validate(false)) {
            MutableCentralConfigDescriptor descriptor = configService.getMutableDescriptor();
            log.debug("Updating Xray config for instance {}", xrayModel.getXrayBaseUrl());
            XrayDescriptor xrayConfig = descriptor.getXrayConfig();
            updateXrayConfig(xrayConfig, xrayModel.toDescriptor());
            descriptor.setXrayConfig(xrayConfig);
            configService.saveEditedDescriptorAndReload(descriptor);
            return true;
        } else {
            log.debug("Invalid Xray config model received!");
            return false;
        }
    }

    private void updateXrayConfig(XrayDescriptor xrayConfig, XrayDescriptor newXrayConfig) {
        xrayConfig.setEnabled(newXrayConfig.isEnabled());
        xrayConfig.setXrayId(newXrayConfig.getXrayId());
        xrayConfig.setArtifactoryId(newXrayConfig.getArtifactoryId());
        xrayConfig.setBaseUrl(newXrayConfig.getBaseUrl());
        xrayConfig.setUser(newXrayConfig.getUser());
        if (StringUtils.isNotBlank(newXrayConfig.getPassword())) {
            xrayConfig.setPassword(newXrayConfig.getPassword());
        }
    }
}
