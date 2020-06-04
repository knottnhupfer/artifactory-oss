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
 * Created by matang on 09/11/2017.
 */

import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfCranController {
    constructor($scope, $element, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.$scope = $scope;
        this.$element = $element;
        this.artifactViewsDao = ArtifactViewsDao;
        this.DICTIONARY = DICTIONARY.cran;
        this.gridDependenciesOptions = {};
        this.gridSuggestsOptions = {};
        this.gridImportsOptions = {};
        this.gridLinkingToOptions = {};
        this.gridEnhancesOptions = {};
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.cranData = {};

    }

    $onInit() {
        this._getCranData();
        this._registerEvents();
    }


    _getCranData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "cran",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise.then((data) => {
            this.cranData = data;
            this._createDependenciesGrid();
            this._createSuggestsGrid();
            this._createImportsGrid();
            this._createLinkingToGrid();
            this._createEnhancesGrid();
        });
    }
    _createDependenciesGrid() {
        if (this.cranData.cranDependencies) {
            this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default')
                    .setColumns(this._getColumns())
                    .setGridData(this.cranData.cranDependencies)
        }
    }

    _createSuggestsGrid() {
        if (this.cranData.cranSuggests) {
            this.gridSuggestsOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default')
                    .setColumns(this._getColumns())
                    .setGridData(this.cranData.cranSuggests)
        }
    }

    _createImportsGrid() {
        if (this.cranData.cranSuggests) {
            this.gridImportsOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default')
                    .setColumns(this._getColumns())
                    .setGridData(this.cranData.cranImports)
        }
    }

    _createLinkingToGrid() {
        if (this.cranData.cranSuggests) {
            this.gridLinkingToOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default')
                    .setColumns(this._getColumns())
                    .setGridData(this.cranData.cranImports)
        }
    }

    _createEnhancesGrid() {
        if (this.cranData.cranSuggests) {
            this.gridEnhancesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default')
                    .setColumns(this._getColumns())
                    .setGridData(this.cranData.cranImports)
        }
    }

    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field: 'name'
            },
            {
                name: 'Version',
                displayName: 'Version',
                field: 'version'
            }]
    }


    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getCranData();
        });
    }
}
export function jfCran() {
    return {
        restrict: 'EA',
        controller: jfCranController,
        controllerAs: 'jfCran',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_cran.html'
    }
}