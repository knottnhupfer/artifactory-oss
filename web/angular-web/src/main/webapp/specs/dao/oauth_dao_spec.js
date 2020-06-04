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
describe('unit test:oauth dao', function () {

    var oauthDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        oauthDao = $injector.get('OAuthDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('oauthDao should return a resource object', function () {
        expect(oauthDao.name).toBe('Resource');
    });


    it('send a get request with oauth dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.OAUTH).respond(200);
        oauthDao.get();
        server.flush();
    });


    it('send an update request with oauth dao ', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.OAUTH).respond(200);
        oauthDao.update();
        server.flush();
    });

    it('send an createProvider request with oauth dao ', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.OAUTH + '/provider').respond(200);
        oauthDao.createProvider();
        server.flush();
    });

    it('send an updateProvider request with oauth dao ', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.OAUTH + '/provider').respond(200);
        oauthDao.updateProvider();
        server.flush();
    });

    it('send an deleteProvider request with oauth dao ', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.OAUTH + '/provider/provider1').respond(200);
        oauthDao.deleteProvider({},{provider:'provider1'});
        server.flush();
    });

    it('send an getUserTokens request with oauth dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.OAUTH + '/user/tokens').respond(200);
        oauthDao.getUserTokens();
        server.flush();
    });

    it('send an deleteUserToken request with oauth dao ', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.OAUTH + '/user/tokens/a_username/a_provider').respond(200);
        oauthDao.deleteUserToken({},{username: 'a_username',provider: 'a_provider'});
        server.flush();
    });

});