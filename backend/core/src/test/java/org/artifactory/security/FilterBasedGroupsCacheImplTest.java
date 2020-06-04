package org.artifactory.security;

import com.google.common.collect.ImmutableList;
import org.artifactory.model.xstream.security.GroupImpl;
import org.artifactory.storage.db.security.service.access.AccessUserGroupStoreService;
import org.artifactory.storage.security.service.UserGroupStoreService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.jfrog.access.model.Realm;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Uriah Levy
 */
@Test
public class FilterBasedGroupsCacheImplTest extends ArtifactoryHomeBoundTest {
    @Mock
    private UserGroupStoreService userGroupStoreService;

    @BeforeMethod
    private void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    public void getAdminGroupsByFilter() {
        FilterBasedGroupsCacheImpl filterBasedGroupsCache = new FilterBasedGroupsCacheImpl(
                userGroupStoreService);
        filterBasedGroupsCache.initCacheLoader();

        when(userGroupStoreService.getAllGroups()).thenReturn(ImmutableList.of(getAdminGroup()));

        Map<String, GroupInfo> adminGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ADMIN);
        assertEquals(adminGroups.size(), 1);
        assertNotNull(adminGroups.get("admins"));
        // Do it again
        filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ADMIN);
        // Only once
        verify(userGroupStoreService).getAllGroups();
    }

    public void getExternalGroupsByFilter() {
        FilterBasedGroupsCacheImpl filterBasedGroupsCache = new FilterBasedGroupsCacheImpl(
                userGroupStoreService);
        filterBasedGroupsCache.initCacheLoader();

        when(userGroupStoreService.getAllGroups()).thenReturn(ImmutableList.of(getExternalGroup()));

        final Map<String, GroupInfo> externalGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.EXTERNAL);
        assertEquals(externalGroups.size(), 1);
        assertNotNull(externalGroups.get("ldap-group"));
        // Do it again
        filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.EXTERNAL);
        // Only once
        verify(userGroupStoreService).getAllGroups();
    }

    public void getAllGroupsByFilter() {
        FilterBasedGroupsCacheImpl filterBasedGroupsCache = new FilterBasedGroupsCacheImpl(
                userGroupStoreService);
        filterBasedGroupsCache.initCacheLoader();

        when(userGroupStoreService.getAllGroups()).thenReturn(ImmutableList.of(getAdminGroup(), getExternalGroup()));

        Map<String, GroupInfo> adminGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ALL);
        assertEquals(adminGroups.size(), 2);
        assertNotNull(adminGroups.get("admins"));
        assertNotNull(adminGroups.get("ldap-group"));
        // Do it again
        filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ALL);
        // Only once
        verify(userGroupStoreService).getAllGroups();
    }

    public void testInvalidateCacheWithExternalRealm() {
        FilterBasedGroupsCacheImpl filterBasedGroupsCache = new FilterBasedGroupsCacheImpl(
                userGroupStoreService);
        filterBasedGroupsCache.initCacheLoader();

        when(userGroupStoreService.getAllGroups()).thenReturn(ImmutableList.of(getExternalGroup(), getAdminGroup()));

        Map<String, GroupInfo> externalGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.EXTERNAL);
        assertEquals(externalGroups.size(), 1);
        assertNotNull(externalGroups.get("ldap-group"));
        verify(userGroupStoreService).getAllGroups();

        Map<String, GroupInfo> adminGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ADMIN);
        assertEquals(adminGroups.size(), 1);
        assertNotNull(adminGroups.get("admins"));
        verify(userGroupStoreService, times(2)).getAllGroups();

        filterBasedGroupsCache.invalidate();
        // Admin groups cache should remain intact
        filterBasedGroupsCache.getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ADMIN);
        verify(userGroupStoreService, times(3)).getAllGroups();

        // External groups cache should be renewed
        filterBasedGroupsCache.getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.EXTERNAL);
        verify(userGroupStoreService, times(4)).getAllGroups();
    }

    public void testInvalidateCacheWithDefaultRealm() {
        FilterBasedGroupsCacheImpl filterBasedGroupsCache = new FilterBasedGroupsCacheImpl(
                userGroupStoreService);
        filterBasedGroupsCache.initCacheLoader();

        when(userGroupStoreService.getAllGroups()).thenReturn(ImmutableList.of(getExternalGroup(), getAdminGroup()));

        Map<String, GroupInfo> externalGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.EXTERNAL);
        assertEquals(externalGroups.size(), 1);
        assertNotNull(externalGroups.get("ldap-group"));
        verify(userGroupStoreService).getAllGroups();

        Map<String, GroupInfo> adminGroups = filterBasedGroupsCache
                .getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ADMIN);
        assertEquals(adminGroups.size(), 1);
        assertNotNull(adminGroups.get("admins"));
        verify(userGroupStoreService, times(2)).getAllGroups();

        filterBasedGroupsCache.invalidate();

        // External groups cache should be renewed
        filterBasedGroupsCache.getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.EXTERNAL);
        // Admin groups should be renewed
        filterBasedGroupsCache.getGroupsByFilter(AccessUserGroupStoreService.GroupFilter.ADMIN);

        verify(userGroupStoreService, times(4)).getAllGroups();
    }

    private GroupImpl getAdminGroup() {
        GroupImpl adminGroup = new GroupImpl();
        adminGroup.setGroupName("admins");
        adminGroup.setAdminPrivileges(true);
        return adminGroup;
    }

    private GroupImpl getExternalGroup() {
        GroupImpl externalGroup = new GroupImpl();
        externalGroup.setRealm(Realm.LDAP.getName());
        externalGroup.setGroupName("ldap-group");
        return externalGroup;
    }
}