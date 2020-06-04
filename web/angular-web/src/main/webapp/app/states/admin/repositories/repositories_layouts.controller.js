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
export class AdminRepositoriesLayoutController {

    constructor($scope, $state, JFrogGridFactory, RepositoriesLayoutsDao, uiGridConstants, ArtifactoryFeatures, JFrogModal) {
        this.$scope = $scope;
        this.$state = $state;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.layoutsDao = RepositoriesLayoutsDao;
        this.gridOptions = {};
        this.modal = JFrogModal;
        this.uiGridConstants = uiGridConstants;
        this.enableNew = ArtifactoryFeatures.getCurrentLicense() !== 'OSS';

        this._createGrid();
        this._getLayouts();
    }

    _createGrid() {
        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this.getColumns())
            .setSingleSelect()
            .setButtons(this.getActions())
            .setRowTemplate('default');

    }


    _getLayouts() {
        this.layoutsDao.getLayouts().$promise.then((data)=>{
            this.gridOptions.setGridData(data);
        });

    }

    getColumns() {
        return [
            {
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC

                },
                name: "Name",
                displayName: "Name",
                cellTemplate: '<div class="ui-grid-cell-contents" id="layout-name" ui-sref="^.repo_layouts.edit({layoutname: row.entity.name,viewOnly: !row.entity.layoutActions.edit})" ><a href="" class="jf-link">{{row.entity.name}}</a></div>',
                width: '15%'
            },
            {
                field: "artifactPathPattern",
                name: "Artifact Path Pattern",
                displayName: "Artifact Path Pattern",
                cellTemplate: '<div class="ui-grid-cell-contents" id="artifact-pattern">{{row.entity.artifactPathPattern}}</div>',
                width: '85%'
            }
        ]
    }

    copyLayout(row) {
        this.$state.go('^.repo_layouts.new',{copyFrom: row.name});
    }

    deleteLayout(row) {
        this.modal.confirm(`Are you sure you want to delete layout '${row.name}?'`).then(()=>{
            this.layoutsDao.deleteLayout({},{layoutName: row.name}).$promise.then((data)=>{
                this._getLayouts();
            });
        });
    }

    getActions() {
        return [
            {
                icon: 'icon icon-copy',
                tooltip: 'Duplicate',
                callback: (row) => this.copyLayout(row),
                visibleWhen: (row) => row.layoutActions.copy
            },
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (row) => this.deleteLayout(row),
                visibleWhen: (row) => row.layoutActions.delete
            }

        ];
    }

}
