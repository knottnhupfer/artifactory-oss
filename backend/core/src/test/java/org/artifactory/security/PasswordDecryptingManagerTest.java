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

package org.artifactory.security;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.xray.XrayAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.repo.XrayDescriptor;
import org.artifactory.descriptor.security.EncryptionPolicy;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.security.access.AccessTokenAuthenticationProvider;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author maximy
 */
public class PasswordDecryptingManagerTest {

    @Test(expectedExceptions = PasswordEncryptionException.class, expectedExceptionsMessageRegExp =
            "Artifactory configured to accept only encrypted passwords but received a clear text password, getting the encrypted password can be done via the WebUI.")
    public void testAuthenticateThrowsPasswordEncryptionExceptionForUnencryptedPassword() {
        PasswordDecryptingManager passwordDecryptingManager = buildPasswordDecryptingManager(null);

        passwordDecryptingManager
                .authenticate(new UsernamePasswordAuthenticationToken("userName", "unencryptedPassword"));
    }

    @Test(expectedExceptions = PasswordEncryptionException.class, expectedExceptionsMessageRegExp =
            "Artifactory configured to accept only encrypted passwords but received a clear text password, getting the encrypted password can be done via the WebUI.")
    public void testXrayBypassPasswordPolicy() {
        PasswordDecryptingManager passwordDecryptingManager = buildPasswordDecryptingManager(null);

        passwordDecryptingManager
                .authenticate(new UsernamePasswordAuthenticationToken("xray", "unencryptedPassword12"));
    }

    @Test
    public void testAuthenticateWithAnonymousUserAndEmptyPasswordDoesNotThrowPasswordEncryptionException() {
        UserGroupService userGroupService = mock(UserGroupService.class);
        PasswordDecryptingManager passwordDecryptingManager = buildPasswordDecryptingManager(userGroupService);

        passwordDecryptingManager.authenticate(new AnonymousAuthenticationToken());

        //make sure decryption not invoked - needsDecryption() returned FALSE
        verify(userGroupService, times(0)).findUser(any());
    }

    private static PasswordDecryptingManager buildPasswordDecryptingManager(UserGroupService userGroupService) {
        // centralConfig
        CentralConfigService centralConfig = mock(CentralConfigService.class);
        CentralConfigDescriptorImpl configDescriptor = new CentralConfigDescriptorImpl();
        SecurityDescriptor security = new SecurityDescriptor();
        PasswordSettings passwordSettings = new PasswordSettings();
        passwordSettings.setEncryptionPolicy(EncryptionPolicy.REQUIRED);
        security.setPasswordSettings(passwordSettings);
        configDescriptor.setSecurity(security);

        XrayDescriptor xrayDescriptor = new XrayDescriptor();
        xrayDescriptor.setUser("xrayConfigUser");
        configDescriptor.setXrayConfig(xrayDescriptor);
        when(centralConfig.getDescriptor()).thenReturn(configDescriptor);
        // authService
        AuthorizationService authService = mock(AuthorizationService.class);
        AddonsManager addonsManager = mock(AddonsManager.class);
        XrayAddon xray = mock(XrayAddon.class);
        when(xray.isDecryptionNeeded(Mockito.anyString())).thenReturn(true);
        when(addonsManager.addonByType(XrayAddon.class)).thenReturn(xray);
        when(authService.isAnonymous()).thenReturn(true);
        //passwordDecryptingManager
        PasswordDecryptingManager passwordDecryptingManager = new PasswordDecryptingManager(authService, centralConfig,
                userGroupService, addonsManager);
        //DelegateAccessToken
        AccessTokenAuthenticationProvider tokenAuthProvider = mock(AccessTokenAuthenticationProvider.class);
        when(tokenAuthProvider.isAccessToken(any())).thenReturn(false);
        passwordDecryptingManager.setDelegateAccessToken(tokenAuthProvider);
        //Delegate
        passwordDecryptingManager.setDelegate(mock(AuthenticationManager.class));

        return passwordDecryptingManager;
    }
}