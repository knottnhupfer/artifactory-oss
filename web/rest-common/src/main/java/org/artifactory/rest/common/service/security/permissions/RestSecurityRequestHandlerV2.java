package org.artifactory.rest.common.service.security.permissions;

import org.artifactory.api.rest.common.model.continues.ContinueResult;
import org.artifactory.api.security.SearchStringPermissionFilter;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ConflictException;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.rest.exception.NotFoundException;
import org.artifactory.security.PermissionTargetNaming;
import org.artifactory.security.SecurityEntityListItem;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.security.permissions.SecurityEntityPermissionTargetModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public interface RestSecurityRequestHandlerV2 {

    /**
     * @return true if a permission target (build, repo, release bundle or any combination of the three of them)
     * with a name {@param entityKey} exists.
     * @throws ForbiddenWebAppException if the user doesn't have manage permission to any of the sections
     * (build, repo, release-bundle).
     * @throws NotFoundException if the permission target doesn't exist (no build/repo/release-bundle exists)
     * OR if only release-bundle permissions exist but user doesn't have EntPlus/Edge lic.
     */
    boolean isPermissionTargetExists(String entityKey) throws ForbiddenWebAppException, NotFoundException;

    /**
     * @param outputMode Governs the way actions strings are written
     * @return all permission targets for the current logged-in user for the type of permission given.
     */
    List<PermissionTargetModel> getAllPermissionTargets(PermissionTargetNaming outputMode);

    /**
     * @param outputMode Governs the way actions strings are written
     * @return all permission targets for the current logged-in user for the type of permission given.
     */
    ContinueResult<PermissionTargetModel> getPagingPermissionTargets(PermissionTargetNaming outputMode, SearchStringPermissionFilter searchStringPermissionFilter);

    /**
     * @param request to be used to get the context url of the request
     * @return all permission targets for the current logged-in user for the type of permission given,
     * in a lightweight type of list. (name + url)
     */
    Set<SecurityEntityListItem> getAllPermissionTargets(HttpServletRequest request);

    /**
     * @param outputMode Governs the way actions strings are written
     * @return a {@link PermissionTargetModel} representing the requested {@param entityKey},
     * null if no permission targets (build or repo) were found.
     * @throws ForbiddenWebAppException if user doesn't have manage permission for any of the permission target sections
     * (build, repo and release-bundle)
     * @throws NotFoundException if user doesn't have manage permission for any of the permission target sections
     * (build, repo and release-bundle) BUT 'hideUnauthorizedResources' flag is on.
     */
    PermissionTargetModel getPermissionTarget(String entityKey, PermissionTargetNaming outputMode)
            throws ForbiddenWebAppException, NotFoundException;

    /**
     * Creates permission targets.
     *
     * @param inputMode Governs the way actions strings are read
     * @throws BadRequestException On any validation error.
     * @throws ConflictException   On mismatch in permission target names.
     */
    void createPermissionTarget(String entityKey, PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode) throws ConflictException, BadRequestException;

    /**
     * Updates permission targets.
     *
     * @param inputMode Governs the way actions strings are read
     * @throws NotFoundException   If the permission sent is missing (or it is hidden due to security config).
     * @throws ForbiddenWebAppException  If user has no manage (or admin) permission on requested permission
     * @throws BadRequestException On any validation error.
     */
    void updatePermissionTarget(String entityKey, PermissionTargetModel permissionTargetModel,
            PermissionTargetNaming inputMode) throws ForbiddenWebAppException, NotFoundException, BadRequestException;

    /**
     * Deletes the permission target identified by {@param entityKey}
     *
     * @throws NotFoundException   If no permission with the name given in {@param entityKey} exists.
     * @throws BadRequestException On any validation error.
     */
    void deletePermissionTarget(String entityKey) throws NotFoundException, BadRequestException;

    /**
     * Provides a list of builds that match the patterns provided.
     * Empty list if no builds were found to match.
     */
    List<String> getBuildsPerPatterns(List<String> includePatterns, List<String> excludePatterns) throws NotFoundException;

    /**
     * Provides list of release-bundles under the given repositories that match the given patterns.
     * @return list of matched release-bundles names or empty list if no matched release-bundles were found.
     */
    List<String> getReleaseBundlesByReposAndPatterns(List<String> repos, List<String> includePatterns, List<String> excludePatterns);

    /**
     * Retrieve A User Permission targets.
     * @param entityKey The name of the User
     */
    List<SecurityEntityPermissionTargetModel> getUserPermissionsSecurityEntity(String entityKey);

    /**
     * Retrieve A Group Permission targets.
     * @param entityKey The name of the Group
     */
    List<SecurityEntityPermissionTargetModel> getGroupPermissionsSecurityEntity(String entityKey);
}
