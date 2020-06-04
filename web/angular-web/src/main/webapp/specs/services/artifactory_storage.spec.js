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
describe('Unit: ArtifactoryStorage Service', function () {

    var artifactoryStorage;
    var TEST_KEY = 'TEST';
    var ITEM = {uid: 2582};

    // inject the main module
    beforeEach(m('artifactory.services'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        artifactoryStorage = $injector.get('ArtifactoryStorage');
    }));


    it('artifactoryStorage should be defined', function () {
        expect(artifactoryStorage).toBeDefined();
    });

    it('should save and retrieve an object from storage', function () {
        expect(artifactoryStorage.setItem(TEST_KEY,ITEM)).toEqual(  ITEM)
    });

    it('should be able to remove an item by key', function () {
        expect(artifactoryStorage.setItem(TEST_KEY,ITEM)).toEqual(ITEM);
        artifactoryStorage.removeItem(TEST_KEY);
        expect(artifactoryStorage.getItem(TEST_KEY)).toBeNull();
    });

    it('should return default value if not exist', function () {
        expect(artifactoryStorage.getItem(TEST_KEY, 10)).toBe(10);
    });

    it('should not return default value if exists', function () {
        artifactoryStorage.setItem(TEST_KEY,ITEM);
        expect(artifactoryStorage.getItem(TEST_KEY, 10)).toEqual(ITEM);
    });

});