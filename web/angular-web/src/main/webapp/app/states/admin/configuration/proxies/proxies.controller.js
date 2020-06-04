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

export class AdminConfigurationProxiesController {

    constructor($scope, ProxiesDao, JFrogGridFactory, JFrogModal, $q, uiGridConstants, commonGridColumns) {
        this.gridOptions = {};
        this.commonGridColumns = commonGridColumns;
        this.uiGridConstants = uiGridConstants;
        this.proxiesDao = ProxiesDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;
        this.$scope=$scope;
        this.$q = $q;
        this.proxies = {};
        this.noSetsMessage = CONFIG_MESSAGES.admin.configuration.proxies.noSetsMessage;
        this._createGrid();
        this._initProxies();
    }

    _initProxies() {
        this.proxiesDao.get().$promise.then((proxies)=> {
            this.proxies = proxies;
            this.gridOptions.setGridData(proxies)
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

    deleteSelectedProxies() {
        let selectedRows = this.gridOptions.api.selection.getSelectedRows();
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} proxies?`)
            .then(() => {
                    //Create an array of the selected propertySet names
                    let keys = _.map(selectedRows, (row) => {return row.key;});
                    //Create Json for the bulk request
                    let json = {'proxyKeys': keys};
                    //console.log('Bulk delete....');
                    //Delete bulk of property set
                    this.proxiesDao.delete(json).$promise
                            .then(()=>this._initProxies());
            })
            .then(() => this._initProxies());
    }

    deleteProxy(key) {
        this.modal.confirm(`Are you sure you want to delete the proxy '${key}'?<br>Any configurations with this proxy on resources such as remote repositories and replications will be removed.`)
            .then(() => this._doDeleteProxy(key))
            .then(() => this._initProxies());
    }

    _doDeleteProxy(key) {

        let json = {proxyKeys:[key]}
        //console.log(json);
        return this.proxiesDao.delete(json).$promise;
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
                cellTemplate: '<div class="ui-grid-cell-contents"><a class="jf-link" ui-sref="^.proxies.edit({proxyKey: row.entity.key})">{{ COL_FIELD }}</a></div>',
                width: '30%'
            },
            {
                field: "host",
                name: "Host",
                displayName: "Host",
                width: '45%'
            },
            {
                field: "port",
                name: "Port",
                displayName: "Port",
                width: '10%'
            },
            {
                field: "defaultProxy",
                name: "Default Proxy",
                displayName: "Default Proxy",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.defaultProxy'),
                width: '15%'
            }
        ]
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedProxies()
            }
        ]
    }

    _getButtons() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteProxy(row.key)
            }

        ];
    }

}