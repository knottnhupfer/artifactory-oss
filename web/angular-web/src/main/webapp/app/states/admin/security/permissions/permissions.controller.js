// /*
//  *
//  * Artifactory is a binaries repository manager.
//  * Copyright (C) 2018 JFrog Ltd.
//  *
//  * Artifactory is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU Affero General Public License as published by
//  * the Free Software Foundation, either version 3 of the License, or
//  *  (at your option) any later version.
//  *
//  * Artifactory is distributed in the hope that it will be useful,
//  * but WITHOUT ANY WARRANTY; without even the implied warranty of
//  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  * GNU Affero General Public License for more details.
//  *
//  * You should have received a copy of the GNU Affero General Public License
//  * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
//  *
//  */
// export class AdminSecurityPermissionsController {
//     constructor($scope, $state, JFrogGridFactory, PermissionsDao, JFrogModal, uiGridConstants, User, commonGridColumns,
//             JFrogTableViewOptions, $rootScope) {
//         this.$state=$state;
//         this.currentTab = 'repo';
//         this.modal = JFrogModal;
//         this.permissionsDao = PermissionsDao.getInstance();
//         this.$scope = $scope;
//         this.$rootScope = $rootScope;
//         this.commonGridColumns = commonGridColumns;
//         this.artifactoryGridFactory = JFrogGridFactory;
//         this.uiGridConstants = uiGridConstants;
//         this.JFrogTableViewOptions = JFrogTableViewOptions;
//         this.user = User.getCurrent();
//         this._createGrid();
//         this.initPermission();
//
//
//     }
//
//     initPermission() {
//         this.permissionsDao.getAll().$promise.then((res)=> {
//             this.gridOption.setGridData(res.data);
//         });
//     }
//
//     showNew() {
//         return this.user.isAdmin();
//     }
//
//     _createGrid() {
//
//         this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
//             .setColumns(this._getColumns())
//             .setRowTemplate('default')
//             .setMultiSelect()
//             .setButtons(this._getActions())
//             .setGridData([])
//             .setBatchActions(this._getBatchActions());
//
//         this.gridOption.isRowSelectable = (row) => {
//             return row.entity.name !== this.user.name;
//         }
//     }
//
//     _getActions() {
//         return [
//             {
//                 icon: 'icon icon-clear',
//                 tooltip: 'Delete',
//                 callback: row => this._deletePermission(row),
//                 visibleWhen: () => this.user.isAdmin()
//             }
// /*
//             {
//                 icon: 'icon icon-builds',
//                 tooltip: 'Edit',
//                 callback: row => this._editPermission(row)
//             }
// */
//         ]
//     }
//
//     editPermission(row) {
//         this.$state.go('^.permissions.edit', {permission: row.name})
//     }
//
//     _deletePermission(row) {
//         let json = {permissionTargetNames:[row.name]};
//         this.modal.confirm(`Are you sure you want to delete permission '${row.name}?'`)
//           .then(() => this.permissionsDao.deletePermission(json).$promise.then(()=>this.initPermission()));
//     }
//
//     bulkDelete() {
//         //Get All selected users
//         let selectedRows = this.gridOption.api.selection.getSelectedRows();
//         //Create an array of the selected permission names
//         let names = _.map(selectedRows, (row) => {return row.name;});
//         //Create Json for the bulk request
//         let json = {permissionTargetNames: names};
//         //console.log('Bulk delete....');
//         //Ask for confirmation before delete and if confirmed then delete bulk of users
//         this.modal.confirm(`Are you sure you want to delete ${names.length} permissions?`).
//         then(() => this.permissionsDao.deletePermission(json).$promise.then(() => this.initPermission()));
//     }
//
//     _getColumns() {
//         return [
//             {
//                 name: 'Permission Target Name',
//                 displayName: 'Permission Target Name',
//                 field: 'name',
//                 sort: {
//                     direction: this.uiGridConstants.ASC
//                 },
//                 cellTemplate: '<div class="ui-grid-cell-contents"><a href class="jf-link" ng-click="grid.appScope.Permissions.editPermission(row.entity)">{{row.entity.name}}</a></div>'
//             },
//             {
//                 name: 'Resources',
//                 displayName: 'Resources',
//                 cellTemplate: `<div class="ui-grid-cell-contents">
//                                     <span ng-if="row.entity.repos.length">Repositories</span><span ng-if="row.entity.repos.length && (row.entity.buildExcludePatterns.length || row.entity.buildIncludePatterns.length)">, </span>
//                                     <span ng-if="row.entity.buildExcludePatterns.length || row.entity.buildIncludePatterns.length">Builds</span>
//                                     <a style="float:right" ng-click="grid.appScope.Permissions.showResources(row.entity);" class="show-all-link" ng-if="(row.entity.buildExcludePatterns.length || row.entity.buildIncludePatterns.length) || row.entity.repos">See All</a></div>
//                                </div>`,
//                 field: 'repoKeysView'
//             },
//             {
//                 name: 'Groups',
//                 displayName: 'Groups',
//                 cellTemplate: this.commonGridColumns.listableColumn('row.entity.groups','row.entity.name'),
//                 field: 'groupsList'
//
//             },
//             {
//                 name: 'Users',
//                 displayName: 'Users',
//                 cellTemplate: this.commonGridColumns.listableColumn('row.entity.users','row.entity.name'),
//                 field: 'usersList'
//             }
//         ]
//     }
//     _getBatchActions() {
//         return [
//             {
//                 icon: 'clear',
//                 name: 'Delete',
//                 visibleWhen: () => this.user.isAdmin(),
//                 callback: () => this.bulkDelete()
//             }
//         ]
//     }
//
//     showResources(resource) {
//         this.resourceModalScope = this.$rootScope.$new();
//
//         let selectedRepositories = [];
//         if (resource.repos) {
//             resource.repos.forEach(repo => {
//                 selectedRepositories.push(repo);
//             });
//         }
//
//         this.resourceModalScope.permissionName = resource.name;
//         this.resourceModalScope.repos = selectedRepositories;
//
//         this.resourceModalScope.tableData = {};
//         this.resourceModalScope.tableData = new this.JFrogTableViewOptions(this.resourceModalScope);
//         this.resourceModalScope.tableData.setColumns(this._getRepoDataColumns());
//         this.resourceModalScope.tableData.setData(this.resourceModalScope.repos);
//
//
//         this.resourceModalScope.buildExcludePatterns = resource.buildExcludePatterns || [];
//         this.resourceModalScope.buildIncludePatterns = resource.buildIncludePatterns || [];
//
//
//         let tabs = [];
//         if (this.resourceModalScope.repos.length) tabs.push({name: 'Repositories'});
//         if (resource.buildExcludePatterns && resource.buildExcludePatterns.length || resource.buildIncludePatterns && resource.buildIncludePatterns.length) tabs.push({name: 'Builds'});
//         this.resourceModalScope.tabsDict = {
//             Repositories: 'Repositories',
//             Builds: 'Builds'
//         };
//
//
//         this.resourceModalScope.tabs = tabs;
//
//         this.modal.launchModal('show_resources_modal', this.resourceModalScope);
//     }
//
//     _getRepoDataColumns() {
//         return [
//             {
//                 header: 'Repository Key',
//                 field: 'repoKey',
//                 sortable: true,
//                 filterable: true,
//                 cellTemplate: `<div><i class="icon-{{row.entity.type}}-repo"></i> {{row.entity.repoKey}}</div>`
//             },
//             {
//                 header: 'Type',
//                 field: 'type',
//                 sortable: true,
//                 cellTemplate: `<div>{{row.entity.type | capitalize}}</div>`
//             }
//         ];
//     }
// }