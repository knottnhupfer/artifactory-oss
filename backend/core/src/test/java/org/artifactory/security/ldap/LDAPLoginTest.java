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

package org.artifactory.security.ldap;

import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.easymock.EasyMock;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Inbar Tal
 *
 * Unit test for LDAP login in case the user doesn't have the "list contents" permission
 * but the Manager DN has the permission - See RTFACT-14132
 */
public class LDAPLoginTest extends ArtifactoryHomeBoundTest {

    private static final String BASE_LDAP_URL = "ldap://localhost:5555/dc=jfrog,dc=org";
    private static final String USER_NAME_WITH_LIST_CONTENTS = "userListContents";
    private static final String USER_PASSWORD_WITH_LIST_CONTENTS = "userListContents1234" ;
    private static final String BASE_LDAP_DN = "dc = jfrog,dc = org";
    private static final String USER_NAME_WITHOUT_LIST_CONTENTS = "userWithoutListContents";
    private static final String USER_PASSWORD_WITHOUT_LIST_CONTENTS = "userWithoutListContents1234";
    private static final String USER_WITH_LIST_CONTENTS_DN = "uid=userListContents,ou=People";
    private static final String USER_WITHOUT_LIST_CONTENTS_DN_ADMIN = "uid=userWithoutListContentsAdmin,ou=People";
    private static final String USER_WITH_LIST_CONTENTS_DN_ADMIN = "uid=userWithListContentsAdmin,ou=People";


    @Test(description = "RTFACT-14132")
    public void testLdapLoginAsUserWithListContentsPermission() {
         //create authenticator with dn pattern in ldap settings and mock context
        ArtifactoryBindAuthenticator authenticator = authenticateWithDnPattern();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(USER_NAME_WITH_LIST_CONTENTS,
                        USER_PASSWORD_WITH_LIST_CONTENTS);
        //simulate authentication to active directory with user that has "list contents" permission
        DirContextOperations user = authenticator.authenticate(authentication);
        assertThat(user.getDn().toString()).isEqualTo(USER_WITH_LIST_CONTENTS_DN);
    }

    @Test(description = "RTFACT-14132", expectedExceptions = Exception.class)
    public void testLdapLoginAsUserWithoutListContentsPermission() {
        //create authenticator with dn pattern in ldap settings and mock context
        ArtifactoryBindAuthenticator authenticator = authenticateWithDnPattern();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(USER_NAME_WITHOUT_LIST_CONTENTS,
                        USER_PASSWORD_WITHOUT_LIST_CONTENTS);
        //simulate authentication to active directory with user that doesn't have "list contents" permission
        authenticator.authenticate(authentication);
    }

    @Test(description = "RTFACT-14132, falky on jenkins", enabled = false)
    public void testLdapLoginAsAdminWithListContentsPermission() {
        //create authenticator with search filter in ldap settings and mock context
        ArtifactoryBindAuthenticator authenticator = authenticateWithSearchFilter();
        //simulate authentication to active directory with user that has "list contents" permission
        DirContextOperations user = login(USER_NAME_WITH_LIST_CONTENTS, USER_PASSWORD_WITH_LIST_CONTENTS,
                USER_WITH_LIST_CONTENTS_DN_ADMIN, authenticator);
        assertThat(user.getDn().toString()).isEqualTo(USER_WITH_LIST_CONTENTS_DN_ADMIN);
    }

    @Test(description = "RTFACT-14132, flaky on jenkins", enabled = false)
    public void testLdapLoginAsAdminWithoutListContentsPermission() {
        //create authenticator with search filter in ldap settings and mock context
        ArtifactoryBindAuthenticator authenticator = authenticateWithSearchFilter();
        //simulate authentication to active directory with user that doesn't have "list contents" permission
        DirContextOperations user = login(USER_NAME_WITHOUT_LIST_CONTENTS, USER_PASSWORD_WITHOUT_LIST_CONTENTS, USER_WITHOUT_LIST_CONTENTS_DN_ADMIN, authenticator);
        assertThat(user.getDn().toString()).isEqualTo(USER_WITHOUT_LIST_CONTENTS_DN_ADMIN);
    }

    private DirContextOperations login(String userName, String password, String userDn, ArtifactoryBindAuthenticator authenticator) {
        List<LdapUserSearch> searches = new ArrayList<>();
        setSearches(userName, userDn, searches);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userName, password);
        ReflectionTestUtils.setField(authenticator, "userSearches", searches);
        return authenticator.authenticate(authentication);
    }

    private void setSearches(String userName, String userDn, List<LdapUserSearch> searches) {
        DirContextOperations userFromSearch = new DirContextAdapter(null, new DistinguishedName(
                userDn), new DistinguishedName(BASE_LDAP_DN), null);
        LdapUserSearch search = EasyMock.createMock(LdapUserSearch.class);
        EasyMock.expect(search.searchForUser(userName)).andReturn(userFromSearch);
        searches.add(search);
        EasyMock.replay(search);
    }

    private ArtifactoryBindAuthenticator authenticateWithSearchFilter() {
        LdapSetting ldapSetting = createValidSearchSettings();
        LdapContextSource securityContext =
                ArtifactoryLdapAuthenticator.createSecurityContext(ldapSetting);
        ArtifactoryBindAuthenticator authenticator = new ArtifactoryBindAuthenticator(
                securityContext, ldapSetting, false);
        ReflectionTestUtils.setField(authenticator, "contextSource", new LdapContextSourceMock());
        try {
            ReflectionTestUtils.setField(authenticator.getContextSource(), "base", new LdapName(BASE_LDAP_DN));
        } catch (InvalidNameException e) {
            Assert.fail(e.toString());
        }
        return authenticator;
    }

    private ArtifactoryBindAuthenticator authenticateWithDnPattern() {
        LdapSetting ldapSetting = createValidUserDnSettings();
        LdapContextSource securityContext =
                ArtifactoryLdapAuthenticator.createSecurityContext(ldapSetting);
        ArtifactoryBindAuthenticator authenticator = new ArtifactoryBindAuthenticator(
                securityContext, ldapSetting, false);

        ReflectionTestUtils.setField(authenticator, "contextSource", new LdapContextSourceMock());
        try {
            ReflectionTestUtils.setField(authenticator.getContextSource(), "base", new LdapName(BASE_LDAP_DN));
        } catch (InvalidNameException e) {
            Assert.fail(e.toString());
        }
        return authenticator;
    }

    private LdapSetting createValidUserDnSettings() {
        LdapSetting ldapSetting = new LdapSetting();
        ldapSetting.setLdapUrl(BASE_LDAP_URL);
        ldapSetting.setUserDnPattern("uid={0},ou=People");
        return ldapSetting;
    }

    private LdapSetting createValidSearchSettings() {
        LdapSetting ldapSetting = new LdapSetting();
        ldapSetting.setLdapUrl(BASE_LDAP_URL);
        SearchPattern search = new SearchPattern();
        search.setSearchFilter("uid={0}");
        ldapSetting.setSearch(search);
        return ldapSetting;
    }
}
