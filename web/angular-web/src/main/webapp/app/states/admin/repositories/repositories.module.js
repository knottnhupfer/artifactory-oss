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
import {AdminRepositoriesController} from './repositories.controller';
import {AdminRepositoryFormController} from './repository_form.controller';
import {VirtualRepositoryFormController} from './virtual_repository_form.controller';
import {AdminRepositoriesLayoutController} from './repositories_layouts.controller';
import {AdminRepositoryLayoutFormController} from './repository_layout_form.controller';

function repositoriesConfig($stateProvider) {
    $stateProvider
        // base state
        .state('admin.repositories', {
            url: '',
            abstract: true,
            template: '<ui-view></ui-view>'
        })

        // repository list and forms
        .state('admin.repositories.list', {
            url: '/repositories/{repoType}',
            templateUrl: 'states/admin/repositories/repositories.html',
            controller: 'AdminRepositoriesController as Repositories',
	        params: {
		        action: null
	        }
        })
        .state('admin.repositories.list.edit', {
            parent: 'admin.repositories',
            url: '/repository/{repoType}/{repoKey}/{distRepoType}/edit',
            templateUrl: 'states/admin/repositories/repository_form.html',
            controller: 'AdminRepositoryFormController as RepositoryForm',
            params: {
	            distRepoType: {
		            squash: true,
		            value: null
	            }
            }
        })
        .state('admin.repositories.list.new', {
	        parent: 'admin.repositories',
	        url: '/repository/{repoType}/{distRepoType}/new',
	        templateUrl: 'states/admin/repositories/repository_form.html',
	        controller: 'AdminRepositoryFormController as RepositoryForm',
	        params: {
		        distRepoType: {
			        squash: true,
			        value: null
		        }
	        }
        })

        // repository layout list and forms
        .state('admin.repositories.repo_layouts', {
            url: '/repo_layouts',
            templateUrl: 'states/admin/repositories/repositories_layouts.html',
            controller: 'AdminRepositoriesLayoutController as RepositoriesLayoutController'
        })
        .state('admin.repositories.repo_layouts.edit', {
            parent: 'admin.repositories',
            url: '/repo_layouts/{layoutname}/edit',
            templateUrl: 'states/admin/repositories/repository_layout_form.html',
            controller: 'AdminRepositoryLayoutFormController as RepositoryLayoutForm',
            params: {viewOnly: true}
        })
        .state('admin.repositories.repo_layouts.new', {
            parent: 'admin.repositories',
            url: '/repo_layouts/new?copyFrom',
            templateUrl: 'states/admin/repositories/repository_layout_form.html',
            controller: 'AdminRepositoryLayoutFormController as RepositoryLayoutForm'
        })

}

export default angular.module('admin.repositories', [])
    .config(repositoriesConfig)
    .controller('AdminRepositoriesController', AdminRepositoriesController)
    .controller('AdminRepositoryFormController', AdminRepositoryFormController)
    .controller('VirtualRepositoryFormController', VirtualRepositoryFormController)
    .controller('AdminRepositoriesLayoutController', AdminRepositoriesLayoutController)
    .controller('AdminRepositoryLayoutFormController', AdminRepositoryLayoutFormController);



