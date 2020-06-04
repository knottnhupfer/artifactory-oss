package org.artifactory.rest.resource.repositories;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.security.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.artifactory.api.rest.constant.RepositoriesRestConstants.*;

/**
 * A resource to return specific type repository configuration.
 *
 * @author Inbar Tal
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(PATH_ROOT_V2)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class RepositoriesResourceV2 {

    @Context
    private HttpHeaders requestHeaders;

    @Autowired
    private AddonsManager addonsManager;

    @GET
    @Path("{repoKey: .+}")
    @Produces({MT_LOCAL_REPOSITORY_CONFIGURATION,
            MT_REMOTE_REPOSITORY_CONFIG,
            MT_VIRTUAL_REPOSITORY_CONFIGURATION, MediaType.APPLICATION_JSON})
    public Response getRepoConfig(@PathParam("repoKey") String repoKey) {
        MediaType mediaType = requestHeaders.getMediaType();
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return restAddon.getRepositoryConfigurationV2(repoKey, mediaType);
    }
}