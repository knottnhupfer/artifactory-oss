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
describe('unit test:general tab dao', function () {

    var artifactPermissionsDao;
    var RESOURCE;
    var mockPermissionsData = {pagingData: [{principal: 'adam', type: 'user', permission: {'delete': true, 'deploy': true, 'annotate': true, 'read': true}}], totalItems: 4};
    var permissionsParams = {"type": "repository", "repoKey": "libs-release-local", "path": "c.bin", pageNum: 1, numOfRows: 4, orderBy: 'principal', direction: 'asc'};

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactPermissionsDao, _RESOURCE_, $httpBackend) {
        artifactPermissionsDao = ArtifactPermissionsDao.getInstance();
        RESOURCE = _RESOURCE_;
        server = $httpBackend;
    }));

    afterEach(function() {
        server.flush();
    });

    it('query should send a GET request', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.ARTIFACT_PERMISSIONS + "?direction=asc&numOfRows=4&orderBy=principal&pageNum=1&path=c.bin&repoKey=libs-release-local&type=repository")
                .respond(mockPermissionsData);
        artifactPermissionsDao.query(permissionsParams);
    });
});
