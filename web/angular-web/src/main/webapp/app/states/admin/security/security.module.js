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
import General from './general/general.module';
import Groups from './groups/groups.module';
import HttpSso from './http_sso/http_sso.module';
import SshServer from './ssh_server/ssh_server.module';
import Permissions from './permissions/permissions.module';
import Users from './users/users.module';
import Saml from './saml_integration/saml_integration.module';
import CrowdIntegration from './crowd_integration/crowd_integration.module';
import OAuth from './oauth/oauth.module';
import LdapSettings from './ldap_settings/ldap_settings.module';
import SigningKeys from './signing_keys/signing_keys.module';
import TrustedKeys from './trusted_keys/trusted_keys.module';
import AccessTokens from './access_tokens/access_tokens.module';
import SslCertificates from './ssl_certificates/ssl_certificates.module';

import {AdminSecurityController} from './security.controller';

function securityConfig($stateProvider) {
    $stateProvider
            .state('admin.security', {
                url: '/security',
                template: '<ui-view></ui-view>',
                controller: 'AdminSecurityController as AdminSecurity'
            })
}


export default angular.module('admin.security', [
    General.name,
    Groups.name,
    HttpSso.name,
    SshServer.name,
    Permissions.name,
    Users.name,
    Saml.name,
    CrowdIntegration.name,
    OAuth.name,
    LdapSettings.name,
    SigningKeys.name,
    TrustedKeys.name,
    AccessTokens.name,
    SslCertificates.name
])
        .config(securityConfig)
        .controller('AdminSecurityController', AdminSecurityController);