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
describe('Unit: proxies Dao', function () {

    var proxiesDao;
    var RESOURCE;
    var server;
    var proxyMock = {
        "key": "charls",
        "host": "localhost",
        "port": 8888,
        "defaultProxy": false
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        proxiesDao = $injector.get('ProxiesDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));


    it('passwordEncryptionDao should return a resource object', function () {
        expect(proxiesDao.name).toBe('Resource')
    });

    it('should send a get request when query for proxies', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.PROXIES + '/crud').respond(200);
        proxiesDao.get();
        server.flush();
    });

    it('should send a get request when get for single proxy', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.PROXIES + '/crud/charls').respond(200);
        proxiesDao.get({key: 'charls'});
        server.flush();
    });

    it('should send a delete request with the key as parameter', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.PROXIES + '/deleteProxies', proxyMock).respond(200);
        proxiesDao.delete(proxyMock);
        server.flush()
    });

    it('should perform an update request with an proxyMock object', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.PROXIES + '/crud/charls', proxyMock).respond(200);
        proxiesDao.update(proxyMock);
        server.flush()
    });

    it('should post a new proxy data object', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.PROXIES, proxyMock).respond(200);
        var proxiesDataObject = new proxiesDao(proxyMock);
        proxiesDataObject.$save();
        server.flush();
    })

});
