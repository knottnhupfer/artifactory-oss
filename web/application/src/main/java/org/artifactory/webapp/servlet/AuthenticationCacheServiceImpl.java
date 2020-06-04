package org.artifactory.webapp.servlet;

import com.google.common.cache.CacheBuilder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.SecurityListener;
import org.artifactory.api.security.SecurityService;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.AccessLogger;
import org.artifactory.security.UserInfo;
import org.artifactory.security.filters.AuthCacheKey;
import org.artifactory.security.filters.AuthenticationCache;
import org.artifactory.security.filters.AuthenticationCacheService;
import org.artifactory.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Yuval Reches
 */
@Service
public class AuthenticationCacheServiceImpl implements AuthenticationCacheService, SecurityListener {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationCacheServiceImpl.class);

    /**
     * Holds cached Authentication instances for the non ui requests based on the Authorization header and client ip
     */
    private ConcurrentMap<AuthCacheKey, Authentication> nonUiAuthCache;
    private ConcurrentMap<String, AuthenticationCache> userChangedCache;

    @PostConstruct
    void initialize() {
        long nonUiCacheIdleSecs = ConstantValues.securityAuthenticationCacheIdleTimeSecs.getLong();
        long initSize = ConstantValues.securityAuthenticationCacheInitSize.getLong();
        nonUiAuthCache = CacheBuilder.newBuilder().softValues()
                .initialCapacity((int) initSize)
                .expireAfterWrite(nonUiCacheIdleSecs, TimeUnit.SECONDS)
                .<AuthCacheKey, Authentication>build().asMap();
        userChangedCache = CacheBuilder.newBuilder().softValues()
                .initialCapacity((int) initSize)
                .expireAfterWrite(nonUiCacheIdleSecs, TimeUnit.SECONDS)
                .<String, AuthenticationCache>build().asMap();
    }

    @PostConstruct
    private void initSecurityListener() {
        SecurityService securityService = ContextHelper.get().beanForType(SecurityService.class);
        securityService.addListener(this);
    }

    @PreDestroy
    private void destroyCaches() {
        if (nonUiAuthCache != null) {
            nonUiAuthCache.clear();
            nonUiAuthCache = null;
        }
        if (userChangedCache != null) {
            userChangedCache.clear();
            userChangedCache = null;
        }
    }

    @Override
    public ConcurrentMap<AuthCacheKey, Authentication> getNonUiAuthCache() {
        log.debug("Non-UI authentication cache accessed");
        return nonUiAuthCache;
    }

    private ConcurrentMap<String, AuthenticationCache> getUserChangedCache() {
        log.debug("User-Changed authentication cache accessed");
        return userChangedCache;
    }

    @Override
    public void onClearSecurity() {
        nonUiAuthCache.clear();
        userChangedCache.clear();
    }

    @Override
    public void onUserUpdate(String username) {
        invalidateUserAuthCache(username);
    }

    @Override
    public void onUserDelete(String username) {
        invalidateUserAuthCache(username);
    }

    @Override
    public int compareTo(SecurityListener o) {
        return 0;
    }

    private void invalidateUserAuthCache(String username) {
        // Flag change to force re-login
        AuthenticationCache authenticationCache = getUserChangedCache().get(username);
        if (authenticationCache != null) {
            authenticationCache.changed(getNonUiAuthCache());
        }
    }

    @Override
    public Authentication getCachedAuthentication(AuthCacheKey authCacheKey, HttpServletRequest request) {
        // return cached authentication only if this is a non ui request (this guards the case when user accessed
        // Artifactory both from external tool and from the ui)
        return RequestUtils.isUiRequest(request) ? null : getNonUiAuthCache().get(authCacheKey);
    }

    @Override
    public String cacheAuthAndRetrieveUsername(HttpServletRequest request, AuthCacheKey authCacheKey,
            Authentication newAuthentication) {
        String username; // Add to user change cache the login state
        AccessLogger.loggedIn(newAuthentication);
        addToUserChange(newAuthentication);
        // Save authentication (if session exists)
        if (SessionUtils.setAuthentication(request, newAuthentication, false)) {
            log.debug("Added authentication {} in Http session.", newAuthentication);
            username = newAuthentication.getName();
        } else {
            // If it did not work use the header cache
            // An authorization cache key with no header can only be used for Anonymous authentication
            username = newAuthentication.getName();
            if (((UserInfo.ANONYMOUS.equals(username) && authCacheKey.hasEmptyHeader()) ||
                    (!UserInfo.ANONYMOUS.equals(username) && !authCacheKey.hasEmptyHeader()))) {
                getNonUiAuthCache().put(authCacheKey, newAuthentication);
                AuthenticationCache userAuthCache = getUserChangedCache().get(username);
                if (userAuthCache != null) {
                    userAuthCache.addAuthCacheKey(authCacheKey);
                }
                log.debug("Added authentication {} to cache.", newAuthentication);
            }
        }
        return username;
    }

    @Override
    public void addToUserChange(Authentication authentication) {
        String username = authentication.getName();
        if (!UserInfo.ANONYMOUS.equals(username)) {
            AuthenticationCache existingCache = getUserChangedCache().putIfAbsent(username,
                    new AuthenticationCache(authentication));
            if (existingCache != null) {
                existingCache.loggedIn(authentication);
            }
        }
    }

    @Override
    public boolean reAuthenticatedRequiredUserChanged(Authentication authentication) {
        String username = authentication.getName();
        AuthenticationCache authenticationCache = getUserChangedCache().get(username);
        if (authenticationCache != null && authenticationCache.isChanged(authentication)) {
            authenticationCache.loggedOut(authentication);
            return true;
        }
        return false;
    }
}
