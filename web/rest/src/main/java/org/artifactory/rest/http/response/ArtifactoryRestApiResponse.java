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

package org.artifactory.rest.http.response;

import org.artifactory.rest.http.ResponseHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author Chen Keinan
 */
@Component("artifactoryUiApiResponse")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactoryRestApiResponse implements IResponse {
    private Collection iModelList;
    private Object iModel;
    private HttpServletResponse servletResponse;
    private boolean hasModelList;
    private boolean hasModel;
    private boolean uiRestCall;

    public boolean isHasModel() {
        return hasModel;
    }

    private int responseCode = HttpServletResponse.SC_OK;

    public boolean isUiRestCall() {
        return uiRestCall;
    }

    public void setUiRestCall(boolean uiRestCall) {
        this.uiRestCall = uiRestCall;
    }

    public ArtifactoryRestApiResponse() {
    }

    public ArtifactoryRestApiResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public int getResponseCode() {
        return responseCode;
    }


    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public Collection getiModelList() {
        return iModelList;
    }

    public void setIModelList(Collection iModelList) {
        this.iModelList = iModelList;
        if (iModelList != null) {
            hasModelList = true;
        }
    }

    @Override
    public void setIModel(Object iModel) {
        this.iModel = iModel;
        if (iModel != null) {
            hasModel = true;
        }
    }

    public Object getIModel() {
        return iModel;
    }


    /**
     * update response data with model data
     */
    public Response buildResponse() {
        Response serviceResponse;
        if (hasModel || hasModelList) {
            serviceResponse = ResponseHandler.buildResponseWithJson(this);
        } else {
            serviceResponse = ResponseHandler.buildResponseWithoutJson(this);
        }
        return serviceResponse;
    }
}
