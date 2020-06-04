class jfEffectivePermissionController {
    constructor($stateParams, $scope, BuildsDao, JFrogEventBus, JFrogTableViewOptions, $state,
            commonGridColumns, User) {
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.user = User.getCurrent();
        this.commonGridColumns = commonGridColumns;
        this.buildsDao = BuildsDao;
        this.historyData = null;
        this.JFrogEventBus = JFrogEventBus;
        this.$scope = $scope;
        this.cellTemplateGenerators = JFrogTableViewOptions.cellTemplateGenerators;


        this._getData();

        this.JFrogEventBus.registerOnScope(this.$scope, this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH, () => {
            this._getData();
        });

        // Users Table
        this.usersEffectivePermissionsOptions = {};
        this.usersEffectivePermissionsOptions = new JFrogTableViewOptions(this.$scope);
        this.usersEffectivePermissionsOptions.setId('users-effective-permission-table')
                .setObjectName('User')
                .setColumns(this.getColumns());

        // Groups Table
        this.groupsEffectivePermissionsOptions = {};
        this.groupsEffectivePermissionsOptions = new JFrogTableViewOptions(this.$scope);
        this.groupsEffectivePermissionsOptions.setId('groups-effective-permission-table')
                .setObjectName('Group')
                .setColumns(this.getColumns());

        // Permission Targets Table
        this.permissionTargetsOptions = {};
        this.permissionTargetsOptions = new JFrogTableViewOptions(this.$scope);
        this.permissionTargetsOptions.setId('permission-targets-table')
                                    .setObjectName('Permission Target')
                                    .setColumns(this.getPermissionTargetsColumns());

    }


    _getData() {
        this.buildsDao.buildEffectivePermission({},{
            name: this.$stateParams.buildName,
            buildNumber: this.$stateParams.buildNumber,
            startTime: this.$stateParams.startTime
        }).$promise.then(response => {
            this.userEffectivePermissions = response.userEffectivePermissions;
            this.groupEffectivePermissions = response.groupEffectivePermissions;
            this.permissionTargets = response.permissionTargets;

            this.usersEffectivePermissionsOptions.setData(this.userEffectivePermissions);
            this.groupsEffectivePermissionsOptions.setData(this.groupEffectivePermissions);
            this.permissionTargetsOptions.setData(this.permissionTargets);
        });
    }

    getColumns() {

        return [
            {
                header: 'Principal',
                field: 'principal',
                filterable: true,
                cellTemplate: `<div>{{row.entity.principal}}</div>`,
                width: '30%'
            },
            {
                header: 'Permission Target',
                field: 'permissionTarget',
                width: '30%',
                cellTemplate: this.cellTemplateGenerators.listableColumn('row.entity.permissionTargets','row.entity.principal'),
            },


            {
                header: 'Read',
                field: 'permission.read',
                textAlign: 'center',
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                sortable: false
            },
            {
                header: 'Annotate',
                field: 'permission.annotate',
                textAlign: 'center',
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                sortable: false
            },
            {
                header: 'Upload',
                field: 'permission.deploy',
                textAlign: 'center',
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                sortable: false
            },
            {
                header: 'Delete',
                textAlign: 'center',
                field: "permission.delete",
                cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
                sortable: false
            }
        ]
    }

    getPermissionTargetsColumns() {

        let permissionNameCell = () => {
            return this.user.isAdmin() ?
                    `<div><a class="jf-link" ng-click="grid.appScope.jfEffectivePermission.editPermission(row.entity)">{{row.entity.permissionName}}</a></div>` :
                    `<div>{{row.entity.permissionName}}</div>`;
        };

        return [
            {
                header: 'Permission Target Name',
                field: 'permissionName',
                filterable: true,
                cellTemplate: permissionNameCell(),
                width: '30%'
            },
            {
                header: 'Users',
                field: 'users',
                cellTemplate: this.cellTemplateGenerators.listableColumn('row.entity.users','row.entity.permissionName')
            },
            {
                header: 'Groups',
                field: 'groups',
                cellTemplate: this.cellTemplateGenerators.listableColumn('row.entity.groups','row.entity.permissionName')
            }
        ]
    }

    editPermission(row) {
        this.$state.go('admin.security.permissions.edit', {permission: row.permissionName})
    }

}

export function jfEffectivePermission() {
    return {
        restrict: 'EA',
        controller: jfEffectivePermissionController,
        controllerAs: 'jfEffectivePermission',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/build_tabs/jf_effective_permission.html'
    }
}