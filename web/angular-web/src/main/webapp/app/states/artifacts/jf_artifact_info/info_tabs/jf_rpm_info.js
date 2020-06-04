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

class jfRpmController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.rpm;
        this.gridProvideOptions = {};
        this.gridRequireOptions = {};
        this.gridObsoleteOptions = {};
        this.gridConflictOptions = {};
        this.rpmData = {};
        this.$scope = $scope;
    }

    $onInit() {
        this._initRpm();
    }

    _initRpm() {
        this._registerEvents();
        this.getRpmData();
    }

    getRpmData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "rpm",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.rpmData = data;
                    this._createGrid();
                });
    }

    _createGrid() {
        if (this.rpmData.provide) {
            if (!Object.keys(this.gridProvideOptions).length) {
                this.gridProvideOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setRowTemplate('default')
                        .setColumns(this._getColumns('provide'))
                        .setGridData(this.rpmData.provide)
            }
            else {
                this.gridProvideOptions.setGridData(this.rpmData.provide)
            }
        }
        if (this.rpmData.require) {
            if (!Object.keys(this.gridRequireOptions).length) {
                this.gridRequireOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setColumns(this._getColumns())
                        .setRowTemplate('default')
                        .setGridData(this.rpmData.require)
            }
            else {
                this.gridRequireOptions.setGridData(this.rpmData.require);
            }
        }
        if (this.rpmData.obsolete) {
            if (!Object.keys(this.gridObsoleteOptions).length) {
                this.gridObsoleteOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setColumns(this._getColumns())
                        .setRowTemplate('default')
                        .setGridData(this.rpmData.obsolete)
            }
            else {
                this.gridObsoleteOptions.setGridData(this.rpmData.obsolete);
            }
        }

        if (this.rpmData.conflict) {
            if (!Object.keys(this.gridConflictOptions).length) {
                this.gridConflictOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                        .setColumns(this._getColumns())
                        .setRowTemplate('default')
                        .setGridData(this.rpmData.conflict)
            }
            else {
                this.gridConflictOptions.setGridData(this.rpmData.conflict);
            }
        }
    }

    _getColumns() {
        return [
            {
                name: 'Name',
                displayName: 'Name',
                field: 'name',
                width: '25%'
            },
            {
                name: 'Flags',
                displayName: 'Flags',
                field: 'flags',
                width: '15%'
            },
            {
                name: 'Epoch',
                displayName: 'Epoch',
                field: 'epoch',
                width: '15%'
            },
            {
                name: 'Version',
                displayName: 'Version',
                field: 'version',
                width: '15%'
            }, {
                name: 'Release',
                displayName: 'Release',
                field: 'release',
                width: '15%'
            },
            {
                name: 'Pre',
                displayName: 'Pre',
                field: 'pre',
                width: '15%'
            }]
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getRpmData();
            }
        });
    }

}
export function jfRpm() {
    return {
        restrict: 'EA',
        controller: jfRpmController,
        controllerAs: 'jfRpm',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_rpm_info.html'
    }
}