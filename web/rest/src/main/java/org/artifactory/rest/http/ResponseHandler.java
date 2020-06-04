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

package org.artifactory.rest.http;

import org.artifactory.api.rest.restmodel.JsonUtil;
import org.artifactory.rest.http.response.ArtifactoryRestApiResponse;

import javax.ws.rs.core.Response;

/**
 * @author chen keinan
 */
public class ResponseHandler {

    /**
     * @return response code and data for Post Request
     */
    public static Response buildResponseWithJson(ArtifactoryRestApiResponse artifactoryRestApiResponse) {
        Response.ResponseBuilder responseBuilder;
        String content;
        if (artifactoryRestApiResponse.isHasModel()) {
            if (artifactoryRestApiResponse.isUiRestCall()) {
                responseBuilder = getResponseBuilder();
                content = objectToJson(artifactoryRestApiResponse.getIModel());
                updateContentLength(responseBuilder, content);
            }
            return Response.status(artifactoryRestApiResponse.getResponseCode()).entity(
                    artifactoryRestApiResponse.getIModel()).build();
        } else {
            if (artifactoryRestApiResponse.isUiRestCall()) {
                responseBuilder = getResponseBuilder();
                content = objectToJson(artifactoryRestApiResponse.getiModelList());
                updateContentLength(responseBuilder, content);
            }
            return Response.status(artifactoryRestApiResponse.getResponseCode()).entity(
                    artifactoryRestApiResponse.getiModelList()).build();
        }
    }

    /**
     * @return response code
     */
    public static Response buildResponseWithoutJson(ArtifactoryRestApiResponse artifactoryRestApiResponse) {
        Response.ResponseBuilder responseBuilder = Response.ok();
        return responseBuilder.status(artifactoryRestApiResponse.getResponseCode()).build();
    }

    private static void updateContentLength(Response.ResponseBuilder responseBuilder, String content) {
        responseBuilder.header("Content-Length", content.getBytes().length);
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
     * update CORS headers
     *
     * @param responseBuilder response builder
     */
    private static void updateCorsHeaders(Response.ResponseBuilder responseBuilder) {
     //   responseBuilder.header("Access-Control-Allow-Origin", "*");
        responseBuilder.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        responseBuilder.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-Codingpedia");
    }

    /**
     * json to String exclude null data
     *
     * @param model - model data to String
     * @return - model data with json format
     */
    public static String objectToJson(Object model) {
        return JsonUtil.jsonToString(model);
    }
}
