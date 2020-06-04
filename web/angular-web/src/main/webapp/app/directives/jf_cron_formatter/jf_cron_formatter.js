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
export function jfCronFormatter(CronTimeDao, $q, $timeout) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function jfCronFormatterLink(scope, element, attrs, ngModel) {


            let cronTimeDao = CronTimeDao.getInstance();
            ngModel.$formatters.push(formatCron);
            ngModel.$parsers.push(input => ngModel.$modelValue);

            // Format the next scheduled time in the server
            function formatCron(input) {
                if (input) {
                    return cronTimeDao.get({cron: input}).$promise
                        .then(function (result) {
                            if (result.error) {
                                return $q.reject();
                            }

                            ngModel.$viewValue = result.nextTime;
                            ngModel.$render();
                            return ngModel.$viewValue;
                        });
                }

                return input;
            }
        }
    }
}