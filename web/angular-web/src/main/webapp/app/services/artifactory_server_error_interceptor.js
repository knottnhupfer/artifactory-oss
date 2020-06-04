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
export function artifactoryServerErrorInterceptor($injector) {
    var $state;
    var $q;

    function initInjectables() {
        $q = $q || $injector.get('$q');
        $state = $state || $injector.get('$state');
    }

    function responseError(res) {
        initInjectables();
        if (res.status <= 0 && res.xhrStatus !== 'abort') {
            $state.go('server_down');
        }
        if(res.status > 500) {
            $state.go('server_error_5XX');
        }
        return $q.reject(res);
    }

    return {
        responseError: responseError
    };
}

