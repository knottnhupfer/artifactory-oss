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

package org.artifactory.ui.rest.service.setmeup;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.ReverseProxyDescriptor;
import org.artifactory.descriptor.repo.ReverseProxyMethod;
import org.artifactory.descriptor.repo.ReverseProxyRepoConfig;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.setmeup.ReverseProxySetMeUpDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Danny Reiser
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReverseProxySetMeUpDataService implements RestService {

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoKey = request.getQueryParamByKey("repoKey");
        ReverseProxySetMeUpDataModel reverseProxySetMeUpDataModel= new ReverseProxySetMeUpDataModel();

        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        ReverseProxyDescriptor reverseProxy = centralConfig.getMutableDescriptor().getCurrentReverseProxy();
        if (!authorizationService.userHasPermissionsOnRepositoryRoot(repoKey)) {
            response.error("User has no permission on " + repoKey);
        }
        else if (reverseProxy != null) {
            reverseProxySetMeUpDataModel.setUsingHttps(reverseProxy.isUseHttps());
            reverseProxySetMeUpDataModel.setMethodSelected(reverseProxy.getDockerReverseProxyMethod() != ReverseProxyMethod.NOVALUE);
            reverseProxySetMeUpDataModel.setServerName(reverseProxy.getServerName());
            if (reverseProxy.getDockerReverseProxyMethod() == ReverseProxyMethod.PORTPERREPO) {
                reverseProxySetMeUpDataModel.setUsingPorts(true);
                ReverseProxyRepoConfig perRepo = reverseProxy.getReverseProxyRepoConfig(repoKey);
                if (perRepo != null) {
                    reverseProxySetMeUpDataModel.setRepoPort(perRepo.getPort());
                }
            }
            else {
                reverseProxySetMeUpDataModel.setUsingPorts(false);
            }
            response.iModel(reverseProxySetMeUpDataModel);
        }

    }
}
