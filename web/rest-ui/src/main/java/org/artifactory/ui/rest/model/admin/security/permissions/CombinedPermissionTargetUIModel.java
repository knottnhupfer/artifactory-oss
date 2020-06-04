package org.artifactory.ui.rest.model.admin.security.permissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.permissions.PermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.build.BuildPermissionTargetUIModel;
import org.artifactory.ui.rest.model.admin.security.permissions.repo.RepoPermissionTargetUIModel;

import javax.annotation.Nonnull;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Holds both repo and build permission targets of the same name for ease of view in the UI.
 *
 * @author Dan Feldman
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CombinedPermissionTargetUIModel extends BaseModel {

    private String name;
    private RepoPermissionTargetUIModel repoPermission;
    private BuildPermissionTargetUIModel buildPermission;
    private Boolean buildGlobalBasicReadAllowed;

    public CombinedPermissionTargetUIModel(@Nonnull PermissionTargetModel permissionTarget,
            boolean buildGlobalBasicReadAllowed) {
        this.name = permissionTarget.getName();
        this.repoPermission = createRepoPermission(permissionTarget);
        this.buildPermission = createBuildPermission(permissionTarget);
        this.buildGlobalBasicReadAllowed = buildGlobalBasicReadAllowed ? true : null;
    }

    private RepoPermissionTargetUIModel createRepoPermission(@Nonnull PermissionTargetModel permissionTarget) {
        if (permissionTarget.getRepo() == null) {
             return null;
        } else {
            return new RepoPermissionTargetUIModel(permissionTarget.getRepo());
        }
    }

    private BuildPermissionTargetUIModel createBuildPermission(@Nonnull PermissionTargetModel permissionTarget) {
        if (permissionTarget.getBuild() == null) {
            return null;
        } else {
            return new BuildPermissionTargetUIModel(permissionTarget.getBuild());
        }
    }
}
