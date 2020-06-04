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

package org.artifactory.rest.filter;


import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.exception.AuthorizationRestException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;

/**
 * Block Rest request during offline state
 *
 * @author  gidis
 */
public class OfflineRestFilter implements ContainerRequestFilter {

    @Context
    HttpServletResponse response;

    @Override
    public void filter(ContainerRequestContext containerRequest) {
        // Filter out all events in case of offline mode
        if (ContextHelper.get().isOffline()) {
            throw new AuthorizationRestException();
        }
    }
}
