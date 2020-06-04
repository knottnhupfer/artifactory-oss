package org.artifactory.security.access.emigrate.conveter;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SecurityService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.event.AccessImportEvent;
import org.artifactory.factory.InfoFactory;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.*;
import org.artifactory.security.access.AccessService;
import org.artifactory.security.access.emigrate.AccessConverter;
import org.artifactory.security.interceptor.SecurityConfigurationChangesInterceptor;
import org.artifactory.update.security.SecurityVersion;
import org.jfrog.access.rest.permission.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

import static org.artifactory.security.PermissionTarget.ANY_PATH;
import static org.artifactory.security.PermissionTarget.DEFAULT_BUILD_PERMISSION_TARGET_NAME;
import static org.artifactory.storage.db.security.service.access.AclMapper.aclToAccessPermission;

/**
 * Creating a default builds permission called {@link PermissionTarget#DEFAULT_BUILD_PERMISSION_TARGET_NAME}.
 * This permission is made to have backward compatibility and allow all the users who previously had access to builds
 * to still have access after introducing Build Permissions.
 *
 * Permission specs:
 * 1. Repository name is {@link InternalBuildService#getBuildInfoRepoKey()}
 * 2. All build names are included (include pattern set for '**').
 *
 * 3. All users/groups who had any repo deploy --> read, write (filter anonymous)
 *
 * 4. If config descriptor "anonAccessToBuildInfosDisabled" value was False prior to field deletion in upgrade:
 * if user 'anonymous' had any deploy --> read, write
 *
 * 5. In addition, under {@link org.artifactory.version.converter.v218.AnonAccessToBuildsConverter} we set 2 flags.
 * These flags will allow users to continue "have read permission" on what they previously saw.
 * Anonymous access is considered there too.
 *
 * For example:
 * User yuvalr is part of group Wizards
 * Group wizards has DEPLOY permission for repo Rohan
 * Result will be --> User yuvalr will be NOT part of the default build permission
 *                    Group wizards will be part of the default build permission with read, write permission
 *                    Upon login user yuvalr will get permissions of both his user, and his group
 *
 * Test at SecurityServiceConverterTest
 *
 * @author Yuval Reches
 */
@Component
public class V6600CreateDefaultBuildAcl implements AccessConverter, SecurityConfigurationChangesInterceptor,
        ApplicationListener<AccessImportEvent> {
    private static final Logger log = LoggerFactory.getLogger(V6600CreateDefaultBuildAcl.class);

    private AccessService accessService;
    private UserGroupService userGroupService;
    private InternalBuildService internalBuildService;
    private CentralConfigService centralConfigService;

    private BiFunction<Object, ArtifactoryPermission, Boolean> isUserHasPermissionFunction;
    private BiFunction<Object, ArtifactoryPermission, Boolean> isGroupHasPermissionFunction;

    @Autowired
    public void setAccessService(AccessService accessService) {
        this.accessService = accessService;
    }

    @Autowired
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    @Autowired
    public void setInternalBuildService(InternalBuildService internalBuildService) {
        this.internalBuildService = internalBuildService;
    }

    @Autowired
    public void setCentralConfigService(CentralConfigService centralConfigService) {
        this.centralConfigService = centralConfigService;
    }

    @Autowired
    public void populateFunctions(AuthorizationService authorizationService) {
        this.isUserHasPermissionFunction = (user, permission) -> {
            List<RepoPermissionTarget> repoPermissionTargets = authorizationService
                    .getRepoPermissionTargets((UserInfo) user, permission);
            for (RepoPermissionTarget repoPermissionTargetInfo : repoPermissionTargets) {
                List<String> repoKeys = repoPermissionTargetInfo.getRepoKeys();
                if (repoKeys.contains(PermissionTarget.ANY_REPO) || repoKeys.contains(PermissionTarget.ANY_LOCAL_REPO)) {
                    return true;
                }
                Set<String> localRepo = centralConfigService.getDescriptor().getLocalRepositoriesMap().keySet();
                for (String repoKey : repoKeys) {
                    if (localRepo.contains(repoKey)) {
                        return true;
                    }
                }
            }
            return false;
        };

        this.isGroupHasPermissionFunction = (group, permission) ->
                authorizationService.hasAnyPermission((GroupInfo) group, permission);
    }

    @Override
    public void convert() {
        log.info("Starting '6.6.0: Add build permission target for existing users' Access Conversion");
        InfoFactory infoFactory = InfoFactoryHolder.get();
        boolean isDefaultAlreadyExists = accessService.getAccessClient().permissions()
                .findByServiceIdResourceTypeAndDisplayName(accessService.getArtifactoryServiceId(),
                        ArtifactoryResourceType.BUILD.getName(), DEFAULT_BUILD_PERMISSION_TARGET_NAME).isPresent();
        if (isDefaultAlreadyExists) {
            log.warn("{} permission already exists, stopping conversion", DEFAULT_BUILD_PERMISSION_TARGET_NAME);
            return;
        }

        MutableBuildPermissionTarget buildPermissionTarget =
                infoFactory.createBuildPermissionTarget(DEFAULT_BUILD_PERMISSION_TARGET_NAME,
                        Collections.singletonList(internalBuildService.getBuildInfoRepoKey()));
        buildPermissionTarget.setIncludesPattern(ANY_PATH);

        MutableAcl<BuildPermissionTarget> buildAcl = infoFactory.createBuildAcl(buildPermissionTarget);

        Set<AceInfo> aces = new HashSet<>();

        // Iterate the users list and add permissions
        List<UserInfo> allUsers = userGroupService.getAllUsers(false);
        addAcesForAllUsers(aces, allUsers);
        addAcesForAnonymous(aces, allUsers);

        // Iterate the groups list and add permissions
        List<GroupInfo> allGroups = userGroupService.getAllGroups();
        addAcesForAllGroups(aces, allGroups);

        buildAcl.setAces(aces);
        buildAcl.setUpdatedBy(SecurityService.USER_SYSTEM);

        createPermissionInAccess(buildAcl);

        log.info("Finished '6.6.0: Add build permission target for existing users' Access Conversion.");
    }

    private void createPermissionInAccess(MutableAcl<BuildPermissionTarget> buildAcl) {
        accessService.getAccessClient().permissions()
                .createPermission(aclToAccessPermission(buildAcl,
                        accessService.getArtifactoryServiceId().getFormattedName()));
    }

    private void addAcesForAnonymous(Set<AceInfo> aces, List<UserInfo> allUsers) {
        if (!getBuildsAnonymousAccessDisabled()) {
            log.debug("Anonymous access was permitted to builds, adding anonymous to permissions");
            allUsers.stream()
                    .filter(UserInfo::isAnonymous)
                    .forEach(userInfo -> {
                        MutableAceInfo ace = InfoFactoryHolder.get().createAce(userInfo.getUsername(), false, 0);
                        // In case any deploy permission for anonymous we add it to permission target
                        if (isUserHasPermissionFunction.apply(userInfo, ArtifactoryPermission.DEPLOY)) {
                            ace.setRead(true);
                            ace.setDeploy(true);
                        }
                        addPermission(aces, ace);
                    });
        }
    }

    private void addAcesForAllGroups(Set<AceInfo> aces, List<GroupInfo> allGroups) {
        allGroups.stream()
                .filter(group -> !group.isAdminPrivileges())
                .forEach(groupInfo -> {
                    String groupName = groupInfo.getGroupName();
                    setPermissionsForUserOrGroup(aces, groupInfo, groupName, isGroupHasPermissionFunction, true);
                });
    }

    private void addAcesForAllUsers(Set<AceInfo> aces, List<UserInfo> allUsers) {
        allUsers.stream()
                .filter(user -> !user.isAnonymous())
                .filter(user -> !user.isEffectiveAdmin())
                .forEach(userInfo -> {
                    String username = userInfo.getUsername();
                    setPermissionsForUserOrGroup(aces, userInfo, username, isUserHasPermissionFunction, false);
                });
    }

    private void setPermissionsForUserOrGroup(Set<AceInfo> aces, Object entityInfo, String entityName,
            BiFunction<Object, ArtifactoryPermission, Boolean> hasPermissionFunction, boolean isGroup) {
        MutableAceInfo ace = InfoFactoryHolder.get().createAce(entityName, isGroup, 0);
        if (hasPermissionFunction.apply(entityInfo, ArtifactoryPermission.DEPLOY)) {
            ace.setRead(true);
            ace.setDeploy(true);
        }
        addPermission(aces, ace);
    }

    private void addPermission(Set<AceInfo> aces, MutableAceInfo ace) {
        if (ace.getMask() != 0) {
            log.debug("User '{}' - adding permission mask {}", ace.getPrincipal(), ace.getMask());
            aces.add(ace);
        }
    }

    private boolean getBuildsAnonymousAccessDisabled() {
        File markerFile = ArtifactoryHome.get()
                .getCreateDefaultBuildPermissionMarkerFile();
        if (markerFile.exists()) {
            try {
                log.info("Reading marker file");
                return Boolean
                        .valueOf(FileUtils
                                .readFileToString(ArtifactoryHome.get().getCreateDefaultBuildPermissionMarkerFile()));
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Couldn't read default build permission marker file: " + e.getMessage());
            }
        } else {
            log.info("Getting anonymous access to builds from configuration");
            SecurityDescriptor security = centralConfigService.getDescriptor().getSecurity();
            return !security.isBuildGlobalBasicReadForAnonymous();
        }
    }

    @Override
    public void onAfterSecurityImport(SecurityInfo securityInfo) {
        SecurityVersion importedSecurityVersion = null;
        try {
            importedSecurityVersion = securityInfo.getVersion() != null ? SecurityVersion.valueOf(securityInfo.getVersion().trim()) : null;
        } catch (Exception e) {
            // Ignore. In case of missing or invalid version we will convert anyway
        }
        if (importedSecurityVersion == null) {
            log.warn("Version of security.xml is missing. Running conversion anyway");
        }

        if (importedSecurityVersion == null || importedSecurityVersion.compareTo(SecurityVersion.v10) < 0) {
            log.info("Importing security.xml older than {}, require conversion", SecurityVersion.v10);
            Optional<Permission> permission = accessService.getAccessClient().permissions()
                    .findByServiceIdResourceTypeAndDisplayName(accessService.getArtifactoryServiceId(),
                            ArtifactoryResourceType.BUILD.getName(), DEFAULT_BUILD_PERMISSION_TARGET_NAME);
            if (permission.isPresent()) {
                log.warn("Deleting {} permission", DEFAULT_BUILD_PERMISSION_TARGET_NAME);
                accessService.getAccessClient().permissions().deletePermissionById(permission.get().getId());
            }
            this.convert();
        }
    }

    @Override
    public void onApplicationEvent(AccessImportEvent event) {
        this.convert();
    }
}
