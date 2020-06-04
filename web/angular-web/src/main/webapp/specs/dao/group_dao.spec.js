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
describe('unit test:groups dao', function () {

    var groupsDao;
    var RESOURCE;
    var server;
    var groupMock = {
        "name": "idan",
        "description": "idan group bla bla",
        "autoJoin": false,
        "realm": "artifactory",
        "groupName": "idan",
        "newUserDefault": false
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        groupsDao = $injector.get('GroupsDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('groupDao should return a resource object', function () {
        expect(groupsDao.name).toBe('Resource');
    });

    it('send an update request group dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.GROUPS + "/idan", groupMock).respond(200);
        groupsDao.update(groupMock);
        server.flush();
    });

    it('send delete request with group dao', function () {

        server.expectPOST(RESOURCE.API_URL + RESOURCE.GROUPS + "/delete", groupMock).respond(200);
        groupsDao.delete(groupMock);
        server.flush();
    });

    it('send an save request  with group dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.GROUPS, groupMock).respond(200);
        groupsDao.save(groupMock);
        server.flush();
    });

});