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

class jfDockerV2Controller {
    constructor($scope, $element, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory, JFrogTableViewOptions) {
        this.$scope = $scope;
        this.$element = $element;
        this.artifactViewsDao = ArtifactViewsDao;
        this.DICTIONARY = DICTIONARY.dockerV2;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.dockerV2Data = {};
        this.labelGridOptions = {};
        this.JFrogTableViewOptions = JFrogTableViewOptions;
    }

    $onInit() {
        this._getDockerV2Data();
        this._registerEvents();
    }

    _getDockerV2Data() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "dockerv2",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise.then((data) => {
            this.dockerV2Data = data;
            this._createGrid();
            if (this.layersController)
                this.layersController.refreshView();
        });
    }

    _createGrid() {
        if (this.dockerV2Data.tagInfo.labels) {
            if(!this.tableViewOptions) {
                this.tableViewOptions = new this.JFrogTableViewOptions(this.$scope)
                        .setColumns(this._getColumns())
                        .setRowsPerPage(5)
                        .setEmptyTableText('No Labels')
                        .setObjectName('Label');
                this.tableViewOptions.setData(this.dockerV2Data.tagInfo.labels);
            } else {
                this.tableViewOptions.setData(this.dockerV2Data.tagInfo.labels);
            }
        }
    }

    _getColumns() {
        return [
            {
                field: 'key',
                header: 'Key',
                width: '35%'
            },
            {
                field: 'value',
                header: 'Value',
                width: '35%'
            }]
    }

    isNotEmptyValue(value) {
        return value && (!_.isArray(value) || value.length > 0);
    }

    formatValue(value) {
        if (_.isArray(value)) {
            return value.join(', ');
        }
        else return value;
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getDockerV2Data();
        });
    }

    scrollToLabels() {
        document.getElementById("labels").scrollIntoView();
    }
}

export function jfDockerV2() {
    return {
        restrict: 'EA',
        controller: jfDockerV2Controller,
        controllerAs: 'jfDockerV2',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_docker_v2.html'
    }
}