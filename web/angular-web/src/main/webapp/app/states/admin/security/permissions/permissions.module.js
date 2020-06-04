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
import {AdminSecurityPermissionsController} from './permissions.controller_new'
import {AdminSecurityPermissionsFormController} from './permissons_form.controller'

function permissionsConfig($stateProvider) {

    $stateProvider
        .state('admin.security.permissions', {
            url: '/permissions',
            templateUrl: 'states/admin/security/permissions/permissions_new.html',
            controller: 'AdminSecurityPermissionsController as Permissions'
        })
        .state('admin.security.permissions.edit', {
            parent: 'admin.security',
            url: '/permissions/{permission}/edit',
            templateUrl: 'states/admin/security/permissions/permission_form.html',
            controller: 'AdminSecurityPermissionsFormController as PermissionForm'
        })
        .state('admin.security.permissions.new', {
            parent: 'admin.security',
            url: '/permission/new',
            templateUrl: 'states/admin/security/permissions/permission_form.html',
            controller: 'AdminSecurityPermissionsFormController as PermissionForm'
        })
}

export default angular.module('security.permissions', [])
    .config(permissionsConfig)
    .controller('AdminSecurityPermissionsController', AdminSecurityPermissionsController)
    .controller('AdminSecurityPermissionsFormController', AdminSecurityPermissionsFormController)