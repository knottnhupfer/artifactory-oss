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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import junit.framework.AssertionFailedError;
import org.apache.commons.io.FileUtils;
import org.artifactory.api.security.ResetPasswordException;
import org.artifactory.api.security.SecurityListener;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserInfoBuilder;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.config.InternalCentralConfigService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.security.*;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.environment.converter.local.PreInitConverter;
import org.artifactory.exception.InvalidNameException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.security.*;
import org.artifactory.repo.*;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.access.emigrate.AccessConverters;
import org.artifactory.security.access.emigrate.conveter.AccessSecurityEmigratorImpl;
import org.artifactory.security.access.emigrate.conveter.V5100ConvertResourceTypeToRepo;
import org.artifactory.security.access.emigrate.conveter.V600DecryptAllUsersCustomData;
import org.artifactory.security.access.emigrate.conveter.V6600CreateDefaultBuildAcl;
import org.artifactory.security.exceptions.PasswordChangeException;
import org.artifactory.storage.security.service.AclCache;
import org.artifactory.storage.security.service.AclStoreService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.storage.security.service.UserLockInMemoryService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.util.NameValidator;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionProvider;
import org.artifactory.version.CompoundVersionDetails;
import org.easymock.EasyMock;
import org.jfrog.access.client.AccessClient;
import org.jfrog.access.client.user.UsersClient;
import org.jfrog.access.rest.user.UserResponse;
import org.jfrog.access.rest.user.UsersResponse;
import org.jfrog.security.crypto.CipherAlg;
import org.jfrog.security.file.SecurityFolderHelper;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.addon.build.BuildAddon.BUILD_INFO_REPO_NAME;
import static org.artifactory.api.security.AuthorizationService.ROLE_ADMIN;
import static org.artifactory.api.security.SecurityService.USER_SYSTEM;
import static org.artifactory.security.PermissionTarget.*;
import static org.easymock.EasyMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * SecurityServiceImpl unit tests.
 *
 * @author Yossi Shaul
 */
@Test
public class SecurityServiceImplTest extends ArtifactoryHomeBoundTest {

    public static final String SSH_KEY = "ssh.basictoken";

    private SecurityContextImpl securityContext;
    private SecurityServiceImpl service;
    private List<RepoAcl> testAcls;
    private AclStoreService aclStoreServiceMock;
    private InternalRepositoryService repositoryServiceMock;
    private InternalBuildService buildServiceMock;
    private LocalRepo localRepoMock;
    private LocalRepo cacheRepoMock;
    private DistributionRepo distRepoMock;
    private InternalCentralConfigService centralConfigServiceMock;
    private UserGroupStoreService userGroupStoreService;
    private SecurityListener securityListenerMock;
    private SecurityServiceImplTestHelper securityServiceImplTestHelper;
    private AclCache<RepoPermissionTarget> aclCache;
    private UserPassAuthenticationProvider userPassAuthenticationProvider;
    private AccessSecurityEmigratorImpl securityEmigrator;
    private V600DecryptAllUsersCustomData decryptAllUsersCustomData;
    private V5100ConvertResourceTypeToRepo resourceTypeToRepoConverter;
    private V6600CreateDefaultBuildAcl createDefaultBuildAcl;
    private AccessConverters accessConverters;

    private UserLockInMemoryService userLockInMemoryService;

    @DataProvider
    public static Object[][] applyEnabled() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @BeforeClass
    public void initArtifactoryRoles() {
        securityServiceImplTestHelper = new SecurityServiceImplTestHelper();
        testAcls = securityServiceImplTestHelper.createTestAcls();
        aclCache = securityServiceImplTestHelper.createUserAndGroupResultMap();
        aclStoreServiceMock = createMock(AclStoreService.class);
        repositoryServiceMock = createRepoServiceMock();
        buildServiceMock = createBuildServiceMock();
        centralConfigServiceMock = createMock(InternalCentralConfigService.class);
        userGroupStoreService = createMock(UserGroupStoreService.class);
        localRepoMock = createLocalRepoMock();
        cacheRepoMock = createCacheRepoMock();
        distRepoMock = createDistRepoMock();
        securityListenerMock = createMock(SecurityListener.class);
        userPassAuthenticationProvider = createMock(UserPassAuthenticationProvider.class);
        securityEmigrator = createMock(AccessSecurityEmigratorImpl.class);
        decryptAllUsersCustomData = createMock(V600DecryptAllUsersCustomData.class);
        resourceTypeToRepoConverter = createMock(V5100ConvertResourceTypeToRepo.class);
        createDefaultBuildAcl = createMock(V6600CreateDefaultBuildAcl.class);
        accessConverters = new AccessConverters(securityEmigrator, resourceTypeToRepoConverter, decryptAllUsersCustomData, createDefaultBuildAcl);
        userLockInMemoryService = createMock(UserLockInMemoryService.class);
    }

    @BeforeMethod
    public void setUp() {
        // create new security context
        securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);

        // new service instance
        service = new SecurityServiceImpl();
        // set the aclManager mock on the security service
        ReflectionTestUtils.setField(service, "userGroupStoreService", userGroupStoreService);
        ReflectionTestUtils.setField(service, "aclStoreService", aclStoreServiceMock);
        ReflectionTestUtils.setField(service, "repositoryService", repositoryServiceMock);
        ReflectionTestUtils.setField(service, "buildService", buildServiceMock);
        ReflectionTestUtils.setField(service, "centralConfig", centralConfigServiceMock);
        ReflectionTestUtils.setField(service, "userPassAuthenticationProvider", userPassAuthenticationProvider);
        ReflectionTestUtils.setField(service, "accessConverters", accessConverters);
        ReflectionTestUtils.setField(service, "userLockInMemoryService", userLockInMemoryService);

        // reset mocks
        reset(aclStoreServiceMock, repositoryServiceMock, centralConfigServiceMock, securityEmigrator,
                userLockInMemoryService, decryptAllUsersCustomData, resourceTypeToRepoConverter,
                createDefaultBuildAcl);

        FileUtils.deleteQuietly(homeStub.getAccessEmigrateMarkerFile());
        FileUtils.deleteQuietly(homeStub.getAccessUserCustomDataDecryptionMarkerFile());
    }

    public void isAdminOnAdminUser() {
        Authentication authentication = setAdminAuthentication();

        boolean admin = service.isAdmin();
        assertTrue(admin, "The user in test is admin");
        // un-authenticate
        authentication.setAuthenticated(false);
        admin = service.isAdmin();
        assertFalse(admin, "Unauthenticated token");
    }

    public void isAdminOnSimpleUser() {
        setSimpleUserAuthentication();

        boolean admin = service.isAdmin();
        assertFalse(admin, "The user in test is not an admin");
    }

    @Test(dependsOnMethods = "isAdminOnAdminUser")
    public void spidermanCanDoAnything() {
        setAdminAuthentication();
        assertFalse(service.isAnonymous());// sanity
        assertTrue(service.isAdmin());// sanity

        RepoPath path = InternalRepoPathFactory.create("someRepo", "blabla");
        boolean canRead = service.canRead(path);
        assertTrue(canRead);
        boolean canDeploy = service.canDeploy(path);
        assertTrue(canDeploy);
    }

    @Test
    public void getUserEffectiveSids() {
        SimpleUser userMock = Mockito.mock(SimpleUser.class);
        Mockito.when(userMock.getUsername()).thenReturn("tester");

        UserInfo userInfoMock = Mockito.mock(UserInfo.class);
        Mockito.when(userMock.getDescriptor()).thenReturn(userInfoMock);
        Set<UserGroupInfo> groups = new HashSet<>();
        for (int i=1; i<=1000; i++) {
            groups.add(new UserGroupImpl("grp" + i));
        }
        when(userInfoMock.getGroups()).thenReturn(groups);

        Set<ArtifactorySid> userEffectiveSids = SecurityServiceImpl.getUserEffectiveSids(userMock);
        // verify first sid is the actual user
        ArtifactorySid firstSid = userEffectiveSids.iterator().next();
        assertFalse(firstSid.isGroup());
        assertEquals(firstSid.getPrincipal(), "tester");

        // make sure all groups exists
        assertEquals(groups.size(), userEffectiveSids.stream().filter(ArtifactorySid::isGroup).count());

        // make sure all expected groups existed in the result
        Set<String> returnedGroupNames = userEffectiveSids.stream()
                .filter(sid -> !sid.getPrincipal().equals("tester"))
                .map(ArtifactorySid::getPrincipal)
                .collect(Collectors.toSet());

        Set<String> expectedGroupNames = groups.stream()
                .map(UserGroupInfo::getGroupName)
                .collect(Collectors.toSet());
        assertTrue(returnedGroupNames.containsAll(expectedGroupNames));
    }

    public void userReadAndDeployPermissions() {
        setSimpleUserAuthentication();

        RepoPath securedPath = InternalRepoPathFactory.create("securedRepo", "blabla");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(securedPath.getRepoKey())).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        // cannot read the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean hasPermissions =
                service.canRead(securedPath) || service.canManage(securedPath) || service.canDeploy(securedPath) ||
                        service.canDelete(securedPath);
        assertFalse(hasPermissions, "User should not have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);

        boolean canDeploy = service.canDeploy(securedPath);
        assertFalse(canDeploy, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "blabla");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(allowedReadPath.getRepoKey())).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(allowedReadPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey(allowedReadPath.getRepoKey())).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        // can read the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        hasPermissions = service.canRead(allowedReadPath);
        assertTrue(hasPermissions, "User should have read permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(allowedReadPath);
        assertFalse(canDeploy, "User should not have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock);

        // cannot admin the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(allowedReadPath);
        assertFalse(canAdmin, "User should not have permissions for this path");
        verify(aclStoreServiceMock);
    }

    public void adminRolePermissions() {
        // user with admin role on permission target 'target1'
        setSimpleUserAuthentication("yossis");

        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "blabla");

        // can read the specified path
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectGetAllAclsCall();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(allowedReadPath);
        assertTrue(canRead, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // can deploy to the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(allowedReadPath);
        assertTrue(canDeploy, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // can admin the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(allowedReadPath);
        assertTrue(canAdmin, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);
    }

    public void groupPermissions() {
        RepoPath allowedReadPath = InternalRepoPathFactory.create("testRepo1", "**");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey(allowedReadPath.getRepoKey())).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        replay(repositoryServiceMock);

        // cannot deploy to the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(allowedReadPath);
        assertFalse(canDeploy, "User should have permissions for this path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);

        // add the user to a group with permissions and expext permission garnted
        setSimpleUserAuthentication("userwithnopermissions", "deployGroup");
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(allowedReadPath);
        assertTrue(canDeploy, "User in a group with permissions for this path");
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    public void userWithPermissionsToAGroupWithTheSameName() {
        setSimpleUserAuthentication(securityServiceImplTestHelper.USER_AND_GROUP_SHARED_NAME,
                securityServiceImplTestHelper.USER_AND_GROUP_SHARED_NAME);

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "**");
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canRead = service.canRead(testRepo1Path);
        assertTrue(canRead, "User should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "**");
        expectGetAllAclsCallWithAnyArray();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        replay(aclStoreServiceMock);
        canRead = service.canRead(testRepo2Path);
        assertTrue(canRead, "User belongs to a group with permissions to the path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);
    }

    public void userWithPermissionsToANonUniqueGroupName() {
        // here we test that a user that belongs to a group which has
        // the same name of a nother user will only get the group permissions
        // and not the user with the same name permissions
        setSimpleUserAuthentication("auser", securityServiceImplTestHelper.USER_AND_GROUP_SHARED_NAME);

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "**");
        expectGetAllAclsCallWithAnyArray();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        replay(aclStoreServiceMock);
        boolean canRead = service.canRead(testRepo1Path);
        assertFalse(canRead, "User should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "**");
        expectGetAllAclsCallWithAnyArray();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        replay(aclStoreServiceMock, repositoryServiceMock);
        canRead = service.canRead(testRepo2Path);
        verify(repositoryServiceMock);
        assertTrue(canRead, "User belongs to a group with permissions to the path");
    }

    public void hasPermissionPassingUserInfo() {
        SimpleUser user = createNonAdminUser("yossis");
        UserInfo userInfo = user.getDescriptor();

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "any/path");

        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, repositoryServiceMock);
        boolean canRead = service.canRead(userInfo, testRepo1Path);
        assertTrue(canRead, "User should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(userInfo, testRepo1Path);
        assertTrue(canDeploy, "User should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDelete = service.canDelete(userInfo, testRepo1Path);
        assertFalse(canDelete, "User should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(userInfo, testRepo1Path);
        assertTrue(canAdmin, "User should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "**");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(userInfo, testRepo2Path);
        assertFalse(canRead, "User should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        SecurityDescriptor securityDescriptor = new SecurityDescriptor();
        securityDescriptor.setAnonAccessEnabled(false);

        CentralConfigDescriptor configDescriptor = createMock(CentralConfigDescriptor.class);
        expect(configDescriptor.getSecurity()).andReturn(securityDescriptor).anyTimes();
        replay(configDescriptor);
        expect(centralConfigServiceMock.getDescriptor()).andReturn(configDescriptor).anyTimes();
        replay(centralConfigServiceMock);

        SimpleUser anon = createNonAdminUser(UserInfo.ANONYMOUS);
        UserInfo anonUserInfo = anon.getDescriptor();

        RepoPath testMultiRepo = InternalRepoPathFactory.create("multi1", "**");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("multi1")).andReturn(cacheRepoMock).anyTimes();
        expectGetAllAclsCallWithAnyArray();
        replay(repositoryServiceMock);

        canRead = service.canRead(anonUserInfo, testMultiRepo);
        verify(repositoryServiceMock);
        assertFalse(canRead, "Anonymous user should have permissions for this path");
    }

    public void hasPermissionWithSpecificTarget() {
        SimpleUser user = createNonAdminUser("shay");
        UserInfo userInfo = user.getDescriptor();
        RepoPath testRepo1Path = InternalRepoPathFactory.create("specific-repo", "com", true);

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("specific-repo")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("specific-repo")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("specific-repo")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canRead = service.canRead(userInfo, testRepo1Path);
        assertTrue(canRead, "User should have read permissions for this path");
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(userInfo, testRepo1Path);
        assertTrue(canDeploy, "User should have deploy permissions for this path");
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDelete = service.canDelete(userInfo, testRepo1Path);
        assertFalse(canDelete, "User should not have delete permissions for this path");
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(userInfo, testRepo1Path);
        assertFalse(canAdmin, "User should not have admin permissions for this path");
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test(dataProvider = "applyEnabled")
    public void hasPermissionsOnRemoteRepoWhenUsingLocalRepoPermissions(boolean applyEnabled) {
        getBound().setProperty(ConstantValues.applyLocalReposPermissionsOnRemoteRepos, String.valueOf(applyEnabled));
        setSimpleUserAuthentication();

        expect(repositoryServiceMock.repositoryByKey("testRepo1")).andReturn(createRemoteRepoMock()).anyTimes();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1-cache")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1-cache"))
                .andReturn(null).anyTimes(); // Local remote repo
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1"))
                .andReturn(new HttpRepoDescriptor()).anyTimes(); // Local remote repo
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1-cache")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        // cannot read the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean hasPermissionOnRemoteRoot = service.userHasPermissionsOnRepositoryRoot("testRepo1");
        assertEquals(hasPermissionOnRemoteRoot, applyEnabled);
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test(dataProvider = "applyEnabled")
    public void hasPermissionsOnLocalRepoWhenUsingRemoteRepoPermissions(boolean applyEnabled) {
        getBound().setProperty(ConstantValues.applyRemoteReposPermissionsOnLocalRepos, String.valueOf(applyEnabled));
        setSimpleUserAuthentication();

        LocalRepo localRepo = createMock(LocalRepo.class);
        expect(localRepo.isLocal()).andReturn(true).anyTimes();
        expect(localRepo.isCache()).andReturn(false).anyTimes();
        expect(localRepo.isReal()).andReturn(true).anyTimes();
        replay(localRepo);

        expect(repositoryServiceMock.repositoryByKey("testRemote")).andReturn(localRepo).anyTimes();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRemote")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRemote")).andReturn(null).anyTimes(); // Local remote repo
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRemote")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();

        // cannot read the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean hasPermissionOnRemoteRoot = service.userHasPermissionsOnRepositoryRoot("testRemote");
        assertEquals(hasPermissionOnRemoteRoot, applyEnabled);
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    public void hasPermissionForGroupInfo() {
        GroupInfo groupInfo = InfoFactoryHolder.get().createGroup("deployGroup");

        RepoPath testRepo1Path = InternalRepoPathFactory.create("testRepo1", "any/path");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo1")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo1")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canRead = service.canRead(groupInfo, testRepo1Path);
        assertFalse(canRead, "Group should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDeploy = service.canDeploy(groupInfo, testRepo1Path);
        assertTrue(canDeploy, "Group should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canDelete = service.canDelete(groupInfo, testRepo1Path);
        assertFalse(canDelete, "Group should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        boolean canAdmin = service.canManage(groupInfo, testRepo1Path);
        assertFalse(canAdmin, "Group should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        RepoPath testRepo2Path = InternalRepoPathFactory.create("testRepo2", "some/path");

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRepo2")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRepo2")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(groupInfo, testRepo2Path);
        assertFalse(canRead, "Group should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        GroupInfo anyRepoGroupRead = InfoFactoryHolder.get().createGroup("anyRepoReadersGroup");
        RepoPath somePath = InternalRepoPathFactory.create("blabla", "some/path");

        expect(repositoryServiceMock.localOrCachedRepositoryByKey("blabla")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("blabla")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("blabla")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(anyRepoGroupRead, somePath);
        assertTrue(canRead, "Group should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(anyRepoGroupRead, somePath);
        assertFalse(canDeploy, "Group should not have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);

        GroupInfo multiRepoGroupRead = InfoFactoryHolder.get().createGroup("multiRepoReadersGroup");
        RepoPath multiPath = InternalRepoPathFactory.create("multi1", "some/path");
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("multi1")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("multi1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("multi1")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("multi2")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("multi2")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("multi2")).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(multiRepoGroupRead, multiPath);
        assertTrue(canRead, "Group should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        RepoPath multiPath2 = InternalRepoPathFactory.create("multi2", "some/path");
        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canRead = service.canRead(multiRepoGroupRead, multiPath2);
        assertTrue(canRead, "Group should have permissions for this path");
        verify(repositoryServiceMock);
        reset(aclStoreServiceMock);

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock);
        canDeploy = service.canDeploy(multiRepoGroupRead, multiPath);
        assertFalse(canDeploy, "Group should not have permissions for this path");
        verify(repositoryServiceMock);
    }

    public void getAllPermissionTargetsForAdminUser() {
        setAdminAuthentication();
        Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> adminRepoToAclMap = aclCache.getUserResultMap().get("yossis");
        for (String repoPath : adminRepoToAclMap.keySet()) {
            RepoPath path = RepoPathFactory.create(repoPath);
            assertTrue(service.canManage(path), "Admin should be manager of all repos in his cache map");
        }
        assertTrue(service.canManage(RepoPathFactory.create("multi1")),
                "Admin should be manager of repos not belonging to his cache map");
    }

    public void getAllPermissionTargetsForUserWithNoPermission() {
        setSimpleUserAuthentication("noadminpermissionsuser");

        expectAclScan();

        List<RepoPermissionTarget> permissionTargets = service.getRepoPermissionTargets(ArtifactoryPermission.MANAGE);
        assertEquals(permissionTargets.size(), 0);

        verify(aclStoreServiceMock);
    }

    @Test(enabled = false)
    public void getDeployPermissionTargetsForUserWithNoPermission() {
        setSimpleUserAuthentication("user");

        expectAclScan();

        List<RepoPermissionTarget> targets = service.getRepoPermissionTargets(ArtifactoryPermission.DEPLOY);
        assertEquals(targets.size(), 0);

        verify(aclStoreServiceMock);
    }

    public void getDeployPermissionTargetsForUserWithDeployPermission() {
        // yossis should have testRepo1 and testRemote-cache
        setSimpleUserAuthentication("yossis");

        expectAclScan();

        List<RepoPermissionTarget> targets = service.getRepoPermissionTargets(ArtifactoryPermission.DEPLOY);
        assertEquals(targets.size(), 2, "Expecting two deploy permission");

        verify(aclStoreServiceMock);
    }

    public void userPasswordMatches() {
        setSimpleUserAuthentication("user");

        assertTrue(service.userPasswordMatches("password"));
        assertFalse(service.userPasswordMatches(""));
        assertFalse(service.userPasswordMatches("Password"));
        assertFalse(service.userPasswordMatches("blabla"));
    }

    public void permissionOnRemoteRoot() {
        setSimpleUserAuthentication();

        expect(repositoryServiceMock.repositoryByKey("testRemote")).andReturn(createRemoteRepoMock()).anyTimes();
        expect(repositoryServiceMock.localOrCachedRepositoryByKey("testRemote-cache")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRemote-cache")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey("testRemote")).andReturn(new HttpRepoDescriptor()).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey("testRemote-cache")).andReturn(null).anyTimes();
        replay(repositoryServiceMock);

        // cannot read the specified path
        expectGetAllAclsCall();
        replay(aclStoreServiceMock);
        boolean hasPermissionOnRemoteRoot = service.userHasPermissionsOnRepositoryRoot("testRemote");
        assertTrue(hasPermissionOnRemoteRoot, "User should have permissions for this path");
        verify(aclStoreServiceMock, repositoryServiceMock);
        reset(aclStoreServiceMock, repositoryServiceMock);
    }

    @Test(dependsOnMethods = {"permissionOnRemoteRoot"},
            expectedExceptions = {InvalidNameException.class})
    protected void findOrCreateExternalAuthUserWithIllegalChars() {
        reset(userGroupStoreService);
        String userName = new String(NameValidator.getForbiddenChars());
        expect(userGroupStoreService.findUser(userName)).andReturn(null).anyTimes();
        replay(userGroupStoreService);
        service.findOrCreateExternalAuthUser(userName, false);
    }

    @DataProvider
    public static Object[][] provideAccessEmigrateData() {
        return new Object[][]{
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v560m005.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false},
        };
    }

    @DataProvider
    public static Object[][] provideAccessEmigrateDataMarker() {
        return new Object[][]{
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false, true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false, true},
                { new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false, false},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v560m005.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
                { new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
        };
    }

    @DataProvider
    public static Object[][] provideAccessResourceTypeData() {
        return new Object[][]{
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v590.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                {new CompoundVersionDetails(ArtifactoryVersionProvider.v5100m009.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false},
        };
    }

    @DataProvider
    public static Object[][] provideAccessDecryptionTypeData() {
        return new Object[][]{
                {new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                {new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                {new CompoundVersionDetails(ArtifactoryVersionProvider.v590.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true},
                {new CompoundVersionDetails(ArtifactoryVersionProvider.v600.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false},
        };
    }

    @DataProvider
    public static Object[][] provideAccessDecryptionAndResourceTypeDataMarker() {
        return new Object[][]{
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false, true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false, true},
                { new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), false, false},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v540.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v400.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
                { new CompoundVersionDetails(ArtifactoryVersionProvider.v590.get(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
                { new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()),
                        new CompoundVersionDetails(ArtifactoryVersion.getCurrent(), "", System.currentTimeMillis()
                        ), true, true},
        };
    }

    @Test(dataProvider = "provideAccessEmigrateData")
    public void testConvertEmigrateToAccess(CompoundVersionDetails source, CompoundVersionDetails target, boolean expected) throws IOException {
        securityEmigrator.convert();
        if (expected) {
            EasyMock.expectLastCall().once();
        } else {
            EasyMock.expectLastCall().andThrow(new AssertionFailedError()).anyTimes();
        }

        replay(securityEmigrator);
        new PreInitConverter(homeStub).convert(source, target);

        assertEquals(homeStub.getAccessEmigrateMarkerFile().exists(), expected);

        service.convert(source, target);

        assertFalse(homeStub.getAccessEmigrateMarkerFile().exists());

        EasyMock.verify(securityEmigrator);
    }

    @Test(dataProvider = "provideAccessEmigrateDataMarker")
    public void testIsInterested(CompoundVersionDetails source, CompoundVersionDetails target,
            boolean createMarker, boolean expected) throws IOException {
        if (createMarker) {
            FileUtils.write(homeStub.getAccessEmigrateMarkerFile(), "");
        }

        assertEquals(service.isInterested(source, target), expected);
    }

    @Test(dataProvider = "provideAccessResourceTypeData")
    public void testConvertResourceType(CompoundVersionDetails source, CompoundVersionDetails target, boolean expected) throws IOException {
        resourceTypeToRepoConverter.convert();
        if (expected) {
            EasyMock.expectLastCall().once();
        } else {
            EasyMock.expectLastCall().andThrow(new AssertionFailedError()).anyTimes();
        }

        replay(resourceTypeToRepoConverter);
        new PreInitConverter(homeStub).convert(source, target);

        assertEquals(homeStub.getAccessResourceTypeConverterMarkerFile().exists(), expected);

        service.convert(source, target);

        assertFalse(homeStub.getAccessResourceTypeConverterMarkerFile().exists());

        EasyMock.verify(resourceTypeToRepoConverter);
    }

    @Test(dataProvider = "provideAccessDecryptionTypeData")
    public void testConvertDecryptPropsType(CompoundVersionDetails source, CompoundVersionDetails target, boolean expected) throws IOException {
        decryptAllUsersCustomData.convert();
        if (expected) {
            EasyMock.expectLastCall().once();
        } else {
            EasyMock.expectLastCall().andThrow(new AssertionFailedError()).anyTimes();
        }

        replay(decryptAllUsersCustomData);
        new PreInitConverter(homeStub).convert(source, target);

        assertEquals(homeStub.getAccessUserCustomDataDecryptionMarkerFile().exists(), expected);

        service.convert(source, target);

        assertFalse(homeStub.getAccessUserCustomDataDecryptionMarkerFile().exists());

        EasyMock.verify(decryptAllUsersCustomData);
    }

    @Test(dataProvider = "provideAccessDecryptionAndResourceTypeDataMarker")
    public void testIsInterestedDecryptProps(CompoundVersionDetails source, CompoundVersionDetails target,
            boolean createMarker, boolean expected) throws IOException {
        if (createMarker) {
            FileUtils.write(homeStub.getAccessUserCustomDataDecryptionMarkerFile(), "");
        }

        assertEquals(service.isInterested(source, target), expected);
    }

    @Test(dataProvider = "provideAccessDecryptionAndResourceTypeDataMarker")
    public void testIsInterestedResourceType(CompoundVersionDetails source, CompoundVersionDetails target,
            boolean createMarker, boolean expected) throws IOException {
        if (createMarker) {
            FileUtils.write(homeStub.getAccessResourceTypeConverterMarkerFile(), "");
        }

        assertEquals(service.isInterested(source, target), expected);
    }

    @Test
    public void testBuildInPermission() {
        assertTrue(service.isBuildInPermissions(Collections.singletonList("bla blo/**"), Collections.emptyList(), "bla blo"));
    }

    @Test(dataProvider = "provideGetBuildNameAcls")
    public void testGetBuildNameAcls(String buildName, List<BuildAcl> acls, boolean expectingValues) {
        expect(aclStoreServiceMock.getAllBuildAcls()).andReturn(acls).anyTimes();
        replay(aclStoreServiceMock);
        assertEquals(!service.getBuildPathAcls(buildName, "1", "2015-05-15T11:49:04.804+02:00").isEmpty(),
                expectingValues);
    }

    @DataProvider
    public static Object[][] provideGetBuildNameAcls() {
        return new Object[][]{
                // {Build name, permission target with pattern, should build be a match to pattern}
                {"test1", Collections.singletonList(createBuildAcl("test1/**")), true},
                {"test2", Collections.singletonList(createBuildAcl("no")), false},
                {"test3", Collections.singletonList(createBuildAcl("**/test3/**")), true},
                {"test4", Collections.singletonList(createBuildAcl("**/test4")), false},
                {"test5", Collections.singletonList(createBuildAcl("*/test5")), false},
                {"test6", Collections.singletonList(createBuildAcl("someDir/test6")), false},
                {"test7", Collections.singletonList(createBuildAcl("someDir/test7/**")), false},
                {"test8", Collections.singletonList(createBuildAcl("test8/1**")), true},
                {"test9", Collections.singletonList(createBuildAcl("test9/2**")), false},

        };
    }

    private static MutableAcl<BuildPermissionTarget> createBuildAcl(String pattern) {
        MutableBuildPermissionTarget buildPermissionTarget = InfoFactoryHolder.get()
                .createBuildPermissionTarget("1", Collections.singletonList("repoKey"));
        buildPermissionTarget.setIncludesPattern(pattern);
        return InfoFactoryHolder.get().createBuildAcl(buildPermissionTarget);
    }

    @Test
    public void testDecryptAllUserPropsWithOldEncryption() throws Exception {
        // unbind first, this test is already bound
        createAndSaveArtifactoryKey();
        AccessService accessService = Mockito.mock(AccessService.class);
        // dependency nightmare
        SecurityServiceImpl securityServiceImpl = new SecurityServiceImpl();
        injectField(securityServiceImpl, "accessService", accessService);
        // Prepare
        AccessClient accessClientMockWithData = Mockito.mock(AccessClient.class);
        UsersClient usersClient = Mockito.mock(UsersClient.class);
        when(accessClientMockWithData.users()).thenReturn(usersClient);
        UsersResponse usersResponse = new UsersResponse();
        usersResponse.users(Collections.singletonList(new UserResponse("johnny")
                .addCustomData(SSH_KEY, ArtifactoryHome.get().getArtifactoryEncryptionWrapper().encryptIfNeeded("notsecure"))));
        when(usersClient.findUsers()).thenReturn(usersResponse);
        when(accessService.getAccessClient()).thenReturn(accessClientMockWithData);

        securityServiceImpl.decryptAllUserProps();
        // Assert - the user should have been updated
        Mockito.verify(usersClient, times(1)).updateUser(any());
    }

    @Test
    public void testDecryptAllUserPropsWithNoEncryptionAndSearchableProperty() throws Exception {
        // unbind first, this test is already bound
        createAndSaveArtifactoryKey();
        AccessService accessService = Mockito.mock(AccessService.class);
        // dependency nightmare
        SecurityServiceImpl securityServiceImpl = new SecurityServiceImpl();
        injectField(securityServiceImpl, "accessService", accessService);
        // Prepare
        AccessClient accessClientMockWithData = Mockito.mock(AccessClient.class);
        UsersClient usersClient = Mockito.mock(UsersClient.class);
        when(accessClientMockWithData.users()).thenReturn(usersClient);
        UsersResponse usersResponse = new UsersResponse();
        usersResponse.users(Collections.singletonList(new UserResponse("johnny")
                .addCustomData(SSH_KEY, "notsecure")));
        when(usersClient.findUsers()).thenReturn(usersResponse);
        when(accessService.getAccessClient()).thenReturn(accessClientMockWithData);

        securityServiceImpl.decryptAllUserProps();
        // Assert - the user should not be updated
        Mockito.verify(usersClient, times(1)).updateUser(any());
    }

    @Test
    public void testDecryptAllUserPropsWithNoEncryptionAndNonSearchableProperty() throws Exception {
        // unbind first, this test is already bound
        createAndSaveArtifactoryKey();
        AccessService accessService = Mockito.mock(AccessService.class);
        // dependency nightmare
        SecurityServiceImpl securityServiceImpl = new SecurityServiceImpl();
        injectField(securityServiceImpl, "accessService", accessService);
        // Prepare
        AccessClient accessClientMockWithData = Mockito.mock(AccessClient.class);
        UsersClient usersClient = Mockito.mock(UsersClient.class);
        when(accessClientMockWithData.users()).thenReturn(usersClient);
        UsersResponse usersResponse = new UsersResponse();
        usersResponse.users(Collections.singletonList(new UserResponse("johnny")
                .addCustomData(SSH_KEY, "notsecure")));
        usersResponse.users(Collections.singletonList(new UserResponse("johnny")
                .addCustomData(SSH_KEY + "_shash", "whatever")));
        when(usersClient.findUsers()).thenReturn(usersResponse);
        when(accessService.getAccessClient()).thenReturn(accessClientMockWithData);

        securityServiceImpl.decryptAllUserProps();
        // Assert - the user should still be updated due to the searchable props being added
        Mockito.verify(usersClient, times(1)).updateUser(any());
    }

    private void injectField(SecurityService securityService, String fieldName, Object object)
            throws Exception {
        Field field = securityService.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(securityService, object);
    }

    private void createAndSaveArtifactoryKey() {
        ArtifactoryHome artifactoryHome = new ArtifactoryHomeStub();
        artifactoryHome.getArtifactoryKey().delete();
        SecurityFolderHelper.createKeyFile(artifactoryHome.getArtifactoryKey(), CipherAlg.AES128);
    }

    public void testUserHasPermissions() {
        // user shouldn't have any permissions
        SimpleUser user = createNonAdminUser("noperm");
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("noperm")).andReturn(user.getDescriptor()).once();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, userGroupStoreService);
        Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> nopermPermMap = aclCache.getUserResultMap().get("noperm");
        boolean hasNoPermissions = nopermPermMap == null || nopermPermMap.size() == 0;
        assertTrue(hasNoPermissions, "User should not have permissions");
        reset(aclStoreServiceMock, userGroupStoreService);
    }

    public void testUserHasPermissionsFromGroup() {
        // user don't have permissions on his own, but should have permission from his group
        SimpleUser user = createNonAdminUser("noperm", securityServiceImplTestHelper.USER_AND_GROUP_SHARED_NAME);
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("noperm")).andReturn(user.getDescriptor()).once();

        expectGetAllAclsCallWithAnyArray();
        replay(aclStoreServiceMock, userGroupStoreService);
        Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> nopermPermMap = aclCache.getUserResultMap().get("noperm");
        boolean hasNoPermissions = nopermPermMap == null || nopermPermMap.size() == 0;
        assertTrue(hasNoPermissions, "User shouldn't have permissions on this path");
        hasNoPermissions = user.getDescriptor()
                .getGroups()
                .stream()
                .allMatch(group -> {
                    Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> nopermGroupPermMap = aclCache.getGroupResultMap()
                            .get(group.getGroupName());
                    return nopermGroupPermMap == null || nopermGroupPermMap.size() == 0;
                });
        assertFalse(hasNoPermissions, "User should have permissions on this path from his groups");
        reset(aclStoreServiceMock, userGroupStoreService);
    }

    public void userReadPermissionsOnAnyRemote() {
        // user should have a read (only) permission on any remote
        setSimpleUserAuthentication("anyRemoteUser");
        //secureRepo is a cached remote repo
        RepoPath securedPath = InternalRepoPathFactory.create("securedRepo", "blabla");

        expectGetAllAclsCall();
        RemoteRepo remoteRepoMock = createRemoteRepoMock();
        expect(repositoryServiceMock.remoteRepositoryByKey("securedRepo")).andReturn(remoteRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(new HttpRepoDescriptor()).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        replay(aclStoreServiceMock);
        assertTrue(service.canRead(securedPath), "User should have read permissions on any remote path");
        assertFalse(
                service.canDeploy(securedPath) || service.canManage(securedPath) || service.canDelete(securedPath) ||
                        service.canManage(securedPath), "User shouldn't have other permissions on any remote path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);
    }

    public void userDeployPermissionsOnAnyLocal() {
        // user should have deploy permission on any local
        setSimpleUserAuthentication("anyLocalUser");

        RepoPath securedPath = InternalRepoPathFactory.create("securedRepo", "blabla");

        expectGetAllAclsCall();
        expect(repositoryServiceMock.localRepositoryByKey("securedRepo")).andReturn(localRepoMock).anyTimes();
        expect(repositoryServiceMock.remoteRepositoryByKey("securedRepo")).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        replay(aclStoreServiceMock);

        assertTrue(service.canDeploy(securedPath), "User should have deploy permissions on any local path");
        assertFalse(
                service.canRead(securedPath) || service.canManage(securedPath) || service.canDelete(securedPath) ||
                        service.canManage(securedPath), "User shouldn't have other permissions on any remote path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);
    }

    public void userDeployPermissionsOnAnyDist() {
        // user should have deploy permission on any local
        setSimpleUserAuthentication("anyDistDeployer");

        RepoPath securedPath = InternalRepoPathFactory.create("securedRepo", "blabla");

        expectGetAllAclsCall();
        expect(repositoryServiceMock.remoteRepositoryByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.distributionRepoByKey(securedPath.getRepoKey())).andReturn(distRepoMock).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        replay(aclStoreServiceMock);

        assertTrue(service.canDeploy(securedPath), "User should have read permissions on any distribution path");
        assertFalse(
                service.canRead(securedPath) || service.canManage(securedPath) || service.canDelete(securedPath) ||
                        service.canManage(securedPath), "User shouldn't have other permissions on any distribution path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);
    }

    public void userReadPermissionsOnAnyDist() {
        // user should have deploy permission on any local
        setSimpleUserAuthentication("anyDistReader");

        RepoPath securedPath = InternalRepoPathFactory.create("securedRepo", "blabla");

        expectGetAllAclsCall();
        expect(repositoryServiceMock.remoteRepositoryByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expect(repositoryServiceMock.distributionRepoByKey(securedPath.getRepoKey())).andReturn(distRepoMock).anyTimes();
        expect(repositoryServiceMock.releaseBundlesRepoDescriptorByKey(securedPath.getRepoKey())).andReturn(null).anyTimes();
        expectRemoteByKeyNullForAnyConsts();
        replay(aclStoreServiceMock);

        assertTrue(service.canRead(securedPath), "User should have read permissions on any distribution path");
        assertFalse(
                service.canDeploy(securedPath) || service.canManage(securedPath) || service.canDelete(securedPath) ||
                        service.canManage(securedPath), "User shouldn't have other permissions on any distribution path");
        verify(aclStoreServiceMock);
        reset(aclStoreServiceMock);
    }

    public void updateLastLoginWithNotExistingUserTest() {
        // Make sure that if the user doesn't exists we stop the "updateLastLogin" process without  exception
        reset(userGroupStoreService);
        // Enable the update last login process "userLastAccessUpdatesResolutionSecs" must be greater or equals to "1"
        getBound().setProperty(ConstantValues.userLastAccessUpdatesResolutionSecs, "1");
        expect(userGroupStoreService.findUser("user")).andReturn(null).once();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", System.currentTimeMillis() + 1000, "momo");
        reset(userGroupStoreService);
    }

    public void testUserLastLoginTimeUpdateBuffer() {
        getBound().setProperty(ConstantValues.userLastAccessUpdatesResolutionSecs, "0");
        service.updateUserLastLogin("user", System.currentTimeMillis(), "127.0.0.1");
        getBound().setProperty(ConstantValues.userLastAccessUpdatesResolutionSecs,
                ConstantValues.userLastAccessUpdatesResolutionSecs.getDefValue());

        MutableUserInfo user = new UserInfoBuilder("user").build();
        user.setLastLoginTimeMillis(0);

        //Simulate No existing last login, expect an update
        expect(userGroupStoreService.findUser("user")).andReturn(user).times(1);
        long currentTimeMillis = System.currentTimeMillis();
        userGroupStoreService.updateUserLastLogin("user", currentTimeMillis, "127.0.0.1");
        EasyMock.expectLastCall();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", currentTimeMillis, "127.0.0.1");
        verify(userGroupStoreService);

        //Give a last login from the near past, expect no update
        long nearPastLogin = System.currentTimeMillis();
        reset(userGroupStoreService);
        user = new UserInfoBuilder("user").build();
        user.setLastLoginTimeMillis(nearPastLogin);
        expect(userGroupStoreService.findUser("user")).andReturn(user).once();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", nearPastLogin + 100L, "momo");
        verify(userGroupStoreService);

        //Give a last login from the future, expect an update
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("user")).andReturn(user).times(1);
        userGroupStoreService.updateUserLastLogin("user", currentTimeMillis + 6000L, "127.0.0.1");
        EasyMock.expectLastCall();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", currentTimeMillis + 6000L, "127.0.0.1");
        verify(userGroupStoreService);

        // Give a last login from the future, expect an update (use an IPv6 Address)
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("user")).andReturn(user).times(1);
        userGroupStoreService.updateUserLastLogin("user", currentTimeMillis + 10000L,
                "[2a05:d014:82c:c112:ecc8:404a:7917:dbc2]");
        EasyMock.expectLastCall();
        replay(userGroupStoreService);
        service.updateUserLastLogin("user", currentTimeMillis + 10000L,
                "[2a05:d014:82c:c112:ecc8:404a:7917:dbc2]");
        verify(userGroupStoreService);
    }

    public void testSelectiveReload() {
        TreeSet<SecurityListener> securityListeners = new TreeSet<>();
        securityListeners.add(securityListenerMock);
        ReflectionTestUtils.setField(service, "securityListeners", securityListeners);
        reset(securityListenerMock);
        securityListenerMock.onClearSecurity();
        expect(securityListenerMock.compareTo(securityListenerMock)).andReturn(0).anyTimes();
        replay(securityListenerMock);

        SecurityDescriptor newSecurityDescriptor = new SecurityDescriptor();
        SecurityDescriptor oldSecurityDescriptor = new SecurityDescriptor();
        oldSecurityDescriptor.addLdap(new LdapSetting());

        CentralConfigDescriptor newConfigDescriptor = createMock(CentralConfigDescriptor.class);
        expect(newConfigDescriptor.getSecurity()).andReturn(newSecurityDescriptor).anyTimes();
        replay(newConfigDescriptor);

        CentralConfigDescriptor oldConfigDescriptor = createMock(CentralConfigDescriptor.class);
        expect(oldConfigDescriptor.getSecurity()).andReturn(oldSecurityDescriptor).anyTimes();
        replay(oldConfigDescriptor);

        expect(centralConfigServiceMock.getDescriptor()).andReturn(newConfigDescriptor).anyTimes();
        replay(centralConfigServiceMock);

        service.reload(oldConfigDescriptor, ImmutableList.of());
        verify(securityListenerMock);

        // The security conf is the same, so onClearSecurity should NOT be called
        service.reload(newConfigDescriptor, ImmutableList.of());
        verify(securityListenerMock);
        ReflectionTestUtils.setField(service, "securityListeners", null);
    }

    public void testPermissionCheckOnAclFitPermission() {
        SimpleUser nonAdminUser = createNonAdminUser("user1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());

        Set<AceInfo> aces = Sets.newHashSet(aceU1);

        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList("repo-a"),
                Lists.newArrayList("**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission = new PrincipalPermissionImpl<>(pt, aceU1);
        assertTrue(service.permissionCheckOnAcl(Collections.singletonList(principalPermission), new RepoPathImpl("repo-a", null),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, nonAdminUser.getUsername()));
    }

    public void testPermissionCheckOnAclDifferentPermission() {
        SimpleUser nonAdminUser = createNonAdminUser("user1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());

        Set<AceInfo> aces = Sets.newHashSet(aceU1);

        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList("repo-a"),
                Lists.newArrayList("**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission = new PrincipalPermissionImpl<>(pt, aceU1);
        assertFalse(service.permissionCheckOnAcl(Collections.singletonList(principalPermission), new RepoPathImpl("repo-a", null),
                Collections.singletonList("repo-a"), ArtifactoryPermission.READ, nonAdminUser.getUsername()));
    }


    public void testPermissionCheckOnAclOnGroupFailed() {
        SimpleUser nonAdminUser = createNonAdminUser("user1", "g1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());

        Set<AceInfo> aces = Sets.newHashSet(aceU1);

        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList("repo-a"),
                Lists.newArrayList("**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission = new PrincipalPermissionImpl<>(pt, aceU1);
        assertFalse(service.permissionCheckOnAcl(Collections.singletonList(principalPermission), new RepoPathImpl("repo-a", null),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, "g1"));
    }

    public void testPermissionCheckOnAclOnGroupSuccess() {
        SimpleUser nonAdminUser = createNonAdminUser("user1", "g1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl aceU2 = new AceImpl("g1", true, ArtifactoryPermission.DEPLOY.getMask());


        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList("repo-a"),
                Lists.newArrayList("**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission1 = new PrincipalPermissionImpl<>(pt, aceU1);
        PrincipalPermission principalPermission2 = new PrincipalPermissionImpl<>(pt, aceU2);
        assertTrue(service.permissionCheckOnAcl(Arrays.asList(principalPermission1, principalPermission2), new RepoPathImpl("repo-a", null),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, "g1"));
    }

    public void testPermissionCheckOnAclOnGroupSuccess2() {
        SimpleUser nonAdminUser = createNonAdminUser("user1", "g1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl aceU2 = new AceImpl("g1", true, ArtifactoryPermission.DEPLOY.getMask());


        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList("repo-a"),
                Lists.newArrayList("**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission1 = new PrincipalPermissionImpl<>(pt, aceU1);
        PrincipalPermission principalPermission2 = new PrincipalPermissionImpl<>(pt, aceU2);
        assertTrue(service.permissionCheckOnAcl(Arrays.asList(principalPermission1, principalPermission2), new RepoPathImpl("repo-a", null),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, "g1"));
    }

    public void testPermissionCheckOnAclAnySuccess() {
        SimpleUser nonAdminUser = createNonAdminUser("user1", "g1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl aceU2 = new AceImpl("g1", true, ArtifactoryPermission.DEPLOY.getMask());


        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList(ANY_REPO),
                Lists.newArrayList("**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission1 = new PrincipalPermissionImpl<>(pt, aceU1);
        PrincipalPermission principalPermission2 = new PrincipalPermissionImpl<>(pt, aceU2);
        assertTrue(service.permissionCheckOnAcl(Arrays.asList(principalPermission1, principalPermission2),
                new RepoPathImpl("repo-a", null),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, "g1"));
    }

    public void testPermissionCheckOnAclPatternSuccess() {
        SimpleUser nonAdminUser = createNonAdminUser("user1", "g1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl aceU2 = new AceImpl("g1", true, ArtifactoryPermission.DEPLOY.getMask());


        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList(ANY_REPO),
                Lists.newArrayList("**dod**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission1 = new PrincipalPermissionImpl<>(pt, aceU1);
        PrincipalPermission principalPermission2 = new PrincipalPermissionImpl<>(pt, aceU2);
        assertTrue(service.permissionCheckOnAcl(Arrays.asList(principalPermission1, principalPermission2),
                new RepoPathImpl("repo-a", "dodge"),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, "g1"));
    }


    public void testPermissionCheckOnAclPatternFailed() {
        SimpleUser nonAdminUser = createNonAdminUser("user1", "g1");

        AceImpl aceU1 = new AceImpl(nonAdminUser.getUsername(), false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl aceU2 = new AceImpl("g1", true, ArtifactoryPermission.DEPLOY.getMask());


        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("pt1",
                Lists.newArrayList(ANY_REPO),
                Lists.newArrayList("**do/e**"),
                Lists.newArrayList());

        PrincipalPermission principalPermission1 = new PrincipalPermissionImpl<>(pt, aceU1);
        PrincipalPermission principalPermission2 = new PrincipalPermissionImpl<>(pt, aceU2);
        assertFalse(service.permissionCheckOnAcl(Arrays.asList(principalPermission1, principalPermission2),
                new RepoPathImpl("repo-a", "dodge"),
                Collections.singletonList("repo-a"), ArtifactoryPermission.DEPLOY, "g1"));
    }

    private void expectAclScan() {
        expect(aclStoreServiceMock.getAllRepoAcls()).andReturn(testAcls).anyTimes();
        expect(aclStoreServiceMock.getAclCache()).andReturn(aclCache).anyTimes();
        replay(aclStoreServiceMock);
    }

    private void expectGetAllAclsCall() {
        expect(aclStoreServiceMock.getAllRepoAcls()).andReturn(testAcls).anyTimes();
        expect(aclStoreServiceMock.getAclCache()).andReturn(aclCache).anyTimes();
    }

    private void expectGetAllAclsCallWithAnyArray() {
        expect(aclStoreServiceMock.getAllRepoAcls()).andReturn(testAcls);
        expect(aclStoreServiceMock.getAclCache()).andReturn(aclCache);
    }

    private Authentication setAdminAuthentication() {
        SimpleUser adminUser = createAdminUser();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                adminUser, null, SimpleUser.ADMIN_GAS);
        securityContext.setAuthentication(authenticationToken);
        return authenticationToken;
    }

    private Authentication setSimpleUserAuthentication() {
        return setSimpleUserAuthentication("user");
    }

    private Authentication setSimpleUserAuthentication(String username, String... groups) {
        SimpleUser simpleUser = createNonAdminUser(username, groups);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                simpleUser, "password", SimpleUser.USER_GAS);
        securityContext.setAuthentication(authenticationToken);
        return authenticationToken;
    }

    private static SimpleUser createNonAdminUser(String username, String... groups) {
        UserInfo userInfo = new UserInfoBuilder(username).updatableProfile(true)
                .internalGroups(new HashSet<>(Arrays.asList(groups))).build();
        return new SimpleUser(userInfo);
    }

    private static SimpleUser createAdminUser() {
        UserInfo userInfo = new UserInfoBuilder("spiderman").admin(true).updatableProfile(true).build();
        return new SimpleUser(userInfo);
    }

    private static LocalRepo createLocalRepoMock() {
        LocalRepo localRepo = createMock(LocalRepo.class);
        expect(localRepo.isLocal()).andReturn(true).anyTimes();
        expect(localRepo.isCache()).andReturn(false).anyTimes();
        replay(localRepo);
        return localRepo;
    }

    private static LocalRepo createCacheRepoMock() {
        LocalRepo localRepo = createMock(LocalRepo.class);
        expect(localRepo.isLocal()).andReturn(true).anyTimes();
        expect(localRepo.isCache()).andReturn(true).anyTimes();
        replay(localRepo);
        return localRepo;
    }

    private RemoteRepo createRemoteRepoMock() {
        RemoteRepo remoteRepo = createMock(RemoteRepo.class);
        expect(remoteRepo.isReal()).andReturn(true).anyTimes();
        replay(remoteRepo);
        return remoteRepo;
    }

    private static DistributionRepo createDistRepoMock() {
        DistributionRepo distRepo = createMock(DistributionRepo.class);
        expect(distRepo.isLocal()).andReturn(true).anyTimes();
        expect(distRepo.isCache()).andReturn(false).anyTimes();
        replay(distRepo);
        return distRepo;
    }

    private InternalRepositoryService createRepoServiceMock() {
        InternalRepositoryService repositoryService = createMock(InternalRepositoryService.class);
        replay(repositoryService);
        return repositoryService;
    }

    private InternalBuildService createBuildServiceMock() {
        InternalBuildService buildService = createMock(InternalBuildService.class);
        expect(buildService.getBuildInfoRepoKey()).andReturn(BUILD_INFO_REPO_NAME).anyTimes();
        replay(buildService);
        return buildService;
    }

    @Test(
            expectedExceptions = {PasswordChangeException.class},
            expectedExceptionsMessageRegExp = "Old password is incorrect"
    )
    public void changePasswordUsingIncorrectOldPassword() {
        SaltedPassword sp = new SaltedPassword("foo", "salt");

        UserInfo user = new UserImpl() {{
            setUsername("test");
            setUpdatableProfile(true);
            setPassword(sp);
        }};

        expect(userGroupStoreService.findUser("test")).andReturn(user).anyTimes();
        expect(userPassAuthenticationProvider.canUserLogin(anyObject(), anyObject())).andReturn(false);

        MutableCentralConfigDescriptor mutableCentralConfigDescriptor = createMock(
                MutableCentralConfigDescriptor.class);
        PasswordExpirationPolicy expirationPolicy = new PasswordExpirationPolicy();
        expirationPolicy.setEnabled(Boolean.TRUE);

        SecurityDescriptor securityDescriptor = new SecurityDescriptor();
        securityDescriptor.setPasswordSettings(new PasswordSettings() {{
            setExpirationPolicy(expirationPolicy);
        }});

        expect(centralConfigServiceMock.getDescriptor()).andReturn(mutableCentralConfigDescriptor).anyTimes();
        expect(centralConfigServiceMock.getMutableDescriptor()).andReturn(mutableCentralConfigDescriptor).anyTimes();
        expect(mutableCentralConfigDescriptor.getSecurity()).andReturn(securityDescriptor).anyTimes();
        expect(userLockInMemoryService.getNextLoginTime("test")).andReturn(-1L).anyTimes();
        userLockInMemoryService.registerIncorrectLoginAttempt("test");
        EasyMock.expectLastCall();
        replay(userGroupStoreService, mutableCentralConfigDescriptor, centralConfigServiceMock);

        service.changePassword("test", "bar", "pass", "pass");
    }

    @Test(
            dependsOnMethods = {"changePasswordUsingIncorrectOldPassword"},
            expectedExceptions = {PasswordChangeException.class},
            expectedExceptionsMessageRegExp = "User is Locked.\n" +
                    "Contact System Administrator to Unlock The Account."
    )

    public void changePasswordForLockedOutUserTest() {
        SaltedPassword sp = new SaltedPassword("foo", "salt");

        UserInfo user = new UserImpl() {{
            setUsername("test");
            setUpdatableProfile(true);
            setPassword(sp);
        }};
        reset(userGroupStoreService);
        expect(userGroupStoreService.findUser("test")).andReturn(user).anyTimes();

        MutableCentralConfigDescriptor mutableCentralConfigDescriptor = createMock(
                MutableCentralConfigDescriptor.class);
        PasswordExpirationPolicy expirationPolicy = new PasswordExpirationPolicy();
        expirationPolicy.setEnabled(Boolean.TRUE);

        SecurityDescriptor securityDescriptor = new SecurityDescriptor();
        securityDescriptor.setPasswordSettings(new PasswordSettings() {{
            setExpirationPolicy(expirationPolicy);
        }});

        UserLockPolicy userLockPolicy = new UserLockPolicy() {{
            setEnabled(Boolean.TRUE);
            setLoginAttempts(1);
        }};
        securityDescriptor.setUserLockPolicy(userLockPolicy);

        expect(centralConfigServiceMock.getDescriptor()).andReturn(mutableCentralConfigDescriptor).anyTimes();
        expect(centralConfigServiceMock.getMutableDescriptor()).andReturn(mutableCentralConfigDescriptor).anyTimes();
        expect(mutableCentralConfigDescriptor.getSecurity()).andReturn(securityDescriptor).anyTimes();
        expect(userGroupStoreService.isUserLocked("test")).andReturn(true);
        replay(userGroupStoreService, mutableCentralConfigDescriptor, centralConfigServiceMock);

        service.changePassword("test", "bar", "pass", "pass");
    }

    public void testValidateResetPasswordAttempt() {
        PasswordResetPolicy policy = new PasswordResetPolicy() {{
            setEnabled(true);
            setMaxAttemptsPerAddress(3);
            setTimeToBlockInMinutes(60);
        }};
        PasswordSettings passwordSettings = new PasswordSettings() {{
            setResetPolicy(policy);
        }};
        SecurityDescriptor securityDescriptor = new SecurityDescriptor() {{
            setPasswordSettings(passwordSettings);
        }};

        PasswordResetPolicy oldPolicy = new PasswordResetPolicy();
        PasswordSettings oldPasswordSettings = new PasswordSettings() {{
            setResetPolicy(oldPolicy);
        }};
        SecurityDescriptor oldSecurityDescriptor = new SecurityDescriptor() {{
            setPasswordSettings(oldPasswordSettings);
        }};

        CentralConfigDescriptor oldConfigDescriptorMock = createMock(CentralConfigDescriptor.class);
        expect(oldConfigDescriptorMock.getSecurity()).andReturn(oldSecurityDescriptor).anyTimes();

        CentralConfigDescriptor configDescriptorMock = createMock(CentralConfigDescriptor.class);
        expect(centralConfigServiceMock.getDescriptor()).andReturn(configDescriptorMock).anyTimes();
        expect(configDescriptorMock.getSecurity()).andReturn(securityDescriptor).anyTimes();
        replay(centralConfigServiceMock, configDescriptorMock, oldConfigDescriptorMock);

        service.reload(oldConfigDescriptorMock, ImmutableList.of());

        int timeBetweenAttempts = 600;
        String remoteAddress1 = "remote.address.1." + System.currentTimeMillis();

        service.validateResetPasswordAttempt(remoteAddress1);
        try {
            service.validateResetPasswordAttempt(remoteAddress1);
            fail("Validation should fail due to too frequent requests");
        } catch (ResetPasswordException e) {
            assertTrue(e.getMessage().toLowerCase().contains("too frequent"));
        }
        sleep(timeBetweenAttempts);
        service.validateResetPasswordAttempt(remoteAddress1);
        sleep(timeBetweenAttempts);
        service.validateResetPasswordAttempt(remoteAddress1);
        try {
            service.validateResetPasswordAttempt(remoteAddress1);
            fail("Validation should fail due to too many requests");
        } catch (ResetPasswordException e) {
            assertTrue(e.getMessage().toLowerCase().contains("too many"));
        }

        String remoteAddress2 = "remote.address.2." + System.currentTimeMillis();
        service.validateResetPasswordAttempt(remoteAddress2);
    }

    public void testAuthenticateAsSystem() {
        service.authenticateAsSystem();
        assertAuthenticatedAsSystem();
    }

    public void testDoAsSystem() {
        Authentication mockAuthentication = createMock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);
        service.doAsSystem(this::assertAuthenticatedAsSystem);
        assertEquals(SecurityContextHolder.getContext().getAuthentication(), mockAuthentication, "original authentication was not restored");

        try {
            service.doAsSystem(() -> {
                throw new RuntimeException("expected exception");
            });
            fail("Exception was expected");
        } catch (Exception e) {
            //ignore expected exception
        }
        assertEquals(SecurityContextHolder.getContext().getAuthentication(), mockAuthentication, "original authentication was not restored");
    }

    private void expectRemoteByKeyNullForAnyConsts() {
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(ANY_LOCAL_REPO)).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(ANY_REMOTE_REPO)).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(ANY_DISTRIBUTION_REPO)).andReturn(null).anyTimes();
        expect(repositoryServiceMock.remoteRepoDescriptorByKey(ANY_REPO)).andReturn(null).anyTimes();
        replay(repositoryServiceMock);
    }

    private void assertAuthenticatedAsSystem() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertTrue(authentication.getAuthorities().stream()
                        .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority())), "Authentication does not apply the admin role");
        assertEquals(((UserDetails)authentication.getPrincipal()).getUsername(), USER_SYSTEM);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
