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
import {AdminConfigurationPropertySetsController} from './property_sets.controller';
import {AdminConfigurationPropertySetFormController} from './property_set_form.controller';
import {PropertyFormModalFactory} from './property_form_modal';

function propertySetsConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.property_sets', {
                params: {feature: 'properties'},
                url: '/property_sets',
                controller: 'AdminConfigurationPropertySetsController as PropertySets',
                templateUrl: 'states/admin/configuration/property_sets/property_sets.html'
            })
            .state('admin.configuration.property_sets.edit', {
                params: {feature: 'properties'},
                parent: 'admin.configuration',
                url: '/property_sets/{propertySetName}/edit',
                templateUrl: 'states/admin/configuration/property_sets/property_set_form.html',
                controller: 'AdminConfigurationPropertySetFormController as PropertySetForm'
            })
            .state('admin.configuration.property_sets.new', {
                params: {feature: 'properties'},
                parent: 'admin.configuration',
                url: '/property_sets/new',
                templateUrl: 'states/admin/configuration/property_sets/property_set_form.html',
                controller: 'AdminConfigurationPropertySetFormController as PropertySetForm'
            })
}

export default angular.module('configuration.property_sets', [])
        .config(propertySetsConfig)
        .controller('AdminConfigurationPropertySetsController', AdminConfigurationPropertySetsController)
        .controller('AdminConfigurationPropertySetFormController', AdminConfigurationPropertySetFormController)
        .factory('PropertyFormModal', PropertyFormModalFactory)