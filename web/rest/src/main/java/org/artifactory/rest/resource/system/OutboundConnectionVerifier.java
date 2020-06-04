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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.util.HttpClientConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

/**
 * Connection verifier for two way handshake with other services.
 *
 * Note: If this ever grow to be a commonly used ping endpoint consider making it a service so we don't re-create the
 * client each time.
 *
 * @author Yuval Reches
 */
public class OutboundConnectionVerifier {
    private static final Logger log = LoggerFactory.getLogger(OutboundConnectionVerifier.class);

    public Response verify(VerifyConnectionModel verifyConnection) throws Exception {
        String endpoint = verifyConnection.getEndpoint();
        assertUrlIsValid(endpoint);
        HttpGet get = new HttpGet(endpoint);
        setRequestCredentials(verifyConnection, get);
        try (CloseableHttpClient client = createHttpClient()) {
            try (CloseableHttpResponse response = client.execute(get)) {
                log.info("Verifying connection to: " + endpoint);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    return handleError(endpoint, response, statusCode);
                }
            } catch (IOException e) {
                log.debug("Error verifying connection to endpoint " + endpoint, e);
                throw new BadRequestException("Could not verify connection to endpoint: " + endpoint + " --> " + e.getCause());
            }
        }
        return Response.ok().entity("Successfully connected to endpoint").build();
    }

    private void assertUrlIsValid(String endpoint) {
        if (endpoint == null) {
            throw new BadRequestException("Endpoint is required to verify connection.");
        }
        try {
            new URL(endpoint);
        } catch (MalformedURLException e) {
            log.debug("Unable to validate URL." + (e.getMessage() != null ? " " + e.getMessage() : ""));
            throw new BadRequestException("Cannot parse the url " + endpoint);
        }
    }

    private Response handleError(String endpoint, CloseableHttpResponse response, int statusCode) throws IOException {
        String responseString = null;
        if (response.getEntity() != null) {
            try (InputStream responseStream = response.getEntity().getContent()) {
                responseString = IOUtils.toString(responseStream);
            }
        }
        throw new BadRequestException("Received error from endpoint url: " + endpoint + " --> " + statusCode
                + (responseString != null ? (": " + responseString) : ""));
    }

    private void setRequestCredentials(VerifyConnectionModel verifyConnection, HttpGet get) {
        String username = verifyConnection.getUsername();
        String password = verifyConnection.getPassword();
        if (username != null && password != null) {
            String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            get.setHeader("Authorization", "Basic " + encoding);
        }
    }

    private CloseableHttpClient createHttpClient() {
        CentralConfigDescriptor descriptor = ContextHelper.get().beanForType(CentralConfigService.class)
                .getDescriptor();
        return new HttpClientConfigurator()
                .proxy(descriptor != null ? descriptor.getDefaultProxy() : null)
                .socketTimeout(15000)
                .connectionTimeout(2000)
                .noRetry()
                .maxTotalConnections(30)
                .maxConnectionsPerRoute(30)
                .build();
    }
}
