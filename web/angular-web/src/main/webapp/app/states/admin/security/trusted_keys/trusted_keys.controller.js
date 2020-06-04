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
export class TrustedKeysController {

	constructor($scope, $rootScope, TrustedKeysDao, JFrogTableViewOptions, JFrogModal, JFrogNotifications) {
		this.$scope = $scope;
		this.$rootScope = $rootScope;
		this.TrustedKeysDao = TrustedKeysDao;
		this.JFrogTableViewOptions = JFrogTableViewOptions;
		this.artifactoryNotifications = JFrogNotifications;
		this.modal = JFrogModal;

		this.modalScope = null;


		this._createGrid();
		this._getTrustedKeysData();

	}

	_getTrustedKeysData() {
		this.TrustedKeysDao.getTrustedKeys().$promise.then((response) => {
			this.trustedKeys = response;
			this.tableViewOptions.setData(this.trustedKeys)

		});
	}

	_createGrid() {
		this.tableViewOptions = new this.JFrogTableViewOptions(this.$scope);
		this.tableViewOptions.setColumns(this._getColumns())
		    .setRowsPerPage(20)
		    .setObjectName('Key/Keys')
		    .setEmptyTableText('No Trusted Keys')
			.setActions(this.getActions())
		    .setBatchActions(this.getBatchActions())
		    .setNewEntityAction(() => {
		    	this._addNewLicense();
		    });
	}

	_addNewLicense() {
		this.modalScope = this.$rootScope.$new();
		this.modalScope.modalTitle = "Add new trusted key";
		this.modalScope.ctrl = {
			aliasName: '',
			cretificateKey: ''
		};
		this.modalScope.dndStyle = {'width':"100%",'height':"200px"}; // the primitive should be wrapped with some object
		this.modalScope.dndOnError = (errorMessage) => {
			this.artifactoryNotifications.create({
				error: errorMessage
			});
		};

		this.modalScope.saveKey = () => {
			let certificateJson = {
				alias: this.modalScope.ctrl.aliasName,
				public_key: this.modalScope.ctrl.cretificateKey.replace(/(?:\r\n|\r)/g, '\n')
			}

			this.TrustedKeysDao.AddTrustedKey(certificateJson).$promise.then((response) => {
				if (response.status === 201) {
					this._getTrustedKeysData();
					this.modalInstance.close();
				}
			});
		};

		this.modalScope.canSave = () =>{
			return !this.modalScope.ctrl.aliasName && !this.modalScope.ctrl.aliasName.length
				|| !this.modalScope.ctrl.cretificateKey && !this.modalScope.ctrl.cretificateKey.length;
		};

		this.modalScope.closeModal = () => this.modalInstance.close();

		this.modalInstance = this.modal.launchModal('ssh_key_modal', this.modalScope);
	}

	_getColumns() {
		return [
			{
				header: "Alias",
				field: "alias",
				cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.alias}}</div>',
				width: '20%'
			},
			{
				header: "Key ID",
				field: "kid",
				cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.kid}}</div>',
				width: '20%'
			},
			{
				header: "Issued By",
				cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.issuedBy}}</div>',
				field: "issuedBy",
				width: '20%'
			},
			{
				header: "Issued On",
				cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.issued | date: "yyyy-MM-dd HH:mm:ss Z"}}</div>',
				field: "issued",
				width: '20%'
			},
			{
				header: "Valid Until",
				cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.expiry || ""}}</div>',
				field: "expiry",
				width: '20%'
			},
			{
				header: "Fingerprint",
				cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.fingerprint}}</div>',
				field: "fingerprint",
				width: '20%'
			}
		]
	}

	getBatchActions() {
		return [
			{
				icon: 'clear',
				name: 'Delete',
				callback: (selected) => {
					let keysToRemove = _.map(selected, (val) => {
						return {'kid': val.kid}
					});
					let message = 'Are you sure you want to delete ';
				        message += (keysToRemove.length > 1) ? keysToRemove.length + ' trusted keys?' : selected[0].alias + ' trusted key?';

					this.modal.confirm(message).then(() => {
						this.TrustedKeysDao.deleteTrustedKey(keysToRemove).$promise.then((response) => {
							if (response.status === 200) {
								this.trustedKeys = _.difference(this.trustedKeys,selected);
								this.tableViewOptions.clearSelection();
								this.tableViewOptions.setData(this.trustedKeys)
							}
						});
					})
				}
			}
		]
	}

    getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (row) => {
                    let message = `Are you sure you want to delete ${row.alias} trusted key?`;

                    this.modal.confirm(message).then(() => {
                        this.TrustedKeysDao.deleteTrustedKey([{'kid' : row.kid}]).$promise.then((response) => {
                            if (response.status === 200) {
                                this.trustedKeys = _.difference(this.trustedKeys,[row]);
                                this.tableViewOptions.setData(this.trustedKeys)
                            }
                        });
                    });
                }
            }
		]
	}
}
