package org.artifactory.security;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.filters.AuthenticationCacheService;
import org.artifactory.security.props.auth.OauthManager;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Yuval Reches
 */
public class LoginHandlerImplTest extends ArtifactoryHomeBoundTest {

    private static final String USERNAME = "Gandalf";

    @Mock
    private ArtifactoryContext artifactoryContextMock;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private OauthManager oauthManager;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private AccessService accessService;
    @Mock
    private AuthenticationCacheService authenticationCacheService;

    private LoginHandlerImpl loginHandler;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        loginHandler = new LoginHandlerImpl(oauthManager, userGroupService, accessService, authenticationCacheService);
        when(artifactoryContextMock.beanForType(AuthenticationManager.class)).thenReturn(authenticationManager);
        ArtifactoryContextThreadBinder.bind(artifactoryContextMock);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryHome.unbind();
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testGetAuthenticationNotCached() {
        String[] tokens = new String[] {USERNAME, ""};
        AuthenticationDetailsSource<HttpServletRequest, ?> ads = new HttpAuthenticationDetailsSource();
        HttpServletRequest servletRequestMock = prepareRequestMock();
        prepareAuthenticateMock(true);

        // Not cached
        when(authenticationCacheService.getCachedAuthentication(any(), any())).thenReturn(null);

        // Act
        loginHandler.getAuthentication(tokens, ads, servletRequestMock, USERNAME);

        // Verify we authenticate
        verify(authenticationManager, times(1)).authenticate(any());
        // Verify we add to cache
        verify(authenticationCacheService, times(1)).cacheAuthAndRetrieveUsername(any(), any(), any());
    }

    @Test
    public void testGetAuthenticationCached() {
        String[] tokens = new String[] {USERNAME, ""};
        AuthenticationDetailsSource<HttpServletRequest, ?> ads = new HttpAuthenticationDetailsSource();
        HttpServletRequest servletRequestMock = prepareRequestMock();

        // Cached
        prepareCachedAuthenticationMock();
        when(authenticationCacheService.reAuthenticatedRequiredUserChanged(any())).thenReturn(false);

        // Act
        loginHandler.getAuthentication(tokens, ads, servletRequestMock, USERNAME);

        // Verify we don't authenticate
        verify(authenticationManager, times(0)).authenticate(any());
        // Verify we update the cache with the cached authentication
        verify(authenticationCacheService, times(1)).addToUserChange(any());
        // Verify we don't re-add to cache, as its already there
        verify(authenticationCacheService, times(0)).cacheAuthAndRetrieveUsername(any(), any(), any());
    }

    @Test
    public void testGetAuthenticationCachedButNotAuthenticated() {
        String[] tokens = new String[] {USERNAME, ""};
        AuthenticationDetailsSource<HttpServletRequest, ?> ads = new HttpAuthenticationDetailsSource();
        HttpServletRequest servletRequestMock = prepareRequestMock();

        // Cached but the Authentication object returns FALSE for isAuthenticated
        prepareAuthenticateMock(false);
        when(authenticationCacheService.reAuthenticatedRequiredUserChanged(any())).thenReturn(false);

        // Act
        loginHandler.getAuthentication(tokens, ads, servletRequestMock, USERNAME);

        // Verify we authenticate
        verify(authenticationManager, times(1)).authenticate(any());
        // Verify we add to cache
        verify(authenticationCacheService, times(1)).cacheAuthAndRetrieveUsername(any(), any(), any());
    }

    private HttpServletRequest prepareRequestMock() {
        HttpServletRequest servletRequestMock = Mockito.mock(HttpServletRequest.class);
        when(servletRequestMock.getRemoteAddr()).thenReturn("127.0.0.1");
        when(servletRequestMock.getHeader("Authorization")).thenReturn("dummyHeader");
        return servletRequestMock;
    }

    private void prepareAuthenticateMock(boolean isAuthenticated) {
        Authentication authenticationMock = getAuthenticationMock(isAuthenticated);
        when(authenticationManager.authenticate(any())).thenReturn(authenticationMock);
    }

    private void prepareCachedAuthenticationMock() {
        Authentication authenticationMock = getAuthenticationMock(true);
        when(authenticationCacheService.getCachedAuthentication(any(), any())).thenReturn(authenticationMock);
    }

    private Authentication getAuthenticationMock(boolean isAuthenticated) {
        Authentication authenticationMock = Mockito.mock(Authentication.class);
        when(authenticationMock.isAuthenticated()).thenReturn(isAuthenticated);
        when(authenticationMock.getPrincipal()).thenReturn(null);
        when(authenticationMock.getAuthorities()).thenReturn(null);
        when(authenticationMock.getCredentials()).thenReturn(null);
        return authenticationMock;
    }

}