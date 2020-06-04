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
 * Validates an input to be unique id in the config descriptor
 */
export function jfValidatorUniqueId(UniqueIdValidatorDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorUniqueId(scope, element, attrs, ngModel) {

            let uniqueIdDao = UniqueIdValidatorDao.getInstance();
            ngModel.$asyncValidators.uniqueId = validateUniqueId;

            function validateUniqueId(modelValue, viewValue) {
                // Don't validate disabled fields
                if ($(element).is(':disabled')) return $q.when();

                var value = modelValue || viewValue;

                if (!value) return $q.when();

                return uniqueIdDao.get({id: value}).$promise
                    .then(function (result) {
                        if (result.error) {
                            return $q.reject();
                        }
                        return true;
                    });
            }
        }
    }
}