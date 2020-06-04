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
 * returns a function that accept some custom info
 * and returns the interceptor object.
 * intent to be injected and use in DAO's
 *
 * @returns {Function}
 */
window._debugOn = function() {
    localStorage._debug = true;
}
window._debugOff = function() {
    delete localStorage._debug;
}
export function artifactoryDebugInterceptor($injector) {
    /**
     * accept an additional info that can be used
     * in the returned interceptor object
     *
     * @returns {{response: Function, responseError: Function}}
     */
    var $q;
    var RESOURCE;
    function debugResponse(res) {
        if (!localStorage._debug) return;
        RESOURCE = RESOURCE || $injector.get('RESOURCE');
        var apiRequest = _.contains(res.config.url, RESOURCE.API_URL);

        if (apiRequest) {
            console.log("========================");
            console.log("URL:      ",res.config.url);
            console.log("METHOD:   ",res.config.method);
            console.log("DATA:     ",res.config.data);
            console.log("Status:   ",res.status);
            console.log("Response: ", res.data);
            console.log("========================");
        }
    }
    function response(res) {
        debugResponse(res);
        return res;
    }
    function responseError(res) {
        $q = $q || $injector.get('$q');
        debugResponse(res);
        return $q.reject(res);
    }
    return {
        response: response,
        responseError: responseError
    };
}
