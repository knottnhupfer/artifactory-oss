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
export function jfValidatorMaxTextLength() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfLimitTextLengthValidator(scope, element, attrs, ngModel) {

            let limitTo = attrs.maxlength || attrs.jfValidatorMaxTextLength;

            registerTransformers();

            function registerTransformers() {
                ngModel.$validators.maxlength = validateTextLength;
            }

            function validateTextLength(modelValue, viewValue) {
                let ok  = !viewValue || viewValue.length <= parseInt(limitTo);
                return ok;
            }
        }
    }
}