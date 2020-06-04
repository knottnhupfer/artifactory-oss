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

class jfDockerAncestryController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, $q) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.dockerAncestry;
        this.dockerAncestryData = {};
        this.$scope = $scope;
        this.$q = $q;
    }

    $onInit() {
        this._initDockerAncestry();
    }

    _initDockerAncestry() {
        this._registerEvents();
        this.getDockerAncestryData();
    }

    getDockerAncestryData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this._findAncestryJsonNode()
            .then((node) => {
                return this.artifactViewsDao.fetch({
                    "view": "dockerancestry",
                    "repoKey": node.repoKey,
                    "path": node.path
                }).$promise;
            })
            .then((data) => {
                this.dockerAncestryData = this._linkedListToArray(data.dockerLinkedImage);
            });
    }


    _findAncestryJsonNode() {
        return this.currentNode.data.getChildren()
        .then((data) => {
            for (var i=0; i<data.length;i++) {
                if (data[i].text === 'ancestry.json') {
                    return data[i];
                }
            }
            return this.$q.reject();
        });
    }

    gotoPath(index) {
        var repoKey = this.currentNode.data.repoKey;
        var fullpath = repoKey + '/' + this.dockerAncestryData[index].path;

        this.JFrogEventBus.dispatch(EVENTS.TREE_NODE_OPEN, fullpath);
    }

    _linkedListToArray(linkedData) {
        var arr = [];
        var curr = linkedData;
        var indent = 1;
        while (curr) {
            var rec = {id: curr.id,
                       size: curr.size,
                       path: curr.path,
                       indent: '|' + '__'.repeat(indent)};
            arr.push(rec);
            curr = curr.child;
            indent++;
        }
        return  arr;
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self.getDockerAncestryData();
            }
        });
    }

}
export function jfDockerAncestry() {
    return {
        restrict: 'EA',
        controller: jfDockerAncestryController,
        controllerAs: 'jfDockerAncestry',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_docker_ancestry.html'
    }
}