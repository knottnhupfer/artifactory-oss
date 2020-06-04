package org.artifactory.ui.rest.model.admin.security.permissions.build;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.security.permissions.RepoPermissionTargetModel;
import org.artifactory.ui.rest.model.admin.security.permissions.repo.RepoPermissionTargetUIModel;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Yuval Reches
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildPermissionTargetUIModel extends RepoPermissionTargetUIModel {

    // List of builds that match the current include and exclude patterns
    private List<String> builds = Lists.newArrayList();

    public BuildPermissionTargetUIModel(RepoPermissionTargetModel permissionTarget) {
        super(permissionTarget);
        // No need to pass the list from parent class on cases build permission is missing
        this.builds = ContextHelper.get().beanForType(RestSecurityRequestHandlerV2.class)
                .getBuildsPerPatterns(getIncludePatterns(), getExcludePatterns());
    }

}
