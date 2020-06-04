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
import {AdminAdvancedConfigDescriptorController} from './config_descriptor.controller';

function configDescriptorConfig($stateProvider) {

    $stateProvider
            .state('admin.advanced.config_descriptor', {
                params: {feature: 'configDescriptor'},
                url: '/config_descriptor',
                templateUrl: 'states/admin/advanced/config_descriptor/config_descriptor.html',
                controller: 'AdminAdvancedConfigDescriptorController as ConfigDescriptorController'
            })
}

export default angular.module('advanced.config_descriptor', [])
        .config(configDescriptorConfig)
        .controller('AdminAdvancedConfigDescriptorController', AdminAdvancedConfigDescriptorController);