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

package org.artifactory.rest.common.service;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
public interface RestResponse<T> {

    /**
     * build rest response instance with status code and entity (if needed)
     *
     * @return standard Rest Response instance
     */
     Response buildResponse();

    /**
     * set the status code following to rest request to be send back
     * with response if not set , default code  = 200
     *
     * @param responseCode - status code
     */
    ArtifactoryRestResponse responseCode(int responseCode);

    /**
     * set List of model (i.e. users list and etc) to be send in entity
     * with rest response
     *
     * @param iModelList
     */
    ArtifactoryRestResponse iModelList(Collection<T> iModelList);

    /**
     * set single model (i.e. users  and etc) to be send in entity
     * with rest response
     *
     * @param iModel
     */
    ArtifactoryRestResponse iModel(T iModel);

    /**
     * feedback error msg
     *
     * @param error - error msg
     */
    ArtifactoryRestResponse error(String error);

    /**
     * feedback warn msg
     *
     * @param warn - warn msg
     */
    ArtifactoryRestResponse warn(String warn);

    /**
     * feedback info msg
     *
     * @param info - info msg
     */
    ArtifactoryRestResponse info(String info);

    /**
     * feedback entity
     *
     * @param entity
     */
    ArtifactoryRestResponse entity(Object entity);

    ArtifactoryRestResponse errors(List<String> errors);

    ArtifactoryRestResponse url(String url);

    /**
     * return rest servlet response
     *
     * @return HttpServletResponse instance for this rest call
     */
     HttpServletResponse getServletResponse();

    <T> T getIModel();

    Collection<T> getiModelList();

    Object getEntity();

    int getResponseCode();

    /**
     * @return whether request has failed
     */
    public boolean isFailed();
}
