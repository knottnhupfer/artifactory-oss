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

package org.artifactory.ui.rest.service.admin.security.general;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.ArtifactoryEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EncryptDecryptService implements RestService {

    @Autowired
    private ArtifactoryEncryptionService artifactoryEncryptionService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String id = request.getPathParamByKey("action");
        if (id == null || id.length() == 0) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //encrypt / decrypt password
        encryptDecryptSecurityConfig(id,response);
    }

    /**
     * encrypt / decrypt security config by
     * @param id - encrypt or decrypt
     */
    private void encryptDecryptSecurityConfig(String id,RestResponse restResponse) {
        if (id.equals("encrypt")){
            artifactoryEncryptionService.encrypt();
            restResponse.info("All passwords in your configuration are currently encrypted.");
        }
        else{
            artifactoryEncryptionService.decrypt();
            restResponse.info("All passwords in your configuration are currently visible in plain text.");
        }
    }
}
