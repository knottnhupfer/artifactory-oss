package org.artifactory.ui.rest.service.admin.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.security.permissions.RestSecurityRequestHandlerV2;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.admin.security.permissions.build.BuildPermissionPatternsUIModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Used in specific Permission page to retrieve the list of builds that match the include/exclude patterns
 *
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildPermissionsTargetBuildsService implements RestService<BuildPermissionPatternsUIModel> {

    @Autowired
    private RestSecurityRequestHandlerV2 securityRequestHandler;
    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest<BuildPermissionPatternsUIModel> request, RestResponse response) {
        if (!authorizationService.hasBuildPermission(ArtifactoryPermission.MANAGE)) {
            throw new ForbiddenException("Only admin or users with manage permissions can access this endpoint");
        }
        BuildPermissionPatternsUIModel patternsUIModel = request.getImodel();
        getBuildsPerPermission(patternsUIModel, response);
    }

    /**
     * Populates the model returned in {@param response} with the list of builds per patterns
     */
    private void getBuildsPerPermission(BuildPermissionPatternsUIModel model, RestResponse response) {
        List<String> buildsPerPatterns = securityRequestHandler
                .getBuildsPerPatterns(model.getIncludePatterns(), model.getExcludePatterns());
        BuildsInPermission buildsInPermission = new BuildsInPermission(buildsPerPatterns);
        response.iModel(buildsInPermission);
    }

    @Getter
    @AllArgsConstructor
    private class BuildsInPermission extends BaseModel {
        private List<String> builds;
    }
}
