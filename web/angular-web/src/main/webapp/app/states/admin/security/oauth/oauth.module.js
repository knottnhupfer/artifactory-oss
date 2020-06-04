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
import {AdminSecurityOAuthController} from './oauth.controller';
import {AdminSecurityOAuthProviderFormController} from './oauth_provider_form.controller';

function oauthConfig($stateProvider) {

    $stateProvider
            .state('admin.security.oauth', {
                params: {feature: 'oauthsso'},
                url: '/oauth',
                templateUrl: 'states/admin/security/oauth/oauth.html',
                controller: 'AdminSecurityOAuthController as AdminSecurityOAuth'
            })
            .state('admin.security.oauth.edit', {
                params: {feature: 'oauthsso'},
                parent: 'admin.security',
                url: '/oauth/{providerName}/edit',
                templateUrl: 'states/admin/security/oauth/oauth_provider_form.html',
                controller: 'AdminSecurityOAuthProviderFormController as ProviderForm'
            })
            .state('admin.security.oauth.new', {
                params: {feature: 'oauthsso'},
                parent: 'admin.security',
                url: '/oauth/newprovider',
                templateUrl: 'states/admin/security/oauth/oauth_provider_form.html',
                controller: 'AdminSecurityOAuthProviderFormController as ProviderForm'
            })

}

export default angular.module('security.oauth', [])
        .config(oauthConfig)
        .controller('AdminSecurityOAuthController', AdminSecurityOAuthController)
        .controller('AdminSecurityOAuthProviderFormController', AdminSecurityOAuthProviderFormController);