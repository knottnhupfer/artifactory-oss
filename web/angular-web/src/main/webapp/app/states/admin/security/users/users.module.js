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
import {AdminSecurityUserController} from './users.controller';
import {AdminSecurityUserFormController} from './user_form.controller';

function usersConfig($stateProvider) {

    $stateProvider
        .state('admin.security.users', {
            url: '/users',
            templateUrl: 'states/admin/security/users/users.html',
            controller: 'AdminSecurityUserController as AdminSecurityUser'
        })
        .state('admin.security.users.edit', {
            parent: 'admin.security',
            url: '/users/{username}/edit',
            templateUrl: 'states/admin/security/users/user_form.html',
            controller: 'AdminSecurityUserFormController as UserForm'
        })
        .state('admin.security.users.new', {
            parent: 'admin.security',
            url: '/users/new',
            templateUrl: 'states/admin/security/users/user_form.html',
            controller: 'AdminSecurityUserFormController as UserForm'
        })

}

export default angular.module('security.users', [])
        .config(usersConfig)
    .controller('AdminSecurityUserController', AdminSecurityUserController)
    .controller('AdminSecurityUserFormController', AdminSecurityUserFormController);