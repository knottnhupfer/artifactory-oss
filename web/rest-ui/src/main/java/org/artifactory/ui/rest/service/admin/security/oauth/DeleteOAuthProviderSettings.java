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

package org.artifactory.ui.rest.service.admin.security.oauth;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.oauth.OAuthProviderSettings;
import org.artifactory.descriptor.security.oauth.OAuthSettings;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gidi Shabat
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteOAuthProviderSettings implements RestService<String> {
    private static final Logger log = LoggerFactory.getLogger(DeleteOAuthProviderSettings.class);

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest<String> request, RestResponse response) {
        String providerName = request.getImodel();
        if(StringUtils.isBlank(providerName)){
            response.error("Couldn't delete provider, missing provider name");
            return;
        }
        log.debug("Deleting OAuth provider '{}'", providerName);
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        OAuthSettings oauthSettings = mutableDescriptor.getSecurity().getOauthSettings();
        if (oauthSettings != null) {
            // Remove provider to delete
            List<OAuthProviderSettings> providers = oauthSettings.getOauthProvidersSettings().stream().filter(
                    e -> !e.getName().equals(providerName)).collect(Collectors.toList());
            // Override providers
            oauthSettings.setOauthProvidersSettings(providers);
            centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
            userGroupService.deletePropertyFromAllUsers("authinfo." + providerName);
            response.info("Successfully deleting OAuth provider "+providerName);
            log.debug("Successfully deleting OAuth provider '{}'", providerName);
        } else {
            response.error("Couldn't delete OAuth provider, OAuth settings doesn't exist");
        }
    }
}