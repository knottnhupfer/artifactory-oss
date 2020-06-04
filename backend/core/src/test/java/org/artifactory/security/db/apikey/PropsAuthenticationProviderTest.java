/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2019 JFrog Ltd.
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
package org.artifactory.security.db.apikey;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.security.ldap.LdapService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.model.xstream.security.UserImpl;
import org.artifactory.security.SimpleUser;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.artifactory.security.access.AccessUserPassAuthenticationProvider.ACCESS_REALM;
import static org.artifactory.security.props.auth.ApiKeyManager.API_KEY;
import static org.jfrog.access.model.Realm.LDAP;
import static org.mockito.Mockito.*;

/**
 * @author Yuval Reches
 */
@Test
public class PropsAuthenticationProviderTest extends ArtifactoryHomeBoundTest {

    @Mock
    private UserGroupStoreService userGroupStore;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private CentralConfigService centralConfig;
    @Mock
    private LdapService ldapService;
    @Mock
    private AddonsManager addonsManager;
    @Mock
    private SecurityService securityService;

    private PropsAuthenticationProvider propsAuthenticationProvider;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Prepare for LDAP testing - make sure we're not trying to get out to LDAP
        homeStub.setProperty(ConstantValues.ldapCleanGroupOnFail, "false");
        MutableCentralConfigDescriptor config = mock(MutableCentralConfigDescriptor.class);
        Mockito.when(centralConfig.getMutableDescriptor()).thenReturn(config);
        SecurityDescriptor security = mock(SecurityDescriptor.class);
        when(config.getSecurity()).thenReturn(security);
        when(security.getLdapGroupSettings()).thenReturn(null);

        propsAuthenticationProvider = new PropsAuthenticationProvider(userGroupStore, userGroupService, centralConfig,
                ldapService, addonsManager, securityService);
    }

    // Verifying the exception thrown - means LDAP is SUPPOSED to be reached, which is what we expect
    @Test(expectedExceptions = InternalAuthenticationServiceException.class)
    public void testLdapTriggering() {
        SimpleUser admin = getSimpleUser(LDAP.getName());
        propsAuthenticationProvider.retrieveLdapUserDetailsInCaseOfApiKey(API_KEY, admin);
        verify(centralConfig, times(1)).getMutableDescriptor();
    }

    @Test
    public void testLdapTriggeringNotAPIKey() {
        SimpleUser admin = getSimpleUser(LDAP.getName());
        propsAuthenticationProvider.retrieveLdapUserDetailsInCaseOfApiKey("otherToken", admin);
        // Verifying the LDAP config is not fetched, since there is no reason to update the user - not API KEY request
        verify(centralConfig, times(0)).getMutableDescriptor();
    }

    @Test
    public void testLdapTriggeringNotLdapRealm() {
        SimpleUser admin = getSimpleUser(ACCESS_REALM);
        propsAuthenticationProvider.retrieveLdapUserDetailsInCaseOfApiKey(API_KEY, admin);
        // Verifying the LDAP config is not fetched, since there is no reason to update the user - not LDAP user request
        verify(centralConfig, times(0)).getMutableDescriptor();
    }

    private SimpleUser getSimpleUser(String realm) {
        UserImpl adminUser = new UserImpl("admin");
        adminUser.setRealm(realm);
        return new SimpleUser(adminUser);
    }
}