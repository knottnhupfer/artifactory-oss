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
import API from '../../../../constants/api.constants';

export class AdminConfigurationLicensesController {

    constructor($scope, $window, JFrogModal, LicensesDao, JFrogGridFactory, ArtifactoryState, uiGridConstants) {
        this.$window = $window;
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.licensesDao = LicensesDao;
        this.$scope=$scope;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;

        this._createGrid();
        this._initLicenses();


        //clear 'prevState' in ArtifactoryState, used to return from license form to another state (Builds->Licenses)
        ArtifactoryState.setState('prevState', undefined);
    }

    _initLicenses() {
        this.licensesDao.getLicense().$promise.then((licenses)=> {
            this.licenses = licenses;
            this.gridOption.setGridData(this.licenses.data)
        });
    }

    _createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getCloumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    deleteLicense(license) {
        let json = {licenseskeys: [license.name]}
        this.modal.confirm(`Are you sure you want to delete ${license.name}?`)
                .then(()=> {
                    this.licensesDao.delete(json).$promise.then(()=>this.updateListTable());
                });
    }

    deleteSelectedLicenses() {
        //Get All selected licenses
        let selectedRows = this.gridOption.api.selection.getSelectedGridRows();
        //Create an array of the selected licenses keys
        let names = _.map(selectedRows, (row) => {return row.entity.name});
        //Create Json for the bulk request
        let json = {licenseskeys: names};
        //console.log('Bulk delete....');
        //Ask for confirmation before delete and if confirmed then delete bulk of licenses
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} licenses?`)
                .then(()=> {
                    this.licensesDao.delete(json).$promise.then(() => this.updateListTable());
                });
    }

    updateListTable() {
        this.licensesDao.getLicense().$promise.then((licenses)=> {
            this.licenses = licenses;
            this.gridOption.setGridData(this.licenses.data)
            if (this.modalInstance) {
                this.closeModal();
            }
        });
    }

    setStatus(row) {
        if (row.approved) {
            row.approved = false;
            row.status = "Unapproved";
        }
        else {
            row.approved = true;
            row.status = "Approved";
        }
        this.licensesDao.update(row).$promise.then(()=>this.updateListTable());
    }

    exportLicenses() {
        this.$window.open(`${API.API_URL}/licenseexport`, '_self', '');
    }

    getCloumns() {
        return [
            {
                name: "License Key",
                displayName: "License Key",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="^.licenses.edit({licenseName: row.entity.name})" id="license-key" class="jf-link text-center ui-grid-cell-contents">{{row.entity.name}}</a></div>',
                width: '15%'
            },
            {
                name: 'Name',
                displayName: 'Name',
                field: "longName",
                cellTemplate: '<div class="ui-grid-cell-contents" id="license-name"><span>{{row.entity.longName}}</span></div>',
                width: '40%'

            },
            {
                name: "URL",
                displayName: "URL",
                field: "url",
                cellTemplate: '<div class="ui-grid-cell-contents" ><a class="jf-link" href="{{row.entity.url}}" target="_blank" id="license-url">{{row.entity.url}}</a></div>',
                width: '30%'
            },
            {
                name: "Status",
                displayName: "Status",
                field: "status",
                cellTemplate: '<div class="ui-grid-cell-contents"><jf-switch-toggle texton="Approved" id="license-status" textoff="Unapproved" ng-click="grid.appScope.AdminConfigurationLicenses.setStatus(row.entity)" ng-class="{\'on\': row.entity.approved, \'off\': !row.entity.approved}"></jf-switch-toggle></div>',
                width: '15%'
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: license => this.deleteLicense(license)
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => this.deleteSelectedLicenses()
            },
        ]
    }


}