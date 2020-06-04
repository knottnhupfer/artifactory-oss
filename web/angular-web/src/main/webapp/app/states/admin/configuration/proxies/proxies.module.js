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
import {AdminConfigurationProxiesController} from './proxies.controller';
import {AdminConfigurationProxyFormController} from './proxy_form.controller';

function proxiesConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.proxies', {
                params: {feature: 'Proxies'},
                url: '/proxies',
                templateUrl: 'states/admin/configuration/proxies/proxies.html',
                controller: 'AdminConfigurationProxiesController as AdminConfigurationProxies'
            })
            .state('admin.configuration.proxies.new', {
                params: {feature: 'Proxies'},
                parent: 'admin.configuration',
                url: '/proxies/new',
                templateUrl: 'states/admin/configuration/proxies/proxy_form.html',
                controller: 'AdminConfigurationProxyFormController as ProxyForm'
            })
            .state('admin.configuration.proxies.edit', {
                params: {feature: 'Proxies'},
                parent: 'admin.configuration',
                url: '/proxies/:proxyKey/edit',
                templateUrl: 'states/admin/configuration/proxies/proxy_form.html',
                controller: 'AdminConfigurationProxyFormController as ProxyForm'
            })
}

export default angular.module('configuration.proxies', [])
        .config(proxiesConfig)
        .controller('AdminConfigurationProxiesController', AdminConfigurationProxiesController)
        .controller('AdminConfigurationProxyFormController', AdminConfigurationProxyFormController);