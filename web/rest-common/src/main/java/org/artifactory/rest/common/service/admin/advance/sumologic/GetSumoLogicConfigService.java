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

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.sumologic.SumoLogicConfigDescriptor;
import org.artifactory.logging.sumologic.SumoLogicService;
import org.artifactory.rest.common.model.sumologic.SumoLogicModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSumoLogicConfigService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private SumoLogicService sumoLogicService;

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        SumoLogicModel model = getSumoLogicModel(request);
        response.iModel(model);
    }

    SumoLogicModel getSumoLogicModel(ArtifactoryRestRequest request) {
        CentralConfigDescriptor configDescriptor = centralConfigService.getDescriptor();
        SumoLogicConfigDescriptor sumoLogicConfig = configDescriptor.getSumoLogicConfig();
        String username = userGroupService.currentUser().getUsername();
        String accessToken = sumoLogicService.getAccessToken(username);
        SumoLogicModel model = new SumoLogicModel();
        String redirectUrl = Joiner.on('/').join(
                HttpUtils.getServletContextUrl(request.getServletRequest()), "ui/sumologic/auth_callback", username);
        model.setRedirectUrl(redirectUrl);
        model.setEnabled(sumoLogicConfig.isEnabled());
        if (sumoLogicConfig.getProxy() != null) {
            model.setProxy(sumoLogicConfig.getProxy().getKey());
        }
        model.setClientId(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), sumoLogicConfig.getClientId()));
        model.setSecret(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), sumoLogicConfig.getSecret()));
        model.setEmail(userGroupService.findUser(username).getEmail());
        if (StringUtils.isNotBlank(sumoLogicConfig.getDashboardUrl()) && StringUtils.isNotBlank(accessToken)) {
            model.setDashboardUrl(sumoLogicConfig.getDashboardUrl() + "?access_token=" + accessToken);
        }
        model.setLicenseType(getLicenseType());
        return model;
    }

    private int getLicenseType() {
        if (addonsManager instanceof OssAddonsManager || addonsManager.getProAndAolLicenseDetails().getType().equals("Trial")) {
            return 0;
        }
        return 1;
    }
}
