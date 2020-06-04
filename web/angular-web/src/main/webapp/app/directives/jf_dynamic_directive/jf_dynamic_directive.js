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
export default function ($compile) {
    return {
        link: function ($scope, $element, attrs) {
            let newElement;
            let directiveName;
            newElement = $element.clone();
            directiveName = $scope.$eval(attrs.dynamicDirective);
            newElement.removeAttr('dynamic-directive');
            newElement.attr(directiveName, '');                       // assign the directive to it
            newElement = $compile(newElement)($scope);                // compile it
            $element.replaceWith(newElement);                         // replace the original element
        }
    }
};