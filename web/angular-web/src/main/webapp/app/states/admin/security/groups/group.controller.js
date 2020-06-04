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
import CONFIG_MESSAGES from "../../../../constants/configuration_messages.constants";

export class AdminSecurityGroupsController {

    constructor(JFrogModal, $scope, $state, GroupsDao, JFrogGridFactory, uiGridConstants, commonGridColumns) {
        this.DEFAULT_REALM = "artifactory";
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.groupsDao = GroupsDao.getInstance();
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;
        this.$scope = $scope;
        this.$state = $state;
        this.noGroupsMessage = CONFIG_MESSAGES.admin.security.groups.noGroupsMessage;
        this._createGrid();
        this._initGroups();
    }

    _initGroups() {
        this.groupsDao.getAll().$promise.then((groups)=> {
            this.gridOption.setGridData(groups);
        });
    }

    _createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setButtons(this._getActions())
                .setMultiSelect()
                .setRowTemplate('default')
                .setBatchActions(this._getBatchActions());
    }

    deleteGroup(group) {
        let json = {groupNames:[group.groupName]};
        this.modal.confirm(`Are you sure you want to delete group '${group.name}' ?`)
            .then(() => this.groupsDao.delete(json).$promise.then(()=>this._initGroups()));
    }

    bulkDelete() {
        //Get All selected users
        let selectedRows = this.gridOption.api.selection.getSelectedRows();
        //Create an array of the selected groups names
        let names = _.map(selectedRows, (group) => {return group.groupName;});
        //Create Json for the bulk request
        let json = {groupNames: names};
        //console.log('Bulk delete....');
        //Ask for confirmation before delete and if confirmed then delete bulk of users
        this.modal.confirm(`Are you sure you want to delete ${names.length} groups ?`).
        then(() => this.groupsDao.delete(json).$promise.then(() => this._initGroups()));
    }
    getColumns() {
        return [
            {
                field: "groupName",
                name: "Group Name",
                displayName: "Group Name",
                cellTemplate: '<div class="ui-grid-cell-contents" ui-sref="^.groups.edit({groupname: row.entity.groupName})"><a href="" class="jf-link" >{{row.entity.groupName}}</a></div>',
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '20%'
            },
            {
                field: "permissions",
                name: "Permissions",
                displayName: "Permissions",
                cellTemplate: this.commonGridColumns.listableColumn('row.entity.permissions','row.entity.groupName'),
                width: '50%'
            },
            {
                name: "External",
                displayName: "External",
                field: "External",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.external'),
                width: '10%'
            }, {
                name: "Admin",
                displayName: "Admin",
                field: "adminPrivileges",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.adminPrivileges'),
                width: '10%'
            },
            {
                name: "Auto Join",
                displayName: "Auto Join",
                field: "Auto Join",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.autoJoin'),
                width: '10%'
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (row) => this.deleteGroup(row)
            }

        ];
    }
   _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.bulkDelete()
            }
        ]
    }

}