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

class jfIvyViewController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus) {
        this.artifactIvyViewDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.$scope = $scope;
    }

    $onInit() {
        this._initIvyView();
    }

    _initIvyView() {
        this._registerEvents();
        this._getIvyViewData();
    }

    _getIvyViewData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactIvyViewDao.fetch({
            "view": "pom",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
                .then((data) => {
                    //console.log(data);
                    this.ivyViewData= data;
                    this.ivyViewData.fileContent=data.fileContent.trim();
                })
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                this._getIvyViewData();
            }
        });
    }
}
export function jfIvyView() {
    return {
        restrict: 'EA',
        controller: jfIvyViewController,
        controllerAs: 'jfIvyView',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_ivy_view.html'
    }
}