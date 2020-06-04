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
import {AdminSecurityGroupsController} from './group.controller';
import {AdminSecurityGroupFormController} from './group_form.controller';

function groupsConfig($stateProvider) {

    $stateProvider
        .state('admin.security.groups', {
            url: '/groups',
            templateUrl: 'states/admin/security/groups/groups.html',
            controller: 'AdminSecurityGroupsController as AdminSecurityGroups'
        })
        .state('admin.security.groups.edit', {
            parent: 'admin.security',
            url: '/groups/{groupname}/edit',
            templateUrl: 'states/admin/security/groups/group_form.html',
            controller: 'AdminSecurityGroupFormController as GroupForm'
        })
        .state('admin.security.groups.new', {
            parent: 'admin.security',
            url: '/groups/new',
            templateUrl: 'states/admin/security/groups/group_form.html',
            controller: 'AdminSecurityGroupFormController as GroupForm'
        })

}

export default angular.module('security.groups', [])
        .config(groupsConfig)
        .controller('AdminSecurityGroupsController', AdminSecurityGroupsController)
    .controller('AdminSecurityGroupFormController', AdminSecurityGroupFormController)