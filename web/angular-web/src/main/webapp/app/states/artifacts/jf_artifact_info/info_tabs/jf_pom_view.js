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

class jfPomViewController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus) {
        this.artifactPomViewDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.$scope = $scope;
    }

    $onInit() {
        this._initPomView();
    }

    _initPomView() {
        this._registerEvents();
        this._getPomViewData();
    }

    _getPomViewData() {

        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactPomViewDao.fetch({
            "view": "pom",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    this.pomViewData= data;
                    this.pomViewData.fileContent=data.fileContent.trim();
                })
    }

    _registerEvents() {
        let self = this;
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getPomViewData();
            }
        });
    }
}
export function jfPomView() {
    return {
        restrict: 'EA',
        controller: jfPomViewController,
        controllerAs: 'jfPomView',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_pom_view.html'
    }
}