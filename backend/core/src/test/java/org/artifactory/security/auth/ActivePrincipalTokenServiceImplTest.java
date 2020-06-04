package org.artifactory.security.auth;

import org.artifactory.security.InternalSecurityService;
import org.artifactory.security.access.AccessService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.token.TokenClient;
import org.jfrog.access.client.token.TokenResponseImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Uriah Levy
 */
@Test
public class ActivePrincipalTokenServiceImplTest extends ArtifactoryHomeBoundTest {

    @Mock
    AccessService accessService;

    @Mock
    InternalSecurityService internalSecurityService;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
    }

    public void testLoad() {
        ActivePrincipalTokenStoreImpl activePrincipalTokenService = spy(new ActivePrincipalTokenStoreImpl(
                accessService, internalSecurityService));
        activePrincipalTokenService.initTokensCache();
        doReturn("groups/group1,group2").when(activePrincipalTokenService).getTokenScope();
        when(internalSecurityService.currentUsername()).thenReturn("johndoe");
        when(activePrincipalTokenService.getTokenScope()).thenReturn("groups/group1,group2");
        AccessClient accessClient = mock(AccessClient.class);
        TokenClient tokenClient = mock(TokenClient.class);
        when(accessService.getAccessClient()).thenReturn(accessClient);
        when(accessClient.token()).thenReturn(tokenClient);
        when(tokenClient.create(any())).thenReturn(new TokenResponseImpl("token", null, null, null));
        Assert.assertEquals(activePrincipalTokenService.getOrLoadToken(), "token");
        // only once
        verify(activePrincipalTokenService).createToken("johndoe");
    }

}