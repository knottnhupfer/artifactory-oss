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

package org.artifactory.rest.common.resource;

import org.artifactory.rest.common.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * @author chen keinan
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public abstract class BaseResource {
    @Context
    protected HttpServletRequest servletRequest;
    @Context
    protected HttpServletResponse servletResponse;
    @Autowired
    protected ServiceExecutor serviceExecutor;
    protected RestResponse artifactoryResponse;

    @Context Request request;
    @Context UriInfo uriInfo;
    @Context HttpHeaders httpHeaders;

    @Autowired
    @Qualifier("artifactoryUiResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     * @param modelData - rest data type
     * @return instance of ArtifactoryRestRequest
     */
    protected <Y> ArtifactoryRestRequest<Y> getArtifactoryRestRequest(Y modelData) {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders)
                .model(modelData);
        return new ArtifactoryRestRequest(builder);
     }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     * @param modelsData - rest data type List
     * @return instance of ArtifactoryRestRequest
     */
    protected <Y> ArtifactoryRestRequest<Y> getArtifactoryRestRequest(List<Y> modelsData) {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders)
                .models(modelsData);
        return new ArtifactoryRestRequest(builder);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) with  model
     *
     * @param service - service type (Login Service and etc)
     * @return - rest response
     */
    protected <Y> Response runService(RestService<Y> service, Y model) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest(model);
        updateServletData();
        // process service request
        return serviceExecutor.process(artifactoryRequest,artifactoryResponse,service);
    }

    /**
     * update http servlet request and response
     */
    private void updateServletData() {
        ((ArtifactoryRestResponse) artifactoryResponse).setServletResponse(servletResponse);
        ((ArtifactoryRestResponse) artifactoryResponse).setServletRequest(servletRequest);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) with  model
     *
     * @param service - service type (Login Service and etc)
     * @return rest response
     */
    protected <Y> Response runService(RestService<List<Y>> service, List<Y> model) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest(model);
        updateServletData();
        // process service request
        return serviceExecutor.process(artifactoryRequest, artifactoryResponse, service);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) without model
     * @param service - service type (Login Service and etc)
     * @return - rest response
     */
    protected <Y> Response runService(RestService<Y> service) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest();
        updateServletData();
        // process service request
        return serviceExecutor.process(artifactoryRequest,artifactoryResponse,service);
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     * @return instance of ArtifactoryRestRequest
     */
    protected <Y> ArtifactoryRestRequest<Y> getArtifactoryRestRequest() {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders);
        return new ArtifactoryRestRequest(builder);
    }
}
