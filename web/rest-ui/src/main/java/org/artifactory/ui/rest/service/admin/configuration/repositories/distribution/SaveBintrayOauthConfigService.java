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

package org.artifactory.ui.rest.service.admin.configuration.repositories.distribution;



import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.distribution.BintrayOAuthAppConfigurator;
import org.artifactory.api.bintray.distribution.model.DistributionRepoCreationDetails;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DistRepoTypeSpecificConfigModel;
import org.artifactory.rest.common.exception.RepoConfigException;
import org.artifactory.util.AlreadyExistsException;
import org.jfrog.common.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SaveBintrayOauthConfigService implements RestService<DistRepoTypeSpecificConfigModel> {

    @Autowired
    private BintrayOAuthAppConfigurator oauthAppConfigurator;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DistRepoTypeSpecificConfigModel distModel = (DistRepoTypeSpecificConfigModel) request.getImodel();

        DistributionRepoCreationDetails creationDetails = null;
        try {
            creationDetails = createBintrayAppConfig(distModel);
            //Messed up response but no exception - shouldn't happen.
            if (!response.isFailed()
                    && (creationDetails == null || StringUtils.isBlank(creationDetails.oauthAppConfigKey))) {
                response.error("Failed to establish trust with Bintray, check logs for more information.")
                        .responseCode(SC_BAD_REQUEST);
            }
        } catch (RepoConfigException rce) {
            response.error(rce.getMessage()).responseCode(rce.getStatusCode());
        }
        if (response.isFailed() || creationDetails == null) {
            return;
        }
        //We just need to return the key of the newly created OAuth app
        DistRepoTypeSpecificConfigModel newModel = new DistRepoTypeSpecificConfigModel();
        newModel.setBintrayAppConfig(creationDetails.oauthAppConfigKey);
        newModel.setPremium(creationDetails.isOrgPremium);
        newModel.setAvailableLicenses(creationDetails.orgLicenses);
        newModel.setOrg(creationDetails.org);
        newModel.setClientId(creationDetails.clientId);
        response.iModel(newModel);
    }

    private DistributionRepoCreationDetails createBintrayAppConfig(DistRepoTypeSpecificConfigModel distModel)
            throws RepoConfigException {
        String[] clientIdAndSecret = validateDistributionRepoParams(distModel);
        try {
            return oauthAppConfigurator.createBintrayAppConfig(clientIdAndSecret[0], clientIdAndSecret[1],
                    distModel.getCode(), distModel.getScope(), distModel.getRedirectUrl());
        } catch (IOException ioe) {
            throw new RepoConfigException("Error executing OAuth token creation request: " + ioe.getMessage(),
                    SC_BAD_REQUEST, ioe);
        } catch (AlreadyExistsException aee) {
            throw new RepoConfigException(aee.getMessage(), SC_BAD_REQUEST);
        }
    }

    /**
     * @return the client id and secret returned from bintray in the {@param distModel}'s getBintrayAuthString()
     * @throws RepoConfigException
     */
    private String[] validateDistributionRepoParams(DistRepoTypeSpecificConfigModel distModel)
            throws RepoConfigException {
        if (StringUtils.isBlank(distModel.getBintrayAuthString())) {
            throw new RepoConfigException("Bintray authorization code provided is valid. Try to authorize again", SC_BAD_REQUEST);
        } else if (StringUtils.isBlank(distModel.getParamClientId())) {
            throw new RepoConfigException("The Bintray client id parameter is empty. Try to authorize again",
                    SC_BAD_REQUEST);
        } else if (StringUtils.isBlank(distModel.getCode())) {
            throw new RepoConfigException("The Bintray code parameter is empty. Try to authorize again",
                    SC_BAD_REQUEST);
        } else if (StringUtils.isBlank(distModel.getRedirectUrl())) {
            throw new RepoConfigException("The redirect url parameter is empty. Try to authorize again", SC_BAD_REQUEST);
        } else if (StringUtils.isBlank(distModel.getScope())) {
            throw new RepoConfigException("The Bintray scope parameter is empty. Try to authorize again",
                    SC_BAD_REQUEST);
        }

        String decodedAuthString = Base64.base64Decode(distModel.getBintrayAuthString());
        //Auth String is client_id:<client_id>:client_secret:<client_secret>
        String[] clientIdAndSecret = decodedAuthString.split(":");
        if (!(clientIdAndSecret.length == 2)) {
            throw new RepoConfigException("An invalid authentication string was pasted in the text box.", SC_BAD_REQUEST);
        } else if (!clientIdAndSecret[0].equals(distModel.getParamClientId())) {
            throw new RepoConfigException("There is a mismatch between the client ID you pasted in the box and the " +
                    "one returned from Bintray. This can pose a security risk.", SC_BAD_REQUEST);
        }
        return clientIdAndSecret;
    }
}
