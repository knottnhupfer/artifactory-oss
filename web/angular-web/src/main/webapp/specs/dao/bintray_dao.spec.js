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
describe('unit test:bintray dao', function () {

    var bintrayDao;
    var RESOURCE;
    var server;
    var bintrayMock = {
        "userName": "chenk",
        "apiKey": "165af2caacac2a636038ac7609eb7215170d946d",
        "fileUploadLimit": 0,
        "bintrayAuth": "chenk:165af2caacac2a636038ac7609eb7215170d946d"
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        bintrayDao = $injector.get('BintrayDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('bintrayDao should return a resource object', function () {
        expect(bintrayDao.name).toBe('Resource');
    });

    it('bintrayDao send an put request', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING).respond(200);
        bintrayDao.update(bintrayMock);
        server.flush();
    });
    it('bintrayDao send an post request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING).respond(200);
        var obj = new bintrayDao(bintrayMock);
        obj.$save();
        server.flush();
    });

    it('bintrayDao send an get request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING).respond(200);
        bintrayDao.get();
        server.flush();
    });

});