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
import fieldsValuesDictionary from "../../constants/field_options.constats";

export function jfRepokeyValidator(RepositoriesDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        scope:{
            controller:'=jfRepokeyValidator'
        },
        link: function jfRepokeyValidatorLink(scope, element, attrs, ngModel) {


            ngModel.$asyncValidators.repoKeyValidator = validateRepoKey;

            function validateRepoKey(modelValue, viewValue) {
                let repoKey = modelValue || viewValue;
                let repoType = scope.controller.repoType;

                if (repoType === 'distribution' && scope.controller.$stateParams.distRepoType === 'ReleaseBundles') repoType = 'releaseBundles';

                if (!repoKey) return $q.when();

                return RepositoriesDao.repoKeyValidator({repoKey, repoType}).$promise
                    .then(function (result) {
                        if (result.error) {
                            scope.controller.repoKeyValidatorMessage = result.error;
                            return $q.reject();
                        }
                        else if (scope.controller.repoInfo.isType('docker') && repoKey.toLowerCase() !== repoKey) {
                            scope.controller.repoKeyValidatorMessage = 'Docker repository key must be in lowercase';
                            return $q.reject();
                        }
                        else if (scope.controller.repoInfo.type === 'localRepoConfig' && repoKey.toLowerCase().endsWith('-cache')) {
                            scope.controller.repoKeyValidatorMessage = 'Cannot create local repository with "-cache" ending';
                            return $q.reject();
                        }
                        return true;
                    });
            }
        }
    }
}