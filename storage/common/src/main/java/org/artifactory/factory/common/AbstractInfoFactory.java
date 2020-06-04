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

package org.artifactory.factory.common;

import org.artifactory.factory.InfoFactory;
import org.artifactory.md.MutableMetadataInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.security.MutableBuildAclImpl;
import org.artifactory.model.xstream.security.MutableReleaseBundleAclImpl;
import org.artifactory.model.xstream.security.MutableRepoAclImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Date: 8/3/11
 * Time: 9:51 AM
 *
 * @author Fred Simon
 */
public abstract class AbstractInfoFactory implements InfoFactory {

    @Override
    public MutableRepoPermissionTarget createRepoPermissionTarget(String permName, List<String> repoKeys) {
        MutableRepoPermissionTarget permissionTarget = createRepoPermissionTarget();
        permissionTarget.setName(permName);
        permissionTarget.setRepoKeys(repoKeys);
        return permissionTarget;
    }

    @Override
    public MutableBuildPermissionTarget createBuildPermissionTarget(String permName, List<String> repoKeys) {
        MutableBuildPermissionTarget permissionTarget = createBuildPermissionTarget();
        permissionTarget.setName(permName);
        permissionTarget.setRepoKeys(repoKeys);
        return permissionTarget;
    }

    @Override
    public MutableReleaseBundlePermissionTarget createReleaseBundlePermissionTarget(String permName, List<String> repoKeys) {
        MutableReleaseBundlePermissionTarget permissionTarget = createReleaseBundlePermissionTarget();
        permissionTarget.setName(permName);
        permissionTarget.setRepoKeys(repoKeys);
        return permissionTarget;
    }

    @Override
    public MutableAceInfo createAce(String principal, boolean group, int mask) {
        MutableAceInfo aceInfo = createAce();
        aceInfo.setPrincipal(principal);
        aceInfo.setGroup(group);
        aceInfo.setMask(mask);
        return aceInfo;
    }

    @Override
    public Set<UserGroupInfo> createGroups(Set<String> names) {
        if (names == null) {
            return null;
        }
        //Create a list of default groups
        Set<UserGroupInfo> userGroupInfos = new HashSet<>(names.size());
        for (String name : names) {
            UserGroupInfo userGroupInfo = createUserGroup(name);
            userGroupInfos.add(userGroupInfo);
        }
        return userGroupInfos;
    }

    @Override
    public MutableGroupInfo createGroup(String groupName) {
        MutableGroupInfo group = createGroup();
        group.setGroupName(groupName);
        group.setRealm(SecurityConstants.DEFAULT_REALM);
        return group;
    }

    @Override
    public MutableUserInfo createUser(String userName) {
        MutableUserInfo user = createUser();
        user.setUsername(userName);
        return user;
    }

    @Override
    public MutableRepoAclImpl createRepoAcl(RepoPermissionTarget permissionTarget) {
        MutableRepoAclImpl acl = new MutableRepoAclImpl();
        acl.setPermissionTarget(permissionTarget);
        return acl;
    }

    @Override
    public MutableAcl<BuildPermissionTarget> createBuildAcl(BuildPermissionTarget permissionTarget) {
        MutableBuildAclImpl acl = new MutableBuildAclImpl();
        acl.setPermissionTarget(permissionTarget);
        return acl;
    }

    @Override
    public RepoAcl createRepoAcl(RepoPermissionTarget permissionTarget, Set<AceInfo> aces, String updatedBy,
            long lastUpdated) {
        return new MutableRepoAclImpl(permissionTarget, aces, updatedBy, lastUpdated);
    }

    @Override
    public RepoAcl createRepoAcl(RepoPermissionTarget permissionTarget, Set<AceInfo> aces, String updatedBy,
            long lastUpdated, String accessIdentifier) {
        return new MutableRepoAclImpl(permissionTarget, aces, updatedBy, lastUpdated, accessIdentifier);
    }

    @Override
    public BuildAcl createBuildAcl(BuildPermissionTarget permissionTarget, Set<AceInfo> aces,
            String updatedBy, long lastUpdated) {
        return new MutableBuildAclImpl(permissionTarget, aces, updatedBy, lastUpdated);
    }

    @Override
    public BuildAcl createBuildAcl(BuildPermissionTarget permissionTarget, Set<AceInfo> aces,
            String updatedBy, long lastUpdated, String accessIdentifier) {
        return new MutableBuildAclImpl(permissionTarget, aces, updatedBy, lastUpdated, accessIdentifier);
    }

    @Override
    public ReleaseBundleAcl createReleaseBundleAcl(ReleaseBundlePermissionTarget permissionTarget, Set<AceInfo> aces,
            String updatedBy, long lastUpdated) {
        return new MutableReleaseBundleAclImpl(permissionTarget, aces, updatedBy, lastUpdated);
    }

    @Override
    public ReleaseBundleAcl createReleaseBundleAcl(ReleaseBundlePermissionTarget permissionTarget, Set<AceInfo> aces,
            String updatedBy, long lastUpdated, String accessIdentifier) {
        return new MutableReleaseBundleAclImpl(permissionTarget, aces, updatedBy, lastUpdated, accessIdentifier);
    }

    @Override
    public MutableMetadataInfo createMetadata(RepoPath repoPath, String metadataName) {
        return createMetadata(new RepoPathImpl(repoPath.getRepoKey(),
                NamingUtils.getMetadataPath(repoPath.getPath(), metadataName)));
    }
}
