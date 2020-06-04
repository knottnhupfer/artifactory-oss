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

class jfBowerController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.bower;
        this.gridDependenciesOptions = {};
        this.bowerData = {};
        this.$scope = $scope;
    }

    $onInit() {
        this._initBower();
    }

    _initBower() {
        this._registerEvents();
        this.getBowerData();
    }

    getBowerData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "bower",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.bowerData = data;
                    this._createGrid();
                });
    }

    _createGrid() {
        if (this.bowerData.bowerDependencies) {
            if (!Object.keys(this.gridDependenciesOptions).length) {
                this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setRowTemplate('default')
                        .setColumns(this._getColumns())
                        .setGridData(this.bowerData.bowerDependencies)
            }
            else {
                this.gridDependenciesOptions.setGridData(this.bowerData.bowerDependencies)
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
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getBowerData();
            }
        });
    }

}
export function jfBower() {
    return {
        restrict: 'EA',
        controller: jfBowerController,
        controllerAs: 'jfBower',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_bower.html'
    }
}