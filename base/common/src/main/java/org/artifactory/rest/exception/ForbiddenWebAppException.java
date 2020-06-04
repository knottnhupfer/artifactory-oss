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

package org.artifactory.rest.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

/**
 * Represents a REST API client error request which will get responded by a 403 HTTP status code.
 *
 * @author Lior Azar
 * @see ForbiddenWebAppException
 */

public class ForbiddenWebAppException extends WebApplicationException {

    public ForbiddenWebAppException() {
        super(Response.status(Response.Status.FORBIDDEN).build());
    }

    public ForbiddenWebAppException(String message) {
        super(Response.status(new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return Response.Status.FORBIDDEN.getStatusCode();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.FORBIDDEN.getFamily();
            }

            @Override
            public String getReasonPhrase() {
                return message;
            }
        }).build());
    }
}
