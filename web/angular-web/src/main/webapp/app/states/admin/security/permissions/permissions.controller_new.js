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

import { assign } from 'lodash';

export class AdminSecurityPermissionsController {
    constructor($scope, $state, JFrogGridFactory, PermissionsDao, JFrogModal, uiGridConstants, User, commonGridColumns,
            JFrogTableViewOptions, $rootScope) {
        this.$state = $state;
        this.currentTab = 'repo';
        this.modal = JFrogModal;
        this.permissionsDao = PermissionsDao.getInstance();
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.commonGridColumns = commonGridColumns;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.uiGridConstants = uiGridConstants;
        this.JFrogTableViewOptions = JFrogTableViewOptions;
        this.user = User.getCurrent();
        this._createGridNew();
        // this.initPermission();
        this.sortState = {};
    }


    _createGridNew() {
        const paginationCallback = ({offset, numOfRows}) => {
            if (this.sorting) {
                this.continueState = null;
                this.sorting = false;
            }
            const queryParams = assign({}, {limit: 50, continueState: this.continueState}, this.sortState);
            return this.permissionsDao
                    .getAll(queryParams)
                    .$promise
                    .then((response => {
                        this.continueState = response.continueState;
                        this.showNoDataMessage = !response.data.length;
                        return assign({}, response, {hasMore: !!response.continueState});
                    }))

        };

        const externalSortCallback = (orderBy, direction) => {
            this.sortState.direction = direction.toUpperCase();
            this.sorting = true;
        };


        this.tableViewOptions = new this.JFrogTableViewOptions(this.$scope);
        this.tableViewOptions.setColumns(this._getColumnsNew())
                .setRowsPerPage('auto')
                .setSelection(this.tableViewOptions.MULTI_SELECTION)
                .setActions(this._getActions())
                .setBatchActions(this._getBatchActions())
                .setEmptyTableText('No permissions')
                .setPaginationMode(this.tableViewOptions.INFINITE_VIRTUAL_SCROLL, paginationCallback)
                .setSortable(true)
                .setNewEntityAction(() => {
                    //TODO: is admin
                    if (this.user.isAdmin()) {
                        return this.$state.go('^.permissions.new');
                    } else {
                        return null;
                    }


                })
                .useExternalSortCallback(externalSortCallback);


        // this.tableViewOptions.isRowSelectable = (row) => {
        //     return row.entity.canDelete;
        // }
    }

    onFilterChange() {
        this.continueState = null;
        this.sortState.searchStr = this.filter;
        this.tableViewOptions.dirCtrl.vsApi.reset();
        this.tableViewOptions.setData([]);
        this.tableViewOptions.sendInfiniteScrollRequest();
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this._deletePermission(row),
                visibleWhen: () => this.user.isAdmin()
            }
        ]
    }

    editPermission(row) {
        this.$state.go('^.permissions.edit', {permission: row.name})
    }

    _deletePermission(row) {
        let json = {permissionTargetNames: [row.name]};
        this.modal.confirm(`Are you sure you want to delete permission '${row.name}?'`)
                .then(() => this.permissionsDao.deletePermission(json).$promise.then(() => this.onFilterChange()));
    }

    bulkDelete() {
        let selectedRows = this.tableViewOptions.getSelectedRows()
        let names = _.map(selectedRows, (row) => {
            return row.name;
        });
        let json = {permissionTargetNames: names};
        this.modal.confirm(`Are you sure you want to delete ${names.length} permissions?`).then(
                () => this.permissionsDao.deletePermission(json).$promise.then(() => this.onFilterChange()));
    }

    _getColumnsNew() {

        const resourcesCellTemplate = `<div class="resource-wrapper">
                                           <span ng-if="row.entity.hasBuilds">Builds</span>
                                           <span ng-if="row.entity.hasRepos && row.entity.hasBuilds">,</span>
                                           <span ng-if="row.entity.hasRepos">Repositories</span>
                                           <span ng-if="row.entity.hasRepos || row.entity.hasBuilds"><span ng-click="grid.appScope.Permissions.showResources(row.entity)" class="jf-link">(See All)</span></span>
                                      </div>`;
        return [
            {
                header: 'Permission Target Name',
                field: 'name',
                cellTemplate: '<div class="ui-grid-cell-contents"><a href class="jf-link" ng-click="grid.appScope.Permissions.editPermission(row.entity)">{{row.entity.name}}</a></div>',
                filterable: true
            },
            {
                header: 'Resources',
                field: '',
                cellTemplate: resourcesCellTemplate,
                sortable: false,
                filterable: false
            },
            {
                header: 'Groups',
                field: 'groups',
                cellTemplate: this.JFrogTableViewOptions.cellTemplateGenerators.listableColumn(
                        'row.entity.groups',
                        'row.entity.name',
                        null,
                        false,
                        null,
                        true,
                        'row.entity.totalGroups'
                ),
                asyncDataCallback: (name) => {
                    return this.permissionsDao.getEntity({action: name, name: 'groups'}).$promise;
                },
                sortable: false,
                filterable: false
            },
            {
                header: 'Users',
                field: 'users',
                cellTemplate: this.JFrogTableViewOptions.cellTemplateGenerators.listableColumn(
                        'row.entity.users',
                        'row.entity.name',
                        null,
                        false,
                        null,
                        true,
                        'row.entity.totalUsers'
                ),
                asyncDataCallback: (name) => {
                    return this.permissionsDao.getEntity({action: name, name: 'users'}).$promise;
                },
                sortable: false,
                filterable: false
            }
        ]
    }


    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                visibleWhen: () => this.user.isAdmin(),
                callback: () => this.bulkDelete()
            },
        ]
    }


    showResources(resource) {
        const resourcesPromise = this.permissionsDao.getResource({action: resource.name, name: 'resources'}).$promise;
        resourcesPromise.then((response) => {
            console.log("I am gere");
            this.resourceModalScope = this.$rootScope.$new();
            let selectedRepositories = [];
            if (response.repos) {
                response.repos.forEach(repo => {
                    selectedRepositories.push(repo);
                });
            }

            this.resourceModalScope.permissionName = resource.name;
            this.resourceModalScope.repos = selectedRepositories;

            this.resourceModalScope.tableData = {};
            this.resourceModalScope.tableData = new this.JFrogTableViewOptions(this.resourceModalScope);
            this.resourceModalScope.tableData.setColumns(this._getRepoDataColumns());
            this.resourceModalScope.tableData.setData(this.resourceModalScope.repos);

            this.resourceModalScope.buildExcludePatterns = response.buildExcludePatterns || [];
            this.resourceModalScope.buildIncludePatterns = response.buildIncludePatterns || [];


            let tabs = [];
            if (resource.hasRepos) {
                tabs.push({name: 'Repositories'});
            }
            if (resource.hasBuilds) {
                tabs.push({name: 'Builds'});
            }
            this.resourceModalScope.tabsDict = {
                Repositories: 'Repositories',
                Builds: 'Builds'
            };

            this.resourceModalScope.tabs = tabs;
            this.modal.launchModal('show_resources_modal', this.resourceModalScope);

        })
    }

    _getRepoDataColumns() {
        return [
            {
                header: 'Repository Key',
                field: 'repoKey',
                sortable: true,
                filterable: true,
                cellTemplate: `<div><i class="icon-{{row.entity.type}}-repo"></i> {{row.entity.repoKey}}</div>`
            },
            {
                header: 'Type',
                field: 'type',
                sortable: true,
                cellTemplate: `<div>{{row.entity.type | capitalize}}</div>`
            }
        ];
    }
}