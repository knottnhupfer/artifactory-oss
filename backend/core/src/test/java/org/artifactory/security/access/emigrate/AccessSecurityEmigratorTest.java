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

package org.artifactory.security.access.emigrate;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.model.xstream.security.*;
import org.artifactory.security.*;
import org.artifactory.security.access.emigrate.conveter.AccessSecurityEmigratorImpl;
import org.artifactory.storage.db.security.service.access.emigrate.SecurityEmigratorFetchers;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.access.model.Realm;
import org.jfrog.common.ClockUtils;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Shemesh
 */
public class AccessSecurityEmigratorTest extends ArtifactoryHomeBoundTest {
    private InternalSecurityService internalSecurityService;

    private SecurityEmigratorFetchers securityEmigratorFetchers;
    private AccessSecurityEmigratorImpl accessSecurityEmigrator;
    private ArgumentCaptor<SecurityInfo> securityInfo;

    @BeforeClass
    public void beforeClass() {
        // Clean env to make sure the original test fails on stubbing instead of this one
        ArtifactoryHome.unbind();
        ArtifactoryContextThreadBinder.unbind();
    }

    @BeforeMethod
    public void beforeMethod() {
        securityEmigratorFetchers = mock(SecurityEmigratorFetchers.class);
        internalSecurityService = mock(InternalSecurityService.class);

        accessSecurityEmigrator = new AccessSecurityEmigratorImpl(securityEmigratorFetchers);
        accessSecurityEmigrator.setSecurityService(internalSecurityService);
    }

    @Test
    public void shouldGenerateCorrectSecurityInfo() {
        regularExpects();

        Arrays.stream(Objects.requireNonNull(ArtifactoryHome.get().getEtcDir()
                .listFiles(file -> file.getName().startsWith("export.security."))))
                .forEach(FileUtils::deleteQuietly);

        accessSecurityEmigrator.convert();
        verifySecurityInfo();

        assertTrue(Objects.requireNonNull(ArtifactoryHome.get().getEtcDir()
                .listFiles(file -> file.getName().startsWith("export.security."))).length > 0);

        testUsersInSecurityInfo();

        assertEquals(securityInfo.getValue().getRepoAcls().size(), 2);
        assertEquals(securityInfo.getValue().getRepoAcls().stream()
                .map(RepoAcl::getPermissionTarget)
                .map(RepoPermissionTarget::getName)
                .collect(Collectors.toSet()), Sets.newHashSet("permission-target-1", "permission-target-2"));
        assertEquals(securityInfo.getValue().getGroups().size(), 1);
        assertEquals(securityInfo.getValue().getVersion(), "v9");

        verifyMocks();
    }

    @Test
    public void shouldGenerateEmptySecurityInfo() {
        emptyExpects();

        accessSecurityEmigrator.convert();
        verifySecurityInfo();

        assertEquals(securityInfo.getValue().getUsers().size(), 0);
        assertEquals(securityInfo.getValue().getRepoAcls().size(), 0);
        assertEquals(securityInfo.getValue().getGroups().size(), 0);
        assertEquals(securityInfo.getValue().getVersion(), "v9");

        verifyMocks();
    }

    private void regularExpects() {
        when(securityEmigratorFetchers.getAllUserInfos()).thenReturn(getUsers());
        when(securityEmigratorFetchers.getAllGroupInfos()).thenReturn(getGroups());
        when(securityEmigratorFetchers.getAllAclInfos()).thenReturn(getAcls());
    }

    private void emptyExpects() {
        when(securityEmigratorFetchers.getAllUserInfos()).thenReturn(Collections.emptyList());
        when(securityEmigratorFetchers.getAllGroupInfos()).thenReturn(Collections.emptyList());
        when(securityEmigratorFetchers.getAllAclInfos()).thenReturn(Collections.emptyList());
    }

    private void verifyMocks() {
        verify(securityEmigratorFetchers).getAllUserInfos();
        verify(securityEmigratorFetchers).getAllGroupInfos();
        verify(securityEmigratorFetchers).getAllAclInfos();
    }

    private void verifySecurityInfo() {
        securityInfo = ArgumentCaptor.forClass(SecurityInfo.class);
        verify(internalSecurityService).importSecurityData(securityInfo.capture());
    }

    private List<GroupInfo> getGroups() {
        return Lists.newArrayList(
                new GroupImpl("group")
        );
    }

    private List<UserInfo> getUsers() {
        UserImpl me = new UserImpl("me");
        me.addGroup("group");

        UserImpl ldapExternalUser = new UserImpl("ldap");
        ldapExternalUser.setPasswordDisabled(true);
        ldapExternalUser.setRealm(Realm.LDAP.getName());

        UserImpl crowdExternalUser = new UserImpl("crowd");
        crowdExternalUser.setPasswordDisabled(true);
        crowdExternalUser.setRealm(Realm.CROWD.getName());

        UserImpl samlExternalUser = new UserImpl("saml");
        samlExternalUser.setPasswordDisabled(true);
        samlExternalUser.setRealm(Realm.SAML.getName());

        UserImpl internalUserWithDisabledPassword = new UserImpl("internalNoPass");
        internalUserWithDisabledPassword.setPasswordDisabled(true);
        internalUserWithDisabledPassword.setRealm(Realm.INTERNAL.getName());

        UserImpl internalUser = new UserImpl("internal");
        internalUser.setPasswordDisabled(false);
        internalUser.setRealm(Realm.INTERNAL.getName());

        UserImpl internalOldUser = new UserImpl("internalOld");
        internalOldUser.setPasswordDisabled(false);
        internalOldUser.setRealm("artifactory");

        return Lists.newArrayList(
                me,
                new UserImpl("notme"),
                ldapExternalUser,
                crowdExternalUser,
                samlExternalUser,
                internalUserWithDisabledPassword,
                internalUser,
                internalOldUser
        );
    }

    private List<RepoAcl> getAcls() {
        return Lists.newArrayList(
                new MutableRepoAclImpl(
                        new RepoPermissionTargetImpl("permission-target-1", Lists.newArrayList("repo"), "", "**"),
                        Sets.newHashSet(new AceImpl("group", true, 1), new AceImpl("user", false, 0)),
                        "me", ClockUtils.epochMillis()
                ),
                new MutableRepoAclImpl(
                        new RepoPermissionTargetImpl("permission-target-2", Lists.newArrayList("repo2"), "**", ""),
                        Sets.newHashSet(new AceImpl("user3", false, 0)),
                        "notme", ClockUtils.epochMillis()
                ));
    }

    private void testUsersInSecurityInfo() {
        assertEquals(securityInfo.getValue().getUsers().size(), 8);
        assertEquals(securityInfo.getValue().getUsers().stream().map(UserInfo::getUsername).collect(Collectors.toSet()), Sets
                .newHashSet("me", "notme", "ldap", "crowd", "saml", "internalNoPass", "internal", "internalOld" ));

        securityInfo.getValue().getUsers().forEach(user -> {
            switch (user.getUsername()) {
                case "ldap":
                case "crowd":
                case "saml":
                case "internalNoPass":
                    assertTrue(user.isPasswordDisabled());
                    break;
                case "internal":
                case "internalOld":
                    assertFalse(user.isPasswordDisabled());
                    break;
            }
        });
    }
}