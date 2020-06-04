package org.artifactory.ui.rest.service.builds.buildsinfo;

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.build.BuildService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Rotem Kfir
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildInfoRepositoryService implements RestService  {

    @Autowired
    private BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        String buildInfoRepo = buildService.getBuildInfoRepoKey();
        response.iModel(ImmutableMap.of("key", buildInfoRepo));
    }
}
