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
import EVENTS from "../../../../constants/artifacts_events.constants";
import DICTIONARY from "./../../constants/artifact_general.constant";

class jfChefInfoController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactoryGridFactory = JFrogGridFactory;
        this.artifactViewsDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.opkg;
        this.chefData = {};
        this.chefDependenciesGridOptions = {};
        this.chefPlatformsGridOptions = {};
        this.$scope = $scope;

    }

    $onInit() {
        this._createGrids();
        this.initChefInfo();
    }

    initChefInfo() {
        this._registerEvents();
        this._getChefInfoData();
    }

    _getChefInfoData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "chef",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
        .then((data) => {
            this.chefData = data.chefCookbookInfo;
            this._setGridsData();
        });
    }

    _setGridsData() {
        this.formattedDependencies = [];
        _.forEach(this.chefData.dependencies, (value, key) => {
            this.formattedDependencies.push({
                name: key,
                version: value
            })
        });

        this.formattedPlatforms = [];
        _.forEach(this.chefData.platforms, (value, key) => {
            this.formattedPlatforms.push({
                name: key,
                version: value
            })
        });
        this.chefDependenciesGridOptions.setGridData(this.formattedDependencies);
        this.chefPlatformsGridOptions.setGridData(this.formattedPlatforms);
    }

    _createGrids() {
        this.chefDependenciesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setRowTemplate('default')
                .setColumns(this._getColumns('dependencies'))

        this.chefPlatformsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setRowTemplate('default')
                .setColumns(this._getColumns('platforms'))
    }

    _getColumns(gridType) {
        if (gridType === 'dependencies') {
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
        if (gridType === 'platforms') {
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
    }

    _registerEvents() {
        let self = this;
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getChefInfoData();
            }
        });
    }
}

export function jfChefInfo() {
    return {
        restrict: 'EA',
        controller: jfChefInfoController,
        controllerAs: 'jfChefInfo',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_chef_info.html'
    }
}