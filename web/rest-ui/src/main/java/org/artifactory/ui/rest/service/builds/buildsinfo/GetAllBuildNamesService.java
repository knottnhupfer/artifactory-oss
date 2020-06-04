package org.artifactory.ui.rest.service.builds.buildsinfo;

import org.artifactory.api.build.BuildService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllBuildNamesService<T> implements RestService<T> {
    private static final Logger log = LoggerFactory.getLogger(GetAllBuildNamesService.class);

    @Autowired
    private BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        try {
            response.iModel(buildService.getBuildNames());
        } catch (Exception e) {
            log.debug("", e);
            response.error("Failed to retrieve all build names: " + e.getMessage());
        }
    }
}
