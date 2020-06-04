package org.artifactory.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.RealmAwareAuthenticationProvider;
import org.artifactory.security.SimpleUser;
import org.artifactory.security.access.AccessService;
import org.jfrog.access.client.token.TokenVerifyResult;
import org.jfrog.access.token.JwtAccessToken;
import org.jfrog.common.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.artifactory.sapi.security.SecurityConstants.DEFAULT_REALM;
import static org.artifactory.security.access.AccessUserPassAuthenticationProvider.ACCESS_REALM;
import static org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.REMEMBER_ME_SCOPE;

/**
 * @author Tamir Hadad
 * @author Nadav Yogev
 */
@Service("rememberMeServices")
public class ArtifactoryRememberMeTokenService extends AbstractRememberMeServices {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryRememberMeTokenService.class);

    private AccessService accessService;
    private Map<String, RealmAwareAuthenticationProvider> authenticators;

    private static final String REALM_FIELD = "realm";
    private static final String EXPIRY_FIELD = "expiry";
    private static final int REMEMBER_ME_LIFESPAN = ConstantValues.securityRememberMeLifeTimeSecs.getInt();
    private static final long TOKEN_EXPIRY = 2L * REMEMBER_ME_LIFESPAN;

    @Autowired
    public ArtifactoryRememberMeTokenService(UserDetailsService userDetailsService,
            AccessService accessService, List<RealmAwareAuthenticationProvider> providers) {
        super("artifactory", userDetailsService);
        this.accessService = accessService;
        authenticators = providers.stream()
                .filter(provider -> provider.getRealm() != null)
                .collect(Collectors.toMap(RealmAwareAuthenticationProvider::getRealm, provider -> provider));
        super.setParameter("_spring_security_remember_me");
        super.setCookieName("SPRING_SECURITY_REMEMBER_ME_COOKIE");
    }

    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication successfulAuthentication) {
        String username = retrieveUserName(successfulAuthentication);
        if (!StringUtils.hasLength(username)) {
            log.debug("Unable to retrieve username");
            return;
        }
        log.trace("Creating remember_me authentication token for user: '{}'", username);
        accessService.revokeAllForUserAndScope(username, REMEMBER_ME_SCOPE);
        String realm = retrieveRealm(successfulAuthentication);
        String signatureValue = accessService.createNoPermissionToken(
                ImmutableList.of(REMEMBER_ME_SCOPE),
                UserTokenSpec.create(username).expiresIn(TOKEN_EXPIRY),
                createExtraInfo(realm)
        ).getTokenValue();
        setCookie(new String[]{signatureValue}, REMEMBER_ME_LIFESPAN, request, response);
        log.trace("Added remember-me cookie for user: '{}', expires in '{}' seconds", username, REMEMBER_ME_LIFESPAN);
    }

    private String createExtraInfo(String realm) {
        HashMap<String, Object> extraInfo = Maps.newHashMap();
        extraInfo.put(REALM_FIELD, realm);
        extraInfo.put(EXPIRY_FIELD, TimeUnit.SECONDS.toMillis(REMEMBER_ME_LIFESPAN) + System.currentTimeMillis());
        return JsonUtils.getInstance().valueToString(extraInfo);
    }

    @Override
    public UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request,
            HttpServletResponse response) {
        if (cookieTokens.length != 1) {
            throw createExceptionAndLog("Unexpected number of values in remember me cookie," +
                    " expected single token but received: '" + Arrays.asList(cookieTokens) + "'");
        }
        String token = cookieTokens[0];
        TokenVerifyResult verify = accessService.getAccessClient().token().verify(token);
        if (!verify.isSuccessful()) {
            throw createExceptionAndLog("Invalid token: " + verify.getReason());
        }

        JwtAccessToken accessToken = verify.getAccessToken();
        if (accessToken == null || !REMEMBER_ME_SCOPE.equals(accessToken.getScope().get(0))) {
            throw createExceptionAndLog("Invalid token: token is not of type 'remember_me'");
        }
        String username = getUserByToken(accessToken);
        JsonNode extraInfo = JsonUtils.getInstance().readTree(accessToken.getExtension());
        JsonNode expiry = extraInfo.get(EXPIRY_FIELD);
        long expiryTime = (expiry == null ? 0 : expiry.asLong()) - System.currentTimeMillis();
        if (expiryTime < 0) {
            accessService.revokeAllForUserAndScope(username, REMEMBER_ME_SCOPE);
            throw createExceptionAndLog("Invalid token: token has expired");
        }
        log.trace("Remember me token for user: '{}' will expire in {}ms", username, expiryTime);
        String realm = getRealm(extraInfo);
        return reauthenticateRememberMe(username, realm);
    }

    private UserDetails reauthenticateRememberMe(String username, String realm) {
        RealmAwareAuthenticationProvider provider = authenticators.get(realm);
        if (provider == null) {
            accessService.revokeAllForUserAndScope(username, REMEMBER_ME_SCOPE);
            throw createExceptionAndLog("Invalid Token: remember me is not support for realm " + realm);
        }
        log.trace("Re-authenticating remember me {} token for user: '{}'", realm, username);
        try {
            return provider.reauthenticateRememberMe(username);
        } catch (AuthenticationException e) {
            accessService.revokeAllForUserAndScope(username, REMEMBER_ME_SCOPE);
            throw e;
        }
    }

    private String getRealm(JsonNode extraInfo) {
        JsonNode realmNode = extraInfo.get(REALM_FIELD);
        if (realmNode == null) {
            return null;
        }
        String realm = realmNode.asText();
        return DEFAULT_REALM.equals(realm) ? ACCESS_REALM : realm;
    }

    private String retrieveUserName(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return authentication.getPrincipal().toString();
    }

    private String retrieveRealm(Authentication authentication) {
        if (authentication.getPrincipal() instanceof SimpleUser) {
            return ((SimpleUser) authentication.getPrincipal()).getDescriptor().getRealm();
        }
        return ACCESS_REALM;
    }

    private String getUserByToken(JwtAccessToken accessToken) {
        String username = accessService.extractSubjectUsername(accessToken);
        if (username != null) {
            return username;
        }
        return accessToken.getSubject();
    }

    private RememberMeAuthenticationException createExceptionAndLog(String message) {
        log.debug(message);
        return new RememberMeAuthenticationException(message);
    }
}
