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
import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfCocoapodsController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.cocoapods;
        this.gridDependenciesOptions = {};
        this.cocoapodsData = {};
        this.$scope = $scope;
    }

    $onInit() {
        this._initCocoapods();
    }

    _initCocoapods() {
        this._registerEvents();
        this.getCocoapodsData();
    }

    getCocoapodsData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "cocoapods",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
            .then((data) => {
                this.cocoapodsData = data;
                this._createGrid();
            });
    }

    _createGrid() {
        if (this.cocoapodsData.dependencies) {
            if (!Object.keys(this.gridDependenciesOptions).length) {
                this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                    .setRowTemplate('default')
                    .setColumns(this._getColumns())
                    .setGridData(this.cocoapodsData.dependencies)
            }
            else {
                this.gridDependenciesOptions.setGridData(this.cocoapodsData.dependencies)
            }
        }
    }

    _getColumns() {
        return [{
            name: 'Name',
            displayName: 'Name',
            field: 'name'
        },
            {
                name: 'Version',
                displayName: 'Version',
                field: 'version'
            }];
    }

    _registerEvents() {

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                this.getCocoapodsData();
            }
        });
    }

}
export function jfCocoapods() {
    return {
        restrict: 'EA',
        controller: jfCocoapodsController,
        controllerAs: 'jfCocoapods',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_cocoapods.html'
    }
}