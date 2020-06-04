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

package org.artifactory.security.providermgr;

import org.artifactory.security.props.auth.model.OauthDockerErrorModel;
import org.artifactory.security.props.auth.model.OauthErrorEnum;
import org.artifactory.security.props.auth.model.OauthModel;
import org.springframework.security.authentication.AuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
public class DockerProviderMgr extends OAuthProviderMgr {

    public DockerProviderMgr(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource,
            String authHeader, HttpServletRequest servletRequest) {
        super(authenticationDetailsSource, authHeader, servletRequest);
    }

    @Override
    protected OauthModel createErrorModel(int statusCode, OauthErrorEnum errorEnum) {
        return new OauthDockerErrorModel(statusCode, errorEnum);
    }

}
