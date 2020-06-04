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

package org.artifactory.addon.docker.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.jfrog.client.http.auth.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Docker specific implementation of a {@link TokenProvider}.
 * The tokens are cached in-memory and auto expire according to the
 * {@code artifactory.docker.tokens.cache.idleTimeSecs} system property
 *
 * @author Shay Yaakov
 */
@Service
public class DockerRemoteTokenProvider implements TokenProvider {
    private static final Logger log = LoggerFactory.getLogger(DockerRemoteTokenProvider.class);

    private LoadingCache<DockerTokenCacheKey, String> tokens;

    @PostConstruct
    public void initTokensCache() {
        tokens = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .expireAfterWrite(ConstantValues.dockerTokensCacheIdleTimeSecs.getLong(), TimeUnit.SECONDS)
                .build(new CacheLoader<DockerTokenCacheKey, String>() {
                    @Override
                    public String load(@Nonnull DockerTokenCacheKey key) throws Exception {
                        String token = fetchNewToken(key);
                        if (StringUtils.isBlank(token)) {
                            throw new IllegalStateException("Can't fetch token for repo: " + key.getRepoKey() + " realm: "
                                    + key.getRealm() + " scope:" + key.getScope());
                        }
                        return token;
                    }
                });
    }

    @Override
    public String getToken(Map<String, String> challengeParams, String method, String uri, String repoKey) {
        try {
            populateParamsIfNeeded(challengeParams, method, uri);
            if (log.isDebugEnabled()) {
                log.trace("Getting token for {}", challengeParams);
            }
            return tokens.get(new DockerTokenCacheKey(challengeParams, repoKey));
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not get token from cache for " + challengeParams, e);
        }
    }

    private void populateParamsIfNeeded(Map<String, String> challengeParams, String method, String uri) {
        handleNoServiceParamInChallenge(challengeParams);
        handleNoScopeParamInChallenge(challengeParams, method, uri);
    }

    private void handleNoServiceParamInChallenge(Map<String, String> challengeParams) {
        if (!challengeParams.containsKey("service")) {
            String realm = challengeParams.get("realm");
            if (StringUtils.isNotBlank(realm) && realm.startsWith("http")) {
                try {
                    challengeParams.put("service", new URL(realm).getHost());
                } catch (MalformedURLException e) {
                    log.error("Realm of token cannot be parsed due to: " + e.getMessage(), e);
                }
            }
        }
    }

    private void handleNoScopeParamInChallenge(Map<String, String> challengeParams, String method, String uri) {
        if (!challengeParams.containsKey("scope")) {
            if (uri.startsWith("/v2/")) {
                handleV2(challengeParams, method, uri);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Docker Bearer challenge has no scope and URI does not starts with /v2 but '{}'", uri);
                }
            }
        }
    }

    private void handleV2(Map<String, String> challengeParams, String method, String uri) {
        StringBuilder scopeBuilder = new StringBuilder("repository:");
        String[] split = uri.split("/"); // The first / ends up with empty string at pos 0
        //noinspection StatementWithEmptyBody
        if (split.length < 3) {
            // Simple ping => empty scope
        } else {
            scopeBuilder.append(split[2]);
        }
        if (split.length > 3) {
            scopeBuilder.append("/");
            scopeBuilder.append(split[3]);
        }
        scopeBuilder.append(":");
        String newScope = appendActionToScope(method, scopeBuilder);
        if (newScope != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Docker Bearer challenge new scope set to '%s'", newScope));
            }
            challengeParams.put("scope", newScope);
        }
    }

    private String appendActionToScope(String method, StringBuilder scopeBuilder) {
        String newScope = null;
        if ("GET".equalsIgnoreCase(method)) {
            scopeBuilder.append("pull");
            newScope = scopeBuilder.toString();
        } else if ("PUT".equalsIgnoreCase(method)) {
            scopeBuilder.append("push");
            newScope = scopeBuilder.toString();
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Docker Bearer challenge has no scope and method is not GET or PUT '{}'", method);
            }
        }
        return newScope;
    }

    private String fetchNewToken(DockerTokenCacheKey tokenCacheKey) {
        log.trace("Fetching new token for '{}'", tokenCacheKey);
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(DockerAddon.class)
                .fetchDockerAuthToken(tokenCacheKey);
    }
}
