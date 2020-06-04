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

package org.artifactory.security.providermgr;

import org.artifactory.security.props.auth.model.OauthDockerErrorModel;
import org.artifactory.security.props.auth.model.OauthErrorModel;
import org.artifactory.security.props.auth.model.OauthModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Artifactory specific token cache implementation
 *
 * @author Chen Keinan
 */
@Component
public class ArtifactoryTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryTokenProvider.class);
    public static final String REALM = "TokenProvider";

    public OauthModel getToken(ArtifactoryCacheKey artifactoryCacheKey) {
        OauthModel oauthModel = null;
        try {
            log.trace("Generating token for {}", artifactoryCacheKey.getUser());
            oauthModel = artifactoryCacheKey.getProviderMgr().fetchAndStoreTokenFromProvider();
            return oauthModel;
        } finally {
            if ((oauthModel instanceof OauthErrorModel || oauthModel instanceof OauthDockerErrorModel)) {
                log.trace("An error occurred while trying to generate token for {}", artifactoryCacheKey.getUser());
            }
        }
    }

    public void invalidateUserCacheEntries(String userName) {
        //TODO [by shayb]: in the past, we has token cache, so we removed the token from the cache. We should consider
        //TODO [by shayb]: deleting the access tokens here, but this might cost in performance, because we need to fetch
        //TODO [by shayb]: all tokens from access, filter them, and then, we can delete the relevant
    }

    public void invalidateCacheEntriesForAllUsers() {
        //TODO [by shayb]: same sd invalidateUserCacheEntries(), consider revoking the access tokens as well
    }
}
