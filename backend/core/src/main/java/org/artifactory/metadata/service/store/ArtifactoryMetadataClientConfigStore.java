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

package org.artifactory.metadata.service.store;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.client.token.TokenRequest;
import org.jfrog.metadata.client.MetadataClient;
import org.jfrog.metadata.client.MetadataClientBuilder;
import org.jfrog.metadata.client.confstore.MetadataClientConfigStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.jfrog.access.client.AccessClientBootstrap.SERVICE_ADMIN_TOKEN_EXPIRY;
import static org.jfrog.access.common.AccessAuthz.ADMIN;

/**
 * @author Uriah Levy
 */
public class ArtifactoryMetadataClientConfigStore implements MetadataClientConfigStore {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryMetadataClientConfigStore.class);
    private AccessService accessService;

    public ArtifactoryMetadataClientConfigStore(AccessService accessService) {
        this.accessService = requireNonNull(accessService, "access service is required");
    }

    @Nonnull
    @Override
    public MetadataClientBuilder authenticatedClientBuilder() {
        String serviceToken = createServiceToken();
        if (StringUtils.isBlank(serviceToken)) {
            throw new IllegalStateException("Unable to create the MDS access token");
        }
        return MetadataClientBuilder.newBuilder()
                .serverUrl(getMetadataServerUrl())
                .authToken(serviceToken)
                .connectionTimeout(getConnectionTimeout())
                .socketTimeout(getSocketTimeout())
                .maxConnections(getMaxConnections()); // also used for max pre-route
    }

    @Nonnull
    @Override
    public MetadataClientBuilder noAuthClientBuilder() {
        return MetadataClientBuilder.newBuilder()
                .serverUrl(getMetadataServerUrl())
                .connectionTimeout(getConnectionTimeout())
                .socketTimeout(getSocketTimeout())
                .maxConnections(getMaxConnections()); // also used for max pre-route
    }

    private String getMetadataServerUrl() {
        return ConstantValues.metadataClientServerUrlOverride.getString();
    }

    private int getMaxConnections() {
        return ConstantValues.metadataClientMaxConnections.getInt();
    }

    private int getSocketTimeout() {
        return ConstantValues.metadataClientSocketTimeout.getInt();
    }

    private int getConnectionTimeout() {
        return ConstantValues.metadataClientConnectionTimeout.getInt();
    }

    private String createServiceToken() {
        String mdsServiceId = getMdsServiceId();
        log.debug("Creating the Metadata Server access token for '{}'", mdsServiceId);
        TokenRequest.Builder tokenRequest = TokenRequest.builder();
        tokenRequest.nonRefreshable()
                .subject(accessService.getArtifactoryServiceId().getFormattedName())
                .scopes("applied-permissions/" + ADMIN)
                .audience(mdsServiceId)
                .expiresIn(SERVICE_ADMIN_TOKEN_EXPIRY);
        return accessService.getAccessClient().token().create(tokenRequest.build()).getTokenValue();
    }

    private String getMdsServiceId() {
        try (MetadataClient noAuthMetadataClient = noAuthClientBuilder().create()) {
            String serviceId = noAuthMetadataClient.system().getServiceId();
            if (StringUtils.isNotBlank(serviceId)) {
                return serviceId;
            }
        } catch (Exception e) {
            log.error("Unable to request the Metadata Service Service-Id");
            log.debug("", e);
        }
        throw new IllegalStateException("Unable to request the Metadata Service Service-Id");
    }
}
