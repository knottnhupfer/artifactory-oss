/*
 *
 * Copyright 2016 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.rest.common.security;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.artifactory.model.xstream.security.AceImpl;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.security.*;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.security.permissions.SecurityEntityPermissionTargetModel;
import org.artifactory.security.permissions.SecurityEntityRepoPermissionTargetModel;
import org.jfrog.common.StreamSupportUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static org.artifactory.security.PermissionTargetNaming.NAMING_BACKEND;

/**
 * @author Noam Y. Tenne
 */
public abstract class SecurityModelPopulator {

    private SecurityModelPopulator() {

    }

    @Nonnull
    public static UserConfigurationImpl getUserConfiguration(@Nonnull UserInfo user) {
        UserConfigurationImpl userConfiguration = new UserConfigurationImpl();
        long lastLoginTimeMillis = user.getLastLoginTimeMillis();
        if (lastLoginTimeMillis > 0) {
            userConfiguration.setLastLoggedIn(RestUtils.toIsoDateString(lastLoginTimeMillis));
        }
        userConfiguration.setRealm(user.getRealm());
        userConfiguration.setAdmin(user.isAdmin());
        userConfiguration.setEmail(user.getEmail());
        userConfiguration.setName(user.getUsername());
        userConfiguration.setProfileUpdatable(user.isUpdatableProfile());
        userConfiguration.setInternalPasswordDisabled(user.isPasswordDisabled());
        Set<UserGroupInfo> groups = user.getGroups();
        if ((groups != null) && !groups.isEmpty()) {
            userConfiguration.setGroups(Sets.newHashSet(groups.stream().map(input -> {
                        if (input == null) {
                            return null;
                        }
                        return input.getGroupName();
                    }).collect(Collectors.toList())
            ));
        }
        Set<UserPropertyInfo> properties = user.getUserProperties();
        List<UserPropertyInfo> disableUiProperty = properties.stream()
                .filter(userPropertyInfo -> userPropertyInfo.getPropKey().equals("blockUiView"))
                .collect(Collectors.toList());
        if (!disableUiProperty.isEmpty()) {
            userConfiguration.setdisableUIAccess(Boolean.valueOf(disableUiProperty.get(0).getPropValue()));
        }
        return userConfiguration;
    }

    @Nonnull
    public static GroupConfigurationImpl getGroupConfiguration(@Nonnull GroupInfo group, @Nullable List<String> users) {
        GroupConfigurationImpl groupConfiguration = new GroupConfigurationImpl();
        groupConfiguration.setDescription(group.getDescription());
        groupConfiguration.setAutoJoin(group.isNewUserDefault());
        groupConfiguration.setName(group.getGroupName());
        groupConfiguration.setRealm(group.getRealm());
        groupConfiguration.setRealmAttributes(group.getRealmAttributes());
        groupConfiguration.setAdminPrivileges(group.isAdminPrivileges());
        groupConfiguration.setUserNames(users);
        return groupConfiguration;
    }

    @Nonnull
    public static PermissionTargetConfigurationImpl getPermissionTargetConfiguration(@Nonnull RepoAcl acl) {
        PermissionTargetConfigurationImpl permissionTargetConfiguration = new PermissionTargetConfigurationImpl();
        RepoPermissionTarget permissionTarget = acl.getPermissionTarget();
        permissionTargetConfiguration.setName(permissionTarget.getName());
        permissionTargetConfiguration.setIncludesPattern(permissionTarget.getIncludesPattern());
        permissionTargetConfiguration.setExcludesPattern(permissionTarget.getExcludesPattern());
        permissionTargetConfiguration.setRepositories(permissionTarget.getRepoKeys());

        PrincipalConfiguration principalConfiguration = getPrincipalConfiguration(acl.getMutableAces(), NAMING_BACKEND);
        permissionTargetConfiguration.setPrincipals(principalConfiguration);
        return permissionTargetConfiguration;
    }

    public static PermissionTargetModel getPermissionTargetModelV2(RepoAcl repoAcl, BuildAcl buildAcl,
            ReleaseBundleAcl releaseBundleAcl, PermissionTargetNaming outputMode) {
        PermissionTargetModel permissionTargetModel = new PermissionTargetModel();
        populateRepoPermissionTargetModel(repoAcl, permissionTargetModel, outputMode);
        populateBuildPermissionTargetModel(buildAcl, permissionTargetModel, outputMode);
        populateReleaseBundlePermissionTargetModel(releaseBundleAcl, permissionTargetModel, outputMode);
        return permissionTargetModel;
    }

    public static List<SecurityEntityPermissionTargetModel> getSecurityEntityPermissionTargetModelForRest(
            Map<String, SecurityEntityRepoPermissionTargetModel> repoPermissions,
            Map<String, SecurityEntityRepoPermissionTargetModel> buildPermissions,
            Map<String, SecurityEntityRepoPermissionTargetModel> releaseBundlePermissions) {
        Set<String> targetNames = new HashSet<>(buildPermissions.keySet());
        targetNames.addAll(repoPermissions.keySet());
        targetNames.addAll(releaseBundlePermissions.keySet());

        return StreamSupportUtils.stream(targetNames)
                .map(name -> {
                    SecurityEntityPermissionTargetModel securityEntityPermissionTargetModel = new SecurityEntityPermissionTargetModel();
                    securityEntityPermissionTargetModel.setName(name);
                    securityEntityPermissionTargetModel.setBuild(buildPermissions.get(name));
                    securityEntityPermissionTargetModel.setRepo(repoPermissions.get(name));
                    securityEntityPermissionTargetModel.setReleaseBundle(releaseBundlePermissions.get(name));
                    return securityEntityPermissionTargetModel;
                })
                .collect(Collectors.toList());
    }

    public static SecurityEntityRepoPermissionTargetModel getSecurityEntityRepoPermissionTargetModelV2(AceInfo aceInfo, RepoPermissionTarget repoPermissionTarget) {
        SecurityEntityRepoPermissionTargetModel securityEntityRepoPermissionTargetModel = fromRepoPermissionTarget(repoPermissionTarget);
        securityEntityRepoPermissionTargetModel.setActions(new ArrayList<>(aceInfo.getPermissionsDisplayNames()));
        return securityEntityRepoPermissionTargetModel;
    }

    public static SecurityEntityRepoPermissionTargetModel getSecurityEntityRepoPermissionTargetModelV2(Collection<AceInfo> aceInfos, RepoPermissionTarget repoPermissionTarget) {
        SecurityEntityRepoPermissionTargetModel securityEntityRepoPermissionTargetModel = fromRepoPermissionTarget(repoPermissionTarget);
        securityEntityRepoPermissionTargetModel.setActions(new ArrayList<>(mergeAceInfoActions(aceInfos).getPermissionsDisplayNames()));
        return securityEntityRepoPermissionTargetModel;
    }

    private static SecurityEntityRepoPermissionTargetModel fromRepoPermissionTarget(RepoPermissionTarget repoPermissionTarget) {
        SecurityEntityRepoPermissionTargetModel securityEntityRepoPermissionTargetModel = new SecurityEntityRepoPermissionTargetModel();
        securityEntityRepoPermissionTargetModel.setRepositories(repoPermissionTarget.getRepoKeys());
        securityEntityRepoPermissionTargetModel.setIncludePatterns(repoPermissionTarget.getIncludes());
        securityEntityRepoPermissionTargetModel.setExcludePatterns(repoPermissionTarget.getExcludes());
        return securityEntityRepoPermissionTargetModel;
    }

    private static void populateRepoPermissionTargetModel(@Nullable RepoAcl repoAcl,
            PermissionTargetModel permissionTargetModel, PermissionTargetNaming outputMode) {
        if (repoAcl == null) {
            return;
        }
        permissionTargetModel.setRepo(createPermissionTargetModel(repoAcl, outputMode));
        permissionTargetModel.setName(repoAcl.getPermissionTarget().getName());
    }

    private static void populateBuildPermissionTargetModel(@Nullable BuildAcl buildAcl,
            PermissionTargetModel permissionTargetModel, PermissionTargetNaming outputMode) {
        if (buildAcl == null) {
            return;
        }
        permissionTargetModel.setBuild(createPermissionTargetModel(buildAcl, outputMode));
        permissionTargetModel.setName(buildAcl.getPermissionTarget().getName());
    }

    private static void populateReleaseBundlePermissionTargetModel(@Nullable ReleaseBundleAcl releaseBundleAcl,
            PermissionTargetModel permissionTargetModel, PermissionTargetNaming outputMode) {
        if (releaseBundleAcl == null) {
            return;
        }
        permissionTargetModel.setReleaseBundle(createPermissionTargetModel(releaseBundleAcl, outputMode));
        permissionTargetModel.setName(releaseBundleAcl.getPermissionTarget().getName());
    }

    private static RepoPermissionTargetModel createPermissionTargetModel(Acl<? extends RepoPermissionTarget> acl,
            PermissionTargetNaming outputMode) {
        RepoPermissionTarget permissionTarget = acl.getPermissionTarget();
        PrincipalConfiguration principalConfiguration = getPrincipalConfiguration(acl.getMutableAces(), outputMode);
        return RepoPermissionTargetModel.builder()
                .includePatterns(permissionTarget.getIncludes())
                .excludePatterns(permissionTarget.getExcludes())
                .repositories(permissionTarget.getRepoKeys())
                .actions(principalConfiguration)
                .build();
    }

    /**
     * {@param outputMode} used for v2 api and ui, where we output human-readable permission strings, or camel cased.
     * (internally they are still saved as short strings - see {@link ArtifactoryPermission})
     */
    private static PrincipalConfiguration getPrincipalConfiguration(Set<MutableAceInfo> aces, PermissionTargetNaming outputMode) {
        Map<String, Set<String>> users = Maps.newHashMap();
        Map<String, Set<String>> groups = Maps.newHashMap();

        for (AceInfo ace : aces) {
            String principal = ace.getPrincipal();
            Set<String> permissionsAsString;
            permissionsAsString = getPermissionsByNamingConvention(ace, outputMode);
            if (ace.isGroup()) {
                groups.put(principal, permissionsAsString);
            } else {
                users.put(principal, permissionsAsString);
            }
        }
        PrincipalConfiguration principalConfiguration = new PrincipalConfiguration();
        if (!users.isEmpty()) {
            principalConfiguration.setUsers(users);
        }
        if (!groups.isEmpty()) {
            principalConfiguration.setGroups(groups);
        }
        return principalConfiguration;
    }

    private static Set<String> getPermissionsByNamingConvention(AceInfo ace, PermissionTargetNaming outputMode) {
        Set<String> permissionsAsString;
        switch (outputMode) {
            case NAMING_BACKEND: //v1 api and access
                permissionsAsString = ace.getPermissionsAsString();
                break;
            case NAMING_DISPLAY:
                permissionsAsString = ace.getPermissionsDisplayNames();
                break;
            case NAMING_UI:
                permissionsAsString = ace.getPermissionsUiNames();
                break;
            default:
                permissionsAsString = ace.getPermissionsAsString();
                break;
        }
        return permissionsAsString;
    }

    public static Set<String> getPermissionsAsString(boolean canRead, boolean canAnnotate, boolean canDeploy,
            boolean canDelete, boolean canAdmin) {

        Set<String> permissionsAsString = Sets.newHashSet();
        if (canRead) {
            appendPermissionString(permissionsAsString, ArtifactoryPermission.READ);
        }
        if (canAnnotate) {
            appendPermissionString(permissionsAsString, ArtifactoryPermission.ANNOTATE);
        }
        if (canDeploy) {
            appendPermissionString(permissionsAsString, ArtifactoryPermission.DEPLOY);
        }
        if (canDelete) {
            appendPermissionString(permissionsAsString, ArtifactoryPermission.DELETE);
        }
        if (canAdmin) {
            appendPermissionString(permissionsAsString, ArtifactoryPermission.MANAGE);
        }
        return permissionsAsString;
    }

    private static void appendPermissionString(Set<String> permissionsAsString, ArtifactoryPermission permission) {
        permissionsAsString.add(permission.getString());
    }

    private static AceImpl mergeAceInfoActions(Collection<AceInfo> aceInfos) {
        Set<String> mergedOrderedPermissionDisplayNames = StreamSupportUtils.stream(aceInfos)
                .flatMap(aceInfo -> StreamSupportUtils.stream(aceInfo.getPermissionsDisplayNames()))
                .collect(Collectors.toSet());
        AceImpl aceInfo = new AceImpl();
        aceInfo.setPermissionsFromDisplayNames(mergedOrderedPermissionDisplayNames);
        return aceInfo;
    }

}
