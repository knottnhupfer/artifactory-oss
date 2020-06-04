package org.artifactory.ui.rest.resource.jcr;

import org.artifactory.addon.subscription.SubscriptionService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.rest.subscription.Subscription;
import org.artifactory.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;

/**
 * @author dudim
 */
@Path("jcr/subscription")
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JcrSubscriptionResource {

    private SubscriptionService subscriptionService;

    @Autowired
    public JcrSubscriptionResource(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PUT
    @Path("setSubscription")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setSubscription(Subscription subscription) {
        subscriptionService.setSubscription(subscription);
        return Response.ok().build();
    }

    @GET
    @Path("getSubscription")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscription() {
        Subscription subscription = subscriptionService.getSubscription();
        if (subscription != null && CollectionUtils.notNullOrEmpty(subscription.getEmails())) {
            return Response.ok(subscription).build();
        }
        Subscription emptySubscription = new Subscription();
        emptySubscription.setEmails(new HashSet<>());
        return Response.ok(emptySubscription).build();
    }
}
