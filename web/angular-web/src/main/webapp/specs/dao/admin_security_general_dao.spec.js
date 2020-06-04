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
describe('Unit: admin security general dao', function () {

    var adminSecurityGeneralDao;
    var RESOURCE;
    var server;

    var securityConfigMock = {

        "anonAccessEnabled": false,
        "hideUnauthorizedResources": "false",
        "passwordSettings": {
            "encryptionPolicy": "SUPPORTED"
        }
    };

    // inject the main module
    beforeEach(m('jfrog.ui.essentials', 'artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        adminSecurityGeneralDao = $injector.get('AdminSecurityGeneralDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));


    it('passwordEncryptionDao should return a resource object', function () {
        //expect(adminSecurityGeneralDao.name).toBe('Resource')
    });

    it('sould send an update request with the updated object', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.SECURITY_CONFIG, securityConfigMock).respond(200);
        adminSecurityGeneralDao.update(securityConfigMock);
        server.flush();
    });

    it('hit a server with a get request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.SECURITY_CONFIG).respond(200);
        adminSecurityGeneralDao.get();
        server.flush();
    });

    it('should post new security config mock', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.SECURITY_CONFIG, securityConfigMock).respond(200);
        var SecurityGeneraData = new adminSecurityGeneralDao(securityConfigMock);
        SecurityGeneraData.$save();
        server.flush();
    });


})
;
