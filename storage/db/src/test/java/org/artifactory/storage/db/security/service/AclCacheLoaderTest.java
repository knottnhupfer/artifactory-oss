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

package org.artifactory.storage.db.security.service;

import com.google.common.collect.Sets;
import org.artifactory.model.xstream.security.AceImpl;
import org.artifactory.model.xstream.security.MutableRepoAclImpl;
import org.artifactory.model.xstream.security.RepoPermissionTargetImpl;
import org.artifactory.security.*;
import org.artifactory.storage.security.service.AclStoreService;
import org.easymock.EasyMock;
import org.jfrog.common.ClockUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.artifactory.security.PermissionTarget.ANY_LOCAL_REPO;
import static org.testng.Assert.*;

/**
 * UnitTest of AclCacheLoader, especially the call method
 * mock the dao to create AclCache
 *
 * @author nadavy
 */
@Test
public class AclCacheLoaderTest {

    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";
    private static final String USERNAME3 = "user3";
    private static final String GROUPNAME1 = "group1";
    private static final String GROUPNAME2 = "group2";
    private static final String GROUPNAME3 = "group3";

    protected static final String REPO1 = "repo1";
    private static final String REPO2 = "repo2";

    private AclCacheLoader.AclCacheItem aclCacheItem;

    private final Supplier<Collection<Acl<RepoPermissionTarget>>> getRepoAcls = this::getDownstreamAllRepoAcls;
    private final Function<RepoPermissionTarget, List<String>> getRepoKeysFunction = this::getRepoKeys;

    /**
     * Create DAO mocks, populate new AclCache and call AclCache.
     */
    @BeforeClass
    public void populateAclInfo() {
        AclStoreService aclStoreService = EasyMock.createMock(AclStoreService.class);
        EasyMock.replay(aclStoreService);

        AclCacheLoader cacheLoader = new AclCacheLoader(getRepoAcls, getRepoKeysFunction);
        aclCacheItem = cacheLoader.call();
    }

    /**
     * Assert the different AclCacheLoader caches- groups and users
     */
    public void testAclCacheLoader() {
        Map<String, Map<String, Set<PrincipalPermission>>> groupResultMap = aclCacheItem.getGroupResultMap();
        assertGroupMap(groupResultMap);

        Map<String, Map<String, Set<PrincipalPermission>>> userResultMap = aclCacheItem.getUserResultMap();
        assertUserMap(userResultMap);
    }

    protected List<String> getRepoKeys(RepoPermissionTarget repoPermissionTarget) {
        return repoPermissionTarget.getRepoKeys();
    }

    private Collection<Acl<RepoPermissionTarget>> getDownstreamAllRepoAcls() {
        Collection<Acl<RepoPermissionTarget>> aclInfos = Lists.newArrayList();
        aclInfos.add(getAnyAcl());
        aclInfos.add(getRepo1and2Acl());
        aclInfos.add(getRepo2Acl());
        return aclInfos;
    }


    /**
     * create readers ACL, with user1 and user2.
     */
    MutableRepoAclImpl getAnyAcl() {
        AceImpl user1 = new AceImpl(USERNAME1, false, 0);
        AceImpl user2 = new AceImpl(USERNAME2, false, 0);
        Set<AceInfo> aces = Sets.newHashSet(user1, user2);

        RepoPermissionTargetImpl pt = new RepoPermissionTargetImpl("readerT",
                Lists.newArrayList(ANY_LOCAL_REPO),
                Lists.newArrayList("**"),
                Lists.newArrayList(""));

        return new MutableRepoAclImpl(pt, aces, "me", ClockUtils.epochMillis());
    }

    /**
     * create deployers ACL, with user2, user3 and group1
     */
    MutableRepoAclImpl getRepo1and2Acl() {
        AceImpl user2 = new AceImpl(USERNAME2, false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl user3 = new AceImpl(USERNAME3, false, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl group1 = new AceImpl(GROUPNAME1, true, ArtifactoryPermission.DEPLOY.getMask());
        AceImpl group3 = new AceImpl(GROUPNAME3, true, ArtifactoryPermission.DEPLOY.getMask());
        Set<AceInfo> aces = Sets.newHashSet(user2, user3, group1, group3);

        RepoPermissionTarget pt = new RepoPermissionTargetImpl("deployT", Lists.newArrayList(REPO1, REPO2),
                Lists.newArrayList("**"), Lists.newArrayList("a/**"));

        return new MutableRepoAclImpl(pt, aces, "me", ClockUtils.epochMillis());
    }

    MutableRepoAclImpl getRepo2Acl() {
        AceImpl user3 = new AceImpl(USERNAME3, false, ArtifactoryPermission.DEPLOY.getMask());
        Set<AceInfo> aces = Sets.newHashSet(user3);

        RepoPermissionTarget pt = new RepoPermissionTargetImpl("repo2", Lists.newArrayList(REPO2),
                Lists.newArrayList("repo2only/**"), Lists.newArrayList("notInRepo2/**"));

        return new MutableRepoAclImpl(pt, aces, "me", ClockUtils.epochMillis());
    }

    private void assertUserMap(Map<String, Map<String, Set<PrincipalPermission>>> userResultMap) {
        assertEquals(3, userResultMap.size(), "UserAclMap should have 3 users");
        // test USERNAME1 first repo
        validateUserPermissions(userResultMap, USERNAME1, ANY_LOCAL_REPO, "readerT",
                Lists.newArrayList("**"), Lists.newArrayList(""), 1, 1, 1, 0);

        // test USERNAME2 first repo
        validateUserPermissions(userResultMap, USERNAME2, ANY_LOCAL_REPO, "readerT",
                Lists.newArrayList("**"), Lists.newArrayList(""), 3, 1, 1, 0);

        // test USERNAME2 second repo
        validateUserPermissions(userResultMap, USERNAME2, REPO1, "deployT", Lists.newArrayList("**"),
                Lists.newArrayList("a/**"), 3, 1, 2,
                ArtifactoryPermission.DEPLOY.getMask());

        // test USERNAME2 third repo
        validateUserPermissions(userResultMap, USERNAME2, REPO2, "deployT", Lists.newArrayList("**"),
                Lists.newArrayList("a/**"), 3, 1, 2,
                ArtifactoryPermission.DEPLOY.getMask());

        // test USERNAME3 repo2 under "deployT" permission target
        validateUserPermissions(userResultMap, USERNAME3, REPO2, "deployT", Lists.newArrayList("**"),
                Lists.newArrayList("a/**"), 2, 2, 2,
                ArtifactoryPermission.DEPLOY.getMask());

        // test USERNAME3 repo2 under "repo2" permission target
        validateUserPermissions(userResultMap, USERNAME3, REPO2, "repo2", Lists.newArrayList("repo2only/**"),
                Lists.newArrayList("notInRepo2/**"), 2, 2, 1,
                ArtifactoryPermission.DEPLOY.getMask());
    }

    private void validateUserPermissions(Map<String, Map<String, Set<PrincipalPermission>>> userResultMap,
            String userName, String repoKey, String permissionName, List includePattern, List excludePattern,
            int numOfPermissionsForUser,
            int numOfPermissionsPointToThisRepo, int allReposUnderThisPermissionTarget, int userPermissionMask) {

        Map<String, Set<PrincipalPermission>> userRepoToAclMap = userResultMap.get(userName);
        // validate user has num of permissions as expected
        assertEquals(userRepoToAclMap.size(), numOfPermissionsForUser);

        Set<PrincipalPermission> userPermission = userRepoToAclMap.get(repoKey);
        assertEquals(userPermission.size(), numOfPermissionsPointToThisRepo);

        // find specific permission data as the repo can be under multiple repos
        PrincipalPermission principalPermission = userPermission.stream()
                .filter(permission -> permission.getPermissionTarget().getName().equals(permissionName)).findFirst()
                .orElse(null);
        assertNotNull(principalPermission);

        RepoPermissionTarget permissionTarget = (RepoPermissionTarget) principalPermission.getPermissionTarget();
        assertEquals(permissionTarget.getRepoKeys().size(), allReposUnderThisPermissionTarget);
        assertTrue(permissionTarget.getRepoKeys().stream().anyMatch(repo -> repo.equals(repoKey)));
        assertEquals(permissionTarget.getIncludes(), includePattern);
        assertEquals(permissionTarget.getExcludes(), excludePattern);
        // validate ace
        AceInfo ace = principalPermission.getAce();
        assertEquals(ace.getPrincipal(), userName);
        assertEquals(ace.getMask(), userPermissionMask);
    }

    /**
     * assert that group1 is in 1 acl, has deploy permission on repo1 only
     * group2 should have any acls
     */
    private void assertGroupMap(Map<String, Map<String, Set<PrincipalPermission>>> groupRepoToAclMap) {
        assertEquals(2, groupRepoToAclMap.size());
        // validate GROUPNAME1
        validateGroupPermission(groupRepoToAclMap, REPO1, GROUPNAME1);
        validateGroupPermission(groupRepoToAclMap, REPO2, GROUPNAME1);

        // validate GROUPNAME2
        assertNull(groupRepoToAclMap.get(GROUPNAME2), "GROUP2 should have no permissions ");

        // validate GROUPNAME3
        validateGroupPermission(groupRepoToAclMap, REPO1, GROUPNAME3);
        validateGroupPermission(groupRepoToAclMap, REPO2, GROUPNAME3);
    }

    private void validateGroupPermission(Map<String, Map<String, Set<PrincipalPermission>>> groupRepoToAclMap,
            String repoKey, String groupName) {
        Map<String, Set<PrincipalPermission>> groupMapToAcl = groupRepoToAclMap.get(groupName);
        Set<PrincipalPermission> groupRepoAcls = groupMapToAcl.get(repoKey);
        assertEquals(1, groupRepoAcls.size(), "GROUP " + groupName + " should have only 1 ACL");
        PrincipalPermission principalPermission = groupRepoAcls.iterator().next();
        // validate permission target
        RepoPermissionTarget permissionTarget = (RepoPermissionTarget) principalPermission.getPermissionTarget();
        assertEquals(permissionTarget.getRepoKeys().size(), 2);
        assertEquals(permissionTarget.getIncludes(), Lists.newArrayList("**"));
        assertEquals(permissionTarget.getExcludes(), Lists.newArrayList("a/**"));
        // validate ace
        AceInfo ace = principalPermission.getAce();
        assertEquals(ace.getPrincipal(), groupName);
        assertEquals(ace.getMask(), ArtifactoryPermission.DEPLOY.getMask());
    }
}
