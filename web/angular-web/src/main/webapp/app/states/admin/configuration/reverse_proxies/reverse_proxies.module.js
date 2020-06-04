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
import {AdminConfigurationReverseProxiesController} from './reverse_proxies.controller';
import {AdminConfigurationReverseProxyFormController} from './reverse_proxy_form.controller';

function reverseProxiesConfig($stateProvider) {
    $stateProvider
/*
            .state('admin.configuration.reverse_proxies', {
                params: {feature: 'ReverseProxies'},
                url: '/reverse_proxies',
                templateUrl: 'states/admin/configuration/reverse_proxies/reverse_proxies.html',
                controller: 'AdminConfigurationReverseProxiesController as AdminConfigurationReverseProxies'
            })
            .state('admin.configuration.reverse_proxies.new', {
                params: {feature: 'ReverseProxies'},
                parent: 'admin.configuration',
                url: '/reverse_proxies/new',
                templateUrl: 'states/admin/configuration/reverse_proxies/reverse_proxy_form.html',
                controller: 'AdminConfigurationReverseProxyFormController as ReverseProxyForm'
            })
            .state('admin.configuration.reverse_proxies.edit', {
                params: {feature: 'ReverseProxies'},
                parent: 'admin.configuration',
                url: '/reverse_proxies/:reverseProxyKey/edit',
                templateUrl: 'states/admin/configuration/reverse_proxies/reverse_proxy_form.html',
                controller: 'AdminConfigurationReverseProxyFormController as ReverseProxyForm'
            })
*/
            .state('admin.configuration.reverse_proxy', {
                params: {feature: 'ReverseProxies', reverseProxyKey: 'nginx'},
                parent: 'admin.configuration',
                url: '/reverse_proxy',
                templateUrl: 'states/admin/configuration/reverse_proxies/reverse_proxy_form.html',
                controller: 'AdminConfigurationReverseProxyFormController as ReverseProxyForm'
            })
}

export default angular.module('configuration.reverse_proxies', [])
        .config(reverseProxiesConfig)
        .controller('AdminConfigurationReverseProxiesController', AdminConfigurationReverseProxiesController)
        .controller('AdminConfigurationReverseProxyFormController', AdminConfigurationReverseProxyFormController);