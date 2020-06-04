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
describe('unit test:builds dao', function () {

    var buildsDao;
    var RESOURCE;
    var server;
    var buildsMock = {
        pagingData: [
            {name: 'asdf', lastBuildTime: '12.12.14'},
            {name: 'alf', lastBuildTime: '12.11.13'},
            {name: 'gradle', lastBuildTime: '12.10.12'},
        ],
        totalItems: 30
    }

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        buildsDao = $injector.get('BuildsDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('buildsDao should return a resource object', function () {
        expect(buildsDao.name).toBe('Resource');
    });

    it('send a get request with builds dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BUILDS).respond(200);
        buildsDao.get();
        server.flush();
    });
    it('send a getHistory request with builds dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.BUILDS + '/buildname').respond(200);
        buildsDao.getData({name: 'buildname'});
        server.flush();
    });

});