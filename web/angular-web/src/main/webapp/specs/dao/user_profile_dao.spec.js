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
describe('unit test:user profile dao', function () {

    var userProfileDao;
    var RESOURCE;
    var server;
    var userParams =  {
        "name": "idan",
        "email": "idanaim@gmail.com",
        "admin": false,
        "profileUpdatable": true,
        "internalPasswordDisabled": false
    };

    var userParms = "?admin=false&email=idanaim@gmail.com&internalPasswordDisabled=false&profileUpdatable=true";

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        userProfileDao = $injector.get('UserProfileDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('userProfileDao should return a resource object', function () {
        expect(userProfileDao.name).toBe('Resource');
    });

    it('should fetch data', function() {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.USER_PROFILE).respond(200);
        userProfileDao.fetch({password: 'password'});
        server.flush();
    });

    it('should update data', function() {
        server.expectPUT(RESOURCE.API_URL + RESOURCE.USER_PROFILE).respond(200);
        userProfileDao.update({password: 'password'});
        server.flush();
    });

});