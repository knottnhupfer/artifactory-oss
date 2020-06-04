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

package org.artifactory.rest.services.permissions;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.release.bundle.ReleaseBundleService;
import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.rest.common.model.continues.FetchFunction;
import org.artifactory.api.rest.common.model.continues.util.PagingUtils;
import org.artifactory.api.rest.constant.RestConstants;
import org.artifactory.api.security.AclService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.SearchStringPermissionFilter;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.build.InternalBuildService;
import org.artifactory.bundle.BundleNameAndRepo;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.model.xstream.security.BuildPermissionTargetImpl;
import org.artifactory.model.xstream.security.MutableRepoAclImpl;
import org.artifactory.model.xstream.security.ReleaseBundlePermissionTargetImpl;
import org.artifactory.model.xstream.security.RepoPermissionTargetImpl;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.rest.common.security.SecurityModelPopulator;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ConflictException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.security.*;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.security.permissions.SecurityEntityPermissionTargetModel;
import org.artifactory.security.permissions.SecurityEntityRepoPermissionTargetModel;
import org.artifactory.storage.StorageException;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.UnsupportedByLicenseException;
import org.jfrog.common.ClockUtils;
import org.jfrog.common.StreamSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.stripEnd;
import static org.artifactory.addon.release.bundle.ReleaseBundleAddon.ENTERPRISE_PLUS_MSG;
import static org.artifactory.api.rest.common.model.continues.util.Direction.DESC;
import static org.artifactory.api.rest.constant.SecurityRestConstants.PATH_ROOT_V2;
import static org.artifactory.api.rest.constant.SecurityRestConstants.PERMISSIONS_ROOT;
import static org.artifactory.descriptor.repo.releasebundle.ReleaseBundlesRepoDescriptor.RELEASE_BUNDLE_DEFAULT_REPO;
import static org.artifactory.rest.common.security.RestSecurityHelperCommon.*;
import static org.artifactory.security.ArtifactoryResourceType.*;
import static org.artifactory.security.PermissionTargetNaming.NAMING_DISPLAY;
import static org.artifactory.security.PermissionTargetNaming.NAMING_UI;
import static org.artifactory.util.CollectionUtils.isNullOrEmpty;

/**
 * Common methods for CRUD actions on permission targets via REST api and ui.
 *
 * @author Yuval Reches
 * @author Dan Feldman
 */
@Component
public class RestSecurityRequestHandlerV2Impl implements InternalRestSecurityRequestHandlerV2 {
    private static final Logger log = LoggerFactory.getLogger(RestSecurityRequestHandlerV2Impl.class);

    public static final String MISSING_PARTS_ERR_MSG = "Permission target request missing repositories, builds and " +
            "release bundles. Must have at least one.";
    public static final String ALREADY_EXISTS_ERR_MSG = "Can't create permission target '%s' for type %s. It already exists.";
    private static final String REPO_NOT_ALLOWED_ERR_MSG = "Setting repository permissions on %s Repository is not allowed.";
    public static final String ENTERPRISE_PLUS_ERR_MSG = "Creating permission target with release bundles " + ENTERPRISE_PLUS_MSG;

    private AclService aclService;
    private UserGroupService userGroupService;
    private CentralConfigService configService;
    private AuthorizationService authorizationService;
    private InternalBuildService buildService;
    private ReleaseBundleService releaseBundleService;
    private InternalRepositoryService repositoryService;
    private AddonsManager addonsManager;

    @Autowired
    public RestSecurityRequestHandlerV2Impl(AclService aclService,
            UserGroupService userGroupService, CentralConfigService configService,
            AuthorizationService authorizationService, InternalBuildService buildService,
            ReleaseBundleService releaseBundleService,
            InternalRepositoryService repositoryService,
            AddonsManager addonsManager) {
        this.aclService = aclService;
        this.userGroupService = userGroupService;
        this.configService = configService;
        this.authorizationService = authorizationService;
        this.buildService = buildService;
        this.releaseBundleService = releaseBundleService;
        this.repositoryService = repositoryService;
        this.addonsManager = addonsManager;
    }

    /*
     * Public methods
     */
    @Override
    public boolean isPermissionTargetExists(String entityKey) throws ForbiddenWebAppException, NotFoundException {
        PermissionTargetAcls permissionExist = getExistingPermissionTargetByLicense(aclService, entityKey);
        RepoAcl repoAcl = permissionExist.getRepoAcl();
        BuildAcl buildAcl = permissionExist.getBuildAcl();
        ReleaseBundleAcl releaseBundleAcl = permissionExist.getReleaseBundleAcl();

        if (repoAcl == null && buildAcl == null && releaseBundleAcl == null) {
            return false;
        }

        // look for at least one existing acl that the user has manage permission for
        boolean repoExistsAndAllowed = isAclExistsAndAllowed(repoAcl);
        boolean buildExistsAndAllowed = isAclExistsAndAllowed(buildAcl);
        boolean releaseBundleExistsAndAllowed = isAclExistsAndAllowed(releaseBundleAcl);

        if (repoExistsAndAllowed || buildExistsAndAllowed || releaseBundleExistsAndAllowed) {
            return true;
        }
        // if we got here all sections are either doesn't exist or exists and denied.
        boolean repoAclDenied = repoAcl != null;
        boolean buildAclDenied = buildAcl != null;
        boolean releaseBundleAclDenied = releaseBundleAcl != null;

        throwForbiddenPermissionErrorMessage(entityKey, getDeniedPermissionTypesMessage(repoAclDenied, buildAclDenied,
                releaseBundleAclDenied));
        return false;
    }

    @Override
    public ContinueResult<PermissionTargetModel> getPagingPermissionTargets(PermissionTargetNaming outputMode,
            SearchStringPermissionFilter searchStringPermissionFilter) {
        Map<Character, List<PermissionTargetAcls>> map =
                aclService.getAllAclsMappedByPermissionTargetFirstChar(searchStringPermissionFilter.getDirection() == DESC);

        List<FetchFunction<PermissionTargetModel>> functionsByFirstPermisionTargetNameChar =
                StreamSupportUtils.mapEntriesStream(map)
                        .map(characterListEntry -> getFunctionByFirstPermisionTargetNameChar(characterListEntry.getValue(), searchStringPermissionFilter, outputMode))
                        .collect(Collectors.toList());
        return PagingUtils.getPagingFromMultipleFunctions(searchStringPermissionFilter, functionsByFirstPermisionTargetNameChar);
    }

    @Override
    public List<PermissionTargetModel> getAllPermissionTargets(PermissionTargetNaming outputMode) {
        Map<String, RepoAcl> keyToRepoAcl = getAllReposAcls();
        Map<String, BuildAcl> keyToBuildAcl = getAllBuildsAcls();
        Map<String, ReleaseBundleAcl> keyToReleaseBundleAcl = getAllReleaseBundleAcls();
        // Aggregate same-name repo, build and release-bundle acls into one model, since we got only user-manageable
        // permissions no need to validate
        List<PermissionTargetModel> repoBuildAndReleaseBundlePermissions = collectRepoBuildAndReleaseBundlePermissions(
                keyToRepoAcl, keyToBuildAcl, keyToReleaseBundleAcl, outputMode);

        addBuildAndReleaseBundlePermissions(repoBuildAndReleaseBundlePermissions, keyToBuildAcl,
                keyToReleaseBundleAcl, outputMode);

        // Adding all the release bundle acls that did not mach any repo and build acl name
        keyToReleaseBundleAcl.keySet().stream()
                .map(aclKey -> toPermissionTargetModel(null, null, keyToReleaseBundleAcl.get(aclKey), outputMode))
                .forEach(repoBuildAndReleaseBundlePermissions::add);
        return repoBuildAndReleaseBundlePermissions;
    }

    private FetchFunction<PermissionTargetModel> getFunctionByFirstPermisionTargetNameChar(List<PermissionTargetAcls> permissionTargetAcls,
            SearchStringPermissionFilter searchStringPermissionFilter, PermissionTargetNaming outputMode) {

        return new FetchFunction<>((skip, limit) -> {

            List<PermissionTargetModel> permissionTargetModels = StreamSupportUtils.stream(permissionTargetAcls)
                    .skip(skip)
                    .filter(permissionTargetAcls1 -> acceptSearch(permissionTargetAcls1, searchStringPermissionFilter))
                    .map(permissionTargetAcls2 -> createPermissionTarget(permissionTargetAcls2, outputMode))
                    .filter(this::acceptContainsAnyAcl)
                    .limit(limit)
                    .collect(Collectors.toList());
            int lastIndex = getLastIndexOfKeys(permissionTargetAcls, permissionTargetModels, limit);

            return new ContinueResult<>(lastIndex, permissionTargetModels);
        }, (long) permissionTargetAcls.size());
    }

    private boolean acceptContainsAnyAcl(PermissionTargetModel permissionTargetAcls) {
        return permissionTargetAcls.getRepo() != null || permissionTargetAcls.getBuild() != null;
    }

    private PermissionTargetModel createPermissionTarget(PermissionTargetAcls permissionTargetAcls, PermissionTargetNaming outputMode) {
        RepoAcl repoAcl = permissionTargetAcls.getRepoAcl() != null && aclService.canManage(permissionTargetAcls.getRepoAcl())
                ? permissionTargetAcls.getRepoAcl() : null;
        BuildAcl buildAcl = permissionTargetAcls.getBuildAcl() != null && aclService.canManage(permissionTargetAcls.getBuildAcl())
                ? permissionTargetAcls.getBuildAcl() : null;
        return toPermissionTargetModel(repoAcl, buildAcl, null, outputMode);
    }

    private int getLastIndexOfKeys(List<PermissionTargetAcls> permissionTargetAcls, List<PermissionTargetModel> permissionTargetModels, Integer limit) {
        List<String> names = StreamSupportUtils.stream(permissionTargetAcls).map(PermissionTargetAcls::getPermissionTargetName).collect(Collectors.toList());
        return CollectionUtils.notNullOrEmpty(permissionTargetModels) && permissionTargetModels.size() == limit
                ? names.indexOf(permissionTargetModels.get(permissionTargetModels.size() - 1).getName()) + 1
                : names.size();
    }

    private boolean acceptSearch(PermissionTargetAcls permissionTargetAcls, SearchStringPermissionFilter searchStringPermissionFilter) {
        return !StringUtils.isNotBlank(searchStringPermissionFilter.getSearchStr()) ||
                StringUtils.containsIgnoreCase(permissionTargetAcls.getPermissionTargetName(), searchStringPermissionFilter.getSearchStr());
    }

    @Override
    public Set<SecurityEntityListItem> getAllPermissionTargets(HttpServletRequest request) {
        return getAllPermissionTargets(NAMING_DISPLAY).stream()
                .map(permission -> getPermissionListItem(request, permission))
                .collect(Collectors.toSet());
    }

    @Override
    public PermissionTargetModel getPermissionTarget(String entityKey, PermissionTargetNaming outputMode)
            throws ForbiddenWebAppException, NotFoundException {
        RepoAcl repoAcl = aclService.getRepoAcl(entityKey);
        BuildAcl buildAcl = aclService.getBuildAcl(entityKey);
        ReleaseBundleAcl releaseBundleAcl =
                hasEdgeOrEnterprisePlusLic() ? aclService.getReleaseBundleAcl(entityKey) : null;

        if (repoAcl == null && buildAcl == null && releaseBundleAcl == null) {
            return null;
        }
        boolean repoExistsAndAllowed = isAclExistsAndAllowed(repoAcl);
        boolean buildExistsAndAllowed = isAclExistsAndAllowed(buildAcl);
        boolean releaseBundleExistsAndAllowed = isAclExistsAndAllowed(releaseBundleAcl);

        if (!repoExistsAndAllowed && !buildExistsAndAllowed && !releaseBundleExistsAndAllowed) {
            // if we got here all sections are either doesn't exist or exists and denied.
            throwForbiddenPermissionErrorMessage(entityKey, getDeniedPermissionTypesMessage(repoAcl != null,
                    buildAcl != null, releaseBundleAcl != null));
        }
        // Removing the acls from response in case no manage permission
        log.debug("Checking for existence of acl entities and removing them from response if needed");
        repoAcl = repoExistsAndAllowed ? repoAcl : null;
        buildAcl = buildExistsAndAllowed ? buildAcl : null;
        releaseBundleAcl = releaseBundleExistsAndAllowed ? releaseBundleAcl : null;

        return toPermissionTargetModel(repoAcl, buildAcl, releaseBundleAcl, outputMode);
    }

    @Override
    public void createPermissionTarget(String entityKey, PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode) {
        if (!authorizationService.isAdmin()) {
            throw new ForbiddenWebAppException("Only an admin user can create permission targets.");
        }
        validateIncomingPermissionTarget(entityKey, permissionTargetModel);

        // In case permission already exists we throw error
        validatePermissionsExistenceForCreate(entityKey, permissionTargetModel);
        // Validate the user input is valid
        validatePermissionTargetSections(permissionTargetModel, inputMode, null);

        Acl repoAcl = createRepoAclModel(entityKey, permissionTargetModel, null, inputMode);
        Acl buildAcl = createBuildAclModel(entityKey, permissionTargetModel, null, inputMode);
        Acl releaseBundleAcl = createReleaseBundleAclModel(entityKey, permissionTargetModel, null, inputMode);
        // Create the ACLs in access
        getTxMe().createAcl(repoAcl, buildAcl, releaseBundleAcl);
    }

    @Override
    public void updatePermissionTarget(String entityKey, PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode) {
        validateIncomingPermissionTarget(entityKey, permissionTargetModel);

        PermissionTargetAcls existingPermission = getExistingPermissionTargetByLicense(aclService, entityKey);
        RepoAcl existingRepoAcl = existingPermission.getRepoAcl();
        BuildAcl existingBuildAcl = existingPermission.getBuildAcl();
        ReleaseBundleAcl existingReleaseBundleAcl = existingPermission.getReleaseBundleAcl();

        //What permission targets came in with the model?
        boolean hasRepoPermissionInRequest = hasRepoPermission(permissionTargetModel);
        boolean hasBuildPermissionInRequest = hasBuildPermission(permissionTargetModel);
        boolean hasReleaseBundlePermissionInRequest = hasReleaseBundlePermission(permissionTargetModel);

        validateUpdateActionPermissions(entityKey, existingRepoAcl, existingBuildAcl, existingReleaseBundleAcl,
                hasRepoPermissionInRequest, hasBuildPermissionInRequest, hasReleaseBundlePermissionInRequest);

        //Validate incoming request for discrepancies in model
        validatePermissionTargetSections(permissionTargetModel, inputMode, existingPermission);

        Acl repoAcl = createRepoAclModel(entityKey, permissionTargetModel, existingRepoAcl, inputMode);
        Acl buildAcl = createBuildAclModel(entityKey, permissionTargetModel, existingBuildAcl, inputMode);
        Acl releaseBundleAcl = createReleaseBundleAclModel(entityKey, permissionTargetModel, existingReleaseBundleAcl,
                inputMode);

        boolean repoAclExists = existingRepoAcl != null;
        boolean buildAclExists = existingBuildAcl != null;
        boolean releaseBundleExists = existingReleaseBundleAcl != null;

        //if acl didn't exist before and exists in request - add
        List<Acl> toAdd = getAclAccordingToExistence(repoAcl, buildAcl, releaseBundleAcl, !repoAclExists,
                !buildAclExists, !releaseBundleExists);
        //if acl existed before and exists also in request - update
        List<Acl> toUpdate = getAclAccordingToExistence(repoAcl, buildAcl, releaseBundleAcl, repoAclExists,
                buildAclExists, releaseBundleExists);
        //if acl existed before and do not exist in request - delete
        List<Acl> toDelete = getAclsToDeleteIfNeeded(entityKey, existingRepoAcl, existingBuildAcl,
                existingReleaseBundleAcl,
                hasRepoPermissionInRequest, hasBuildPermissionInRequest, hasReleaseBundlePermissionInRequest);

        getTxMe().updateAcl(toUpdate, toDelete, toAdd);
    }

    @Override
    public void deletePermissionTarget(String entityKey) {
        PermissionTargetAcls existingPermission = getExistingPermissionTarget(aclService, entityKey);
        RepoAcl repoAcl = existingPermission.getRepoAcl();
        BuildAcl buildAcl = existingPermission.getBuildAcl();
        ReleaseBundleAcl releaseBundleAcl = existingPermission.getReleaseBundleAcl();

        boolean repoAclMissing = repoAcl == null;
        boolean buildAclMissing = buildAcl == null;
        boolean releaseBundleAclMissing = releaseBundleAcl == null;

        if (repoAclMissing && buildAclMissing && releaseBundleAclMissing) {
            throw new NotFoundException("No such permission target '" + entityKey + "'");
        }
        //nulls filtered by underlying call
        getTxMe().deleteAcl(repoAcl, buildAcl, releaseBundleAcl);
    }

    @Override
    public List<String> getBuildsPerPatterns(List<String> includePatterns, List<String> excludePatterns) {
        return getAllBuildsInPermission(includePatterns, excludePatterns);
    }

    @Override
    public List<String> getReleaseBundlesByReposAndPatterns(List<String> repos, List<String> includePatterns,
            List<String> excludePatterns) {
        List<BundleNameAndRepo> bundlesByRepo = filterBundlesByRepos(releaseBundleService.getAllCompletedBundles(), repos);
        return getAllReleaseBundlesInPermission(bundlesByRepo, includePatterns, excludePatterns);
    }

    @Override
    public List<SecurityEntityPermissionTargetModel> getUserPermissionsSecurityEntity(String userName) {
        validateUser(userName);
        return SecurityModelPopulator.getSecurityEntityPermissionTargetModelForRest(
                getPermissionTargetsForUserByResourceType(userName, REPO),
                getPermissionTargetsForUserByResourceType(userName, BUILD),
                getPermissionTargetsForUserByResourceType(userName, RELEASE_BUNDLES));
    }

    @Override
    public List<SecurityEntityPermissionTargetModel> getGroupPermissionsSecurityEntity(String groupName) {
        GroupInfo groupInfo = userGroupService.findGroup(groupName);
        if (groupInfo == null) {
            throw new NotFoundException();
        }
        return SecurityModelPopulator.getSecurityEntityPermissionTargetModelForRest(
                getPermissionTargetsForGroupByResourceType(groupName, REPO),
                getPermissionTargetsForGroupByResourceType(groupName, BUILD),
                getPermissionTargetsForGroupByResourceType(groupName, RELEASE_BUNDLES));
    }

     /*
     * Actions
     */

    /**
     * @throws BadRequestException on error creating an acl.
     */
    public void createAcl(Acl... acls) {
        try {
            Stream.of(acls)
                    .filter(Objects::nonNull)
                    .forEach(acl -> aclService.createAcl(acl));
        } catch (StorageException e) {
            log.warn("Failed to create permission: {}", e.getCause().getMessage());
            log.debug("Failed to create permission: {}", e);
            throw new BadRequestException("Failed to create permission: " + e.getCause().getMessage());
        }
    }

    /**
     * @param toUpdate - acls to update
     * @param toDelete - to maintain atomicity this is also the correct place to delete any acls if a request contained
     *                 null value for a pre-existing acl while it contained another one to update.
     * @param toAdd    - acls to add
     * @throws BadRequestException on error creating an acl.
     */
    public void updateAcl(List<Acl> toUpdate, List<Acl> toDelete, List<Acl> toAdd) throws BadRequestException {
        try {
            toDelete.stream()
                    .filter(Objects::nonNull)
                    .forEach(aclService::deleteAcl);
            toUpdate.stream()
                    .filter(Objects::nonNull)
                    .forEach(aclService::updateAcl);
            toAdd.stream()
                    .filter(Objects::nonNull)
                    .forEach(aclService::createAcl);
        } catch (StorageException e) {
            log.warn("Failed to update permission: {}", e.getCause().getMessage());
            log.debug("Failed to update permission: {}", e);
            throw new BadRequestException("Failed to update permission: " + e.getCause().getMessage());
        }
    }

    /**
     * @throws BadRequestException on error creating an acl.
     */
    public void deleteAcl(Acl... acls) throws BadRequestException {
        try {
            Stream.of(acls)
                    .filter(Objects::nonNull)
                    .forEach(acl -> aclService.deleteAcl(acl));
        } catch (StorageException e) {
            log.warn("Failed to delete permission: {}", e.getCause().getMessage());
            log.debug("Failed to delete permission: {}", e);
            throw new BadRequestException("Failed to delete permission: " + e.getCause().getMessage());
        }
    }

    private Map<String, RepoAcl> getAllReposAcls() {
        return aclService.getAllRepoAcls(ArtifactoryPermission.MANAGE).stream()
                .collect(Collectors.toMap(repoAcl -> repoAcl.getPermissionTarget().getName(), repoAcl -> repoAcl));
    }

    private Map<String, BuildAcl> getAllBuildsAcls() {
        return aclService.getAllBuildAcls(ArtifactoryPermission.MANAGE).stream()
                .collect(Collectors.toMap(buildAcl -> buildAcl.getPermissionTarget().getName(), buildAcl -> buildAcl));
    }

    /**
     * @return all release-bundle acls that user has manage permission on.
     */
    private Map<String, ReleaseBundleAcl> getAllReleaseBundleAcls() {
        return aclService.getAllReleaseBundleAcls(ArtifactoryPermission.MANAGE).stream()
                .collect(Collectors.toMap(releaseBundleAcl -> releaseBundleAcl.getPermissionTarget().getName(),
                        releaseBundleAcl -> releaseBundleAcl));
    }

    private List<PermissionTargetModel> collectRepoBuildAndReleaseBundlePermissions(Map<String, RepoAcl> keyToRepoAcl,
            Map<String, BuildAcl> keyToBuildAcl, Map<String, ReleaseBundleAcl> keyToReleaseBundleAcl,
            PermissionTargetNaming outputMode) {
        return keyToRepoAcl.keySet().stream()
                .map(aclKey -> toPermissionTargetModel(keyToRepoAcl.get(aclKey), keyToBuildAcl.get(aclKey),
                        keyToReleaseBundleAcl.get(aclKey), outputMode))
                .peek(permission -> {
                    keyToBuildAcl.remove(permission.getName());
                    keyToReleaseBundleAcl.remove(permission.getName());
                }).collect(Collectors.toList());
    }

    private void addBuildAndReleaseBundlePermissions(
            List<PermissionTargetModel> repoBuildAndReleaseBundlePermissions, Map<String, BuildAcl> keyToBuildAcl,
            Map<String, ReleaseBundleAcl> keyToReleaseBundleAcl, PermissionTargetNaming outputMode) {
        keyToBuildAcl.keySet().stream()
                .map(aclKey -> toPermissionTargetModel(null, keyToBuildAcl.get(aclKey),
                        keyToReleaseBundleAcl.get(aclKey), outputMode))
                .peek(permission -> keyToReleaseBundleAcl.remove(permission.getName()))
                .forEach(repoBuildAndReleaseBundlePermissions::add);
    }

    private SecurityEntityListItem getPermissionListItem(HttpServletRequest request, PermissionTargetModel permission) {
        String name = permission.getName();
        return new SecurityEntityListItem(name, generatePermissionUri(request, name));
    }

    private static String generatePermissionUri(HttpServletRequest request, String permissionName) {
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        return Joiner.on("/").join(servletContextUrl, RestConstants.PATH_API, PATH_ROOT_V2, PERMISSIONS_ROOT,
                HttpUtils.encodeQuery(permissionName));
    }

    private PermissionTargetModel toPermissionTargetModel(RepoAcl repoAcl, BuildAcl buildAcl,
            ReleaseBundleAcl releaseBundleAcl, PermissionTargetNaming outputMode) {
        if (repoAcl != null) {
            MutableRepoAclImpl toModifyAcl = new MutableRepoAclImpl(repoAcl);
            repoAcl = aclService.convertNewAclCachedRepoKeysToRemote(toModifyAcl);
        }
        return SecurityModelPopulator.getPermissionTargetModelV2(repoAcl, buildAcl, releaseBundleAcl, outputMode);
    }

    //On the update list we add to the list if the acl already exists, on the create list we add only if it does not
    private ArrayList<Acl> getAclAccordingToExistence(Acl repoAcl, Acl buildAcl, Acl releaseBundleAcl,
            boolean shouldAddRepoToList, boolean shouldAddBuildToList, boolean shouldAddReleaseBundleToList) {
        ArrayList<Acl> aclList = Lists.newArrayList();
        if (shouldAddRepoToList && repoAcl != null) {
            aclList.add(repoAcl);
        }
        if (shouldAddBuildToList && buildAcl != null) {
            aclList.add(buildAcl);
        }
        if (shouldAddReleaseBundleToList && releaseBundleAcl != null) {
            aclList.add(releaseBundleAcl);
        }
        return aclList;
    }

    private List<String> getAllBuildsInPermission(List<String> includePatterns, List<String> excludePatterns) {
        return buildService.getBuildNamesInternally().stream()
                .filter(build -> userGroupService.isBuildInPermissions(includePatterns, excludePatterns, build))
                .collect(Collectors.toList());
    }

    private List<String> getAllReleaseBundlesInPermission(List<BundleNameAndRepo> bundlesByRepo,
            List<String> includePatterns, List<String> excludePatterns) {
       return bundlesByRepo.stream().filter(bundle -> userGroupService
                .isReleaseBundleInPermission(includePatterns, excludePatterns, bundle)).map(BundleNameAndRepo::getName)
               .distinct().collect(Collectors.toList());
    }

    private List<BundleNameAndRepo> filterBundlesByRepos(List<BundleNameAndRepo> bundles, List<String> repos) {
        return bundles.stream().filter(bundle -> repos.contains(bundle.getStoringRepo())).collect(Collectors.toList());
    }

    private void setPermissionTargetFields(RepoPermissionTargetImpl newPermissionTarget,
            RepoPermissionTargetModel permissionTargetFromRequest) {
        newPermissionTarget.setRepoKeys(permissionTargetFromRequest.getRepositories());
        newPermissionTarget.setIncludes(permissionTargetFromRequest.getIncludePatterns());
        newPermissionTarget.setExcludes(permissionTargetFromRequest.getExcludePatterns());
    }

    /**
     * Request is treated as delete of old ACLs in case the section is missing in the model and the ACL exists,
     * but treat as actual delete only if the user has manage on it
     *
     * @return the acls to delete if required, they will be passed to the {@link #updateAcl} method to maintain atomicity
     */
    private List<Acl> getAclsToDeleteIfNeeded(String entityKey, RepoAcl existingRepoAcl, BuildAcl existingBuildAcl,
            ReleaseBundleAcl existingReleaseBundleAcl, boolean hasRepoPermissionInRequest,
            boolean hasBuildPermissionInRequest, boolean hasReleaseBundlePermissionInRequest) {
        List<Acl> toDelete = Lists.newArrayList();

        if ((existingRepoAcl != null) && !hasRepoPermissionInRequest &&
                aclService.canManage(existingRepoAcl.getPermissionTarget())) {
            log.debug("Will delete permission {} of type repo as its missing in user request but exists in DB",
                    entityKey);
            toDelete.add(existingRepoAcl);
        }
        if ((existingBuildAcl != null) && !hasBuildPermissionInRequest &&
                aclService.canManage(existingBuildAcl.getPermissionTarget())) {
            log.debug("Will delete permission {} of type build as its missing in user request but exists in DB",
                    entityKey);
            toDelete.add(existingBuildAcl);
        }
        if ((existingReleaseBundleAcl != null) && !hasReleaseBundlePermissionInRequest &&
                aclService.canManage(existingReleaseBundleAcl.getPermissionTarget())) {
            log.debug(
                    "Will delete permission {} of type release bundle as its missing in user request but exists in DB",
                    entityKey);
            toDelete.add(existingReleaseBundleAcl);
        }
        return toDelete;
    }

    private Map<String, SecurityEntityRepoPermissionTargetModel> getPermissionTargetsForGroupByResourceType(
            String groupName, ArtifactoryResourceType type) {
        List<String> groups = Collections.singletonList(groupName);
        return StreamSupportUtils.multimapEntriesStream(aclService.getGroupsPermissions(groups, type))
                .collect(Collectors.toMap(entry -> entry.getKey().getName(),
                        entry -> SecurityModelPopulator.getSecurityEntityRepoPermissionTargetModelV2(entry.getValue(),
                                (RepoPermissionTarget) entry.getKey())));

    }

    private Map<String, SecurityEntityRepoPermissionTargetModel> getPermissionTargetsForUserByResourceType(
            String userName, ArtifactoryResourceType type) {
        return StreamSupportUtils.mapEntriesStream(aclService.getUserPermissionAndItsGroups(userName, type).asMap())
                .collect(Collectors.toMap(entry -> entry.getKey().getName(),
                        entry -> SecurityModelPopulator.getSecurityEntityRepoPermissionTargetModelV2(entry.getValue(),
                                (RepoPermissionTarget) entry.getKey())));
    }

    private InternalRestSecurityRequestHandlerV2 getTxMe() {
        return ContextHelper.get().beanForType(InternalRestSecurityRequestHandlerV2.class);
    }


    /*
     * Model
     */

    /**
     * In case Repositories section in user request is empty --> return null
     */
    @Nullable
    private Acl createRepoAclModel(String entityKey, PermissionTargetModel permissionTargetModel,
            @Nullable RepoAcl previousAcl, PermissionTargetNaming inputMode) {
        if (!hasRepoPermission(permissionTargetModel)) {
            return null;
        }
        validateRepoAclModifications(previousAcl, permissionTargetModel.getRepo().getRepositories());
        String currentUsername = authorizationService.currentUsername();
        // Permission Target
        RepoPermissionTargetImpl repoPermissionTarget = new RepoPermissionTargetImpl(entityKey);
        RepoPermissionTargetModel repositories = permissionTargetModel.getRepo();
        setPermissionTargetFields(repoPermissionTarget, repositories);
        Set<AceInfo> aceInfos = getAcesForAclAndValidateActions(inputMode, repositories.getActions());
        if (previousAcl != null) {
            return InfoFactoryHolder.get()
                    .createRepoAcl(repoPermissionTarget, aceInfos, currentUsername,
                            ClockUtils.epochMillis(), previousAcl.getAccessIdentifier());
        }
        return InfoFactoryHolder.get()
                .createRepoAcl(repoPermissionTarget, aceInfos, currentUsername,
                        ClockUtils.epochMillis());
    }

    /**
     * In case Builds section in user request is empty --> return null
     */
    @Nullable
    private Acl createBuildAclModel(String entityKey, PermissionTargetModel permissionTargetModel,
            @Nullable BuildAcl previousAcl, PermissionTargetNaming inputMode) {
        if (!hasBuildPermission(permissionTargetModel)) {
            return null;
        }
        validateAclPatternsModifications(previousAcl, permissionTargetModel.getBuild());
        String currentUsername = authorizationService.currentUsername();
        // Permission Target
        BuildPermissionTargetImpl buildPermissionTarget = new BuildPermissionTargetImpl(entityKey);
        RepoPermissionTargetModel builds = permissionTargetModel.getBuild();
        setPermissionTargetFields(buildPermissionTarget, builds);
        Set<AceInfo> aceInfos = getAcesForAclAndValidateActions(inputMode, builds.getActions());
        if (previousAcl != null) {
            return InfoFactoryHolder.get()
                    .createBuildAcl(buildPermissionTarget, aceInfos, currentUsername,
                            ClockUtils.epochMillis(), previousAcl.getAccessIdentifier());
        }
        return InfoFactoryHolder.get()
                .createBuildAcl(buildPermissionTarget, aceInfos, currentUsername,
                        ClockUtils.epochMillis());
    }

    private Acl createReleaseBundleAclModel(String entityKey, PermissionTargetModel permissionTargetModel,
            @Nullable ReleaseBundleAcl previousAcl, PermissionTargetNaming inputMode) {
        //if its UI request we shouldn't have release bundle in permission target model
        if (!hasReleaseBundlePermission(permissionTargetModel)) {
            return null;
        }
        validateAclPatternsModifications(previousAcl, permissionTargetModel.getReleaseBundle());
        String currentUsername = authorizationService.currentUsername();
        // Permission Target
        ReleaseBundlePermissionTargetImpl releaseBundlePermissionTarget = new ReleaseBundlePermissionTargetImpl(
                entityKey);
        RepoPermissionTargetModel releaseBundle = permissionTargetModel.getReleaseBundle();
        setPermissionTargetFields(releaseBundlePermissionTarget, releaseBundle);
        Set<AceInfo> aceInfos = getAcesForAclAndValidateActions(inputMode, releaseBundle.getActions());
        if (previousAcl != null) {
            return InfoFactoryHolder.get()
                    .createReleaseBundleAcl(releaseBundlePermissionTarget, aceInfos, currentUsername,
                            ClockUtils.epochMillis(), previousAcl.getAccessIdentifier());
        }
        return InfoFactoryHolder.get()
                .createReleaseBundleAcl(releaseBundlePermissionTarget, aceInfos, currentUsername,
                        ClockUtils.epochMillis());
    }

    /*
     * Assertions and validations
     */

    /**
     * A set of validations on what can the user actually change based on what came in with the response.
     * A second set of validations on the content itself is performed later inside {@link #createRepoAclModel}
     * and {@link #createBuildAclModel}
     */
    private void validateUpdateActionPermissions(String entityKey, RepoAcl existingRepoAcl, BuildAcl existingBuildAcl,
            ReleaseBundleAcl existingReleaseBundleAcl, boolean hasRepoPermissionInRequest,
            boolean hasBuildPermissionInRequest, boolean hasReleaseBundlePermissionInRequest) {
        //What can this user change?
        boolean repoExistsAndDenied = isAclExistsAndDenied(existingRepoAcl);
        boolean buildExistsAndDenied = isAclExistsAndDenied(existingBuildAcl);
        boolean releaseBundleExistsAndDenied = isAclExistsAndDenied(existingReleaseBundleAcl);

        //If any model that the user can't manage came in with the request, fail it.
        if (repoExistsAndDenied && hasRepoPermissionInRequest) {
            throwForbiddenPermissionErrorMessage(entityKey, getDeniedTypeMsg(ArtifactoryResourceType.REPO));
        } else if (buildExistsAndDenied && hasBuildPermissionInRequest) {
            throwForbiddenPermissionErrorMessage(entityKey, getDeniedTypeMsg(ArtifactoryResourceType.BUILD));
        } else if (releaseBundleExistsAndDenied && hasReleaseBundlePermissionInRequest) {
            throwForbiddenPermissionErrorMessage(entityKey, getDeniedTypeMsg(ArtifactoryResourceType.RELEASE_BUNDLES));
        }
        // At this point user can either manage both and both exists, or the one they can't manage isn't present anyway
    }

    /**
     * Try to get Ace set for Acl and validate that the user request is with valid permissions.
     * @return a set of converted Aces into internal naming
     * @throws BadRequestException in case the user request contains invalid permissions
     */
    private Set<AceInfo> getAcesForAclAndValidateActions(PermissionTargetNaming inputMode,
            PrincipalConfiguration principals) {
        try {
            return getAcesForAcl(principals, inputMode);
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        }
    }

    private void diffListAndError(List<String> newElementsList, List<String> oldElementsList, String element) {
        //Create a copy so we don't mess up the model, its still being used.
        List<String> newElements = new ArrayList<>(newElementsList);
        newElements.removeAll(oldElementsList);
        if (isNotEmpty(newElements)) {
            throw new ForbiddenWebAppException("Manager user cannot modify " + element + " in a permission target");
        }
    }

    /**
     * @throws UnsupportedByLicenseException on insufficient license (only for release bundle section)
     * @throws ConflictException             on entity name mismatch
     * @throws BadRequestException           on no entities given
     */
    private void validateIncomingPermissionTarget(String entityKey, PermissionTargetModel permissionTargetModel) {
        String name = permissionTargetModel.getName();
        if (isNotBlank(name) && !entityKey.equals(name)) {
            throw new ConflictException(CONFLICT_ERR_MSG);
        }
        if (!hasEdgeOrEnterprisePlusLic() && hasReleaseBundlePermission(permissionTargetModel)) {
            throw new UnsupportedByLicenseException(ENTERPRISE_PLUS_ERR_MSG);
        }
        if (!hasRepoPermission(permissionTargetModel) && !hasBuildPermission(permissionTargetModel)
                && !hasReleaseBundlePermission(permissionTargetModel)) {
            throw new BadRequestException(MISSING_PARTS_ERR_MSG);
        }
    }

    private void validatePermissionTargetSections(PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode, PermissionTargetAcls existingPermission) {
        validateRepositoriesSection(permissionTargetModel, existingPermission);
        validateBuildsSection(permissionTargetModel, inputMode, existingPermission);
        validateReleaseBundleSection(permissionTargetModel, existingPermission);
    }

    /**
     * If user is not admin they can only touch the patterns and principals
     *
     * @param previousAcl Previous value of the acl being modified, if exists.
     * @param newRepoKeys New permission target's repos about to be sent to the update action.
     */
    private void validateRepoAclModifications(@Nullable RepoAcl previousAcl, List<String> newRepoKeys) {
        //Manage permission already verified at this point
        if (previousAcl != null && !authorizationService.isAdmin()) {
            diffListAndError(aclService.convertCachedRepoKeysToRemote(newRepoKeys),
                    aclService.convertCachedRepoKeysToRemote(previousAcl.getPermissionTarget().getRepoKeys()),
                    "repositories");
        }
    }

    /**
     * Validate that we don't already have the ACLs with same entity key
     *
     * @throws BadRequestException in case of assertion error
     */
    private void validatePermissionsExistenceForCreate(String entityKey, PermissionTargetModel permissionTargetModel) {
        PermissionTargetAcls permissionExist = getExistingPermissionTarget(aclService, entityKey);
        if (permissionExist.getRepoAcl() != null && hasRepoPermission(permissionTargetModel)) {
            generatePermissionExistsError(entityKey, ArtifactoryResourceType.REPO.getName());
        }
        if (permissionExist.getBuildAcl() != null && hasBuildPermission(permissionTargetModel)) {
            generatePermissionExistsError(entityKey, ArtifactoryResourceType.BUILD.getName());
        }
        if (permissionExist.getReleaseBundleAcl() != null && hasReleaseBundlePermission(permissionTargetModel)) {
            generatePermissionExistsError(entityKey, ArtifactoryResourceType.RELEASE_BUNDLES.getName());
        }
    }

    /**
     * Requires at least one include or exclude release-bundle pattern.
     * Asserting repositories section for Edge and EntPlus lic and that principals exist.
     *
     * @throws BadRequestException in case of assertion errors
     */
    private void validateReleaseBundleSection(PermissionTargetModel permissionTargetModel,
            PermissionTargetAcls existingPermission) throws BadRequestException {
        RepoPermissionTargetModel releaseBundle = permissionTargetModel.getReleaseBundle();
        if (releaseBundle == null) {
            log.debug("No release bundle permission content in request for Acl {} of type {}",
                    permissionTargetModel.getName(), RELEASE_BUNDLES.getName());
            return;
        }
        // Principals validation
        checkForNonExistingPrinciples(userGroupService, releaseBundle.getActions(), existingPermission);
        // Asserting at least one include/exclude pattern exists
        validatePatterns(releaseBundle, ArtifactoryResourceType.RELEASE_BUNDLES);
        assertReleaseBundleModelRequiredEntities(releaseBundle);
    }

    /**
     * Requires at least one include or exclude build pattern.
     * Asserting build repo key is the only one exists, principals exist.
     *
     * @throws BadRequestException in case of assertion errors
     */
    private void validateBuildsSection(PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode, PermissionTargetAcls existingPermission) throws BadRequestException {
        RepoPermissionTargetModel builds = permissionTargetModel.getBuild();
        if (builds == null) {
            log.debug("No build permissions content in request for Acl {} of type {}", permissionTargetModel.getName(),
                    BUILD.getName());
            return;
        }
        // Asserting common part between repo and build
        validateRepoPermissionModel(builds, existingPermission);
        validatePatterns(builds, ArtifactoryResourceType.BUILD);
        // Repo key validation
        assertBuildModelRequiredEntities(permissionTargetModel, inputMode);
    }

    /**
     * Requires repositories list and valid principals
     *
     * @throws BadRequestException in case of assertion error
     */
    private void validateRepositoriesSection(PermissionTargetModel permissionTargetModel,
            PermissionTargetAcls existingPermission)
            throws BadRequestException {
        RepoPermissionTargetModel repositories = permissionTargetModel.getRepo();
        if (repositories == null) {
            log.debug("No repo permissions content in request for Acl {} of type {}", permissionTargetModel.getName(),
                    REPO.getName());
            return;
        }
        validateRepoPermissionModel(repositories, existingPermission);
        validateNoForbiddenReposAreIncluded(repositories.getRepositories());
    }

    private void validateRepoPermissionModel(RepoPermissionTargetModel repositories,
            PermissionTargetAcls existingPermission) {
        if (repositories.getRepositories() == null) {
            throw new BadRequestException("Permission target request missing repositories.");
        }
        // RepoKeys validation
        String missingRepo = getFirstNonExistingRepoFromList(repositories.getRepositories(), configService, aclService);
        if (isNotBlank(missingRepo)) {
            throw new BadRequestException(
                    "Permission target contains a reference to a non-existing repository '" + missingRepo + "'.");
        }
        // Principals validation
        checkForNonExistingPrinciples(userGroupService, repositories.getActions(), existingPermission);
    }

    private void validateNoForbiddenReposAreIncluded(List<String> includedRepos) {
        if (includedRepos.contains(buildService.getBuildInfoRepoKey())) {
            throw new BadRequestException(String.format(REPO_NOT_ALLOWED_ERR_MSG, "Build Info"));
        }
        boolean isReleaseBundleRepoIncluded = repositoryService.getReleaseBundlesRepoDescriptors()
                .stream()
                .map(RepoBaseDescriptor::getKey)
                .anyMatch(includedRepos::contains);
        if (isReleaseBundleRepoIncluded) {
            throw new BadRequestException(String.format(REPO_NOT_ALLOWED_ERR_MSG, "Release Bundle"));
        }
    }

    /**
     * In build and release bundle permission targets - if the user is not admin he can only touch principals
     *
     * @param previousAcl   Previous value of the acl being modified, if exists.
     * @param newPermission New incoming permission target to validate
     */
    private void validateAclPatternsModifications(@Nullable Acl<? extends RepoPermissionTarget> previousAcl,
            RepoPermissionTargetModel newPermission) {
        if (previousAcl != null && !authorizationService.isAdmin()) {
            RepoPermissionTarget previousPermission = previousAcl.getPermissionTarget();
            diffListAndError(newPermission.getIncludePatterns(), previousPermission.getIncludes(), "include patterns");
            diffListAndError(newPermission.getExcludePatterns(), previousPermission.getExcludes(), "exclude patterns");
        }
    }

    /**
     * Requires that {@link InternalBuildService#getBuildInfoRepoKey()} is the only repo key in the repositories list.
     * In case request came from UI we add the repo ourselves. Otherwise throw exception.
     *
     * @throws BadRequestException on assertion error.
     */
    private void assertBuildModelRequiredEntities(PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode) throws BadRequestException {
        RepoPermissionTargetModel builds = permissionTargetModel.getBuild();
        String buildRepoKey = buildService.getBuildInfoRepoKey();
        if (isNotTheOnlyRepoExists(builds, buildRepoKey)) {
            if (NAMING_UI.equals(inputMode)) {
                builds.setRepositories(Collections.singletonList(buildRepoKey));
            } else {
                throw new BadRequestException(
                        "Build permission target repositories list must contain " + buildRepoKey +
                                " as the only repository key");
            }
        }
    }

    /**
     * Asserting repositories section for Edge and EntPlus licenses.
     *
     * @throws BadRequestException on assertion error.
     */
    private void assertReleaseBundleModelRequiredEntities(RepoPermissionTargetModel releaseBundle)
            throws BadRequestException {
        if (CollectionUtils.isNullOrEmpty(releaseBundle.getRepositories())) {
            releaseBundle.setRepositories(Collections.singletonList(RELEASE_BUNDLE_DEFAULT_REPO));
        } else if (hasEdgeLic() && isNotTheOnlyRepoExists(releaseBundle, RELEASE_BUNDLE_DEFAULT_REPO)) {
            throw new BadRequestException(
                    "Release bundle permission target should contain 'release-bundles' as the only repository.");
        }
    }

    private boolean isNotTheOnlyRepoExists(RepoPermissionTargetModel permissionModel, String repoKey) {
        List<String> releaseBundleRepoList = Collections.singletonList(repoKey);
        return !releaseBundleRepoList.equals(permissionModel.getRepositories());
    }

    private void validatePatterns(RepoPermissionTargetModel permissionModel, ArtifactoryResourceType type) {
        if (isNullOrEmpty(permissionModel.getIncludePatterns()) &&
                isNullOrEmpty(permissionModel.getExcludePatterns())) {
            throw new BadRequestException(
                    String.format("Permission target %s must contain at least one include or exclude pattern",
                            getDeniedTypeMsg(type)));
        }
    }

    /**
     * Throws when user doesn't have manage on both permission targets, for actions that require manage on at least one
     * of them.
     *
     * @throws ForbiddenWebAppException if permission was denied for {@param entityKey}
     * @throws NotFoundException        if permission was denied for {@param entityKey} and Artifactory is set to hide existence
     */

    private boolean isAclExistsAndDenied(Acl<? extends PermissionTarget> acl) {
        return acl != null && !aclService.canManage(acl.getPermissionTarget());
    }

    private boolean isAclExistsAndAllowed(Acl<? extends PermissionTarget> acl) {
        return acl != null && aclService.canManage(acl.getPermissionTarget());
    }

    private boolean hasRepoPermission(PermissionTargetModel model) {
        return model != null && model.getRepo() != null;
    }

    private boolean hasBuildPermission(PermissionTargetModel model) {
        return model != null && model.getBuild() != null;
    }

    private boolean hasReleaseBundlePermission(PermissionTargetModel model) {
        return model != null && model.getReleaseBundle() != null;
    }

    private void validateUser(String userName) {
        try {
            UserInfo user = userGroupService.findUser(userName);
            if (addonsManager.addonByType(CoreAddons.class).isAolAdmin(user)) {
                throw new NotFoundException();
            }
        } catch (UsernameNotFoundException e) {
            throw new NotFoundException();
        }
    }

    /**
     * All release-bundle section in the permission target should be visible/enabled only if user has Edge or
     * Enterprise plus license.
     */
    private boolean hasEdgeOrEnterprisePlusLic() {
        return addonsManager.isClusterEnterprisePlus() || hasEdgeLic();
    }

    private boolean hasEdgeLic() {
        return addonsManager.isEdgeLicensed() && !addonsManager.isEdgeMixedInCluster();
    }

    /*
     * Errors
     */

    private void generatePermissionExistsError(String entityKey, String type) {
        throw new ConflictException(String.format(ALREADY_EXISTS_ERR_MSG, entityKey, type));
    }

    /**
     * @throws ForbiddenWebAppException if permission was denied for {@param entityKey}
     * @throws NotFoundException        if permission was denied for {@param entityKey} and Artifactory is set
     *                                  to hide existence of unauthorized resources.
     */
    private void throwForbiddenPermissionErrorMessage(String entityKey, String deniedType) {
        if (configService.getDescriptor().getSecurity().isHideUnauthorizedResources()) {
            log.debug("User tried to access permission {} but it is forbidden and " +
                    "isHideUnauthorizedResources is true. Returning 404 instead of 403.", entityKey, deniedType);
            throw new NotFoundException("");
        }
        throw new ForbiddenWebAppException(
                "Accessing permission target '" + entityKey + "' " + deniedType + " is forbidden.");
    }

    private String getDeniedPermissionTypesMessage(boolean repoExistsAndDenied, boolean buildExistsAndDenied,
            boolean releaseBundleExistsAndDenied) {
        String repoType = repoExistsAndDenied ? ArtifactoryResourceType.REPO.getName() + ", " : StringUtils.EMPTY;
        String buildType = buildExistsAndDenied ? ArtifactoryResourceType.BUILD.getName() + ", " : StringUtils.EMPTY;
        String releaseBundleType =
                releaseBundleExistsAndDenied ? ArtifactoryResourceType.RELEASE_BUNDLES.getName() : StringUtils.EMPTY;
        String deniedMessage = "of types: " + repoType + buildType + releaseBundleType;
        return stripEnd(deniedMessage, ", ");
    }

    private String getDeniedTypeMsg(ArtifactoryResourceType resourceType) {
        return "of type: " + resourceType.getName();
    }
}
