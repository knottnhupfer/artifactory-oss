package org.artifactory.rest.servlet;

import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.common.RestResponseFilter;
import org.artifactory.rest.common.access.AccessTokenScopeResourceFilter;
import org.artifactory.rest.filter.*;
import org.artifactory.rest.jersey.RemoveOptionsBinder;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author haims
 */

public class JerseyApplication extends ResourceConfig {

    /**
     * Register JAX-RS application components.
     */

    private static final Logger log = LoggerFactory.getLogger(JerseyApplication.class);

    public JerseyApplication() {
        //RestAuthenticationFilter registered with @Provider. Couldn't make it run before RolesAllowedDynamicFeature otherwise
        register(RestResponseFilter.class);
        register(AccessTokenScopeResourceFilter.class);
        register(OfflineRestFilter.class);
        register(LicenseRestFilter.class);
        register(HaRestFilter.class);
        //register(JerseyReponseHeadProtectionFilter.class);
        //register(ConversionFilterDynamicFeature.class);
        packages("org.artifactory");
        property(CommonProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, -1);
        property(ServerProperties.RESOURCE_VALIDATION_DISABLE,
                Boolean.valueOf(System.getProperty(ServerProperties.RESOURCE_VALIDATION_DISABLE)));
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
        register(RolesAllowedDynamicFeature.class);
        if (disableOptionsMethod()) {
            log.debug("disabling jersey options support");
            register(new RemoveOptionsBinder());
        }
    }

    private boolean disableOptionsMethod() {
        String property = System.getProperty("artifactory.disable.jersey.options");
        if (StringUtils.isBlank(property)) {
            return false;
        }
        return Boolean.parseBoolean(property);
    }
}
