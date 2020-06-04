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

package org.artifactory.webapp.servlet.authentication;

import org.artifactory.security.ldap.LdapRealmAwareAuthentication;
import org.artifactory.webapp.servlet.RequestUtils;
import org.junit.Assert;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author dudim
 */
@PrepareForTest({RequestUtils.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.parsers.*", "org.xml.*", "javax.security.*"})
public class ArtifactoryBasicAuthenticationFilterTest extends PowerMockTestCase {

    @Mock
    private ArtifactoryBasicAuthenticationFilter artifactoryBasicAuthenticationFilter;
    @Mock
    private LdapRealmAwareAuthentication ldapRealmAwareAuthentication;

    @BeforeMethod
    public void setUp() throws Exception {
        PowerMockito.mockStatic(RequestUtils.class);
    }

    /**
     * RTFACT-15967
     * BasicAuthenticationFilter is used when authenticating LDAP users,
     * when first time authenticate success, the user creds is stored in AccessFilter:nonUiAuthCache,
     * when doing second time authentication, we check if user creds already exist,
     * this check is failed due case sensitive issue.
     */
    @Test
    public void testRequiresReAuthentication() throws Exception {
        when(artifactoryBasicAuthenticationFilter.acceptFilter(any())).thenReturn(true);
        when(ldapRealmAwareAuthentication.getPrincipal()).thenReturn("dudim");
        when(artifactoryBasicAuthenticationFilter.requiresReAuthentication(any(), any())).thenCallRealMethod();
        PowerMockito.when(RequestUtils.extractUsernameFromRequest(any())).thenReturn("DUDIM");
        Assert.assertFalse(
                artifactoryBasicAuthenticationFilter.requiresReAuthentication(null, ldapRealmAwareAuthentication));
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }
}