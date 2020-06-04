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
var faker = require('faker');
class UserMock {
  constructor(data = {}) {
    let defaults = {
      name: faker.name.firstName(),
      admin: true,
      profileUpdatable: true,
      internalPasswordDisabled: false,
      canManage: true,
      canDeploy: true,
      preventAnonAccessBuild: true
    };
    data = angular.extend(defaults, data);

    angular.copy(data, this);
  }

  mockUserMethods() {
    inject((User, $q) => {
      let user = new User(this);
      spyOn(User, 'getCurrent').and.returnValue(user);
      spyOn(User, 'loadUser').and.returnValue($q.when(user));
    });
  }
  getUserObj() {
    let user;
    inject((User) => {
      user = new User(this);
    });    
    return user;
  }

  static regularUser(data) {
    data = data || {};
    data.admin = false;
    return new UserMock(data);
  };
  static guest() {
    return new UserMock({name: 'anonymous', admin: false, canManage: false, canDeploy: false, preventAnonAccessBuild: true});
  };
  static mockCurrentUser() {
    inject(($httpBackend, RESOURCE) => {
      $httpBackend.whenGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(this.guest());
    });
  }
}

export default UserMock;