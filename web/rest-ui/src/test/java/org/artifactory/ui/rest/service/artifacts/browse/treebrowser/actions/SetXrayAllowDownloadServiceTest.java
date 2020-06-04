package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.artifact.BaseArtifact;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.rest.common.service.RestResponse;
import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;

/**
 * @author Alexei Vainshtein
 */
public class SetXrayAllowDownloadServiceTest {

    @InjectMocks
    private SetXrayAllowDownloadService setXrayAllowDownloadService;

    @Spy
    private AddonsManager addonsManager;

    @Spy
    private AuthorizationService authorizationService;

    @Mock
    private RestResponse response;

    @Mock
    private ArtifactoryRestRequest request;

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "isAdminAllowDownloadProvider")
    public void testExecute(boolean isAdmin) {
        ArtifactoryRestResponse artifactoryRestResponse = Mockito.mock(ArtifactoryRestResponse.class);
        BaseArtifact baseArtifact = new BaseArtifact();
        baseArtifact.setRepoKey("docker-local");
        baseArtifact.setPath("test/t/manifest.json");
        when(request.getImodel()).thenReturn(baseArtifact);
        when(response.responseCode(HttpServletResponse.SC_FORBIDDEN)).thenReturn(artifactoryRestResponse);
        when(artifactoryRestResponse.buildResponse()).thenReturn(Response.noContent().build());
        Mockito.doReturn(isAdmin).when(authorizationService).isAdmin();
        XrayAddon xrayAddon = mock(XrayAddon.class);
        doReturn(false).when(xrayAddon).setAlertIgnored(any());
        doReturn(xrayAddon).when(addonsManager).addonByType(XrayAddon.class);
        setXrayAllowDownloadService.execute(request, response);
        verify(addonsManager.addonByType(XrayAddon.class), isAdmin ? times(1) : times(0)).setAlertIgnored(any());
    }

    @DataProvider
    public Object[][] isAdminAllowDownloadProvider() {
        return new Object[][]{
                {true},
                {false}
        };
    }
}