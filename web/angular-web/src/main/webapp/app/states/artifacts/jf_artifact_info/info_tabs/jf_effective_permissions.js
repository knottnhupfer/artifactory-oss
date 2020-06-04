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
import EVENTS from '../../../../constants/artifacts_events.constants';

class jfEffectivePermissionsController {
    constructor(JFrogGridFactory, ArtifactPermissionsDao,PermissionsDao,$state, $scope, $timeout, JFrogEventBus,
            uiGridConstants, commonGridColumns,User,$q) {
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.$scope = $scope;
        this.$state = $state;
        this.$timeout = $timeout;
        this.$q = $q;
        this.User = User;
        this.currentTab = 'Users';
        this.userEffectivePermissionsGridOption = {};
        this.groupEffectivePermissionsGridOption = {};
        this.permissionTargetsGridOption = {};
        this.permissionsDao = ArtifactPermissionsDao.getInstance();
        this.entityPermissionsDao = PermissionsDao.getInstance();
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.currentUser = this.User.getCurrent();
    }

    $onInit() {
        this._registerEvents();
        this._createGrids();
        this._getPermissionsData();
    }

    _getPermissionsData() {
        return this.permissionsDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey,
            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "principal"
        }).$promise.then((data) => {
            this.userEffectivePermissionsGridOption.setGridData(data.userEffectivePermissions);
            this.groupEffectivePermissionsGridOption.setGridData(data.groupEffectivePermissions);
            this.permissionTargetsGridOption.setGridData(data.permissionTargets);
        });
    }

    _createGrids() {
        this.userEffectivePermissionsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getUserGroupGridColumns('users'))
                .setRowTemplate('default');
        this.groupEffectivePermissionsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getUserGroupGridColumns('groups'))
                .setRowTemplate('default');
        this.permissionTargetsGridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getPermissionTargetGridColumns())
                .setRowTemplate('default');
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.userEffectivePermissionsGridOption.resetPagination();
            this.userEffectivePermissionsGridOption.getPage();
            this.groupEffectivePermissionsGridOption.resetPagination();
            this.groupEffectivePermissionsGridOption.getPage();
            this.permissionTargetsGridOption.resetPagination();
            this.permissionTargetsGridOption.getPage();
            this.$timeout(() => {
                this._getPermissionsData();
            });
        });
    }

    goToEditPrincipal(tableType,row){
        // Make sure the session has not timed out yet
        this.User.loadUser(true).then(()=>{
            // If session is expired  => go to login state
            if (!this.User.getCurrent().isAdmin()) {
                this.$state.go('login');
            }

            // If admin - go to edit page of selected principal
            let principal = {};
            let state = 'admin.security.'+tableType+'.edit';
            let principalName = (tableType === 'users' ? 'username' : 'groupname');
            principal[principalName] = row.principal;
            this.$state.go(state, principal)
        });
    }

    isCurrentUserAdmin(){
        return this.currentUser.isAdmin();
    }

    getUserGroupGridColumns(tableType) {
        return [
            {
                name: 'Principal',
                displayName: 'Principal',
                field: "principal",
                cellTemplate:
                    `<div class="ui-grid-cell-contents">
                        <i jf-tooltip="Admin Privileges"
                           class="icon pull-left"
                           ng-class="row.entity.admin ? 'icon-admin-new' : 'icon-blank'"></i>
                        <a href 
                           ng-if="grid.appScope.jfEffectivePermissions.isCurrentUserAdmin()"
                           class="jf-link"
                           ng-click="grid.appScope.jfEffectivePermissions.goToEditPrincipal('${tableType}',row.entity)">
                            {{COL_FIELD CUSTOM_FILTERS}}
                        </a>
                        <span ng-if="!grid.appScope.jfEffectivePermissions.isCurrentUserAdmin()">
                            {{COL_FIELD CUSTOM_FILTERS}}
                        </span>
                    </div>`,
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '25%'
            },
            {
                name: 'Permission Targets',
                displayName: 'Permission Targets',
                field: "permissionTargets",
                cellTemplate: this.commonGridColumns.listableColumn(
                        'row.entity.permissionTargets',
                        'row.entity.principal',
                        '',false,null,
                        'row.entity.permissionTargetsCap',
                        'row.entity.permissionTargetsCount'
                ),
                asyncDataCallback: (name) => {
                    let queryParams = {
                        path: this.currentNode.data.path,
                        repoKey: this.currentNode.data.repoKey,
                    };
                    let pathParams = {
                        name: name,
                        entity_type: tableType
                    };
                    return this.entityPermissionsDao.getEntityPermissions(queryParams, pathParams).$promise;
                },
                width: '33%'
            },
            {
                name: "Delete/Overwrite",
                displayName: "Delete/Overwrite",
                field: "permission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '14%'
            },
            {
                name: "Deploy/Cache",
                displayName: "Deploy/Cache",
                field: "permission.deploy",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '12%'
            },
            {
                name: "Annotate",
                displayName: "Annotate",
                field: "permission.annotate",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '9%'
            },
            {
                name: "Read",
                displayName: "Read",
                field: "permission.read",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                width: '7%'
            }
        ]
    }

    triggerTimoutBeforeSwitch(){
        this.$timeout(()=>{
            try {
                window.dispatchEvent(new Event('resize'));
            }
            catch (e) {
                let resizeEvent = document.createEvent('Event');
                window.dispatchEvent(resizeEvent);
            }
        });
    }

    goToEditPermission(row) {
        this.$state.go('admin.security.permissions.edit', {permission: row.permissionName})
    }

    getPermissionTargetGridColumns(){
        return [
            {
                name: 'Permission Target Name',
                displayName: 'Permission Target Name',
                field: 'permissionName',
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: `<div class="ui-grid-cell-contents">
                                    <a href 
                                       class="jf-link" 
                                       ng-if="grid.appScope.jfEffectivePermissions.isCurrentUserAdmin()"
                                       ng-click="grid.appScope.jfEffectivePermissions.goToEditPermission(row.entity)">
                                        {{COL_FIELD CUSTOM_FILTERS}}
                                    </a>
                                    <span ng-if="!grid.appScope.jfEffectivePermissions.isCurrentUserAdmin()">
                                        {{COL_FIELD CUSTOM_FILTERS}}
                                    </span>
                             </div>`,
                width: '25%'
            },
            {
                name: 'Repositories',
                displayName: 'Repositories',
                cellTemplate: this.commonGridColumns.listableColumn('row.entity.repoKeys','row.entity.permissionName'),
                field: 'repoKeysView',
                width: '25%'
            },
            {
                name: 'Groups',
                displayName: 'Groups',
                cellTemplate: this.commonGridColumns.listableColumn('row.entity.groups','row.entity.permissionName'),
                field: 'groupsList',
                width: '25%'

            },
            {
                name: 'Users',
                displayName: 'Users',
                cellTemplate: this.commonGridColumns.listableColumn('row.entity.users','row.entity.permissionName'),
                field: 'usersList',
                width: '25%'
            }
        ]
    }

}
export function jfEffectivePermissions() {
    return {
        restrict: 'EA',
        controller: jfEffectivePermissionsController,
        controllerAs: 'jfEffectivePermissions',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_effective_permissions.html'
    }
}