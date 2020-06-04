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

class jfBuildsController {
    constructor($state, JFrogGridFactory, ArtifactBuildsDao, $scope, JFrogEventBus, JFrogModal, uiGridConstants) {
        this.uiGridConstants = uiGridConstants;
        this.producedByGridOptions = {};
        this.usedByGridOptions = {};
        this.$state = $state;
        this.buildsDao = ArtifactBuildsDao.getInstance();
        this.$scope = $scope;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.modal = JFrogModal;
        this.mode = 'ProducedBy';
        this.builds = {};

    }

    $onInit() {
        this._generateViewBySwitch();
        this._registerEvents();
        this._createGrids();
        this._getBuildData();
    }

    downloadJson(build) {
        this.buildsDao.getJson({
            buildNumber: build.number,
            buildName: build.name,
            startTime: build.started
        })
            .$promise.then((result) => {
                this.modal.launchCodeModal('Build #' + build.number, result.json,
                    {name: "javascript", json: true});
            });
    }

    _getBuildData() {
        // if the node does not have a path the build cannot be loaded
        // this may occur in navigation to a node that does not have a path (repo node)
        if (!this.currentNode.data.path) {
            return;
        }

        this.buildsDao.query({
            path: this.currentNode.data.path,
            repoKey: this.currentNode.data.repoKey
        }).$promise.then((builds) => {
                    this.builds = builds;
                    this.producedByGridOptions.setGridData(builds.producedBy);
                    this.usedByGridOptions.setGridData(builds.usedBy);
                    this._generateViewBySwitch();
                });
    }

    _generateViewBySwitch() {
        this.viewBySwitch = [
            {
                text: 'Produced By' + (this.producedByGridOptions.data && this.producedByGridOptions.data.length ? ' (' + this.builds.producedBy.length + ')' : ''),
                value: 'ProducedBy'
            },
            {
                text: 'Used By' + (this.usedByGridOptions.data && this.usedByGridOptions.data.length ? ' (' + this.builds.usedBy.length + ')' : ''),
                value: 'UsedBy'
            }
        ];

        if (this.switchControl) {
            this.switchControl.options = this.viewBySwitch;
            this.switchControl.updateOptionObjects();
        }
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode !== node) {
                this.currentNode = node;
                self._getBuildData();
            }
        });
    }

    _createGrids() {
        this.producedByGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getProducedByColumns())
            .setRowTemplate('default')
            .setButtons(this._getActions());
        this.usedByGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getUsedByColumns())
            .setRowTemplate('default')
            .setButtons(this._getActions());
    }

    _getProducedByColumns() {
        let columns = this._getCommonColumns();
        columns.splice(4, 0, {
                displayName: 'Started At',
                name: 'Started At',
                cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.startedString }}</a>',
                field: "started",
                type: 'number'
            });
        return columns;
    }

    _getCommonColumns() {
        return [{
            displayName: 'Project Name',
            sort: {
                direction: this.uiGridConstants.ASC
            },
            name: 'Project Name',
            allowGrouping: true,
            grouped: true,
            field: "name",
            cellTemplate: '<div class="ui-grid-cell-contents" id="project-name" >{{row.entity.name}}</div>',
        }, {
            displayName: 'Build ID',
            name: 'Build ID',
            grouped: true,
            allowGrouping: true,
            field: "number",
            cellTemplate: '<div class="ui-grid-cell-contents"><a class="jf-link" ui-sref="builds.build_page({buildName:row.entity.name,buildNumber:row.entity.number,tab:\'general\',startTime:row.entity.started})" id="build-id">{{row.entity.number}}</a></div>'
        }, {
            name: 'Module ID',
            displayName: 'Module ID',
            allowGrouping: true,
            grouped: true,
            field: "moduleID",
            cellTemplate: '<div class="ui-grid-cell-contents"><a class="jf-link" ui-sref="builds.build_page({buildName:row.entity.name,buildNumber:row.entity.number,tab:\'published\',startTime:row.entity.started,moduleID:row.entity.moduleID})" id="module-id" >{{row.entity.moduleID}}</a></div>'
        }, {
            displayName: 'CI Server',
            name: 'CI Server',
            field: "ciUrl",
            cellTemplate: '<div class="ui-grid-cell-contents"><a class="jf-link" ng-href="{{row.entity.ciUrl}}" target="_blank" id="ci-server">{{row.entity.ciUrl}}</a></div>'
        }];
    }

    _getUsedByColumns() {
        let columns = this._getCommonColumns();
        columns.splice(3, 0, {
            displayName: 'Scope',
            name: 'Scope',
            allowGrouping: true,
            grouped: true,
            field: "scope"
        });
        return columns;
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-view',
                tooltip: 'View Build JSON',
                callback: row => this.downloadJson(row)
            }

        ];
    }
}

export function jfBuilds() {
    return {
        restrict: 'EA',
        controller: jfBuildsController,
        controllerAs: 'jfBuilds',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_builds.html'
    }
}

