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

class jfNugetController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.nuget;
        this.gridDependenciesOptions = {};
        this.gridFrameworkAssembliesOptions = {};
        this.nugetData = {};
        this.$scope = $scope;
    }

    $onInit() {
        this._initNuget();
    }

    _initNuget() {
        this._registerEvents();
        this.getNugetData();
    }

    getNugetData() {

        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "nuget",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.nugetData = data;
                        this._createGrid();
                });
    }

    _createGrid() {
        if (this.nugetData.dependencies) {
            if (!Object.keys(this.gridDependenciesOptions).length) {
                this.gridDependenciesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setRowTemplate('default')
                        .setColumns(this._getColumns('dependencies'))
                        .setGridData(this.nugetData.dependencies)
            }
            else {
                this.gridDependenciesOptions.setGridData(this.nugetData.dependencies)
            }
        }
        if (this.nugetData.frameworkAssemblies) {
            if (!Object.keys(this.gridFrameworkAssembliesOptions).length) {
                this.gridFrameworkAssembliesOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setColumns(this._getColumns('frameworkAssemblies'))
                        .setRowTemplate('default')
                        .setGridData(this.nugetData.frameworkAssemblies)
            }
            else {
                this.gridFrameworkAssembliesOptions.setGridData(this.nugetData.frameworkAssemblies);
            }
        }
    }

    _getColumns(gridType) {
        if (gridType === 'dependencies') {
            return [{
                name: 'Id',
                displayName: 'Id',
                field: 'id'
            },
                {
                    name: 'Version',
                    displayName: 'Version',
                    field: 'version'
                },
                {
                    name: 'Target Framework',
                    displayName: 'Target Framework',
                    field: 'targetFramework'
                }]
        }
        if (gridType === 'frameworkAssemblies') {
            return [
                {
                    name: 'Assembly Name',
                    displayName: 'Assembly Name',
                    field: 'assemblyName'
                },
                {
                    name: 'Target Framework',
                    displayName: 'Target Framework',
                    field: 'targetFramework'
                }
            ]
        }
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getNugetData();
            }
        });
    }

}
export function jfNuget() {
    return {
        restrict: 'EA',
        controller: jfNugetController,
        controllerAs: 'jfNuget',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_nuget.html'
    }
}