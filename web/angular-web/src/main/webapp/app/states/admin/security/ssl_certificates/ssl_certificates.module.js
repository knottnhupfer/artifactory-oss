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
/**
 * Created by tomere on 07/06/2017.
 */
import {SslCertificatesController} from './ssl_certificates.controller';

function sslCertificatesConfig($stateProvider) {

    $stateProvider
            .state('admin.security.ssl_certificates', {
                params: {feature: 'sslcertificates'},
                url: '/ssl_certificates',
                templateUrl: 'states/admin/security/ssl_certificates/ssl_certificates.html',
                controller: 'SslCertificatesController as SslCertificates'
            });
}

export default angular.module('security.ssl_certificates', [])
        .config(sslCertificatesConfig)
        .controller('SslCertificatesController', SslCertificatesController);