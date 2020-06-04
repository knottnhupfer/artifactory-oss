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

package org.artifactory.ui.rest.service.admin.configuration.repositories.util;


import com.google.common.base.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.util.HttpUtils;
import org.jfrog.client.util.PathUtils;
import org.jfrog.security.crypto.EncryptionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URISyntaxException;
import java.util.List;

import static org.artifactory.request.ArtifactoryRequest.ARTIFACTORY_ORIGINATED;

/**
 * Create Http request for test replication button
 *
 * @author Aviad Shikloshi
 */
public class TestMethodFactory {
    private static final Logger log = LoggerFactory.getLogger(TestMethodFactory.class);

    public static HttpRequestBase createTestMethod(String repoUrl, RepoType repoType, @Nullable String queryParams,
            @Nullable RemoteNetworkRepositoryConfigModel networkModel) {

        if (repoType == null) {
            throw new RuntimeException("Missing repository type");
        }
        HttpRequestBase request;
        // Some endpoints will block HEAD, so we want to avoid sending a HEAD request to them
        // For example, VCS can be a Stash/Bitbucket endpoint and these don't allow HEAD
        boolean shouldSendGet = (repoType.equals(repoType.VCS) || repoType.equals(repoType.CocoaPods));
        if (shouldSendGet) {
            request = createGenericGetTestMethod(repoUrl);
            return request;
        }
        // Other special cases (NuGet, Docker...) + default case (HEAD)
        switch (repoType) {
            case NuGet:
                request = createNuGetTestMethod(repoUrl, queryParams);
                break;
            case Docker:
                request = createDockerTestMethod(repoUrl);
                break;
            case Npm:
                request = createNpmTestMethod(repoUrl, networkModel);
                break;
            default:
                request = new HttpHead(HttpUtils.encodeUrl(repoUrl));
                //Add the current requester host id
        }
        addOriginatedHeader(request);
        return request;
    }

    /**
     * add originated header to request
     *
     * @param request - http servlet request
     */
    public static void addOriginatedHeader(HttpRequestBase request) {
        String hostId = ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaCommonAddon.class)
                .getHostId();
        request.addHeader(ARTIFACTORY_ORIGINATED, hostId);
    }

    // Generic GET to the root of the repo URL

    private static HttpRequestBase createGenericGetTestMethod(String repoUrl) {
        if (repoUrl.endsWith("/")) {
            repoUrl = PathUtils.trimTrailingSlashes(repoUrl);
        }
        return new HttpGet(repoUrl);
    }
    private static HttpRequestBase createNuGetTestMethod(String repoUrl, String queryParams) {
        try {
            URIBuilder uriBuilder = new URIBuilder(repoUrl);
            HttpRequestBase request = new HttpGet();
            if (StringUtils.isNotBlank(queryParams)) {
                List<NameValuePair> queryParamsMap = URLEncodedUtils.parse(queryParams, Charsets.UTF_8);
                uriBuilder.setParameters(queryParamsMap);
            }
            request.setURI(uriBuilder.build());
            return request;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to build test URI", e);
        }
    }

    private static HttpRequestBase createDockerTestMethod(String repoUrl) {
        String path = repoUrl;
        if (path.endsWith("/")) {
            path = PathUtils.trimTrailingSlashes(path);
        }
        path += "/v2/";
        return new HttpGet(path);
    }

    private static HttpRequestBase createNpmTestMethod(String repoUrl,
            @Nullable RemoteNetworkRepositoryConfigModel networkModel) {
        //standard http HEAD request (fallback)
        HttpRequestBase request = new HttpHead(HttpUtils.encodeQuery(repoUrl));
        if (networkModel == null) {
            return request;
        }
        EncryptionWrapper artifactoryEncryptionWrapper = ArtifactoryHome.get().getArtifactoryEncryptionWrapper();
        String userName = networkModel.getUsername();
        String password = networkModel.getPassword();
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            return request;
        }
        HttpGet getRequest = new HttpGet(HttpUtils.encodeQuery(PathUtils.trimTrailingSlashes(repoUrl) + "/-/whoami"));
        String basicCreds = userName + ":" + artifactoryEncryptionWrapper.decryptIfNeeded(password).getDecryptedData();
        getRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String(basicCreds.getBytes()));
        return getRequest;
    }

    private TestMethodFactory() {
    }

}
