package org.artifactory.ui.rest.service.admin.security.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.ArtifactoryPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Used in specific Permission page to retrieve security descriptor flag {@link SecurityDescriptor#isBuildGlobalBasicReadAllowed()}
 *
 * @author Yuval Reches
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildGlobalBasicReadAllowedService implements RestService {

    @Autowired
    private CentralConfigService centralConfigService;
    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        //Show the warning about global basic read to any user that's capable to edit a permission target
        boolean flagValue = authorizationService.hasBuildPermission(ArtifactoryPermission.MANAGE) &&
                centralConfigService.getDescriptor().getSecurity().isBuildGlobalBasicReadAllowed();
        response.iModel(new BuildGlobalBasicReadAllowed(flagValue));
    }

    @Getter
    @AllArgsConstructor
    private class BuildGlobalBasicReadAllowed extends BaseModel {
        private boolean buildGlobalBasicReadAllowed;
    }
}
