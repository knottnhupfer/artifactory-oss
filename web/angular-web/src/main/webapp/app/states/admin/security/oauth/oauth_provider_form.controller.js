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
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminSecurityOAuthProviderFormController {

    constructor($state, $stateParams, ArtifactoryModelSaver, OAuthDao) {
        this.OAuthDao = OAuthDao;
        this.$state = $state;
        this.TOOLTIP = TOOLTIP.admin.security.OAuthSSO;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['providerData']);

        this.selectizeConfig = {
            sortField: 'text',
            create: false,
            maxItems: 1
        };


        if ($stateParams.providerName) {
            this.mode = 'edit';
            this.providerName = $stateParams.providerName;
            this.title = 'Edit ' + this.providerName + ' Provider';
        }
        else {
            this.mode = 'create';
            this.providerData = {
                enabled: true
            };
            this.title = 'Add New Provider';
        }

        this._getData();

    }

    _getData() {
        this.OAuthDao.get().$promise.then((data) => {
            this._setMandatoryFieldsData(data.availableTypes);

            this.selectizeOptions = _.map(data.availableTypes, (t)=>Object({text:t.displayName,value:t.type}));
            if (this.mode === 'create') {
                this.providerData.providerType = data.availableTypes[0].type;
                this._setDefaultValues();
            }
            else if (this.mode === 'edit') {
                this.providerData = _.findWhere(data.providers,{name: this.providerName});
            }
        this.ArtifactoryModelSaver.save();
        });
    }

    _setMandatoryFieldsData(typesData) {
        this.mandatoryFields = {};
        this.fieldHolders = {};
        typesData.forEach((typeRec) => {
            this.mandatoryFields[typeRec.type] = {};
            this.fieldHolders[typeRec.type] = {};
            typeRec.mandatoryFields.forEach((field) => {
                this.fieldHolders[typeRec.type][field] = typeRec.fieldHolders[typeRec.mandatoryFields.indexOf(field)] || '';
                this.mandatoryFields[typeRec.type][field] = typeRec.fieldsValues[typeRec.mandatoryFields.indexOf(field)] || '';
            });
        });
    }

    _setDefaultValues() {
        this.providerData.apiUrl = this.providerData.authUrl = this.providerData.tokenUrl = this.providerData.basicUrl =
        this.providerData.apiUrlHolder = this.providerData.authUrlHolder = this.providerData.tokenUrlHolder = this.providerData.basicUrlHolder = '';
        for (let key in this.mandatoryFields[this.providerData.providerType]) {
            this.providerData[key] = this.mandatoryFields[this.providerData.providerType][key];
            this.providerData[key + 'Holder'] = this.fieldHolders[this.providerData.providerType][key];
        };
    }

    onChangeProviderType() {
        this._setDefaultValues()
    }

    save() {
        if (this.savePending) return;

        this.savePending = true;

        if (this.mode === 'edit') {
            this.OAuthDao.updateProvider(this.providerData).$promise.then(()=>{
                this.savePending = false;
                this.ArtifactoryModelSaver.save();
                this.$state.go('^.oauth');
            }).catch(()=>this.savePending = false);
        }
        else if (this.mode === 'create') {
            this.OAuthDao.createProvider(this.providerData).$promise.then(()=>{
                this.savePending = false;
                this.ArtifactoryModelSaver.save();
                this.$state.go('^.oauth');
            }).catch(()=>this.savePending = false);
        }
    }

    cancel() {
        this.$state.go('^.oauth');
    }

    isSaveDisabled() {
        return this.savePending || !this.providerForm.$valid;
    }
}