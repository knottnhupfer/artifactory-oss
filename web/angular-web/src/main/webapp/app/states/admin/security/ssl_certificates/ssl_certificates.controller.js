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
/**
 * Created by tomere on 07/06/2017.
 */
import DICTIONARY from '../../../../constants/field_options.constats';

export class SslCertificatesController {

    constructor(JFrogNotifications, JFrogGridFactory, uiGridConstants,
            $timeout,$state, $scope, $rootScope, JFrogModal,SslCertificateDao) {
        this.artifactoryNotifications = JFrogNotifications;

        this.$state = $state;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.$rootScope = $rootScope;
        this.modal = JFrogModal;
        this.sslCertificateDao = SslCertificateDao;
        this.DICTIONARY = DICTIONARY.sslCertificate;

        this.gridOption = {};
        this.artifactoryGridFactory = JFrogGridFactory;
        this.uiGridConstants = uiGridConstants;

        this.createSslCertificatesGrid();
        this.initSiginingKeysGrid();
    }

    initSiginingKeysGrid() {
        this.sslCertificateDao.getAllCertificates().$promise.then((results)=> {
            this.sslCertificates = results.data;
            this.gridOption.setGridData(this.sslCertificates);
        });
    }

    createSslCertificatesGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getCloumns())
                .setRowTemplate('default')
                .setMultiSelect()
                .setButtons(this._getActions())
                .setBatchActions(this._getBatchActions());
    }

    /**
    * Data grid actions
    * */
    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (certificate) => {
                    this.deleteCertificate(certificate);
                }
            }
        ];
    }

    _getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: () => {
                    this.deleteSelectedCertificates()
                }
            }
        ]
    }

    deleteCertificate(certificate) {
        let alias = certificate.alias;
        this.modal.confirm(`Are you sure you want to delete '${alias}' certificate?`)
            .then(() => {
                let certificatesJson = {
                    'certificates': [alias]
                };
                this.sslCertificateDao.delete(certificatesJson).$promise.then(()=>this.updateListTable());
            });
    }

    deleteSelectedCertificates() {
        //Get All selected licenses
        let selectedRows = this.gridOption.api.selection.getSelectedGridRows();
        //Create an array of the selected licenses keys
        let certificates = _.map(selectedRows, (row) => {
            return row.entity.alias;
        });

        //Create Json for the bulk request
        let certificatesJson = {
            'certificates': certificates
        };

        //Ask for confirmation before delete and if confirmed then delete bulk of licenses
        this.modal.confirm(`Are you sure you want to delete ${selectedRows.length} certificates?`)
            .then(()=> {
                this.sslCertificateDao.delete(certificatesJson).$promise.then(() => {
                    this.refreshTable();
                });
            })
    }

    refreshTable(){
        this.updateListTable().finally(()=>{
            this.clearSelectAllButton();
        });
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

    updateListTable() {
        return this.sslCertificateDao.getAllCertificates().$promise.then((certificates)=> {
            this.sslCertificates = certificates.data;
            this.gridOption.setGridData(this.sslCertificates);
        });
    }

    /**
     * Data grid columns template
     * */
    showCertificateDetails(entity) {
        this.getCertificateData(entity.alias)
    }

    getCloumns() {
        return [
            {
                name: "Certificate Alias",
                displayName: "Certificate Alias",
                field: "alias",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate:
                    `<div class="ui-grid-cell-contents">
                            <a href 
                               ng-click="grid.appScope.SslCertificates.showCertificateDetails(row.entity)">
                               {{row.entity.alias}}
                            </a>
                      </div>`,
                width: '15%'
            },
            {
                name: "Issued By",
                displayName: "Issued By",
                field: "issued_by",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                    {{row.entity.issued_by}}
                               </div>`,
                width: '15%'
            },
            {
                name: "Issued To",
                displayName: "Issued To",
                field: "issued_to",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                    {{row.entity.issued_to}}
                               </div>`,
                width: '15%'
            },
            {
                name: "Valid Until",
                displayName: "Valid Until",
                field: "valid_until",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                    {{row.entity.valid_until}}
                               </div>`,
                width: '15%'
            },
            {
                name: "Fingerprint",
                displayName: "Fingerprint",
                field: "fingerprint",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                    {{row.entity.fingerprint}}
                               </div>`,
                width: '40%'
            }
        ]
    }

    /**
     * Add certificate modal
     * */
    launchCertificateKeyModal(){
        this.modalScope = this.$rootScope.$new();
        this.modalScope.modalTitle = 'Add New Certificate';
        this.modalScope.newCertificate = {text: ''}; // Whenever two-way binding a primitive (i.e. string) on a scope
        this.modalScope.aliasName = {text: ''};
        this.modalScope.dndStyle = {'width':"100%",'height':"200px"};// the primitive should be wrapped with some object
        this.modalScope.dndOnError = (errorMessage) =>{
            this.artifactoryNotifications.create({
                error: errorMessage
            });
        };

        this.modalScope.saveCertificate = () => {

            let certificateJson = {
                certificateName : this.modalScope.aliasName.text,
                certificatePEM : this.modalScope.newCertificate.text.replace(/(?:\r\n|\r)/g, '\n')
            };
            this.sslCertificateDao.add({$suppress_toaster: false}, certificateJson).$promise
                    .then((data)=> {
                        if(data.status === 200) {
                            this.updateListTable().finally(()=> this.closeModalIfOpen());
                        }
                    });
        };

        this.modalScope.canSave = () =>{
            return !this.modalScope.aliasName.text
                    || !this.modalScope.newCertificate.text
                    || !this.modalScope.aliasName.text.length
                    || !this.modalScope.newCertificate.text.length;
        }

        this.modalScope.closeModal = () => {
            return this.modalInstance.close();
        };

        this.modalInstance = this.modal.launchModal("add_ssl_certificate_modal", this.modalScope, 'lg');
    }

    /**
     * View certificate details modal
     * */
    getCertificateData(certificateAlias){
        this.sslCertificateDao.getDetails({certificate_alias: certificateAlias}).$promise.then((data)=>{
            if(data.status === 200) {
                console.log(data);
                this.launchViewCertificateDetailsModal(data.data,certificateAlias);
            }
        });
    }

    launchViewCertificateDetailsModal(certificate,certificateAlias){
        this.modalScope = this.$rootScope.$new();
        this.modalScope.modalTitle = certificateAlias + ' Certificate Details ';
        this.modalScope.certificateDetails = certificate;
        this.modalScope.DICTIONARY = this.DICTIONARY;

        this.modalScope.closeModal = () => {
            return this.modalInstance.close();
        };

        this.modalInstance = this.modal.launchModal("ssl_certificate_details_modal", this.modalScope, 'lg');
    }

    closeModalIfOpen(){
        if (this.modalInstance) {
            this.modalScope.closeModal();
        }
    }

}
