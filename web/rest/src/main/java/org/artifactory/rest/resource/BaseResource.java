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

package org.artifactory.rest.resource;

import org.artifactory.api.rest.restmodel.IModel;
import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.ArtifactoryRestApiResponse;
import org.artifactory.rest.http.response.IResponse;
import org.artifactory.rest.services.IService;
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
    public static final String MEDIA_TYPE_APPLICATION_YAML = "application/yaml";

    @Context
    protected HttpServletRequest servletRequest;
    @Context
    protected HttpServletResponse servletResponse;
    @Autowired
    protected ServiceExecutor serviceExecutor;

    protected IResponse artifactoryResponse;

    @Context
    Request request;
    @Context
    UriInfo uriInfo;
    @Context
    HttpHeaders httpHeaders;

    @Autowired
    @Qualifier("artifactoryUiApiResponse")
    public void setArtifactoryResponse(IResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     *
     * @param modelData - rest data type
     * @return instance of ArtifactoryRestRequest
     */
    protected ArtifactoryRestRequest getArtifactoryRestRequest(IModel modelData) {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders)
                .model(modelData);
        return new ArtifactoryRestRequest(builder);
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     *
     * @param modelsData - rest data type List
     * @return instance of ArtifactoryRestRequest
     */
    protected ArtifactoryRestRequest getArtifactoryRestRequest(List<IModel> modelsData) {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders)
                .models(modelsData);
        return new ArtifactoryRestRequest(builder);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) with  model
     *
     * @param service - service type (Login Service and etc)
     */
    protected Response runService(IService service, IModel model) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest(model);
        artifactoryResponse.setServletResponse(servletResponse);
        // process service request
        return serviceExecutor.process(artifactoryRequest, artifactoryResponse, service);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) with  model
     *
     * @param service - service type (Login Service and etc)
     * @return
     */
    protected Response runService(IService service, List model) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest(model);
        ((ArtifactoryRestApiResponse) artifactoryResponse).setServletResponse(servletResponse);
        // process service request
        return serviceExecutor.process(artifactoryRequest, artifactoryResponse, service);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) without model
     *
     * @param service - service type (Login Service and etc)
     * @return
     */
    protected Response runService(IService service) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest();
        artifactoryResponse.setServletResponse(servletResponse);
        // process service request
        return serviceExecutor.process(artifactoryRequest, artifactoryResponse, service);
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     *
     * @return instance of ArtifactoryRestRequest
     */
    protected ArtifactoryRestRequest getArtifactoryRestRequest() {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders);
        return new ArtifactoryRestRequest(builder);
    }
}
