package org.artifactory.ui.rest.service.admin.security.permissions;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.release.bundle.ReleaseBundleAddon;
import org.artifactory.api.jackson.JacksonWriter;
import org.artifactory.api.rest.distribution.bundle.models.BundlesNamesResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.exception.BadRequestException;
import org.artifactory.rest.exception.ForbiddenException;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.ui.rest.model.distribution.ReleaseBundlePermissionIModel;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Used in specific Permission page to retrieve the list of release-bundles under given repositories that match the
 * include/exclude patterns
 *
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetReleaseBundlesByReposAndPatternsService implements RestService<ReleaseBundlePermissionIModel> {
    private static final Logger log = LoggerFactory.getLogger(GetReleaseBundlesByReposAndPatternsService.class);

    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest<ReleaseBundlePermissionIModel> request, RestResponse response) {
        if (!authorizationService.hasReleaseBundlePermission(ArtifactoryPermission.MANAGE)) {
            throw new ForbiddenException("Only admin or users with manage permissions can access this endpoint");
        }
        ReleaseBundlePermissionIModel model = request.getImodel();
        validateModel(model);
        ReleaseBundleAddon releaseBundleAddon = addonsManager.addonByType(ReleaseBundleAddon.class);
        BundlesNamesResponse bundlesByReposAndPatterns = releaseBundleAddon
                .getBundlesByReposAndPatterns(model.getRepositories(), model.getIncludePatterns(),
                        model.getExcludePatterns());
        try {
            response.iModel(JacksonWriter.serialize(bundlesByReposAndPatterns));
        } catch (IOException e) {
            log.error("Failed to serialize response ", e);
            throw new IllegalArgumentException(e);
        }
    }

    private void validateModel(ReleaseBundlePermissionIModel model) {
        if (CollectionUtils.isNullOrEmpty(model.getRepositories())) {
            throw new BadRequestException("Repositories cannot be empty");
        }
    }
}
