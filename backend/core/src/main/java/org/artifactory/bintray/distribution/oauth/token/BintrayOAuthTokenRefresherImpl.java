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

package org.artifactory.bintray.distribution.oauth.token;

import com.google.common.collect.Lists;
import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.handle.Bintray;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.distribution.BintrayOAuthTokenRefresher;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.bintray.BintrayTokenResponse;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.crypto.CryptoHelper;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.BintrayApplicationConfig;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.artifactory.bintray.distribution.util.DistributionUtils.getFormEncodedHeader;

/**
 * Queries Bintray for an OAuth token and saves the response refreshToken into the config descriptor if required.
 *
 * @author Dan Feldman
 * @author Shay Yaakov
 */
@Component
public class BintrayOAuthTokenRefresherImpl implements BintrayOAuthTokenRefresher {
    private static final Logger log = LoggerFactory.getLogger(BintrayOAuthTokenRefresher.class);

    @Autowired
    CentralConfigService configService;

    @Autowired
    RepositoryService repoService;

    @Autowired
    BintrayService bintrayService;

    @Override
    public String refreshBintrayOAuthAppToken(String repoKey) throws BintrayCallException {
        DistributionRepoDescriptor distRepoDescriptor = repoService.distributionRepoDescriptorByKey(repoKey);
        if (distRepoDescriptor == null || distRepoDescriptor.getBintrayApplication() == null) {
            String err = "Repository " + repoKey + " does not have a Bintray OAuth Application config attached to it.";
            log.debug(err);
            throw new BintrayCallException(HttpStatus.SC_BAD_REQUEST, "", err);
        }
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        BintrayApplicationConfig config = getBintrayApplicationConfig(distRepoDescriptor, configDescriptor);
        BintrayTokenResponse tokenResponse = executeRefreshTokenRequest(config, distRepoDescriptor.getProxy());
        if (tokenResponse == null || !tokenResponse.isValid()) {
            String response = tokenResponse == null ? "empty" : tokenResponse.toString();
            throw new BintrayCallException(HttpStatus.SC_CONFLICT, "Failed to refresh and acquire token from Bintray",
                    "response is invalid: " + response);
        } else if (!config.getRefreshToken().equalsIgnoreCase(tokenResponse.refreshToken)) {
            config.setRefreshToken(tokenResponse.refreshToken);
            //Save descriptor with new Bintray app config only if refresh token has changed
            configService.saveEditedDescriptorAndReload(configDescriptor);
        }
        return Base64.encodeBase64String(tokenResponse.token.getBytes());
    }

    private BintrayTokenResponse executeRefreshTokenRequest(final BintrayApplicationConfig config,
            final ProxyDescriptor proxy) throws BintrayCallException {
        try (Bintray client = bintrayService.createBasicAuthBintrayClient(config.getClientId(), config.getSecret(),
                proxy, false)) {
            HttpResponse response = client.post("oauth/token", getFormEncodedHeader(),
                    getTokenRefreshRequestFormParams(config));
            return JacksonReader.streamAsClass(response.getEntity().getContent(), BintrayTokenResponse.class);
        } catch (BintrayCallException bce) {
            throw bce;
        } catch (IOException ioe) {
            String err = "Error executing refresh token request: ";
            log.debug(err, ioe);
            //IO can either be problem with streams or failure http return code
            throw new BintrayCallException(HttpStatus.SC_BAD_REQUEST, err, ioe.getMessage());
        }
    }

    //params -> grant_type = refresh_token / client_id / refresh_token / scope
    private InputStream getTokenRefreshRequestFormParams(BintrayApplicationConfig config) {
        List<BasicNameValuePair> params = Lists.newArrayList(new BasicNameValuePair("grant_type", "refresh_token"),
                new BasicNameValuePair("client_id", config.getClientId()),
                new BasicNameValuePair("refresh_token", config.getRefreshToken()),
                new BasicNameValuePair("scope", config.getScope()));
        return IOUtils.toInputStream(URLEncodedUtils.format(params, "UTF-8"));
    }

    private BintrayApplicationConfig getBintrayApplicationConfig(DistributionRepoDescriptor descriptor,
            MutableCentralConfigDescriptor configDescriptor) throws BintrayCallException {
        String bintrayAppKey = descriptor.getBintrayApplication().getKey();
        BintrayApplicationConfig config = configDescriptor.getBintrayApplication(bintrayAppKey);
        if (config == null) {
            throw new BintrayCallException(HttpStatus.SC_NOT_FOUND, "Bintray Application config " + bintrayAppKey
                    + " not found.", "");
        }
        decryptConfigIfNeeded(config);
        return config;
    }

    private void decryptConfigIfNeeded(BintrayApplicationConfig config) {
        config.setClientId(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), config.getClientId()));
        config.setSecret(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), config.getSecret()));
        config.setRefreshToken(CryptoHelper.decryptIfNeeded(ArtifactoryHome.get(), config.getRefreshToken()));
    }
}
