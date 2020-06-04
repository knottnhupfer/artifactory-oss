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
var mockStorage = require('../../mocks/artifactory_storage_mock.browserify.js');

describe('unit test:footer dao', function () {

    var footerDao;
    var RESOURCE;
    var server;
    var footerResponse = {"isAol": false, "versionID": "PRO", "versionInfo":"Artifactory Professional","buildNumber":"5.x-SNAPSHOT rev devel","licenseInfo":"Licensed to JFrog","copyRights":"© Copyright 2015 JFrog Ltd","copyRightsUrl":"http://www.jfrog.org"};
    mockStorage();

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        footerDao = $injector.get('FooterDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    //
    it('should send a get request to return an footerDao', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
        footerDao.get();
        server.flush();
    });
    it('should cache the result', function (finito) {
        server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
        footerDao.get();
        footerDao.get()
            .then(function(footerInfo) {
                expect(footerDao.getInfo()).toEqual(footerInfo);
                finito();
            });
        server.flush();
    });

});