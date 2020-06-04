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
import {AdminConfigurationRegisterController} from './register_pro.controller';

function registerProConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.register_pro', {
                params: {feature: 'register_pro'},
                url: '/artifactory_licenses',
                templateUrl: 'states/admin/configuration/register_pro/register_pro.html',
                controller: 'AdminConfigurationRegisterController as AdminConfigurationRegister'
            })
}

export default angular.module('configuration.register_pro', [])
        .config(registerProConfig)
        .controller('AdminConfigurationRegisterController', AdminConfigurationRegisterController);