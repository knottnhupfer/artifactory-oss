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

package org.artifactory.ui.rest.service.admin.security.accesstokens;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.access.AccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * @author Yinon Avraham
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RevokeAccessTokensService implements RestService {

    private static final Logger log = LoggerFactory.getLogger(RevokeAccessTokensService.class);

    @Autowired
    private AccessService accessService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        List<String> tokenIds = getTokenIds(request);
        if (tokenIds.isEmpty()) {
            response.responseCode(SC_BAD_REQUEST).error("token ids array is required");
            return;
        }
        int revokedSuccessfully = 0;
        int revokeErrors = 0;
        for (String tokenId : tokenIds) {
            try {
                accessService.revokeTokenById(tokenId);
                revokedSuccessfully++;
            } catch (Exception e) {
                revokeErrors++;
                log.warn("Failed to revoke token by ID {}: {}", tokenId, e.toString());
                log.debug("Failed to revoke token by ID {}", tokenId, e);
            }
        }
        if (revokeErrors > 0) {
            response.responseCode(SC_BAD_REQUEST).error(revokeErrors + " tokens failed to revoke.");
        }
        if (revokedSuccessfully > 0) {
            response.info(revokedSuccessfully + " tokens revoked successfully.");
        }
    }

    private List<String> getTokenIds(ArtifactoryRestRequest request) {
        List<String> tokenIds = Lists.newArrayList();
        try {
            for (Object value : request.getModels()) {
                tokenIds.add((String) value);
            }
        } catch (Exception e) {
            log.warn("Could not get token IDs from request body: {}", e.toString());
            log.debug("Could not get token IDs from request body: {}", request.getImodel(), e);
        }
        return tokenIds;
    }
}
