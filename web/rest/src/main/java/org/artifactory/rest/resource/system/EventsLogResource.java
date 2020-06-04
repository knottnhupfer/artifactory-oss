package org.artifactory.rest.resource.system;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.event.EventsLogCleanUpService;
import org.artifactory.rest.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Path("events/log")
public class EventsLogResource {

    @Autowired
    EventsLogCleanUpService eventsLogCleanUpService;

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("cleanup")
    public Response startEventsCleanup() {
        eventsLogCleanUpService.cleanup();
        ResponseModel responseModel = new ResponseModel(200, "Started events log cleanup.");
        return Response.ok(responseModel).build();
    }
}
