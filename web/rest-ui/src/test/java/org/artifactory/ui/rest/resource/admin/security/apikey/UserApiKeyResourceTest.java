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

package org.artifactory.ui.rest.resource.admin.security.apikey;

import org.apache.http.HttpHeaders;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.artifactory.ui.rest.service.admin.security.user.userprofile.UserProfileHelperService;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * @author Bar Haim
 */
public class UserApiKeyResourceTest {

    @Test
    public void testIsAllowApiKeyAccessUserAuthenticatedNoAuthHeader() throws Exception {
        UserInfo userInfo = mock(UserInfo.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        SecurityServiceFactory securityService = mock(SecurityServiceFactory.class);
        UserProfileHelperService userProfileHelperService = mock(UserProfileHelperService.class);
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        UserApiKeyResource userApiKeyResource = new UserApiKeyResource(securityService,
                userProfileHelperService, authorizationService);
        Field requestField = UserApiKeyResource.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(userApiKeyResource,httpServletRequest);
        when(authorizationService.requireProfileUnlock()).thenReturn(false);
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(userProfileHelperService.loadUserInfo()).thenReturn(userInfo);
        when(userInfo.isAnonymous()).thenReturn(false);
        assertTrue(isBlank(userApiKeyResource.isAllowApiKeyAccess()));
    }

    @Test
    public void testIsAllowApiKeyAccessAuthenticateWithPassword() throws Exception {
        UserInfo userInfo = mock(UserInfo.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic YWRtaW46cGFzc3dvcmQ=");
        SecurityServiceFactory securityService = mock(SecurityServiceFactory.class);
        UserProfileHelperService userProfileHelperService = mock(UserProfileHelperService.class);
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        UserApiKeyResource userApiKeyResource = new UserApiKeyResource(securityService,
                userProfileHelperService, authorizationService);
        when(userProfileHelperService.authenticate(userInfo,"password")).thenReturn(true);
        Field requestField = UserApiKeyResource.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(userApiKeyResource,httpServletRequest);
        when(authorizationService.requireProfileUnlock()).thenReturn(true);
        when(userProfileHelperService.loadUserInfo()).thenReturn(userInfo);
        when(userInfo.getUsername()).thenReturn("admin");
        when(userInfo.isAnonymous()).thenReturn(false);
        assertTrue(isBlank(userApiKeyResource.isAllowApiKeyAccess()));
    }

}