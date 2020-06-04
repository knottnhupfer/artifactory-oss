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

package org.artifactory.rest.common.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Represents a REST API client error request which will get responded by a 401 HTTP status code.
 *
 * @author Dan Feldman
 */
public class UnauthorizedException extends WebApplicationException {

    public UnauthorizedException() {
        super(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    public UnauthorizedException(String message) {
        super(Response.status(new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return Response.Status.UNAUTHORIZED.getStatusCode();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.UNAUTHORIZED.getFamily();
            }

            @Override
            public String getReasonPhrase() {
                return message;
            }
        }).build());
    }
}
