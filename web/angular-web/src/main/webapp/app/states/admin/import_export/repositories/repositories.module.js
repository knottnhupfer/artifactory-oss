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
import {ImportExportRepositoriesController} from './repositories.controller';


function repositoriesConfig($stateProvider) {
    $stateProvider
            .state('admin.import_export.repositories', {
                params: {feature: 'repositories'},
                url: '/repositories',
                templateUrl: 'states/admin/import_export/repositories/repositories.html',
                controller: 'ImportExportRepositoriesController as Repositories'
            })
}

export default angular.module('import_export.repositories', [])
        .config(repositoriesConfig)
        .controller('ImportExportRepositoriesController', ImportExportRepositoriesController)

