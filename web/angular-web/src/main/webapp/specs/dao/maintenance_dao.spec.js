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
describe('unit test:maintenance dao', function () {

    var maintenanceDao;
    var RESOURCE;
    var server;

    var settingsParams = {
        cleanUnusedCachedCron: '',
        cleanVirtualRepoCron: '*/5 * * * *',
        garbageCollectorCron: '* */5 * * *',
        quotaControl: true,
        storageLimit: '95',
        storageWarning: '85'
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        maintenanceDao = $injector.get('MaintenanceDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('maintenanceDao should return a resource object', function () {
        expect(maintenanceDao.name).toBe('Resource');
    });

    it('should get settings', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.MAINTENANCE).respond(200);
        maintenanceDao.get();
        server.flush();
    });

    it('should save settings', function() {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.MAINTENANCE).respond(200);
        maintenanceDao.update(settingsParams);
        server.flush();
    });

    it('should run garbage collection', function() {
        var moduleName = 'garbageCollection';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should clean unused  cache', function() {
        var moduleName = 'cleanUnusedCache';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should clean Virtual Repositories', function() {
        var moduleName = 'cleanVirtualRepo';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should prune Unreferenced Data', function() {
        var moduleName = 'prune';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

    it('should compress internal data', function() {
        var moduleName = 'compress';
        server.expectPOST(RESOURCE.API_URL + RESOURCE.MAINTENANCE + '/' + moduleName).respond(200);
        maintenanceDao.perform({module: moduleName});
        server.flush();
    });

});