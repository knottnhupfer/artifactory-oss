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

class jfDockerController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.docker;
        this.dockerData = {};
        this.$scope = $scope;
    }

    $onInit() {
        this._initDocker();
    }

    _initDocker() {
        this._registerEvents();
        this.getDockerData();
    }

    gotoPath(key) {
        var repoKey = this.currentNode.data.repoKey;
        var pathField = key === 'imageId' ? 'imageIdPath' : (key === 'parent' ? 'parentIdPath' : undefined);
        if (pathField) {
            this.JFrogEventBus.dispatch(EVENTS.TREE_NODE_OPEN,
                                              repoKey + '/' + this.dockerData.dockerInfo[pathField]);
        }
    }

    getDockerData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "docker",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path + '/json.json'
        }).$promise.then((data) => {
            this.dockerData = data;
        });
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getDockerData();
            }
        });
    }



}
export function jfDocker() {
    return {
        restrict: 'EA',
        controller: jfDockerController,
        controllerAs: 'jfDocker',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_docker.html'
    }
}