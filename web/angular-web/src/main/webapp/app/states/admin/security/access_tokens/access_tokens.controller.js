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

export class AccessTokensController {
    constructor($scope, $timeout, AccessTokensDao, uiGridConstants, commonGridColumns, JFrogGridFactory, JFrogModal) {

        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.AccessTokensDao = AccessTokensDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.modal = JFrogModal;
        this.noTokensMessage = CONFIG_MESSAGES.admin.security.accessTokens.noTokensMessage;

        this.showExpirable = false;

        this._createGrid();
        this._getData();

        this.$timeout(() => $('.counter-and-filter').after($('#expireableTokens')), 100);

    }

    filterGrid() {
        let filteredData;

        if (this.showExpirable) {
            filteredData = this.allTokens;
        }
        else {
            filteredData = _.filter(this.allTokens,(token) => {
                return !this.isRowExpirable(token);
            })
        }


        this.gridOption.setGridData(filteredData);
    }

    _getData() {
        // get all tokens
        this.AccessTokensDao.getTokens().$promise.then((tokens)=> {
            this.allTokens = [].concat(tokens); //shallow copy
            this.filterGrid();
        });
    }

    _createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getColumns())
                .setButtons(this._getActions())
                .setMultiSelect()
                .setRowTemplate('default')
                .setBatchActions(this._getBatchActions());

        this.gridOption.isRowSelectable = (row) => {
            return !this.isRowExpirable(row.entity);
        };
    }


    getColumns() {

        return [
            {
                field: "subject",
                name: "Subject",
                displayName: "Subject",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.subject}}</div>',
                width: '28%'
            },
            {
                field: 'tokenId',
                name: 'Token ID',
                displayName: 'Token ID',
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.tokenId}}</div>',
                width: '24%'
            },
            {
                field: "issuedAt",
                name: "Issued At",
                displayName: "Issued At",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.issuedAt}}</div>',
                sort: {
                    direction: this.uiGridConstants.DESC
                },
                width: '18%'
            },
            {
                field: "expiryDate",
                name: "Expiry Date",
                displayName: "Expiry Date",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.expiry}}</div>',
                width: '18%'
            },
            {
                field: "refreshable",
                name: "Refreshable",
                displayName: "Refreshable",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.refreshable'),
                width: '12%'
            }
        ]

    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Revoke',
                visibleWhen: (row) => !this.isRowExpirable(row),
                callback: (row) => this.revokeToken(row)
            }

        ];
    }
    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Revoke',
                callback: () => this.bulkRevoke()
            }
        ]
    }

    revokeToken(token) {
        // Create array with single token ID to revoke
        let json = [token.tokenId];
        this.modal.confirm(`Are you sure you want to revoke this access token? Once revoked,
                            it can not be used again.`, 'Revoke access token', {confirm: 'Revoke'})
                .then(() => this.AccessTokensDao.revokeTokens(json).$promise.then(()=>this._getData()));
    }


    bulkRevoke() {
        // Get All selected users
        let selectedRows = this.gridOption.api.selection.getSelectedRows();
        // Create an array of the selected tokens
        let tokenIds = _.map(selectedRows, (token) => {return token.tokenId;});

        //Ask for confirmation before revoke and if confirmed then revoke bulk of tokens
        this.modal.confirm(`Are you sure you want to revoke these access tokens? Once revoked, 
                            they can not be used again.`, 'Revoke access tokens', {confirm: 'Revoke'})
                            .then(() => this.AccessTokensDao.revokeTokens(tokenIds).$promise.then(() => this._getData()));
    }

    isRowExpirable(row) {
        return (row.expiry && !row.refreshable);
    }

}