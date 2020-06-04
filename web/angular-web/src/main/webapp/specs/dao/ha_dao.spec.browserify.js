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
import {regularResponse as haMock} from '../../mocks/ha_mock.browserify.js';

describe('unit test:ha dao', () => {
    let haDao;
    let RESOURCE;
    let server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject( ($injector) => {
        haDao = $injector.get('HaDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('haDao should return a resource object',  () => {
        expect(haDao.name).toBe('Resource');
    });

    it('send a get request with ha dao ',  () => {
        server.expectGET(RESOURCE.API_URL + RESOURCE.HIGH_AVAILABILITY).respond(haMock);
        haDao.query();
        server.flush();
    });
    it('send a delete request with ha dao ',  () => {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.HIGH_AVAILABILITY + '/art-8080').respond(200);
        haDao.delete({id: haMock[0].id});
        server.flush();
    });

});