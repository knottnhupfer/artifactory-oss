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

package org.artifactory.bintray.distribution.oauth.app;

import com.google.common.collect.Lists;
import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.handle.Bintray;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.distribution.BintrayOAuthAppConfigurator;
import org.artifactory.api.bintray.distribution.model.DistributionRepoCreationDetails;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.bintray.BintrayTokenResponse;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.BintrayApplicationConfig;
import org.artifactory.repo.DistributionRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.util.AlreadyExistsException;
import org.artifactory.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.artifactory.bintray.distribution.util.DistributionUtils.*;

/**
 * Provides all Bintray OAuth Application actions - creating one and performing auxiliary calls for the repo wizard
 *
 * @author Dan Feldman
 */
@Component
public class BintrayOAuthAppConfiguratorImpl implements BintrayOAuthAppConfigurator {
    private static final Logger log = LoggerFactory.getLogger(BintrayOAuthAppConfiguratorImpl.class);

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private BintrayService bintrayService;

    @Autowired
    private CentralConfigService configService;

    @Autowired
    private InternalRepositoryService repoService;

    private static final String OSS_LICENSES_ENDPOINT = "licenses/oss_licenses";
    private static final String PLAN_ENDPOINT = "orgs/%s/plan";
    private static final String OAUTH_TOKEN_ENDPOINT = "oauth/token";

    public DistributionRepoCreationDetails createBintrayAppConfig(String clientId, String secret, String code,
            String scope, String redirectUrl) throws IOException {
        String org = scope.split(":")[1];
        //In order to persist the oauth app before the repo is created we have to randomly generate some ID to be
        //used as the oauth config's key appended to the org. timestamp seems like a good bet.
        String appConfigKey = org + "-" + System.currentTimeMillis();
        BintrayApplicationConfig config = new BintrayApplicationConfig(appConfigKey, clientId, secret, org, scope);
        try (Bintray client = bintrayService.createBlankBintrayClient()) {
            BintrayTokenResponse tokenResponse = getBintrayTokenResponse(code, redirectUrl, client, config);
            log.debug("Adding Bintray Application OAuth config: {}", config.getKey());
            saveNewBintrayAppConfig(config);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + new String(Base64.encodeBase64(tokenResponse.token.getBytes())));
            DistributionRepoCreationDetails details = getRepoCreationDetails(client, org, headers);
            details.oauthAppConfigKey = config.getKey();
            details.oauthToken = tokenResponse.token;
            details.org = org;
            details.clientId = clientId;
            return details;
        }
    }

    /**
     * Executes the oauth token request and returns the parsed response as {@link BintrayTokenResponse}
     */
    private BintrayTokenResponse getBintrayTokenResponse(String code, String redirectUrl, Bintray client,
            BintrayApplicationConfig appConfig) throws IOException {
        BintrayTokenResponse tokenResponse;
        try {
            Map<String, String> requestHeaders = getFormEncodedHeader();
            String basicAuth = Base64.encodeBase64String((appConfig.getClientId() + ":" + appConfig.getSecret()).getBytes());
            requestHeaders.put(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
            log.debug("Executing OAuth token request for org {}", appConfig.getOrg());
            InputStream tokenEntity = getTokenRequestFormParams(appConfig, code, redirectUrl);
            HttpResponse response = client.post(OAUTH_TOKEN_ENDPOINT, requestHeaders, tokenEntity);
            tokenResponse = JacksonReader.streamAsClass(response.getEntity().getContent(), BintrayTokenResponse.class);
            appConfig.setRefreshToken(tokenResponse.refreshToken);
        } catch (Exception e) {
            String err = "Error executing get token request :";
            log.debug(err, e);
            //IO can either be problem with streams or failure http return code
            Throwable btCause = ExceptionUtils.getCauseOfType(e, BintrayCallException.class);
            if (btCause != null) {
                err += btCause.toString();
            } else {
                err += e.getMessage();
            }
            log.error(err);
            throw e;
        }
        return tokenResponse;
    }

    //params -> grant_type = authorization_code / code / redirect_uri / client_id / scope / artifactory_hash
    private InputStream getTokenRequestFormParams(BintrayApplicationConfig config, String code,
            String redirectUrl) {
        List<BasicNameValuePair> params = Lists.newArrayList(new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("code", code),
                new BasicNameValuePair("redirect_uri", redirectUrl),
                new BasicNameValuePair("client_id", config.getClientId()),
                new BasicNameValuePair("scope", config.getScope()),
                new BasicNameValuePair("artifactory_hash", addonsManager.getLicenseKeyHash(false)));
        return IOUtils.toInputStream(URLEncodedUtils.format(params, "UTF-8"));
    }

    private void saveNewBintrayAppConfig(BintrayApplicationConfig config) throws AlreadyExistsException {
        MutableCentralConfigDescriptor configDescriptor = configService.getMutableDescriptor();
        configDescriptor.addBintrayApplication(config);
        configService.saveEditedDescriptorAndReload(configDescriptor);
    }

    /**
     * Used by the UI repo wizard to acquire auxiliary details about the org that was used to create {@param repoKey}
     */
    public DistributionRepoCreationDetails getRepoCreationDetails(String repoKey) throws IOException {
        DistributionRepo repo = repoService.distributionRepoByKey(repoKey);
        if (repo == null) {
            throw new IOException("No such repo " + repoKey);
        }
        BintrayApplicationConfig appConfig = repo.getDescriptor().getBintrayApplication();
        if (appConfig == null) {
            throw new IOException("Repo " + repoKey + " does not have an OAuth app config, can't retrieve org-specific details.");
        }
        return getRepoCreationDetails(repo.getClient(), appConfig.getOrg(), null);
    }

    /**
     * Populates {@param details} with this {@param org}'s available licenses - which are Bintray's OSS license list
     * and optionally any other custom license this org has defined (retrieved with a different REST call).
     * Also populates the 'isPremium' field in {@param details}.
     */
    private DistributionRepoCreationDetails getRepoCreationDetails(Bintray client, String org,
            Map<String, String> headers) throws IOException {
        DistributionRepoCreationDetails details = new DistributionRepoCreationDetails();
        log.debug("Executing plan details request for org {}", org);
        HttpResponse response = client.get(String.format(PLAN_ENDPOINT, org), headers);
        details.isOrgPremium = getIsPremiumFromResponse(response);
        log.debug("Executing OSS Licenses request");
        response = client.get(OSS_LICENSES_ENDPOINT, headers);
        details.orgLicenses.addAll(getLicensesFromResponse(response));
        return details;
    }
}
