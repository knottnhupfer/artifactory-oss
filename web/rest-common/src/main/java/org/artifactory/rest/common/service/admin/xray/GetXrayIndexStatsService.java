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
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.exception.UnsupportedOperationException;
import org.artifactory.rest.common.model.xray.XrayRepoIndexStatsModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.security.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Yinon Avraham
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetXrayIndexStatsService implements RestService<Void> {
    private static final Logger log = LoggerFactory.getLogger(GetXrayIndexStatsService.class);

    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String remoteAddress = AuthenticationHelper.getRemoteAddress(AuthenticationHelper.getAuthentication());
        String err = "Failing Xray index request received from " + remoteAddress + ": ";
        XrayAddon xrayAddon = addonsManager.addonByType(XrayAddon.class);
        if (!xrayAddon.isXrayEnabled()) {
            String disabledErr = "Xray is disabled or not supported by this instance's license.";
            response.error(disabledErr).responseCode(400);
            log.debug(err + disabledErr);
            return;
        }

        String repokey = request.getPathParamByKey("repoKey");
        if (StringUtils.isBlank(repokey)) {
            throw new BadRequestException("Repository key cannot be empty");
        }
        fetchXrayIndexStats(xrayAddon, repokey, response);
    }

    private void fetchXrayIndexStats(XrayAddon xrayAddon, String repoKey, RestResponse response) {
        try {
            int potential = xrayAddon.getXrayPotentialCountForRepo(repoKey);
            response.iModel(new XrayRepoIndexStatsModel(potential));
        } catch (UnsupportedOperationException e) {
            String message = "An error occurred while retrieving xray index statistics for repository: " +
                    "'" + repoKey + "'. " + e.getMessage();
            log.error(message);
            throw new BadRequestException(message);
        } catch (Exception e) {
            String message = "An error occurred while retrieving xray index statistics for repository: '" + repoKey + "'";
            log.error(message);
            log.debug(message, e);
            response.error(e.getMessage());
            response.responseCode(500);
        }
    }
}