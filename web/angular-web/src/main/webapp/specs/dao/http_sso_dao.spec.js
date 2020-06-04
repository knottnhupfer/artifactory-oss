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
describe('Unit: http sso dao', function () {

    var httpSsoDao;
    var RESOURCE;
    var server;
    var ssoMock = {
        "httpSsoProxied": true,
        "noAutoUserCreation": false,
        "remoteUserRequestVariable": "REMOTE_USER"
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        httpSsoDao = $injector.get('HttpSsoDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('passwordEncryptionDao should return a resource object', function () {
        expect(httpSsoDao.name).toBe('Resource')
    });

    it('should send a get request when query for proxies', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.HTTPSSO).respond(200);
        httpSsoDao.get();
        server.flush();
    });

    it('should send a get request when query for proxies', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.HTTPSSO, ssoMock).respond(200);
        httpSsoDao.update(ssoMock);
        server.flush();
    });

});
