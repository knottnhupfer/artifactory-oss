package org.artifactory.ui.rest.model.admin.security.permissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.security.permissions.TypedPermissionTargetModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * A single permission target (either build or repo)
 *
 * @author Dan Feldman
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class PermissionTargetUIModel extends BaseModel {

    private List<PrincipalPermissionActions> userPermissionActions = Lists.newArrayList();
    private List<PrincipalPermissionActions> groupPermissionActions = Lists.newArrayList();

    public PermissionTargetUIModel(@Nullable TypedPermissionTargetModel permissionTarget) {
        if (permissionTarget != null && permissionTarget.getActions() != null) {
            this.userPermissionActions = populatePermissionActions(permissionTarget.getActions().getUsers());
            this.groupPermissionActions = populatePermissionActions(permissionTarget.getActions().getGroups());
        }
    }

    private List<PrincipalPermissionActions> populatePermissionActions(Map<String, Set<String>> actionsPerPrincipal) {
        if (actionsPerPrincipal != null) {
            return actionsPerPrincipal.entrySet()
                    .stream()
                    .map(userActions -> new PrincipalPermissionActions(userActions.getKey(), userActions.getValue()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
