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
describe('unit test:pom view tab dao', function () {

    var artifactViewsDao;
    var RESOURCE;
    var pomViewTabDataMock = {
        "view":"pom",
        "path": "DecodedBase64/DecodedBase64/DecodedBase64/DecodedBase64-DecodedBase64.pom",
        "repoKey": "ext-releases-local"
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactViewsDao, _RESOURCE_, $httpBackend) {
        artifactViewsDao = ArtifactViewsDao;
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function () {
        server.flush();
    });

    it('fetch should send a put request to serve', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.VIEWS+"/pom").respond(200);
        artifactViewsDao.fetch(pomViewTabDataMock);
    });
});