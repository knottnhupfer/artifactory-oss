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
describe('unit test:ldap dao', function () {

    var ldapDao;
    var RESOURCE;
    var server;
    var ldapMock = {
        "key": "frogs",
        "enabled": true,
        "ldapUrl": "ldap://win2012:389/dc=jfrog,dc=local",
        "search": {
            "searchFilter": "sAMAccountName={0}",
            "searchBase": "ou=il,ou=frogs|ou=us,ou=frogs",
            "searchSubTree": true,
            "managerDn": "cn=Administrator,cn=Users,dc=jfrog,dc=local",
            "managerPassword": "Win20132013"
        },
        "autoCreateUser": true,
        "emailAttribute": "mail"
    }

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ldapDao = $injector.get('LdapDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ldapDao should return a resource object', function () {
        expect(ldapDao.name).toBe('Resource');
    });

    it('send a query request with ldap dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP).respond(200);
        ldapDao.query();
        server.flush();
    });

    it('send a get a single ldap setting request with ldap dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP + "/frogs").respond(200);
        ldapDao.get({key:'frogs'});
        server.flush();
    });

    it('send a create request to ldap dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP, ldapMock).respond(200);
        ldapDao.save(ldapMock);
        server.flush();
    });

    it('send an update request ldap dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.LDAP + "/frogs").respond(200);
        ldapDao.update(ldapMock);
        server.flush();
    });

    it('send delete request with ldap dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.LDAP + "/frogs").respond(200);
        ldapDao.delete({key:'frogs'});
        server.flush();
    });

    it('send a test request to ldap dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP + "/test/frogs").respond(200);
        ldapDao.test({key:'frogs'});
        server.flush();
    });
});