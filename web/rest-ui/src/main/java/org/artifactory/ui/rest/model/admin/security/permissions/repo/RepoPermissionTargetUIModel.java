package org.artifactory.ui.rest.model.admin.security.permissions.repo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.PermissionTargetUIModel;

import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Dan Feldman
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoPermissionTargetUIModel extends PermissionTargetUIModel {

    //Holds selected repos of this target or the ANY_ variants (can be one or more)
    private List<String> repoKeys = Lists.newArrayList();
    private List<String> includePatterns = Lists.newArrayList();
    private List<String> excludePatterns = Lists.newArrayList();

    public RepoPermissionTargetUIModel(RepoPermissionTargetModel permissionTarget) {
        super(permissionTarget);
        this.repoKeys = Optional.ofNullable(permissionTarget.getRepositories()).orElse(Lists.newArrayList());
        this.includePatterns = Optional.ofNullable(permissionTarget.getIncludePatterns()).orElse(Lists.newArrayList());
        this.excludePatterns = Optional.ofNullable(permissionTarget.getExcludePatterns()).orElse(Lists.newArrayList());

    }
}
