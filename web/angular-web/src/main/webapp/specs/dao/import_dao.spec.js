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
describe('unit test: import dao', function () {

    var ImportDao;
    var RESOURCE;
    var server;


    var importMock = {
        action:'system',
        path: '/home/jfrog/import',
        excludeContent: false,
        excludeMetadata: false,
        verbose: false
    };


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ImportDao = $injector.get('ImportDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ImportDao should return a resource object', function () {
        expect(ImportDao.name).toBe('Resource');
    });

    it('send an import request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.IMPORT+"/system" , importMock).respond(200);
        ImportDao.save(importMock);
        server.flush();
    });
});