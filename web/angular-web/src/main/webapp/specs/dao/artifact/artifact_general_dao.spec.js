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
describe('unit test:artifact general dao', function () {

    var artifactGeneralDao;
    var RESOURCE;
    var generalTabDataMock = {"type": "repository", "repoKey": "libs-release-local", "path": ""}

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactGeneralDao, _RESOURCE_, $httpBackend) {
        artifactGeneralDao = ArtifactGeneralDao;
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function() {
        server.flush();
    });

    it('fetch should send a put request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_GENERAL).respond(200);
        artifactGeneralDao.fetch(generalTabDataMock);
    });

    it('bintray should send a post request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_GENERAL_BINTRAY + '?$no_spinner=true&sha1=asdf').respond(200);
        artifactGeneralDao.bintray({sha1: 'asdf'});
    });    
});