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
import EVENTS from '../../constants/artifacts_events.constants';

class jfManageHaLicensesController {

    constructor($timeout,$state, $rootScope, $scope, $window, JFrogModal, User, HaLicensesDao,
                JFrogGridFactory, JFrogEventBus, uiGridConstants, JFrogNotifications, SaveArtifactoryHaLicenses, ArtifactoryState) {
        this.$rootScope = $rootScope;
        this.$scope = $scope;
        this.$window = $window;
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.$scope = $scope;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;
        this.User = User;
        this.$state = $state;
        this.haLicensesDao = HaLicensesDao;
        this.artifactoryNotifications = JFrogNotifications;
        this.ArtifactoryState = ArtifactoryState;
        this.saveArtifactoryHaLicenses = SaveArtifactoryHaLicenses;
        this.$timeout = $timeout;
        this._createGrid();
        this._initLicenses();
    }

    _initLicenses() {
        this.haLicensesDao.getLicenses().$promise.then((result)=> {
            this.licenses = result.data.licenses;
            this.gridOption.setGridData(this.licenses);
        });
    }

    launchAddHaLicenseModal() {
        this.modalScope = this.$rootScope.$new();
        this.modalScope.modalTitle = 'Add license';
        this.modalScope.dndStyle = {'width':"100%",'height':"230px"};
        this.modalScope.newLicenses = {text: ''}; // Whenever two-way binding a primitive (i.e. string) on a scope
                                                  // the primitive should be wrapped with some object
        this.modalScope.helpText = 'For adding more than one license, use a semicolon or a space as a separator between the different keys.';

        this.modalScope.dndOnError = (errorMessage) =>{
            this.artifactoryNotifications.create({
                error: errorMessage
            });
        };

        this.modalScope.saveLicenses = () => {
            let rawText = this.modalScope.newLicenses.text;
            this.saveArtifactoryHaLicenses.saveLicenses({$suppress_toaster: false}, rawText)
                .then((data)=> {
                    if(data.status === 200) {
                        // Initialize the 'has licanse already' state
                        let initStatus = this.ArtifactoryState.getState('initStatus');
                        if (initStatus) {
                            initStatus.hasLicenseAlready = true;
                        }

                        this.JFrogEventBus.dispatch(EVENTS.FOOTER_REFRESH);

                        // If this is the first time a license is provided
                        // and the user is not admin then redirect to home and refresh (so changes would become valid)
                        let usr = this.User.getCurrent();

                        if (!usr || !usr.isAdmin()) {
                            this.$state.go('home');
                            location.reload();
                        }
                        else{
                            this.updateListTable().finally(()=> this.closeModalIfOpen());
                        }
                    }
                });
        };

        this.modalScope.closeModal = () => {
            return this.modalInstance.close();
        };

        this.modalInstance = this.modal.launchModal("add_ha_licenses_modal", this.modalScope, "lg");
    }

    launchReplaceHaLicenseModal() {
        this.modalScope = this.$rootScope.$new();
        this.modalScope.modalTitle = 'Replace licenses';
        this.modalScope.headingText = 'Type the license key(s), or drag a file';
        this.modalScope.newLicenses = {text: ''}; // Whenever two-way binding a primitive (i.e. string) on a scope
                                                  // the primitive should be wrapped with some object
        this.modalScope.helpText = 'For adding more than one license, use a semicolon or a space as a separator between the different keys.';

        this.modalScope.saveLicenses = () => {
            this.onSaveReplacedLicenses(this);
        };

        this.modalScope.closeModal = () => {
            return this.modalInstance.close();
        };

        this.modalInstance = this.modal.launchModal("add_ha_licenses_modal", this.modalScope, "lg");
    }

    onSaveReplacedLicenses() {
        //Get selected licenses
        let selectedRows = this.gridOption.api.selection.getSelectedGridRows(),
            //Create an array of the selected licenses keys
            oldLicenses = _.map(selectedRows, (row) => {
                return row.entity.licenseHash
            });

        // Split text into licenses
        let rawText = this.modalScope.newLicenses.text,
            splittedText = this.saveArtifactoryHaLicenses.splitText(rawText);

        // Build two arrays - one for new licenses and another for the old licenses
        let newLicensesObjArray = this.saveArtifactoryHaLicenses.toLicensesObjArray(splittedText, "licenseHash"),
            oldLicensesHashCodesObjArray = this.saveArtifactoryHaLicenses.toLicensesObjArray(oldLicenses, "licenseHash"),
            numProvidedLicenses = newLicensesObjArray.length,
            numSelectedLicenses = oldLicensesHashCodesObjArray.length;

        // Upon user approval - make the swap
        if (numProvidedLicenses === numSelectedLicenses) {
            this.replaceLicenses(oldLicensesHashCodesObjArray, newLicensesObjArray, this);
        }
        else {
            this.artifactoryNotifications.create({
                error: `Failed to replace licenses. You provided ${numProvidedLicenses} license keys while selecting ${numSelectedLicenses} licenses.`
            });
        }
    }

    replaceLicenses(oldLicensesHashCodesObjArray, newLicensesObjArray) {
        //Ask for confirmation before replacing
        this.modal.confirm(`Are you sure you want to replace ${oldLicensesHashCodesObjArray.length} licenses?`)
            .then(()=> {
                let replaceLicensesJson = {
                    'licenses': oldLicensesHashCodesObjArray,
                    'newLicenses': newLicensesObjArray
                };

                this.haLicensesDao.replace(replaceLicensesJson).$promise.then(() => {
                    this.updateListTable(this).finally(() => this.closeModalIfOpen());
                });
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

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (license) => {
                    this.deleteLicense(license);
                },
                visibleWhen: (row) => {
                    return (row.nodeUrl && row.nodeUrl === 'Not in use' || this.hasFreeLicenseInPool());
                }
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                // This tooltip will show only when action is disabled
                getDisabledTooltip: () => {
                    return !this.hasFreeLicenseInPool() && this.gridOption && this.gridOption.api &&
                            this.gridOption.api.selection.getSelectedGridRows().length ?
                            'Attached licenses cannot be deleted from running nodes</br>when there are no available licenses' : '';
                },
                callback: () => {
                    this.deleteSelectedLicenses()
                },
                disabledWhen: () => {
                    return !this.hasFreeLicenseInPool();
                }
            }
            // No need for replace functionality for now...
            //,
            // {
            //     icon: 'replace',
            //     name: 'Replace',
            //     callback: () => {
            //         this.launchReplaceHaLicenseModal()
            //     }
            // },
        ]
    }

    hasFreeLicenseInPool(){
        let hasFreeLicense = false;
        if(this.gridOption.data && this.gridOption.data.length){
            this.gridOption.data.forEach((license)=>{
                if(license.nodeUrl && license.nodeUrl === 'Not in use'){
                    hasFreeLicense = true;
                }
            });
        }
        return hasFreeLicense;
    }

    deleteLicense(license) {
        let hashCode = license.licenseHash;
        this.modal.confirm(`Are you sure you want to delete ${hashCode}?`)
            .then(()=> {
                let licensesHashCodes = this.saveArtifactoryHaLicenses.toLicensesObjArray([hashCode], 'licenseHash');
                let licensesJson = {
                    'licenses': licensesHashCodes
                };
                this.haLicensesDao.delete(licensesJson).$promise.then(()=>this.updateListTable());
            });
    }

    deleteSelectedLicenses() {
        //Get All selected licenses
        let selectedRows = this.gridOption.api.selection.getSelectedGridRows();
        //Create an array of the selected licenses keys
        let licensesHashCodes = _.map(selectedRows, (row) => {
            return {
                'licenseHash': row.entity.licenseHash
            };
        });

        //Create Json for the bulk request
        let licensesJson = {
            'licenses': licensesHashCodes
        };

        //Ask for confirmation before delete and if confirmed then delete bulk of licenses
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} licenses?`)
            .then(()=> {
                this.haLicensesDao.delete(licensesJson).$promise.then(() => {
                    this.refreshTable();
                });
            })
    }

    refreshTable(){
        this.updateListTable().finally(()=>{
            this.clearSelectAllButton();
        });
    }

    updateListTable() {
        return this.haLicensesDao.getLicenses().$promise.then((licenses)=> {
            this.licenses = licenses.data.licenses;
            this.gridOption.setGridData(this.licenses);
        });
    }

    closeModalIfOpen(){
        if (this.modalInstance) {
            this.modalScope.closeModal();
        }
    }

    clearSelectAllButton(){
        this.$timeout(()=>{
            let selectAllButton = $('.ui-grid-header-cell-row .ui-grid-selection-row-header-buttons');
            let selectAllButtonIsOn = selectAllButton.is('.ui-grid-all-selected');
            if(selectAllButtonIsOn){
                selectAllButton.trigger('click');
            }
        });

    }

    getCloumns() {
        return [
            {
                name: "License Hash",
                displayName: "License Hash",
                field: "licenseHash",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: `<div class="ui-grid-cell-contents">
                                {{row.entity.licenseHash}}
                              </div>`,
                width: '25%'
            },
            {
                name: 'Licensed To',
                displayName: 'Licensed To',
                field: "licensedTo",
                cellTemplate: `<div class="ui-grid-cell-contents" id="license-owner">
                                <span>{{row.entity.licensedTo}}</span>
                               </div>`,
                width: '10%'

            },
            {
                name: "Valid Through",
                displayName: "Valid Through",
                field: "validThrough",
                cellTemplate: `<div ng-if="!row.entity.expired"
                                    class="ui-grid-cell-contents">
                                    {{row.entity.validThrough}}
                               </div>
                               <div ng-if="row.entity.expired"
                                    class="ui-grid-cell-contents license-expired">
                                    {{row.entity.validThrough}} (License Expired)
                               </div>`,
                width: '15%'
            },
            {
                name: "License Type",
                displayName: "License Type",
                field: "type",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                    {{row.entity.type}}
                               </div>`,
                width: '10%'
            },
            {
                name: "Node ID",
                displayName: "Node ID",
                field: "nodeId",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                {{row.entity.nodeId}}
                              </div>`,
                width: '10%'
            },
            {
                name: "Node URL",
                displayName: "Node URL",
                field: "nodeUrl",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                 <a href="{{row.entity.nodeUrl}}"
                                     target="_blank"
                                     id="node-url"
                                     ng-if="row.entity.nodeUrl!='Not in use'">{{row.entity.nodeUrl}}</a>
                                   <span ng-if="row.entity.nodeUrl=='Not in use'">Not in use</span>
                              </div>`,
                width: '30%'
            }
        ]
    }

}


export function jfManageHaLicenses() {

    return {
        restrict: 'E',
        scope: {items: '='},
        controller: jfManageHaLicensesController,
        controllerAs: 'jfManageHaLicenses',
        templateUrl: 'directives/jf_manage_artifactory_licenses/jf_manage_ha_licenses.html',
        bindToController: true
    };
}