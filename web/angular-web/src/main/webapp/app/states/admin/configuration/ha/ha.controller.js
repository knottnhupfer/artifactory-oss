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

let HaDao, $scope, artifactoryGridFactory, modal;

export class AdminConfigurationHAController {

    constructor(_$scope_, _JFrogModal_, _HaDao_, _JFrogGridFactory_, _ArtifactoryState_) {
        HaDao = _HaDao_;
        $scope = _$scope_;
        artifactoryGridFactory = _JFrogGridFactory_;
        modal = _JFrogModal_;
        this.CONFIG_MESSAGES = CONFIG_MESSAGES.admin.configuration.ha;

        this.gridOptions = {};
        this._createGrid();
        this._initHa();
    }

    _initHa() {
        HaDao.query().$promise.then((ha)=> {
            this.ha = ha;
            this.gridOptions.setGridData(this.ha)
        });
    }

    _createGrid() {
        this.gridOptions = artifactoryGridFactory.getGridInstance($scope)
                .setColumns(this._getColumns())
                .setButtons(this._getActions())
                .setRowTemplate('default');
    }


    _getColumns() {
        return [
            {
                field: "id",
                name: "Node ID",
                displayName: "Node ID",
                width: '7%'},
            {
                field: "startTime",
                name: "Start Time",
                displayName: "Start Time",
                width: '9%'
            },
            {
                field: "url",
                name: "URL",
                displayName: "URL",
                width: '18%',
                cellTemplate: '<div class="ui-grid-cell-contents"><a class="jf-link" target="_blank" href="{{ COL_FIELD }}">{{ COL_FIELD }}</a></div>'
            },
            {
                field: "memberShipPort",
                name: "Membership Port",
                displayName: "Membership Port",
                width: '11%'
            },
            {
                field: "state",
                name: "State",
                displayName: "State",
                width: '11%',
                cellTemplate: `<div class="ui-grid-cell-contents">{{ COL_FIELD }}`+
                                `<span class="ha-node-has-no-license"
                                      ng-if="!row.entity.hasLicense"
                                      jf-tooltip="No license installed.">&nbsp;(Inactive)</span>
                               </div>`
            },
            {
                field: "role",
                name: "Role",
                displayName: "Role",
                width: '8%'
            },
            {
                field: "lastHeartbeat",
                name: "Last Heartbeat",
                displayName: "Last Heartbeat",
                width: '11%',
                cellTemplate: `
                    <div ng-if="row.entity.heartbeatStale"
                         class="ui-grid-cell-contents ha-heartbeat-stale"
                         jf-tooltip="Heartbeat is stale. Check if your server is down."><i class="icon icon-notif-warning"></i>{{ COL_FIELD }}</div>
                    <div ng-if="!row.entity.heartbeatStale"
                         class="ui-grid-cell-contents">{{ COL_FIELD }}</div>
                         `
            },
            {
                field: "version",
                name: "Version",
                displayName: "Version",
                width: '9%'
            },
            {
                field: "revision",
                name: "Revision",
                displayName: "Revision",
                width: '6%'
            },
            {
                field: "releaseDate",
                name: "Release Date",
                displayName: "Release Date",
                width: '10%'
            }
        ]
    }

    _deleteNode(node) {
        modal.confirm('Are you sure you wish to remove ' + node.id + ' from the nodes list?')
            .then(() => HaDao.delete({id: node.id}))
            .then(() => this._initHa());
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                visibleWhen: node => node.heartbeatStale,
                callback: node => this._deleteNode(node)
            }
        ];
    }
}