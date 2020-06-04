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
import UserMock from '../../mocks/user_mock.browserify.js';

var GUEST = 1;
var USER = 2;
var ADMIN = 3;
describe('Unit: User Service', function () {
    var User;
    var RESOURCE;
    var artifactoryStorage;
    var server;
    var $rootScope;
    var $window;
    var USER_KEY = 'USER';
    
    var user = new UserMock({name: 'admin'});
    var regularUser = UserMock.regularUser();
    var guestUser = UserMock.guest();
    var guestUserObj;
    var regularUserObj;
    var adminUserObj;

    var footerResponse = {"isAol": false, "versionID": "PRO", "versionInfo":"Artifactory Professional","buildNumber":"5.x-SNAPSHOT rev devel","licenseInfo":"Licensed to JFrog","copyRights":"© Copyright 2015 JFrog Ltd","copyRightsUrl":"http://www.jfrog.org"};

    useAngularEquality();

    // inject the main module
    beforeEach(m('jfrog.ui.essentials', 'artifactory.dao', 'artifactory.services', 'conf.fixer'));

    // must run this before injecting User Service
    beforeEach(inject(function (ArtifactoryStorage) {
        artifactoryStorage = ArtifactoryStorage;
        artifactoryStorage.removeItem(USER_KEY);
    }));

    // run this code before each case
    function setup(initialUser) {
        if (initialUser) {
            artifactoryStorage.setItem(USER_KEY, initialUser);
        }
        inject(function ($injector, _RESOURCE_, _User_, _$rootScope_, _$window_) {
            RESOURCE = _RESOURCE_;
            User = _User_;
            server = $injector.get('$httpBackend');
            $rootScope = _$rootScope_;
            $window = _$window_;
            guestUserObj = guestUser.getUserObj();
            regularUserObj = regularUser.getUserObj();
            adminUserObj = user.getUserObj();
//            spyOn($window.location, 'replace');
        });
    }

    describe('static methods', () => {
        describe('start as guest', () => {
            beforeEach(() => {
                setup();
            });

            it('User should be defined', function () {
                expect(User).toBeDefined();
            });

            it('should save user data after logging in', function(done) {
//                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_LOGIN + 'false').respond(user);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                User.login({username: 'admin', password: '123123'}, false).then(() => {
                    var userResponse = artifactoryStorage.getItem(USER_KEY);
                    expect(userResponse).not.toBeNull();
                    expect(userResponse.name).toBe('admin');
                    done();
                });
                server.flush();
            });

            it('should reload user from server after logging out', function(done) {
                //                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(guestUser);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_IS_SAML).respond(false);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_LOGOUT).respond(200);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(guestUser);
                User.logout().then(() => {
                    expect(artifactoryStorage.getItem(USER_KEY)).toEqual(guestUser);
                    done();
                });
                server.flush();
            });

            //it('should redirect user to SAML logout', function(done) {
            //    server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_IS_SAML).respond(true);
            //    server.expectGET(RESOURCE.API_URL + RESOURCE.SAML_LOGOUT).respond('url');
            //    User.logout().then(() => {
            //        expect($window.location.replace).toHaveBeenCalledWith('url');
            //        done();
            //    });
            //    server.flush();
            //});

            it('should send password reset email', function(done) {
//                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_FORGOT_PASSWORD).respond(200);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                User.forgotPassword({username: 'admin'}).then(function(response) {
                    expect(response.status).toBe(200);
                    done();
                });
                server.flush();
            });

            it('should validate reset password key', function(done) {
                var key = 'YHSH8@(@&!773';
                var serverResponse = {"user": "admin"};
//                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_VALIDATE_KEY + key).respond(serverResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                User.validateKey(key).then(function(response) {
                    expect(response.status).toBe(200);
                    expect(response.data.user).toBe('admin');
                    done();
                });
                server.flush();
            });

            it('should reset password', function(done) {
                var key = 'YHSH8@(@&!773';
//                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_RESET_PASSWORD + key).respond(200);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                User.resetPassword(key, user).then(function(response) {
                    expect(response.status).toBe(200);
                    done();
                });
                server.flush();
            });

            it('should allow to setUser', () => {
                User.setUser(user);
                expect(artifactoryStorage.getItem(USER_KEY)).toEqual(user);
            });    

            it('should allow to getCurrent when there is no user', () => {
                expect(User.getCurrent()).toEqual({});
            });

            it('should allow to reload the user from localstorage', function(finito) {
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                artifactoryStorage.setItem(USER_KEY, user);
                User.loadUser().then((user) => {
                    expect(user).toEqual(adminUserObj);
                    finito();
                });
                $rootScope.$digest();
            });

            it('should allow to reload the user from the server', function(voila) {
//                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(regularUser);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(regularUser);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                User.loadUser(true).then((response) => {
                    expect(response).toEqual(regularUserObj);
                    voila();
                });
                server.flush();
                $rootScope.$digest();
            });

            it('should have a whenLoadedFromServer variable', function(voila) {
//                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
                server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
                User.whenLoadedFromServer.then((response) => {
                    expect(response).toEqual(adminUserObj);
                    voila();
                });
                server.flush();
                $rootScope.$digest();
            });

            it('should not change reference after reloading from localstorage', () => {
                var getCurrent = User.getCurrent();
                artifactoryStorage.setItem(USER_KEY, user);
                User.loadUser();
                expect(User.getCurrent()).toBe(getCurrent);
            });
        });
        describe('start as logged in user', () => {
            beforeEach(() => {
                setup(user);
            });
            it ('should allow to getCurrent when there is a user', () => {
                expect(User.getCurrent()).toEqual(adminUserObj);
            });
        });
    });
    describe('instance methods', () => {
        it('should have an isGuest method', () => {
            expect(guestUserObj.isGuest()).toBeTruthy();
            expect(regularUserObj.isGuest()).toBeFalsy();
            expect(adminUserObj.isGuest()).toBeFalsy();
        });
        it('should have an isAdmin method', () => {
            expect(guestUserObj.isAdmin()).toBeFalsy();
            expect(regularUserObj.isAdmin()).toBeFalsy();
            expect(adminUserObj.isAdmin()).toBeTruthy();
        });
        it('should have an isRegularUser method', () => {
            expect(guestUserObj.isRegularUser()).toBeFalsy();
            expect(regularUserObj.isRegularUser()).toBeTruthy();
            expect(adminUserObj.isRegularUser()).toBeFalsy();
        });
        it('should have an isLoggedIn method', () => {
            expect(guestUserObj.isLoggedIn()).toBeFalsy();
            expect(regularUserObj.isLoggedIn()).toBeTruthy();
            expect(adminUserObj.isLoggedIn()).toBeTruthy();
        });
        it('should have a getCanManage method', () => {
            expect(guestUserObj.getCanManage()).toBeFalsy();
            expect(adminUserObj.getCanManage()).toBeTruthy();
        });
        it('should have a getCanDeploy method', () => {
            expect(guestUserObj.getCanDeploy()).toBeFalsy();
            expect(adminUserObj.getCanDeploy()).toBeTruthy();
        });
        it('should have a canView method', () => {
            expect(guestUserObj.canView('admin.security.permission')).toBeFalsy();
            expect(adminUserObj.canView('admin.security.permission')).toBeTruthy();
            expect(regularUserObj.canView('admin.security.general')).toBeFalsy();
            expect(adminUserObj.canView('admin.security.general')).toBeTruthy();
            expect(guestUserObj.canView('home')).toBeTruthy();
            expect(regularUserObj.canView('home')).toBeTruthy();
            expect(adminUserObj.canView('home')).toBeTruthy();
        });
    });
});