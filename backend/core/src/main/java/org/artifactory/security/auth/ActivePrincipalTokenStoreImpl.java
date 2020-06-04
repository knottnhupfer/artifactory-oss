package org.artifactory.security.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.*;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.client.token.TokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Uriah Levy
 * A holder of active principal tokens backed by a loading cache.
 */
@Service
public class ActivePrincipalTokenStoreImpl implements ActivePrincipalTokenStore {
    private static final Logger log = LoggerFactory.getLogger(ActivePrincipalTokenStoreImpl.class);
    private LoadingCache<String, String> tokens;
    private AccessService accessService;
    private InternalSecurityService securityService;

    @Autowired
    public ActivePrincipalTokenStoreImpl(AccessService accessService,
            InternalSecurityService securityService) {
        this.accessService = accessService;
        this.securityService = securityService;
    }

    @PostConstruct
    public void initTokensCache() {
        tokens = CacheBuilder.newBuilder()
                .initialCapacity(200)
                .maximumSize(ConstantValues.activePrincipalTokenCacheMaxSize.getInt())
                .expireAfterWrite(ConstantValues.activePrincipalTokenTtl.getLong(), TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(@Nonnull String key) {
                        String token = createToken(key);
                        if (StringUtils.isBlank(token)) {
                            throw new IllegalStateException(
                                    String.format("Unable to create Active Principal Access token for user: '%s'", key));
                        }
                        return token;
                    }
                });
    }

    @Override
    public String getOrLoadToken() {
        try {
            return tokens.get(securityService.currentUsername());
        } catch (ExecutionException e) {
            log.error("Unable to get token for user: '{}'", securityService.currentUsername(), e);
        }
        return null;
    }

    @Override
    public void invalidateToken(String username) {
        tokens.invalidate(username);
    }

    String createToken(String username) {
        TokenRequest.Builder tokenRequest = TokenRequest.builder();
        tokenRequest.nonRefreshable()
                .subject(username)
                .expiresIn(TimeUnit.SECONDS.toMillis(ConstantValues.activePrincipalTokenTtl.getLong() + 10))
                .scopes("applied-permissions/" + getTokenScope());
        return accessService.getAccessClient().token().create(tokenRequest.build()).getTokenValue();
    }

    String getTokenScope() {
        List<String> activePrincipalGroups = getLoggedInUsernameGroups();
        if (activePrincipalGroups.isEmpty()) {
            return "user";
        }
        return "groups:" + String.join(",", activePrincipalGroups);
    }

    private List<String> getLoggedInUsernameGroups() {
        SingleSignOnService singleSignOnService = ContextHelper.get().beanForType(SingleSignOnService.class);
        Authentication authentication = AuthenticationHelper.getAuthentication();
        UserInfo userInfo = singleSignOnService
                .extractAuthenticatedUserInfo(securityService.currentUsername(), authentication);
        return userInfo.getGroups().stream()
                .map(UserGroupInfo::getGroupName)
                .collect(Collectors.toList());
    }
}
