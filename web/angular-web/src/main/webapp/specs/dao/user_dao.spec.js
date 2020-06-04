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
describe('unit test:user dao', function () {

    var userDao;
    var RESOURCE;
    var server;
    var userParams =  {
        "name": "idan",
        "email": "idanaim@gmail.com",
        "admin": false,
        "profileUpdatable": true,
        "internalPasswordDisabled": false
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        userDao = $injector.get('UserDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('groupDao should return a resource object', function () {
        expect(userDao.name).toBe('Resource');
    });
    //
    it('send an update request with user dao ', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.USERS + "/idan").respond(200);
        userDao.update(userParams);
        server.flush();
    });

    it('send a get request  with user dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.USERS).respond(200);
        userDao.get();
        server.flush();
    });

    it('send a get request  with user dao ', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.USERS).respond(200);
        userDao.save(userParams);
        server.flush();
    });

    it('send delete request with user dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.USERS +"/userDelete", userParams).respond(200);
        userDao.delete(userParams);
        server.flush();
    });
});