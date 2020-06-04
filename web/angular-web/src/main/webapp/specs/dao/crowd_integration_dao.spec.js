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
describe('unit test:crowd integration dao', function () {

    var crowdIntegrationDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (CrowdIntegrationDao, _RESOURCE_, _$httpBackend_) {
        crowdIntegrationDao = CrowdIntegrationDao;
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
    }));

    it('artifactBuildsDao should return a resource object', function () {
        expect(crowdIntegrationDao.name).toBe('Resource');
    });

    it('query should send the correct GET request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.CROWD + '/test').respond({});
        crowdIntegrationDao.test({
            "enableIntegration": true,
            "serverUrl": "http://localhost:8095/crowd",
            "applicationName": "artifactory",
            "password": "password",
            "sessionValidationInterval": 0,
            "useDefaultProxy": false,
            "noAutoUserCreation": true,
            "directAuthentication": false
        });
    });

    it('refresh data should send the correct GET request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.CROWD + '/refresh/chenk').respond({});
        crowdIntegrationDao.refresh({
            "enableIntegration": true,
            "serverUrl": "http://localhost:8095/crowd",
            "applicationName": "artifactory",
            "password": "password",
            "sessionValidationInterval": 0,
            "useDefaultProxy": false,
            "noAutoUserCreation": true,
            "directAuthentication": false
        });
    });
});