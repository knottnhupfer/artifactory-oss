package org.artifactory.security.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Fred Simon
 */
public class AuthenticationCache {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationCache.class);
    Set<AuthCacheKey> authCacheKeys;
    Map<Integer, Integer> authState = new HashMap<>(3);

    public AuthenticationCache(Authentication first) {
        authState.put(first.hashCode(), 0);
    }

    public synchronized void addAuthCacheKey(AuthCacheKey authCacheKey) {
        if (authCacheKeys == null) {
            authCacheKeys = new HashSet<>();
        }
        authCacheKeys.add(authCacheKey);
    }

    public synchronized void changed(
            ConcurrentMap<AuthCacheKey, Authentication> nonUiAuthCache) {
        if (authCacheKeys != null) {
            for (AuthCacheKey authCacheKey : authCacheKeys) {
                Authentication removed = nonUiAuthCache.remove(authCacheKey);
                if (removed != null) {
                    Integer key = removed.hashCode();
                    log.debug("Removed {}:{} from the non-ui authentication cache", removed.getName(), key);
                    authState.put(key, 1);
                }
            }
            authCacheKeys.clear();
        }
        Set<Integer> keys = new HashSet<>(authState.keySet());
        for (Integer key : keys) {
            authState.put(key, 1);
        }
    }

    public boolean isChanged(Authentication auth) {
        int key = auth.hashCode();
        Integer state = authState.get(key);
        if (state != null) {
            return state == 1;
        }
        return false;
    }

    public void loggedOut(Authentication auth) {
        authState.put(auth.hashCode(), 2);
    }

    public void loggedIn(Authentication auth) {
        authState.put(auth.hashCode(), 0);
    }
}