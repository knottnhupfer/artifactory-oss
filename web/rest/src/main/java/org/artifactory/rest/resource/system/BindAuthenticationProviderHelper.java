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
package org.artifactory.rest.resource.system;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.api.security.access.TokenSpec;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.rest.resource.token.TokenResponseModel;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.common.ServiceType;
import org.jfrog.security.file.PemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Dudi Morad
 */
public class BindAuthenticationProviderHelper {
    private static final Logger log = LoggerFactory.getLogger(BindAuthenticationProviderHelper.class);

    void validateUserInput(AuthenticationProviderInfo authenticationProviderInfo) {
        if (authenticationProviderInfo == null || StringUtils.isBlank(authenticationProviderInfo.getRegistryId())) {
            throw new IllegalArgumentException("Invalid value for registry_id attribute, Cannot be null");
        }
        ServiceId serviceId = ServiceId.fromFormattedName(authenticationProviderInfo.getRegistryId());
        if (StringUtils.isBlank(serviceId.getInstanceId()) || StringUtils.isBlank(serviceId.getServiceType())) {
            throw new IllegalArgumentException("Invalid value for registry_id attribute");
        }
        if (authenticationProviderInfo.getType() == null ||
                (!authenticationProviderInfo.getType().equals(ServiceType.ARTIFACTORY) &&
                        !authenticationProviderInfo.getType().equals(ServiceType.ACCESS))) {
            throw new IllegalArgumentException(
                    "Invalid value for type attribute, expected " + ServiceType.ARTIFACTORY + "/" + ServiceType.ACCESS);
        }
        try {
            new URL(authenticationProviderInfo.getUrl());
            PemHelper.readCertificate(authenticationProviderInfo.getCertificate());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid value for url attribute");
        } catch (Exception e2) {
            log.debug("Invalid value for certificate attribute", e2);
            throw new IllegalArgumentException("Invalid value for certificate attribute");
        }
    }

    public CreatedTokenInfo createToken(String registryServiceId) {
        AccessService accessService = ContextHelper.get().beanForType(AccessService.class);
        ServiceId artifactoryServiceId = accessService.getArtifactoryServiceId();
        TokenSpec tokenSpec = UserTokenSpec.create(registryServiceId)
                .scope(Arrays.asList(artifactoryServiceId.getFormattedName() + ":admin"))
                .expiresIn(0L)
                .refreshable(true)
                .audience(Arrays.asList(artifactoryServiceId.getFormattedName()));
        return accessService.createToken(tokenSpec);
    }

    TokenResponseModel toTokenResponseModel(CreatedTokenInfo createdTokenInfo) {
        return TokenResponseModel.builder().expiresIn(createdTokenInfo.getExpiresIn())
                .refreshToken(createdTokenInfo.getRefreshToken())
                .scope(createdTokenInfo.getScope())
                .accessToken(createdTokenInfo.getTokenValue())
                .tokenType(createdTokenInfo.getTokenType()).build();
    }
}
