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
export function PropertyMock(data = {}) {
  let defaults = {
    name: faker.name.firstName(),
    propertyType: "ANY_VALUE",
    predefinedValues: [
      {value: 'value1', defaultValue: false},
      {value: 'value2', defaultValue: true}
    ]
  };
  data = angular.extend(defaults, data);

  return data;
}

export function PropertySetMock(data = {}) {
  let defaults = {
    name: faker.name.firstName(),
    properties: [new PropertyMock(), new PropertyMock()]
  };
  data = angular.extend(defaults, data);

  return data;
}

