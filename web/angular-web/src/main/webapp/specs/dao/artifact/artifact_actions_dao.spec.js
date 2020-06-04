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
describe('unit test:artifact actions dao', function () {

    var artifactActionsDao;
    var RESOURCE;
    var server;
    var artifactData = {"repoKey": "libs-release-local", "path": "c/d.bin"};
    var artifactParams = {};

    // inject the main module
    beforeEach(m('jfrog.ui.essentials', 'artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactActionsDao, _RESOURCE_, _$httpBackend_) {
        artifactActionsDao = ArtifactActionsDao;
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
    }));
    afterEach(function() {
        server.flush();
    });

    //// Generic perform function:
    it('perform should send the correct POST request', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_ACTIONS + '/someAction?searchKey=searchValue').respond(200);
        artifactParams.action = 'someAction';
        artifactParams.searchKey = 'searchValue';
        artifactActionsDao.perform(artifactParams, artifactData);
    });
    it('perform should send the correct POST request for dry run', function () {
        server.expectPOST(RESOURCE.API_URL + RESOURCE.ARTIFACT_ACTIONS + '/someAction?searchKey=searchValue').respond(200);
        artifactParams.action = 'someAction';
        artifactParams.searchKey = 'searchValue';
        artifactActionsDao.dryRun(artifactParams, artifactData);
    });
});