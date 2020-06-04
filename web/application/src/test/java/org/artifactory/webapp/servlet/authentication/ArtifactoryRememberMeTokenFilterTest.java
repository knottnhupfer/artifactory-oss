package org.artifactory.webapp.servlet.authentication;

import org.artifactory.api.security.access.CreatedTokenInfo;
import org.artifactory.api.security.access.UserTokenSpec;
import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.security.RealmAwareAuthenticationProvider;
import org.artifactory.security.SimpleUser;
import org.artifactory.security.UserInfo;
import org.artifactory.security.access.AccessService;
import org.artifactory.session.ArtifactoryRememberMeTokenService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.token.TokenClient;
import org.jfrog.access.client.token.TokenVerifyResult;
import org.jfrog.access.token.JwtAccessToken;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.util.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService.REMEMBER_ME_SCOPE;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author NadavY
 */
public class ArtifactoryRememberMeTokenFilterTest extends ArtifactoryHomeBoundTest {

    private ArtifactoryRememberMeTokenService rememberMeTokenFilter;

    @Mock
    private AccessService accessService;

    @Mock
    private AccessClient accessClient;

    @Mock
    private TokenClient tokenClient;

    @Mock
    private RealmAwareAuthenticationProvider internalProvider;

    @Mock
    private RealmAwareAuthenticationProvider ldapProvider;

    @BeforeClass
    public void setup() {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        initMocks(this);
        when(accessService.getAccessClient()).thenReturn(accessClient);
        when(accessClient.token()).thenReturn(tokenClient);
        when(internalProvider.getRealm()).thenReturn("access");
        when(ldapProvider.getRealm()).thenReturn("ldap");
        bindArtifactoryHome();
        rememberMeTokenFilter = new ArtifactoryRememberMeTokenService(
                userDetailsService,
                accessService, Lists.newArrayList(internalProvider, ldapProvider));
    }

    @Test
    public void testOnLoginSuccessGoodUser() {
        String username = "goodInternalUser";
        addTokenResult(username, "access", true);
        UserDetails userDetails = loginWithRememberMe(username, true);
        assertEquals(username, userDetails.getUsername());
        assertEquals("accessPassword", userDetails.getPassword());
    }

    @Test
    public void testOnLoginSuccessGoodLdapUser() {
        String username = "goodLdapUser";
        addTokenResult(username, "ldap", true);
        UserDetails userDetails = loginWithRememberMe(username, false);
        assertEquals(username, userDetails.getUsername());
        assertEquals("ldapPassword", userDetails.getPassword());
    }


    @Test(expectedExceptions = RememberMeAuthenticationException.class)
    public void testOnLoginSuccessBadUser() {
        String username = "badInternalUser";
        addTokenResult(username, "access", false);
        loginWithRememberMe(username, true);
    }

    @Test(expectedExceptions = RememberMeAuthenticationException.class)
    public void testOnLoginSuccessBadRealm() {
        String username = "badRealmUser";
        addTokenResult(username, "evil", true);
        loginWithRememberMe(username, true);
    }

    @Test(expectedExceptions = RememberMeAuthenticationException.class)
    public void testOnLoginSuccessBadToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        rememberMeTokenFilter.processAutoLoginCookie(new String[]{"BadToken", "WithTwoParts"}, request, response);
    }

    private UserDetails loginWithRememberMe(String username, boolean internal) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        addReauthenticateResponse(username, internal);

        Authentication authentication = mock(Authentication.class);
        UserInfo userInfo = new UserImpl("test");
        when(authentication.getPrincipal()).thenReturn(new SimpleUser(userInfo));

        rememberMeTokenFilter.onLoginSuccess(request, response, authentication);
        Cookie cookie = response.getCookies()[0];
        String[] cookieTokens = decodeCookie(cookie.getValue());
        return rememberMeTokenFilter.processAutoLoginCookie(cookieTokens, request, response);
    }

    private void addReauthenticateResponse(String username, boolean internal) {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);
        if (internal) {
            doReturn(internalProvider.getRealm() + "Password").when(userDetails).getPassword();
            when(internalProvider.reauthenticateRememberMe(username)).thenReturn(userDetails);
        } else {
            doReturn(ldapProvider.getRealm() + "Password").when(userDetails).getPassword();
            when(ldapProvider.reauthenticateRememberMe(username)).thenReturn(userDetails);
        }
    }

    private void addTokenResult(String username, String realm, boolean success) {
        TokenVerifyResult result = mock(TokenVerifyResult.class);
        when(result.isSuccessful()).thenReturn(success);
        JwtAccessToken accessToken = mock(JwtAccessToken.class);
        when(accessToken.getExtension()).thenReturn("{\"realm\" : \"" + realm + "\", \"expiry\":\"99999999999999\"}");
        when(result.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getScope()).thenReturn(Lists.newArrayList(REMEMBER_ME_SCOPE));
        when(tokenClient.verify(username)).thenReturn(result);
        CreatedTokenInfo tokenInfo = mock(CreatedTokenInfo.class);
        when(accessService.createNoPermissionToken(anyList(), any(UserTokenSpec.class), any()))
                .thenReturn(tokenInfo);
        when(accessService.extractSubjectUsername(accessToken)).thenReturn(username);
        when(tokenInfo.getTokenValue()).thenReturn(username);
    }

    private String[] decodeCookie(String cookieValue) {
        StringBuilder cookieValueBuilder = new StringBuilder(cookieValue);
        for (int j = 0; j < cookieValueBuilder.length() % 4; ++j) {
            cookieValueBuilder.append("=");
        }
        cookieValue = cookieValueBuilder.toString();

        if (!Base64.isBase64(cookieValue.getBytes())) {
            throw new RememberMeAuthenticationException("Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        } else {
            String cookieAsPlainText = new String(Base64.decode(cookieValue.getBytes()));
            String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, ":");
            if ((tokens[0].equalsIgnoreCase("http") || tokens[0].equalsIgnoreCase("https")) &&
                    tokens[1].startsWith("//")) {
                String[] newTokens = new String[tokens.length - 1];
                newTokens[0] = tokens[0] + ":" + tokens[1];
                System.arraycopy(tokens, 2, newTokens, 1, newTokens.length - 1);
                tokens = newTokens;
            }
            return tokens;
        }
    }
}