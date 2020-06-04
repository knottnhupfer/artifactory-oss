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
window.m = angular.mock.module;
window.compileHtml = function(htmlStr, data) {
    data = data || {};
    var $scope;
    inject(function($compile, $rootScope) {
      angular.element(document.body).html(htmlStr);
      $scope = $rootScope.$new();
      angular.extend($scope, data);
      $compile(document.body)($scope);
      $scope.$digest();
    });
    return $scope;
}

function angularEquality(first, second) {
  return angular.equals(first, second);
}

window.useAngularEquality = function() {
    beforeEach(function() {
      jasmine.addCustomEqualityTester(angularEquality);
    });
}