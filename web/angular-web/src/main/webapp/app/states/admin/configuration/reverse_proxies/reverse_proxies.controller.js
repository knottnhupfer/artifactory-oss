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
export class AdminConfigurationReverseProxiesController {

    constructor($scope, ReverseProxiesDao, JFrogGridFactory, JFrogModal, $q, uiGridConstants, commonGridColumns) {
        this.gridOptions = {};
        this.commonGridColumns = commonGridColumns;
        this.uiGridConstants = uiGridConstants;
        this.reverseProxiesDao = ReverseProxiesDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;
        this.$scope=$scope;
        this.$q = $q;

        this._createGrid();
        this._initReverseProxies();
    }

    _initReverseProxies() {
        this.reverseProxiesDao.get().$promise.then((reverseProxies)=> {
            this.gridOptions.setGridData(reverseProxies)
        });
    }

    _createGrid() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setMultiSelect()
                .setButtons(this._getButtons())
                .setBatchActions(this._getBatchActions())
                .setRowTemplate('default');
    }

    deleteSelectedReverseProxies() {
        let selectedRows = this.gridOptions.api.selection.getSelectedRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} reverse proxies?`)
            .then(() => {
                    let keys = _.map(selectedRows, (row) => {return row.key;});
                    this.reverseProxiesDao.delete({proxyKeys: keys}).$promise
                            .then(()=>this._initReverseProxies());
            })
            .then(() => this._initReverseProxies());
    }

    deleteReverseProxy(key) {
        this.modal.confirm(`Are you sure you want to delete the reverse proxy '${key}'?`)
            .then(() => this._doDeleteReverseProxy(key))
            .then(() => this._initReverseProxies());
    }

    _doDeleteReverseProxy(key) {
        return this.reverseProxiesDao.delete({proxyKeys:[key]}).$promise;
    }

    _getColumns() {
        return [
            {
                field: "key",
                name: "Key",
                displayName: "Key",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a class="jf-link" ui-sref="^.reverse_proxies.edit({reverseProxyKey: row.entity.key})">{{ COL_FIELD }}</a></div>',
                width: '30%'
            },
            {
                field: "webServerType",
                name: "Web Server Type",
                displayName: "Web Server Type",
                width: '20%'
            },
            {
                field: "serverName",
                name: "Server Name",
                displayName: "Server Name",
                width: '50%'
            }
        ]
    }
    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedReverseProxies()
            }
        ]
    }

    _getButtons() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteReverseProxy(row.key)
            }

        ];
    }

}