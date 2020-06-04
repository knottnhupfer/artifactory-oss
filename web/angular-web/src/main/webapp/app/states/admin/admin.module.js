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
import Configuration from './configuration/configuration.module';
import Repositories from './repositories/repositories.module';
import Advanced from './advanced/advanced.module';
import Dashboard from './dashboard/dashboard.module';
import ImportExport from './import_export/import_export.module';
import Security from './security/security.module';
import Services from './services/admin.services.module';

import {AdminController} from './admin.controller';

function adminConfig($stateProvider) {
    $stateProvider
            .state('admin', {
                url: '/admin',
                parent: 'app-layout',
                templateUrl: "states/admin/admin.html",
                controller: 'AdminController as Admin'
            })
}

export default angular.module('admin.module', [
    Configuration.name,
    Repositories.name,
    Advanced.name,
    Dashboard.name,
    ImportExport.name,
    Security.name,
    Services.name
])
        .config(adminConfig)
        .controller('AdminController', AdminController)