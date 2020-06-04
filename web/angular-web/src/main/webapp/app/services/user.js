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
import EVENTS from "../constants/artifacts_events.constants";

const USER_KEY = 'USER';
const GUEST_USER = {
    name: 'anonymous',
    admin: false,
    profileUpdatable: true,
    internalPasswordDisabled: false,
    canDeploy: false,
    canManage: false,
    preventAnonAccessBuild: false,
    proWithoutLicense: false
};

class User {
    constructor(data) {
        User.JFrogEventBus.register(EVENTS.USER_LOGOUT, (confirmDiscard) => {
            if (!confirmDiscard) {
                User.logout().then(() => {
                    if (!this.saml) {
                        User.$state.go("home");
                    }
                });
            }
            else if (confirmDiscard === "logoutAndLogin") {
                User.logout();
            }
        });

        if (data) {
            this.setData(data);
        }

        this.setIsHa();
    }

    setData(data) {
        if (!_.isEqual(this._data, data)) {
            data.userPreferences = data.userPreferences || {};

            angular.copy(data, this);
            this._data = data;
            User.JFrogEventBus.dispatch(EVENTS.USER_CHANGED);
        }
        User.footerDao.get(false).then(footerData => this.footerData = footerData);
    }

    isProWithoutLicense() {
        return this.proWithoutLicense;
    }

    // Instance methods:
    isGuest() {
        return this.name === GUEST_USER.name;
    }

    isAdmin() {
        return this.admin;
    }

    isRegularUser() {
        return this.isLoggedIn() && !this.isAdmin();
    }

    isLoggedIn() {
        return !!this.name && !this.isGuest();
    }

    getCanManage() {
        return this.canManage || this.isProWithoutLicense();
    }

    getCanDeploy() {
        if (this.isProWithoutLicense()) {
            return false
        }
        return this.canDeploy;
    }

    setIsHa(){
        User.footerDao.get(false).then(footerData => {
            this.isHaConfigured = footerData.haConfigured;
        });
    }


    haLicenseInstalled() {
        return (this.isHaConfigured && !this.isProWithoutLicense());
    }

    canView(state, stateParams = {}) {
        // High Availability configured and the user is not admin and the master node has a license installed
        if (this.haLicenseInstalled() && !this.isAdmin()){
            if (state === "admin.configuration.register_pro"){
                return false;
            }
        }

	    if (User.ArtifactoryFeatures.isEdgeNode()) {
		    if (state === 'builds') return false;
	    }

        if (this.isProWithoutLicense()) {
            if (state === "admin.configuration.register_pro" || state === "admin.configuration" || state === "admin" ||
                    state === "home" || state === "login") {
                return true;
            } else {
                return false;
            }
        }

        if (state === "artifacts") {
            return true;
        }
        if (state.match(/^admin.security.permissions/) || state === "admin") {
            return this.getCanManage();
        }
        else if (state.match(/^admin/)) {
            return this.isAdmin();
        }
        else {
            return true;
        }
    }

    // Class methods:
    static login(username, remember) {
        let loginRequest = this.http.post(this.RESOURCE.AUTH_LOGIN + remember,
            angular.extend(username, {type: 'login'}));

        loginRequest.then(
                (response) => {
                    this.setUser(response.data);
                    User.$timeout(()=>User.JFrogEventBus.dispatch(EVENTS.FOOTER_REFRESH));
                    return username;
                }).catch(err => this.JFrogNotifications.create({'error': err.data.error}));
        return loginRequest;
    }

    static logout() {
        return this.http.get(this.RESOURCE.AUTH_IS_SAML, null, {}).then((res=> {
            if (res.data) {
                return this.http.get(this.RESOURCE.SAML_LOGOUT, null, {}).then((res)=> {
                    this.saml = true;
                    this.$window.location.replace(res.data);
                });
            }
            else {
                return this.http.post(this.RESOURCE.AUTH_LOGOUT, null, {bypassSessionInterceptor: true})
                        .then((res) => {
                            let isOnboarding = this.artifactoryState.getState('onboardingWizardOpen') === true;

                            if (!isOnboarding) {
                                this.clearStates();
                            }


                            if (this.$state.current.name === 'home' && isOnboarding !== true) {
                                this.$state.go(this.$state.current, this.$stateParams, {reload: true});
                            }

                            return this.loadUser(true);

                        });
            }
        }));
    }

    static clearStates() {
        // save some states we want to keep
        let tempStates = this._getStates(['systemMessage','clearErrorsOnStateChange','sidebarEventsRegistered'])
        // clear the states
        this.artifactoryState.clearAll();
        // restore saved states
        this._setStates(tempStates);
    }

    static redirect(redirectTo) {
        let redirectRequest = this.http.get(this.RESOURCE.AUTH_REDIRECT + redirectTo);

        redirectRequest.then(
            (response) => {
                this.$window.location.replace(response.data.url);
            });

        return redirectRequest;
    }

    static _getStates(states) {
        let savedStates = {};
        states.forEach(s=>{
            savedStates[s] = this.artifactoryState.getState(s);
        })
        return savedStates;
    }
    static _setStates(savedStates) {
        for (let key in savedStates) {
            this.artifactoryState.setState(key, savedStates[key]);
        }
    }

    static forgotPassword(user) {
        return this.http.post(this.RESOURCE.AUTH_FORGOT_PASSWORD, user);
    }

    static validateKey(key) {
        return this.http.post(this.RESOURCE.AUTH_VALIDATE_KEY + key);
    }

    static resetPassword(key, user) {
        return this.http.post(this.RESOURCE.AUTH_RESET_PASSWORD + key, user);
    }

    static canAnnotate(repoKey, path) {
        return this.http.get(this.RESOURCE.AUTH_CAN_ANNOTATE + repoKey + '&path=' + path).then((response) => {
            return response;
        });
    }

    static getLoginData(redirectTo) {
        return this.http.post(this.RESOURCE.AUTH_LOGIN_DATA, {
            dummy: 'dummy',
            redirectTo: redirectTo
        }).then((response) => {
            return response.data;
        });
    }

    static getOAuthLoginData() {
        return this.http.get(this.RESOURCE.OAUTH_LOGIN, {params: {redirectTo: this.$stateParams.redirectTo}})
            .then((response) => response.data);
    }

    static setUser(user) {
        this.currentUser.setData(user);
        this.storage.setItem(USER_KEY, user);
        return this.currentUser;
    }

    static loadUser(force = false) {
        var user = this.storage.getItem(USER_KEY);
        if (user) {
            this.currentUser.setData(user);
        }
        if (force || !user) {
            this.whenLoadedFromServer =
                    this.http.get(this.RESOURCE.AUTH_CURRENT, {bypassSessionInterceptor: true}).then((user) => {
                        return this.setUser(user.data)
                    });
            return this.whenLoadedFromServer;
            /*

             this.whenLoadedFromServer = this.http.get(this.RESOURCE.AUTH_CURRENT, {bypassSessionInterceptor: true})
             .then((user) => this.setUser(user.data));
             return this.whenLoadedFromServer;

             */
        }
        else {
            return this.$q.when(this.currentUser)
        }
    }

    static getCurrent() {
        return this.currentUser;
    }

    static reload() {
        this.loadUser(true);
    }
}


export function UserFactory(ArtifactoryHttpClient, ArtifactoryStorage, RESOURCE, $q, $window, $state, $timeout,
                            $stateParams, JFrogEventBus, ArtifactoryFeatures, JFrogNotifications,
                            ArtifactoryState, FooterDao) {
    // Set static members on class:
    User.http = ArtifactoryHttpClient;
    User.storage = ArtifactoryStorage;
    User.RESOURCE = RESOURCE;
    User.$q = $q;
    User.$window = $window;
    User.$timeout = $timeout;
    User.$state = $state;
    User.$stateParams = $stateParams;
    User.artifactoryState = ArtifactoryState;
    User.JFrogEventBus = JFrogEventBus;
    User.footerDao = FooterDao;
    User.currentUser = new User();
    User.ArtifactoryFeatures = ArtifactoryFeatures;
    User.JFrogNotifications = JFrogNotifications;
    // Load user from localstorage:
    User.loadUser(/* force */ true);

    return User;
}
