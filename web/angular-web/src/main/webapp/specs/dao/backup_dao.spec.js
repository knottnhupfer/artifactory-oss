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
describe('unit test:backup dao', function () {

    var backupDao;
    var RESOURCE;
    var server;
    var groupMock = {
        "key": "hhhh",
        "enabled": true,
        "cronExp": "hhhh"
    };
    var queryParams = "?hhhh&&cronExp=hhhh&enabled=true";


    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        backupDao = $injector.get('BackupDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('groupDao should return a resource object', function () {
        expect(backupDao.name).toBe('Resource');
    });

    it('send an update request group dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.BACKUP + '/hhhh' , groupMock).respond(200);
        backupDao.update(groupMock);
        server.flush();
    });

    it('send an save request  with group dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.BACKUP, groupMock).respond(200);
        backupDao.save(groupMock);
        server.flush();
    });

    it('send a delete request group dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.BACKUP + '/hhhh').respond(200);
        backupDao.delete({key: 'hhhh'});
        server.flush();
    });

});