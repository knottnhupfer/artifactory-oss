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

package org.artifactory.rest.services;

import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;

/**
 * @author Chen Keinan
 */
public interface IService {

    /**
     * execute service method (i.e:login,create user and etc)
     *
     * @param artifactoryRequest  - encapsulate all data require for request processing
     * @param artifactoryResponse - encapsulate all data require from response
     * @return data model to be build in response
     */
    void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse);
}
