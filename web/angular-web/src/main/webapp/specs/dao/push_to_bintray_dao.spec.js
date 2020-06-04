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
describe('unit test:push_to_bintray dao', function () {

    var ptbDao;
    var RESOURCE;
    var server;
    var ptbMock = {};

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        ptbDao = $injector.get('PushToBintrayDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('ptbDao should return a resource object', function () {
        expect(ptbDao.name).toBe('Resource');
    });

    it('send a getBuildRepos request with ptb dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.PUSH_TO_BINTRAY).respond(200);
        ptbDao.getBuildRepos();
        server.flush();
    });

    it('send a getBuildPacks request with ptb dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.PUSH_TO_BINTRAY + '/build/pkg').respond(200);
        ptbDao.getBuildPacks();
        server.flush();
    });

    it('send a getBuildVersions request with ptb dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.PUSH_TO_BINTRAY + '/build/versions').respond(200);
        ptbDao.getBuildVersions();
        server.flush();
    });

    it('send a pushBuildToBintray request with ptb dao ', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.PUSH_TO_BINTRAY + '/build').respond(200);
        ptbDao.pushBuildToBintray();
        server.flush();
    });

    it('send a getArtifactData request with ptb dao ', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.PUSH_TO_BINTRAY + '/artifact').respond(200);
        ptbDao.getArtifactData();
        server.flush();
    });

    it('send a pushArtifactToBintray request with ptb dao ', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.PUSH_TO_BINTRAY + '/artifact').respond(200);
        ptbDao.pushArtifactToBintray();
        server.flush();
    });
});
