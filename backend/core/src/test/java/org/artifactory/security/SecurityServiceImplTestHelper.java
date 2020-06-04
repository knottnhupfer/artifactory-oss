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

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.PrincipalPermissionImpl;
import org.artifactory.storage.security.service.AclCache;
import org.jfrog.common.ClockUtils;

import java.util.*;

/**
 * @author nadavy
 */
class SecurityServiceImplTestHelper {

    private Map<String, Map<String, Set<PrincipalPermission<RepoPermissionTarget>>>> userToRepoToAclMap = Maps.newHashMap();
    private Map<String, Map<String, Set<PrincipalPermission<RepoPermissionTarget>>>> groupToRepoToAclMap = Maps.newHashMap();
    private InfoFactory factory = InfoFactoryHolder.get();
    private List<RepoAcl> repoAcls = Lists.newArrayList();

    String USER_AND_GROUP_SHARED_NAME = "usergroup";

    List<RepoAcl> createTestAcls() {
        return repoAcls;
    }

    AclCache<RepoPermissionTarget> createUserAndGroupResultMap() {

        // Permission Target 1
        MutableAceInfo adminAce = factory.createAce("yossis", false, ArtifactoryPermission.MANAGE.getMask());
        adminAce.setDeploy(true);
        adminAce.setRead(true);
        MutableAceInfo readerAce = factory.createAce("user", false, ArtifactoryPermission.READ.getMask());
        MutableAceInfo deleteAce = factory.createAce("shay", false, ArtifactoryPermission.DELETE.getMask());
        deleteAce.setDeploy(true);
        deleteAce.setAnnotate(true);
        deleteAce.setRead(true);
        MutableAceInfo userGroupAce =
                factory.createAce(USER_AND_GROUP_SHARED_NAME, false, ArtifactoryPermission.READ.getMask());
        MutableAceInfo deployerGroupAce =
                factory.createAce("deployGroup", true, ArtifactoryPermission.DEPLOY.getMask());

        List<String> repoPaths = Lists.newArrayList("testRepo1", "testRemote-cache");
        List<MutableAceInfo> aces = Lists.newArrayList(adminAce, readerAce, deleteAce, userGroupAce, deployerGroupAce);
        addAcesWithPathsToAclCache(aces, repoPaths);

        // Permission Target 2
        MutableAceInfo target2GroupAce = factory.createAce(USER_AND_GROUP_SHARED_NAME, true,
                ArtifactoryPermission.READ.getMask());
        addAceWithPathToAclCache(target2GroupAce, "testRepo2");

        // acl for any repository with read permissions to group
        MutableAceInfo readerGroupAce =
                factory.createAce("anyRepoReadersGroup", true, ArtifactoryPermission.READ.getMask());
        addAceWithPathToAclCache(readerGroupAce, PermissionTarget.ANY_REPO);

        // acl with multiple repo keys with read permissions to group and anonymous
        MutableAceInfo multiReaderGroupAce =
                factory.createAce("multiRepoReadersGroup", true, ArtifactoryPermission.READ.getMask());
        MutableAceInfo multiReaderAnonAce =
                factory.createAce(UserInfo.ANONYMOUS, false, ArtifactoryPermission.READ.getMask());
        List<String> repoKeys = Lists.newArrayList("multi1", "multi2");
        List<MutableAceInfo> multiAces = Lists.newArrayList(multiReaderAnonAce, multiReaderGroupAce);
        addAcesWithPathsToAclCache(multiAces, repoKeys);

        // acl for any repository with specific path delete permissions to user
        MutableRepoPermissionTarget anyRepoSpecificPathTarget = InfoFactoryHolder.get().createRepoPermissionTarget(
                "anyRepoSpecificPathTarget",
                Collections.singletonList("specific-repo"));
        anyRepoSpecificPathTarget.setIncludes(Collections.singletonList("com/acme/**"));
        addAceWithPathToAclCache(deleteAce, "specific-repo", anyRepoSpecificPathTarget);
        addAceWithPathToAclCache(deleteAce, "specific-repo", anyRepoSpecificPathTarget);

        MutableAceInfo anyLocalAce =
                factory.createAce("anyLocalUser", false, ArtifactoryPermission.DEPLOY.getMask());
        addAceWithPathToAclCache(anyLocalAce, PermissionTarget.ANY_LOCAL_REPO);

        MutableAceInfo anyRemoteAce =
                factory.createAce("anyRemoteUser", false, ArtifactoryPermission.READ.getMask());
        addAceWithPathToAclCache(anyRemoteAce, PermissionTarget.ANY_REMOTE_REPO);

        MutableAceInfo anyDistReadAce =
                factory.createAce("anyDistReader", false, ArtifactoryPermission.READ.getMask());
        addAceWithPathToAclCache(anyDistReadAce, PermissionTarget.ANY_DISTRIBUTION_REPO);

        MutableAceInfo anyDistWriteAce =
                factory.createAce("anyDistDeployer", false, ArtifactoryPermission.DEPLOY.getMask());
        addAceWithPathToAclCache(anyDistWriteAce, PermissionTarget.ANY_DISTRIBUTION_REPO);


        // create the AclCache
        return new AclCache<>(groupToRepoToAclMap, userToRepoToAclMap);
    }

    private void addAceWithPathToAclCache(MutableAceInfo aceInfo, String repoPath,
            MutableRepoPermissionTarget pmi) {
        Set<AceInfo> targetAces = new HashSet<>(Collections.singletonList(aceInfo));
        RepoAcl repoAcl = factory.createRepoAcl(pmi, targetAces, "me", ClockUtils.epochMillis());
        repoAcls.add(repoAcl);
        Map<String, Map<String, Set<PrincipalPermission<RepoPermissionTarget>>>> resultMap;
        if (aceInfo.isGroup()) {
            resultMap = groupToRepoToAclMap;
        } else {
            resultMap = userToRepoToAclMap;
        }

        Map<String, Set<PrincipalPermission<RepoPermissionTarget>>> itemRepoMap = resultMap.get(aceInfo.getPrincipal());
        if (itemRepoMap == null) {
            resultMap.put(aceInfo.getPrincipal(), Maps.newHashMap());
            itemRepoMap = resultMap.get(aceInfo.getPrincipal());
        }
        if (itemRepoMap.containsKey(repoPath)) {
            itemRepoMap.get(repoPath).add(new PrincipalPermissionImpl<>(repoAcl.getPermissionTarget(), aceInfo));
        } else {
            itemRepoMap.put(repoPath, Sets.newHashSet(new PrincipalPermissionImpl<>(repoAcl.getPermissionTarget(), aceInfo)));
        }
    }

    private void addAceWithPathToAclCache(MutableAceInfo aceInfo, String repoPath) {
        MutableRepoPermissionTarget pmi = InfoFactoryHolder.get()
                .createRepoPermissionTarget("target_" + aceInfo.getPrincipal(),
                        Collections.singletonList(repoPath));
        addAceWithPathToAclCache(aceInfo, repoPath, pmi);
    }

    private void addAcesWithPathToAclCache(List<MutableAceInfo> aceInfo, String repoPath) {
        aceInfo.forEach(ace -> addAceWithPathToAclCache(ace, repoPath));
    }

    private void addAcesWithPathsToAclCache(List<MutableAceInfo> aceInfo, List<String> repoPaths) {
        repoPaths.forEach(repoPath -> addAcesWithPathToAclCache(aceInfo, repoPath));
    }
}
