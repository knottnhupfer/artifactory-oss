package org.artifactory.rest.filter;

import org.artifactory.rest.common.BlockOnConversion;
import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * @author orid
 */

@Component
@Provider
public class ConversionFilterDynamicFeature implements DynamicFeature {


    @Autowired
    private ConversionFilter conversionFilter;

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext configuration) {

        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        if (am.isAnnotationPresent(BlockOnConversion.class)) {
            configuration.register(conversionFilter);
        }

    }
}
