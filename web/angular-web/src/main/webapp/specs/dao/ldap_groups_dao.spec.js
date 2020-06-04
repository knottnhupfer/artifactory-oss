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
describe('unit test:ldap groups dao', function () {

    var ldapGroupsDao;
    var RESOURCE;
    var server;
    var ldapMock = {
        "name": "il-users",
        "groupBaseDn": "ou=frogs",
        "groupNameAttribute": "cn",
        "groupMemberAttribute": "memberOf",
        "subTree": true,
        "filter": "description",
        "descriptionAttribute": "description",
        "enabledLdap": "frogs",
        "strategy": "DYNAMIC",
        "enabled": true
    }

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ldapGroupsDao = $injector.get('LdapGroupsDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ldapGroupsDao should return a resource object', function () {
        expect(ldapGroupsDao.name).toBe('Resource');
    });

    it('send a get (query) request with ldap groups dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS).respond(200);
        ldapGroupsDao.query();
        server.flush();
    });

    it('send a get request with ldap groups dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users").respond(200);
        ldapGroupsDao.get({name:'il-users'});
        server.flush();
    });

    it('send a POST request to ldap groups dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS, ldapMock).respond(200);
        ldapGroupsDao.save(ldapMock);
        server.flush();
    });

    it('send an update request ldap groups dao', function () {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users").respond(200);
        ldapGroupsDao.update({name:'il-users'});
        server.flush();
    });

    it('send delete request with ldap groups dao', function () {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users").respond(200);
        ldapGroupsDao.delete({name:'il-users'});
        server.flush();
    });

    it('send a refresh request to ldap groups dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users/refresh").respond(200);
        ldapGroupsDao.refresh({name:'il-users'});
        server.flush();
    });

    it('send an import request to ldap groups dao', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.LDAP_GROUPS + "/il-users/import").respond(200);
        ldapGroupsDao.import({name:'il-users'});
        server.flush();
    });

});