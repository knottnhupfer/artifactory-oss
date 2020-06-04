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

/**
 * Created by gidis on 7/26/15.
 */

export function artifactoryMessageInterceptor(ArtifactoryState, $q) {

    function request(req) {
        return req;
    }

    function response(res) {
        handleResponse(res);
        return res;
    }

    function responseError(res) {
        handleResponse(res);
        return $q.reject(res);
    }

    function handleResponse(res) {
        let messages=res.headers()["artifactory-ui-messages"];
        if (messages) ArtifactoryState.setState('constantMessages', JSON.parse(messages));
    }

    return {
        response: response,
        request: request,
        responseError: responseError
    };
}
