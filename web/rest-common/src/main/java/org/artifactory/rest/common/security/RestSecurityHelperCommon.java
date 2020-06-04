package org.artifactory.rest.common.security;

import com.google.common.collect.Sets;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.model.xstream.security.AceImpl;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.security.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.artifactory.security.PermissionTarget.*;
import static org.artifactory.util.CollectionUtils.notNullOrEmpty;

/**
 * (Bunch of shared code taken out from {@see RestSecurityRequestHandler})
 * @author Noam Y. Tenne
 * @author Dan Feldman
 */
public class RestSecurityHelperCommon {

    private static final String ERR_CANNOT_ADD_ADMIN = "' has admin privileges, and cannot be added to a Permission Target.";
    private static final String ERR_NON_EXISTING_PRINCIPAL = "Permission target contains a reference to a non-existing ";
    public static final String CONFLICT_ERR_MSG = "The permission target name that was " +
            "provided in the request path does not match the permission name in the provided permission " +
            "configuration object.";

    private RestSecurityHelperCommon() {}

    /**
     * @param entityKey Permission target to look for
     *
     * @return {@link PermissionTargetAcls}
     */
    public static PermissionTargetAcls getExistingPermissionTarget(AclService aclService, String entityKey) {
        return new PermissionTargetAcls(entityKey, aclService.getRepoAcl(entityKey), aclService.getBuildAcl(entityKey),
                aclService.getReleaseBundleAcl(entityKey));
    }

    /**
     * @param entityKey Permission target to look for.
     * If user doesn't have EntPlus/Edge license release-bundle is excluded from result.
     *
     * @return {@link PermissionTargetAcls}
     */
    public static PermissionTargetAcls getExistingPermissionTargetByLicense(AclService aclService, String entityKey) {
        return new PermissionTargetAcls(entityKey, aclService.getRepoAcl(entityKey), aclService.getBuildAcl(entityKey),
                aclService.getReleaseBundleAclByLicense(entityKey));
    }

    /**
     * @param inputMode governs which naming convention should be used when parsing permission action strings
     */
    @Nonnull
    public static Set<AceInfo> getAcesForAcl(PrincipalConfiguration actions, PermissionTargetNaming inputMode) {
        if (actions != null) {
            Set<AceInfo> aces = Sets.newHashSet();
            addPrincipalAces(aces, actions.getUsers(), false, inputMode);
            addPrincipalAces(aces, actions.getGroups(), true, inputMode);
            return aces;
        }
        return Sets.newHashSet();
    }

    /**
     * @param inputMode governs which naming convention should be used when parsing permission action strings
     */
    private static void addPrincipalAces(Set<AceInfo> aces, Map<String, Set<String>> actions, boolean group,
            PermissionTargetNaming inputMode) {
        if (actions != null) {
            for (Map.Entry<String, Set<String>> principalToActions : actions.entrySet()) {
                MutableAceInfo ace = new AceImpl();
                ace.setPrincipal(principalToActions.getKey());
                setPermissionActionsByNamingConvention(inputMode, ace, principalToActions.getValue());
                ace.setGroup(group);
                aces.add(ace);
            }
        }
    }

    private static void setPermissionActionsByNamingConvention(PermissionTargetNaming inputMode, MutableAceInfo ace,
            Set<String> actions) {
        switch (inputMode) {
            case NAMING_BACKEND:
                ace.setPermissionsFromStrings(actions);
                break;
            case NAMING_DISPLAY:
                ace.setPermissionsFromDisplayNames(actions);
                break;
            case NAMING_UI:
                ace.setPermissionsFromUiNames(actions);
                break;
            default:
                ace.setPermissionsFromStrings(actions);
                break;
        }
    }

    /**
     * @return the first non-existing repo contained in the {@param repositories} list
     */
    public static String getFirstNonExistingRepoFromList(List<String> repositories, CentralConfigService configService, AclService aclService) {
        if (repositories != null) {
            MutableCentralConfigDescriptor mutableDescriptor = configService.getMutableDescriptor();
            //remote-repo-cache should be handled like remote-repo, so remove the '-cache' suffix
            for (String repository : aclService.convertCachedRepoKeysToRemote(repositories)) {
                if (!mutableDescriptor.isRepositoryExists(repository)
                        && !ANY_REPO.equals(repository)
                        && !ANY_REMOTE_REPO.equals(repository)
                        && !ANY_LOCAL_REPO.equals(repository)
                        && !ANY_DISTRIBUTION_REPO.equals(repository)) {
                    return repository;
                }
            }
        }
        return null;
    }

    /**
     * @throws BadRequestException on non-existing principals or admin principals that were now added.
     * It is allowed to save a permission with admin principals that were part of the permission before the edit.
     */
    public static void checkForNonExistingPrinciples(UserGroupService userGroupService, PrincipalConfiguration principals,
            PermissionTargetAcls existingPermission) throws BadRequestException {
        if (principals != null) {
            if (principals.getUsers() != null) {
                principals.getUsers().entrySet().stream()
                        .filter(principalEntry -> notNullOrEmpty(principalEntry.getValue()))
                        .map(Map.Entry::getKey)
                        .forEach(principalName -> {
                            boolean isNewlyAddedUser = permissionDoesntContainPrincipal(existingPermission, principalName, false);
                            validateUserExistsAndNonAdmin(userGroupService, principalName, isNewlyAddedUser);
                        });
            }
            if (principals.getGroups() != null) {
                principals.getGroups().entrySet().stream()
                        .filter(principalEntry -> notNullOrEmpty(principalEntry.getValue()))
                        .map(Map.Entry::getKey)
                        .forEach(principalName -> {
                            boolean isNewlyAddedGroup = permissionDoesntContainPrincipal(existingPermission, principalName, true);
                            validateGroupExistsAndNonAdmin(userGroupService, principalName, isNewlyAddedGroup);
                        });
            }
        }
    }

    private static void validateUserExistsAndNonAdmin(UserGroupService userGroupService, String username, boolean performAdminValidation) {
        UserInfo user;
        try {
            user = userGroupService.findUser(username);
        } catch (Exception e) {
            throw new BadRequestException(ERR_NON_EXISTING_PRINCIPAL + "user '" + username + "'.");
        }
        if (performAdminValidation && user.isEffectiveAdmin()) {
            throw new BadRequestException(String.format("The user: '%s'%s", username, ERR_CANNOT_ADD_ADMIN));
        }
    }

    private static void validateGroupExistsAndNonAdmin(UserGroupService userGroupService, String groupName, boolean performAdminValidation) {
        GroupInfo group = userGroupService.findGroup(groupName);
        if (group == null) {
            throw new BadRequestException(ERR_NON_EXISTING_PRINCIPAL + "group '" + groupName + "'.");
        }
        if (performAdminValidation && group.isAdminPrivileges()) {
            throw new BadRequestException("Group '" + groupName + ERR_CANNOT_ADD_ADMIN);
        }
    }

    private static boolean permissionDoesntContainPrincipal(PermissionTargetAcls existingPermission, String principalName, boolean isGroup) {
        if (existingPermission == null) {
            return true;
        }
        return aclDoesntContainPrincipal(existingPermission.getRepoAcl(), principalName, isGroup) &&
                aclDoesntContainPrincipal(existingPermission.getBuildAcl(), principalName, isGroup) &&
                aclDoesntContainPrincipal(existingPermission.getReleaseBundleAcl(), principalName, isGroup);
    }

    private static boolean aclDoesntContainPrincipal(Acl<? extends RepoPermissionTarget> acl, String principalName, boolean isGroup) {
        return (acl == null ||
                acl.getAces().stream().noneMatch(aceInfo -> principalName.equals(aceInfo.getPrincipal()) && aceInfo.isGroup() == isGroup));
    }
}
