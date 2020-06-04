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

package org.artifactory.rest.common.util;

import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.StreamRestResponse;

import javax.ws.rs.core.Response;

/**
 * @author chen keinan
 */
public class ResponseHandler {

    /**
     * @return response code and data for Post Request
     */
    public static Response buildJerseyResponse(RestResponse response) {
        Response.ResponseBuilder responseBuilder = getResponseBuilder();
        Object entity = response.getEntity();
        if (entity != null) {
            return responseBuilder.status(response.getResponseCode()).entity(
                    entity).build();
        } else {
            return responseBuilder.status(response.getResponseCode()).build();
        }
    }

    /**
     * @return response code and data for Post Request
     */
    public static Response buildFileResponse(RestResponse response, boolean downloadFile) {
        Response.ResponseBuilder responseBuilder;
        if (downloadFile) {
            responseBuilder = getFileDownloadResponseBuilder(((StreamRestResponse) response).getFileName());
        } else {
            responseBuilder = getResponseBuilder();
        }
        return responseBuilder.status(response.getResponseCode()).entity(response.getEntity()).build();
    }

    /**
     * create Response with support for cross site
     *
     * @return rest Response
     */
    private static Response.ResponseBuilder getResponseBuilder() {
        Response.ResponseBuilder responseBuilder = Response.ok();
        updateCorsHeaders(responseBuilder);
        return responseBuilder;
    }

    /**
     * create Response with support for cross site
     *
     * @return rest Response
     */
    private static Response.ResponseBuilder getFileDownloadResponseBuilder(String fileName) {
        Response.ResponseBuilder responseBuilder = Response.ok();
        updateCorsHeaders(responseBuilder);
        // add file attachment header
        responseBuilder.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        return responseBuilder;
    }

    /**
     * update CORS headers
     *
     * @param responseBuilder response builder
     */
    private static void updateCorsHeaders(Response.ResponseBuilder responseBuilder) {
        //  responseBuilder.header("Access-Control-Allow-Origin", "*");
        responseBuilder.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        responseBuilder.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-Codingpedia");
        responseBuilder.header("Cache-Control", "no-store");
    }
}
