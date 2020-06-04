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

package org.artifactory.repo.distribution.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.distribution.BintrayOAuthTokenRefresher;
import org.artifactory.common.ConstantValues;
import org.jfrog.client.http.auth.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Bintray specific implementation of a {@link TokenProvider}.
 * The tokens are cached in-memory and auto expire according to the
 * {@code ConstantValues.bintrayOAuthTokenExpirySeconds} system property
 *
 * @author Dan Feldman
 */
@Service
public class BintrayTokenProvider implements TokenProvider {
    private static final Logger log = LoggerFactory.getLogger(BintrayTokenProvider.class);

    @Autowired
    private BintrayOAuthTokenRefresher tokenRefresher;

    private LoadingCache<String, String> tokens;

    @PostConstruct
    public void initTokensCache() {
        tokens = CacheBuilder.newBuilder()
                .initialCapacity(100)
                .expireAfterWrite(ConstantValues.bintrayOAuthTokenExpirySeconds.getLong(), TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(@Nonnull String repoKey) throws Exception {
                        String token = fetchNewToken(repoKey);
                        if (StringUtils.isBlank(token)) {
                            throw new Exception("Can't fetch Bintray OAuth token for repo: " + repoKey);
                        }
                        return token;
                    }
                });
    }

    @Override
    public String getToken(Map<String, String> challengeParams, String method, String uri, String repoKey) {
        try {
            log.trace("Getting Bintray OAuth token for {}", repoKey);
            return tokens.get(repoKey);
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get Bintray OAuth token from cache for " + repoKey, e);
        }
    }

    private String fetchNewToken(String repoKey) throws Exception {
        log.trace("Fetching new OAuth token from Bintray for repo '{}' that has expiry of {} seconds", repoKey,
                ConstantValues.bintrayOAuthTokenExpirySeconds.getLong());
        return tokenRefresher.refreshBintrayOAuthAppToken(repoKey);
    }
}
