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
window._sessionExpire = function () {
    localStorage._forceSessionExpire = true;
}

export function artifactorySessionInterceptor($injector) {
    var User;
    var $state;
    var $stateParams;
    var ArtifactoryState;
    var $location;
    var RESOURCE;
    var $q;
    var JFrogNotifications;
    var ArtifactoryHttpClient;
    var $window;

    function initInjectables() {
        $q = $q || $injector.get('$q');
        $window = $window || $injector.get('$window');
        User = User || $injector.get('User');
        $state = $state || $injector.get('$state');
        $stateParams = $stateParams || $injector.get('$stateParams');
        ArtifactoryState = ArtifactoryState || $injector.get('ArtifactoryState');
        $location = $location || $injector.get('$location');
        RESOURCE = RESOURCE || $injector.get('RESOURCE');
        JFrogNotifications = JFrogNotifications || $injector.get('JFrogNotifications');
        ArtifactoryHttpClient = ArtifactoryHttpClient || $injector.get('ArtifactoryHttpClient');
    }

    function bypass(res) {
        return res.config && res.config.bypassSessionInterceptor;
    };

    function isSessionInvalid(res) {
        return res.headers().sessionvalid === "false";
    }

    function isApiRequest(res) {
        return _.contains(res.config.url, RESOURCE.API_URL);
    }
    function isOpenApi(res) {
        return isApiRequest(res) && (res.config.url.endsWith('/auth/current') || res.config.url.endsWith('/auth/screen/footer'));
    }
    function isLoggedIn() {
        return !User.getCurrent().isGuest();
    }

    function handleExpiredSession() {
        // if session invalid and we think we are logged in - session expired on server
        delete localStorage._forceSessionExpire;
        User.loadUser(true);

        if ($state.current !== 'login' && $location.path() !== '/login') {
            setUrlAfterLogin();
        }
        return true;
    }

    function verifySession(res) {
        initInjectables();
        if (bypass(res)) {
            return true;
        }

        User.loadUser(); // Refresh from localstorage (parallel tab support)
        if (isApiRequest(res) && !isOpenApi(res) && isSessionInvalid(res) && isLoggedIn() || localStorage._forceSessionExpire) {
            // if the user is not logged in but is in a bypassed request
            // let the request go through but log out the user.
            if ($location.path() !== '/login'){
                setUrlAfterLogin();
            }
            return handleExpiredSession();
        }
        return true;
    }

    function checkAuthorization(res) {
        if (res.status === 401) {
            User.getLoginData($stateParams.redirectTo).then((res) => {
                if (res.ssoProviderLink) {
                   if ($location.path() == '/login') {
                       reloadUserAndChangeState('login');
                   } else {
                       $window.open(res.ssoProviderLink, "_self");
                   }
               } else {
                   if ($state.current.name !== 'reset-password' || !$stateParams.key) {
                       if ($state.current !== 'login' && $location.path() !== '/login'
                               && $state.current !== 'reset-password' && $location.path() !== '/resetpassword') {
                           setUrlAfterLogin();
                       }
                       reloadUserAndChangeState('login');
                   }
               }
            });
        }
        else if (res.status === 403) {
            if (res.config.url.indexOf('permissiontargets') !== -1) {
                JFrogNotifications.create({error: 'You are not authorized to view this page'});
                $state.go('home');
            }
        }
    }

    // Reloading user after receiving a 401 is necessary.
    // Otherwise the user would not be considered as logged in by the UI.
    function reloadUserAndChangeState(toState){
        User.loadUser(true).then(()=>$state.go(toState));
    }

    function setUrlAfterLogin() {
        ArtifactoryState.setState('urlAfterLogin', $location.path());
    }

    function response(res) {
        if (verifySession(res)) {
            return res;
        }
        else {
            return $q.reject(res);
        }
    }

    function request(req) {
        req.headers['X-Requested-With'] = 'artUI';
        return req;
    }

    function responseError(res) {
        verifySession(res);
        checkAuthorization(res);
        return $q.reject(res);
    }

    return {
        response: response,
        responseError: responseError,
        request: request
    };
}
