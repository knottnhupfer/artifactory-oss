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
describe('unit test:configDescriptorDao', function () {

    var configDescriptorDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('jfrog.ui.essentials', 'artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        configDescriptorDao = $injector.get('ConfigDescriptorDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('configDescriptorDao should return a resource object', function () {
        expect(configDescriptorDao.name).toBe('Resource');
    });

    it('configDescriptorDao send an PUT request', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.CONFIG_DESCRIPTOR).respond(200);
        configDescriptorDao.update();
        server.flush();
    });
});