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
import {AdminConfigurationXrayController} from './xray.controller';

function xrayConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.xray', {
                params: {feature: 'xray'},
                url: '/xray',
                templateUrl: 'states/admin/configuration/xray/xray.html',
                controller: 'AdminConfigurationXrayController as AdminConfigurationXray'
            })
}

export default angular.module('configuration.xray', [])
        .config(xrayConfig)
        .controller('AdminConfigurationXrayController', AdminConfigurationXrayController)