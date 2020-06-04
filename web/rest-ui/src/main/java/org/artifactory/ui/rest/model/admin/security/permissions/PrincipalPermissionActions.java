package org.artifactory.ui.rest.model.admin.security.permissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.ui.rest.model.admin.security.permissions.repo.RepoPermissionTargetUIModel;

import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Represents the permissions of a single principal under a {@link RepoPermissionTargetUIModel}
 *
 * @author Dan Feldman
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrincipalPermissionActions extends BaseModel {

    private String principal;
    private Set<String> actions = Sets.newHashSet();

    PrincipalPermissionActions(String principal, Set<String> actions) {
        this.principal = principal;
        this.actions = actions;
    }
}
