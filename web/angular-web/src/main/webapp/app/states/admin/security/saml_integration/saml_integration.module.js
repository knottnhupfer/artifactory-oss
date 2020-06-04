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
import {AdminSecuritySamlIntegrationController} from './saml_integration.controller';

function samlIntegrationConfig($stateProvider) {

    $stateProvider
            .state('admin.security.saml_integration', {
                params: {feature: 'samlsso'},
                url: '/saml_integration',
                templateUrl: 'states/admin/security/saml_integration/saml_integration.html',
                controller: 'AdminSecuritySamlIntegrationController as AdminSecuritySamlIntegration'
            })
}

export default angular.module('security.saml_integration', [])
        .config(samlIntegrationConfig)
        .controller('AdminSecuritySamlIntegrationController', AdminSecuritySamlIntegrationController);