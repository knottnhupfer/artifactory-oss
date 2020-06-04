package org.artifactory.engine;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.InternalRequestContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Shay Bagants
 */
@Test
public class DownloadServiceImplTest {

    private DownloadServiceImpl downloadService = new DownloadServiceImpl();

    @Mock
    private InternalRequestContext requestContext;
    @Mock
    private ArtifactoryRequest request;
    @Mock
    private AuthorizationService authorizationService;

    @BeforeMethod
    private void setup() {
        MockitoAnnotations.initMocks(this);
        when(requestContext.getRequest()).thenReturn(request);
        downloadService.authorizationService = authorizationService;
    }

    @Test(dataProvider = "provideRequestData")
    public void testGetXrayUserName(String userAgent, String principal, String expectedResult) {
        when(authorizationService.currentUsername()).thenReturn(principal);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        String result = downloadService.getXrayUserName(requestContext);
        Assert.assertEquals(result, expectedResult);
    }

    @DataProvider
    public static Object[][] provideRequestData() {
        return new Object[][]{
                //agent, user, expectedResult
                {"xray/1.0", "token:jfxr@123", "xray"},
                {"xray/1.0", "xray", "xray"},
                {"xray/1.0", "test-user", null},
                {"jfrog-cli", "xray", null},
                {"xray/1.0", "token:jfrt@123", null},
                {"jfrog-cli", "token:jfxr@123", null},
                {"x-ray/1.0", "token:jfxr@123", null},
        };
    }
}