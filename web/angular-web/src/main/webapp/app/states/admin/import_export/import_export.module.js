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
import Repositories from './repositories/repositories.module';
import System from './system/system.module';

import {AdminImportExportController} from './import_export.controller';

function importExportConfig($stateProvider) {
    $stateProvider
            .state('admin.import_export', {
                url: '/import_export',
                template: '<ui-view></ui-view>',
                controller: 'AdminImportExportController as AdminImportExport'
            })
}

export default angular.module('admin.import_export', [
    Repositories.name,
    System.name
])
        .config(importExportConfig)
        .controller('AdminImportExportController', AdminImportExportController);

