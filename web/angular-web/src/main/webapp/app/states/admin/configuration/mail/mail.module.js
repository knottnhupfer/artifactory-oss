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
import {AdminConfigurationMailController} from './mail.controller';

function mailConfig($stateProvider) {
    $stateProvider
            .state('admin.configuration.mail', {
                params: {feature: 'mail'},
                url: '/mail',
                templateUrl: 'states/admin/configuration/mail/mail.html',
                controller: 'AdminConfigurationMailController as AdminConfigurationMail'
            })
}

export default angular.module('configuration.mail', [])
        .config(mailConfig)
        .controller('AdminConfigurationMailController', AdminConfigurationMailController)