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

class jfViewSourceController {
    constructor($scope, ArtifactViewSourceDao, JFrogEventBus) {
        this.sourceData = '';
        this.artifactViewSourceDao = ArtifactViewSourceDao.getInstance();
        this.JFrogEventBus = JFrogEventBus;
        this.$scope = $scope;
        this.editorOptions = {
            lineNumbers: true,
            readOnly: 'nocursor',
            lineWrapping: true,
            viewportMargin: Infinity
        };
    }

    $onInit() {
        this.loadSourceData();
        this._registerEvents();
    }

    loadSourceData() {
        if (this.currentNode.data.mimeType) {
            this.editorOptions.mode = this.currentNode.data.mimeType;
        }
        // get source path from general info
        let info = _.findWhere(this.currentNode.data.tabs, {name: 'General'}).info;
        if (info) {
            let sourcePath = info.path;
            // fetch source from server
            this.artifactViewSourceDao.fetch({
                "archivePath": this.currentNode.data.archivePath,
                "repoKey": this.currentNode.data.repoKey,
                "sourcePath": sourcePath
            }).$promise
                    .then((result) => {
                        this.sourceData = result.source;
                    })
        }

    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                this.loadSourceData();
            }
        });
    }
}
export function jfViewSource() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '='
        },
        controller: jfViewSourceController,
        controllerAs: 'jfViewSource',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_view_source.html'
    }
}