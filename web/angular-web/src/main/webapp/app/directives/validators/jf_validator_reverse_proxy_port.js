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
 * Validates an input to be valid entiyy name
 */
export function jfValidatorReverseProxyPort(ReverseProxiesDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfValidatorReverseProxyPort(scope, element, attrs, ngModel) {

            let repoKey = attrs.jfValidatorReverseProxyPort;

            let portValidatorDao = ReverseProxiesDao;
            ngModel.$asyncValidators.port = validatePort;

            function validatePort(modelValue, viewValue) {
                var value = modelValue || viewValue;

                if (!value) {
                    return $q.when();
                }

                return ReverseProxiesDao.checkPort(repoKey ? {repoKey: repoKey} : {},{port: value}).$promise
                    .then(function (result) {
                        if (!result.portAvailable) {
                            return $q.reject();
                        }
                        return true;
                    });
            }
        }
    }
}