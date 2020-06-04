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
import {ChangePasswordController} from './change_password.controller';

function changePasswordConfig ($stateProvider) {
    $stateProvider
            .state('change-password', {
                url: '/change-password',
                templateUrl: 'states/change_password/change_password.html',
                controller: 'ChangePasswordController as ChangePassword',
                parent: 'login-layout',
                params: {username: ''},
            })
}

export default angular.module('login', [])
        .config(changePasswordConfig)
        .controller('ChangePasswordController', ChangePasswordController);