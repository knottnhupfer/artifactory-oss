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
angular.module('artifactory.mocks', [])
	.run(function($window) {
		var store = {};
		spyOn($window.localStorage, 'getItem').and.callFake((key) => {
			return store[key];
		});
		spyOn($window.localStorage, 'setItem').and.callFake((key, value) => {
			return store[key] = value;
		});
		spyOn($window.localStorage, 'removeItem').and.callFake((key) => {
			delete store[key];
		});
	});

// The mock function loads the above module before each
export default function mock() {
	beforeEach(m('artifactory.mocks'));
}