/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.rest.response;


import org.apache.commons.io.output.NullOutputStream;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

/**
 * @author saffih
 * <p>
 * make sure Head to GET rest endpoint would not leak.
 */
public class JerseyReponseHeadProtectionFilter implements ContainerResponseFilter {
    private static final Logger log = LoggerFactory.getLogger(JerseyReponseHeadProtectionFilter.class);
    // bugfix RTFACT-15774  // RTFACT-15893
    private static void consumeHead(Object entity) {
        if ((entity != null) && entity instanceof StreamingOutput) {
            try {
                ((StreamingOutput) entity).write(new NullOutputStream());
            } catch (IOException e) { // should be done properly
                if (log.isTraceEnabled()) {
                    log.trace("Consume head ", e);
                }
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!request.getMethod().equalsIgnoreCase("head")) {
            return;
        }

        if (response.getEntity() == null) {
            return;
        }

        if (!(response.getEntity() instanceof StreamingOutput)) {
            return;
        }

        if (log.isDebugEnabled()){
            log.debug("Consume head: {} ", ((ContainerRequest)request).getAbsolutePath());
        }

        consumeHead(response.getEntity());
        return;
    }
}

