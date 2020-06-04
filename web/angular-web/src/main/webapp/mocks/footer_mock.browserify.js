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
class FooterMock {
  constructor() {
    this.mockData = {
      isAol: false,
      versionID: 'OSS'
    };
    beforeEach(inject(($httpBackend, RESOURCE) => {
      $httpBackend.whenGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(() => {
        return [200, this.mockData];
      });
    }));
  }

  mockOss() {
    this.mockData.versionID = 'OSS';
    return this;
  };
  mockPro() {
    this.mockData.versionID = 'PRO';
    return this;
  };
  mockEnt() {
    this.mockData.versionID = 'ENT';
    return this;
  };
  mockAol() {
    this.mockData.isAol = true;
    return this;
  };
}

export default FooterMock;