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

export class AdminSecurityOAuthController {

    constructor($scope, ArtifactoryModelSaver, JFrogModal, OAuthDao, JFrogGridFactory, commonGridColumns, uiGridConstants) {
        this.$scope = $scope;
        this.OAuthDao = OAuthDao;
        this.modal = JFrogModal;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.commonGridColumns = commonGridColumns;
        this.uiGridConstants = uiGridConstants;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['oauthData']);
        this.providersGridOptions = null;
        this.TOOLTIP = TOOLTIP.admin.security.OAuthSSO;
        this.selectizeConfig = {
            sortField: 'text',
            create: false,
            maxItems: 1
        };
        this._createGrid();
        this._init();
    }

    _init() {
        this.OAuthDao.get().$promise.then((data)=>{
            this.oauthData = data;
            this.selectizeOptions = [{text:' ',value: '*'}];
            let githubProviders = _.filter(data.providers,(p)=>{return p.providerType === 'github'});
            this.selectizeOptions = this.selectizeOptions.concat(_.map(githubProviders, (p)=>Object({text:p.name,value:p.name})));
            if (!_.findWhere(githubProviders,{name: data.defaultNpm})) {
                this.selectizeOptions.push({text: data.defaultNpm,value:data.defaultNpm});
            }
            data.providers.forEach((provider) => {
                provider.typeDisplayName = _.findWhere(data.availableTypes, {type: provider.providerType}).displayName;
            });
            this.providersGridOptions.setGridData(data.providers);
        this.ArtifactoryModelSaver.save();
        });
    }

    _createGrid() {
        this.providersGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getColumns())
                .setSingleSelect()
                .setButtons(this._getActions())
                .setRowTemplate('default');
    }

    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field: "name",
                cellTemplate: '<div class="ui-grid-cell-contents" ui-sref="^.oauth.edit({providerName: row.entity.name})"><a href="" class="jf-link">{{row.entity.name}}</a></div>',
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '20%'
            },
            {
                name: 'Type',
                displayName: 'Type',
                field: "typeDisplayName",
                width: '15%'
            },
            {
                name: 'ID',
                displayName: 'ID',
                field: "id",
                width: '20%'
            },
            {
                name: 'Auth Url',
                displayName: 'Auth Url',
                field: "authUrl",
                width: '35%'
            },
            {
                name: "Enabled",
                displayName: "Enabled",
                field: "enabled",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.enabled'),
                width: '10%'
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (row) => this.deleteProvider(row)
            }

        ];
    }

    deleteProvider(row) {
        this.modal.confirm(`Are you sure you want to delete provider '${row.name}?'`)
                .then(() => {
                    this.OAuthDao.deleteProvider({},{provider: row.name}).$promise.then(()=>{
                        this._init();
                    });
                });
    }

    save() {

        let payload = _.cloneDeep(this.oauthData);

        if (payload.defaultNpm === '*') delete payload.defaultNpm;
        delete payload.providers;
        delete payload.availableTypes;

        this.OAuthDao.update(payload).$promise.then(()=>{
            this.ArtifactoryModelSaver.save();
        });

    }

    cancel() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this._init();
        });
    }
    canSave() {
        return this.oauthForm.$valid;
    }
}