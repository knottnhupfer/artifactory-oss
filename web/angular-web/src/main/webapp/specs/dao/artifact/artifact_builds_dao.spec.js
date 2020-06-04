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
describe('unit test:artifact builds dao', function () {

    var artifactBuildsDao;
    var RESOURCE;
    var server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactBuildsDao, _RESOURCE_, _$httpBackend_) {
        artifactBuildsDao = ArtifactBuildsDao.getInstance();
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
    }));

    it('artifactBuildsDao should return a resource object', function () {
        expect(artifactBuildsDao.name).toBe('Resource');
    });

    it('query should send the correct GET request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_BUILDS + '?repoKey=libs-release-local&path=c/d.bin').respond({});
        artifactBuildsDao.query({"repoKey": "libs-release-local", "path": "c/d.bin"});
    });

    it('getJSON should send the correct GET request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_BUILDS + '/json/123').respond({});
        artifactBuildsDao.getJson({buildId: '123'});
    });
});