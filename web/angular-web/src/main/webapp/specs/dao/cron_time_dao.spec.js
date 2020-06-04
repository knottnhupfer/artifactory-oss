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
describe('unit test:crontime dao', function () {

    var cornTimeDao;
    var RESOURCE;
    var server;
    var cornMock = {
        "cron": "0 23 5 * * ?"
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        cronTimeDao = $injector.get('CronTimeDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('cornTimeDao should return a resource object', function () {
      //  console.log(cornTimeDao);
       expect(cronTimeDao.name).toBe('Resource');
    });
    //
    it('should send a get request to return an cornTimeDao', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.CRON_TIME).respond(200);
        cronTimeDao.get();
        server.flush();
    })
});