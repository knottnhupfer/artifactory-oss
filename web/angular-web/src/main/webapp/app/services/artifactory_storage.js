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
/**
 * wrapper around the HTML5 local storage API.
 * support JSON serialization de-serialization.
 *
 */
let storage;
export class ArtifactoryStorage {

    constructor($window) {
        storage = $window.localStorage;
    }

    setItem(key, item) {
        try {
            storage.setItem(key, JSON.stringify(item));
            return this.getItem(key);
        }
        catch (e) {
            console.log(e)
        }
    }

    getItem(key, defaultValue = null) {
        try {
            let itemStr = storage.getItem(key);
            if (itemStr) {
                return JSON.parse(itemStr);
            }
            else {
                return defaultValue;
            }
        }
        catch (e) {
            console.log(e)
        }
    }

    removeItem(key) {
        storage.removeItem(key);
    }

    isLocalStorageNameSupported() {
        let testKey = 'test', storage = window.sessionStorage;
        try {
            storage.setItem(testKey, '1');
            storage.removeItem(testKey);
            return localStorageName in win && win[localStorageName];
        }
        catch (error) {
            return false;
        }
    }
}