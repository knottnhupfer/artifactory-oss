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
describe('unit test:tree browser tab dao', function () {

    var viewSourceDao;
    var RESOURCE;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactViewSourceDao, _RESOURCE_, $httpBackend) {
        viewSourceDao = ArtifactViewSourceDao.getInstance();
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));
    afterEach(function() {
        server.flush();
    });

    it('fetch should send a POST request to server', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_VIEW_SOURCE).respond(200);
        viewSourceDao.fetch({"type": "file", "repoKey": "libs-release-local", "path": ""});
    })
});