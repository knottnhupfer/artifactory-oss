package org.artifactory.ui.rest.service.admin.security.crowdsso;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.sso.crowd.CrowdAddon;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.ui.rest.model.admin.security.login.UserLoginSso;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertTrue;


/**
 * @author Yoaz Menda
 */
public class CrowdSsoLoginServiceTest {

    private CrowdSsoLoginService crowdSsoLoginService;
    @Mock
    private ArtifactoryRestRequest request;

    private ArtifactoryRestResponse respose;
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private CrowdAddon crowdAddon;


    @BeforeMethod
    public void setup() throws Exception {
        respose = new ArtifactoryRestResponse();
        initMocks(this);
        when(addonsManager.addonByType(CrowdAddon.class)).thenReturn(crowdAddon);
        doNothing().when(crowdAddon)
                .loginSso(anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));
        crowdSsoLoginService = new CrowdSsoLoginService(addonsManager);
    }

    @Test
    public void testExecute() {
        UserLoginSso userLoginSso = new UserLoginSso("mySsoCookieValue");
        when(request.getImodel()).thenReturn(userLoginSso);
        crowdSsoLoginService.execute(request, respose);
    }

    @Test
    public void testExecuteCookieMissing() {
        when(request.getImodel()).thenReturn(null);
        crowdSsoLoginService.execute(request, respose);
        String entity = (String) respose.buildResponse().getEntity();
        assertTrue(entity.contains("SSO login info must be present"));
    }

    @Test
    public void testExecuteCookieValueMissing() {
        UserLoginSso userLoginSso = new UserLoginSso();
        when(request.getImodel()).thenReturn(userLoginSso);
        crowdSsoLoginService.execute(request, respose);
        String entity = (String) respose.buildResponse().getEntity();
        assertTrue(entity.contains("SSO login info must be present"));
    }

    @Test
    public void testExecuteCookieTooShort() {
        UserLoginSso userLoginSso = new UserLoginSso("a");
        when(request.getImodel()).thenReturn(userLoginSso);
        crowdSsoLoginService.execute(request, respose);
        String entity = (String) respose.buildResponse().getEntity();
        assertTrue(entity.contains("SSO login info must be present"));
    }
}