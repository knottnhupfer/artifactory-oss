/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.service.admin.security.auth.login;

import org.apache.commons.io.FileUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.admin.security.general.SecurityConfig;
import org.artifactory.ui.rest.model.admin.security.login.UserLogin;
import org.artifactory.ui.rest.service.admin.security.general.GetSecurityConfigService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Shay Bagants
 */
@Test
public class LoginServiceTest {

    private LoginService loginService = new LoginService();
    private ArtifactoryHome artifactoryHome;
    private File homedir;

    @Mock
    private ArtifactoryRestRequest requestMock;
    @Mock
    private RestResponse responseMock;
    @Mock
    private HttpServletRequest servletRequestMock;
    @Mock
    private ArtifactoryContext artifactoryContextMock;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private GetSecurityConfigService getSecurityConfigService;
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private PluginsAddon pluginsAddon;
    @Mock
    private InfoFactory infoFactory;
    @Mock
    private SecurityService securityService;
    @Mock
    private CentralConfigService centralConfigService;
    @Mock
    private CentralConfigDescriptor configDescriptor;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private SecurityDescriptor securityDescriptor;

    @BeforeMethod
    private void beforeMethod() throws IOException {
        prepareHome();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(loginService, "userGroupService", userGroupService);
        ReflectionTestUtils.setField(loginService, "getSecurityConfigService", getSecurityConfigService);
        when(requestMock.getServletRequest()).thenReturn(servletRequestMock);
        ReflectionTestUtils.setField(loginService, "addonsManager", addonsManager);
        when(artifactoryContextMock.getCentralConfig()).thenReturn(centralConfigService);
        when(centralConfigService.getDescriptor()).thenReturn(configDescriptor);
        when(configDescriptor.getSecurity()).thenReturn(securityDescriptor);
        when(securityDescriptor.getCrowdSettings()).thenReturn(null);
        ReflectionTestUtils.setField(loginService, "authorizationService", authorizationService);
        when(artifactoryContextMock.beanForType(SecurityService.class)).thenReturn(securityService);
        ArtifactoryContextThreadBinder.bind(artifactoryContextMock);
    }

    @AfterMethod
    private void cleanup() throws IOException {
        ArtifactoryHome.unbind();
        ArtifactoryContextThreadBinder.unbind();
        FileUtils.deleteDirectory(homedir);
    }

    @Test
    public void testLastLoginUpdateOnUiAuthentication() throws NoSuchFieldException, IllegalAccessException {
        when(servletRequestMock.getRemoteAddr()).thenReturn("10.0.0.1");

        UserLogin user = new UserLogin("myuser");
        user.setPassword("password");
        when(requestMock.getImodel()).thenReturn(user);

        prepareAuthenticationMock(user);
        prepareUserGroupServiceMock(user);
        SecurityConfig securityConfig = new SecurityConfig(true, false, false, true, new PasswordSettings(), null);
        when(responseMock.getIModel()).thenReturn(securityConfig);

        injectInfoFactory();
        prepareAddonsManager();
        prepateAuthorizationService();

        loginService.doExecute(requestMock, responseMock);
        verify(getSecurityConfigService, times(1)).execute(requestMock, responseMock);
        // ensure last login time is called
        verify(securityService, times(1)).updateUserLastLogin(any(), anyLong(), any());
    }

    @Test
    public void testCrowdHeader() throws NoSuchFieldException, IllegalAccessException {
        when(servletRequestMock.getRemoteAddr()).thenReturn("10.0.0.1");

        UserLogin user = new UserLogin("myuser");
        user.setPassword("password");
        when(requestMock.getImodel()).thenReturn(user);

        prepareAuthenticationMock(user);
        prepareUserGroupServiceMock(user);
        SecurityConfig securityConfig = new SecurityConfig(true, false, false, true, new PasswordSettings(), null);
        when(responseMock.getIModel()).thenReturn(securityConfig);
        CrowdSettings crowdSettings = mock(CrowdSettings.class);
        when(crowdSettings.isEnableIntegration()).thenReturn(true);
        when(crowdSettings.getSessionValidationInterval()).thenReturn(2L);
        when(securityDescriptor.getCrowdSettings()).thenReturn(crowdSettings);
        injectInfoFactory();
        prepareAddonsManager();
        prepateAuthorizationService();
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        doNothing().when(servletResponse).addHeader(anyString(), anyString());
        when(responseMock.getServletResponse()).thenReturn(servletResponse);
        loginService.doExecute(requestMock, responseMock);
        verify(servletResponse, times(1)).addHeader(anyString(), anyString());
        verify(getSecurityConfigService, times(1)).execute(requestMock, responseMock);
        // ensure last login time is called
        verify(securityService, times(1)).updateUserLastLogin(any(), anyLong(), any());
    }

    private void prepareUserGroupServiceMock(UserLogin user) {
        Properties props = Mockito.mock(Properties.class);
        when(props.get(anyString())).thenReturn(null);
        when(userGroupService.findPropertiesForUser(user.getUser())).thenReturn(props);
    }

    private void prepareAuthenticationMock(UserLogin user) {
        when(artifactoryContextMock.beanForType("authenticationManager", AuthenticationManager.class))
                .thenReturn(authenticationManager);
        Authentication authenticationMock = Mockito.mock(Authentication.class);
        when(authenticationMock.isAuthenticated()).thenReturn(true);
        when(authenticationMock.getPrincipal()).thenReturn(user.getUser());
        when(authenticationManager.authenticate(any())).thenReturn(authenticationMock);
    }

    private void prepareAddonsManager() {
        when(addonsManager.addonByType(PluginsAddon.class)).thenReturn(pluginsAddon);
        when(addonsManager.isLicenseInstalled()).thenReturn(true);
        when(artifactoryContextMock.beanForType(AddonsManager.class)).thenReturn(addonsManager);
    }

    private void prepateAuthorizationService() {
        when(authorizationService.isAdmin()).thenReturn(true);
        when(authorizationService.isUpdatableProfile()).thenReturn(true);
        when(authorizationService.requireProfileUnlock()).thenReturn(false);
        when(authorizationService.requireProfilePassword()).thenReturn(false);
        when(authorizationService.isTransientUser()).thenReturn(true);
    }

    private void prepareHome() throws IOException {
        homedir = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
        artifactoryHome = new ArtifactoryHome(homedir);
        artifactoryHome.initArtifactorySystemProperties();
        artifactoryHome.getArtifactoryProperties().setProperty("artifactory.security.disableRememberMe", "true");
        artifactoryHome.getArtifactoryProperties().setProperty("version.query.enabled", "false");
        ArtifactoryHome.bind(artifactoryHome);
    }

    private void injectInfoFactory() throws NoSuchFieldException, IllegalAccessException {
        Field field = InfoFactoryHolder.class.getDeclaredField("DEFAULT_FACTORY");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, infoFactory);
    }
}