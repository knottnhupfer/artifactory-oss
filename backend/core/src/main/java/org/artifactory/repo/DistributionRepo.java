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

package org.artifactory.repo;

import com.jfrog.bintray.client.api.handle.Bintray;
import com.jfrog.bintray.client.impl.BintrayClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.distribution.DistributionRepoDescriptor;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.util.HttpClientConfigurator;
import org.artifactory.util.bearer.BintrayBearerPreemptiveAuthInterceptor;
import org.jfrog.client.http.CloseableHttpClientDecorator;
import org.jfrog.client.util.PathUtils;

/**
 * @author Dan Feldman
 */
public class DistributionRepo extends DbLocalRepo<DistributionRepoDescriptor> {

    private Bintray client;
    private CloseableHttpClient httpClient;

    public DistributionRepo(DistributionRepoDescriptor descriptor, InternalRepositoryService repositoryService,
            DistributionRepo oldLocalRepo) {
        super(descriptor, repositoryService, oldLocalRepo);
    }

    @Override
    public void init() {
        super.init();
        this.client = createClient();
    }

    @Override
    public void close() {
        if (httpClient instanceof CloseableHttpClientDecorator) {
            ((CloseableHttpClientDecorator) httpClient).onClose();
        }
    }

    private Bintray createClient() {
        HttpClientConfigurator configurator = new HttpClientConfigurator();
        configurator
                .hostFromUrl(getBaseBintrayApiUrl())
                .socketTimeout(ConstantValues.bintrayClientDistributionRequestTimeout.getInt())
                .connectionTimeout(ConstantValues.bintrayClientDistributionRequestTimeout.getInt())
                .noRetry()
                .maxTotalConnections(30)
                .maxConnectionsPerRoute(30);
        httpClient = configurator
                .enableTokenAuthentication(true, getKey(), new BintrayBearerPreemptiveAuthInterceptor(getKey()))
                .proxy(getProxy()).build();

        return createBintrayClient(httpClient);
    }

    private Bintray createBintrayClient(CloseableHttpClient httpClient) {
        return BintrayClient.create(httpClient, PathUtils.trimTrailingSlashes(getBaseBintrayApiUrl()),
                ConstantValues.bintrayClientThreadPoolSize.getInt(),
                ConstantValues.bintrayClientSignRequestTimeout.getInt());
    }

    private String getBaseBintrayApiUrl() {
        return PathUtils.addTrailingSlash(ConstantValues.bintrayApiUrl.getString());
    }

    private ProxyDescriptor getProxy() {
        return getDescriptor().getProxy();
    }

    public Bintray getClient() {
        return client;
    }
}
