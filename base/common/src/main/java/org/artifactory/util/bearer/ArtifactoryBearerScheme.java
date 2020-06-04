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

package org.artifactory.util.bearer;

import org.apache.http.HttpRequest;
import org.apache.http.auth.Credentials;
import org.apache.http.protocol.HttpContext;
import org.artifactory.addon.docker.rest.DockerRemoteTokenProvider;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.distribution.auth.BintrayTokenProvider;
import org.jfrog.client.http.auth.BearerScheme;
import org.jfrog.client.http.auth.TokenProvider;

import java.util.Optional;

/**
 * Bearer authentication scheme as defined in RFC 2617
 *
 * @author Shay Yaakov
 */
public class ArtifactoryBearerScheme extends BearerScheme {

    private String repoKey;

    public ArtifactoryBearerScheme(String repoKey) {
        super(getTokenProviderByRepoType(repoKey));
        this.repoKey = repoKey;
    }

    @Override
    public String getToken(Credentials dummyCredentials, HttpRequest request, HttpContext context) {
        return tokenProvider.getToken(getParameters(),
                request.getRequestLine().getMethod(),
                request.getRequestLine().getUri(),
                repoKey);
    }

    private static TokenProvider getTokenProviderByRepoType(String repoKey) {
        TokenProvider provider;
        RepoType type = Optional.ofNullable(ContextHelper.get().beanForType(RepositoryService.class)
                .repoDescriptorByKey(repoKey))
                .orElseThrow(() -> new RuntimeException("No such repository " + repoKey))
                .getType();
        switch (type) {
            case Distribution:
                provider = ContextHelper.get().beanForType(BintrayTokenProvider.class);
                break;
            case Docker:
                provider = ContextHelper.get().beanForType(DockerRemoteTokenProvider.class);
                break;
            default:
                throw new IllegalArgumentException("Token Authentication is not available for repositories of type "
                        + type);
        }
        return provider;
    }
}
