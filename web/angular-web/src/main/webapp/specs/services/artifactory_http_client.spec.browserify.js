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
describe('Unit test: ArtifactoryHttpClient', () => {
	let httpBackend;
	let ArtifactoryHttpClient;
	let data = {};
	beforeEach(m('artifactory.services'));
	function setup($httpBackend, RESOURCE, _ArtifactoryHttpClient_) {
		httpBackend = $httpBackend;
		RESOURCE.API_URL = '/base';
		ArtifactoryHttpClient = _ArtifactoryHttpClient_;
	}
	beforeEach(inject(setup));
	describe('post', () => {
		it('should make a post request', () => {
			httpBackend.expectPOST('/base/path', data, {'Content-Type': 'application/json'});
			ArtifactoryHttpClient.post('/path', data);
		});

		it('should allow to extend the headers', () => {
			httpBackend.expectPOST('/base/path', data, {'Content-Type': 'application/json', 'X-Custom1': 'value'});
			ArtifactoryHttpClient.post('/path', data, {headers: {'X-Custom1': 'value'}});
		});
	});

	describe('put', () => {
		it('should make a put request', () => {
			httpBackend.expectPUT('/base/path', data, {'Content-Type': 'application/json'});
			ArtifactoryHttpClient.put('/path', data);
		});

		it('should allow to extend the headers', () => {
			httpBackend.expectPUT('/base/path', data, {'Content-Type': 'application/json', 'X-Custom1': 'value'});
			ArtifactoryHttpClient.put('/path', data, {headers: {'X-Custom1': 'value'}});
		});
	});

	describe('get', () => {
		it('should make a get request', () => {
			httpBackend.expectGET('/base/path', {'Content-Type': 'application/json'});
			ArtifactoryHttpClient.get('/path');
		});

		it('should allow to extend the headers', () => {
			httpBackend.expectGET('/base/path', {'Content-Type': 'application/json', 'X-Custom1': 'value'});
			ArtifactoryHttpClient.get('/path', {headers: {'X-Custom1': 'value'}});
		});
	});
});