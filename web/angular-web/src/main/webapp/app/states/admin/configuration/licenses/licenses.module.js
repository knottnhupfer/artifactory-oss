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
import {AdminConfigurationLicensesController} from './licenses.controller';
import {AdminConfigurationLicenseFormController} from './license_form.controller'

function licensesConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.licenses', {
                url: '/licenses',
                params: {feature: 'licenses'},
                templateUrl: 'states/admin/configuration/licenses/licenses.html',
                controller: 'AdminConfigurationLicensesController as AdminConfigurationLicenses'
            })
            .state('admin.configuration.licenses.edit', {
                parent: 'admin.configuration',
                url: '/licenses/{licenseName}/edit',
                params: {feature: 'licenses'},
                templateUrl: 'states/admin/configuration/licenses/license_form.html',
                controller: 'AdminConfigurationLicenseFormController as AdminLicenseForm'
            })
            .state('admin.configuration.licenses.new', {
                parent: 'admin.configuration',
                url: '/licenses/new',
                params: {feature: 'licenses'},
                templateUrl: 'states/admin/configuration/licenses/license_form.html',
                controller: 'AdminConfigurationLicenseFormController as AdminLicenseForm'
            })
}


export default angular.module('configuration.licenses', ['ui.router'])
        .config(licensesConfig)
        .controller('AdminConfigurationLicensesController', AdminConfigurationLicensesController)
        .controller('AdminConfigurationLicenseFormController', AdminConfigurationLicenseFormController);        