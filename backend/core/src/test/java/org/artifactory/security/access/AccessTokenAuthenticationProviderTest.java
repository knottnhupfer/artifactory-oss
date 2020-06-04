package org.artifactory.security.access;

import com.google.common.collect.ImmutableList;
import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.security.MutableUserInfo;
import org.jfrog.access.common.ServiceId;
import org.jfrog.access.token.JwtAccessToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Shay Bagants
 */
@Test
public class AccessTokenAuthenticationProviderTest {

    private AccessTokenAuthenticationProvider tokenAuthenticationProvider;

    @Mock
    private AccessService accessService;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        tokenAuthenticationProvider = new AccessTokenAuthenticationProvider();
        tokenAuthenticationProvider.accessService = accessService;
        ServiceId serviceId = new ServiceId("jfrt", "xyz");
        when(accessService.getArtifactoryServiceId()).thenReturn(serviceId);
    }

    @Test(dataProvider = "provideScopes")
    public void testPopulateArtifactoryPrivileges(List<String> scopes, boolean expectedAdmin) {
        UserImpl user = new UserImpl();
        JwtAccessToken token = mock(JwtAccessToken.class);
        when(token.getScope()).thenReturn(scopes);
        MutableUserInfo mutableUserInfo = tokenAuthenticationProvider.populateArtifactoryPrivileges(user, token);
        Assert.assertEquals(mutableUserInfo.isAdmin(), expectedAdmin);
    }

    @DataProvider
    public static Object[][] provideScopes() {
        return new Object[][]{
                {ImmutableList.of("applied-permissions/admin"), true},
                {ImmutableList.of("api:*", "applied-permissions/admin"), true},
                {ImmutableList.of("applied-permissions/nonadmin"), false},
                {ImmutableList.of("applied-permissions/adminn"), false},
                {ImmutableList.of("applied-permissions:admin"), false},
                {ImmutableList.of("applied-permission/admin"), false},
                {ImmutableList.of("applied-permission"), false},
                {ImmutableList.of("admin"), false},
                {ImmutableList.of("applied-permissions/user"), false},
                {ImmutableList.of("jfrt@xyz:admin"), true},
                {ImmutableList.of("api:*", "jfrt@xyz:admin"), true},
                {ImmutableList.of("jfxr@xyz:admin"), false},
                {ImmutableList.of("api:*", "jfxr@xyz:admin"), false},
                {ImmutableList.of("jfrt@abcd:admin"), false},
                {ImmutableList.of("api:*", "jfrt@abcd:admin"), false},
                {ImmutableList.of("jfrt@xyz:user"), false},
        };
    }
}