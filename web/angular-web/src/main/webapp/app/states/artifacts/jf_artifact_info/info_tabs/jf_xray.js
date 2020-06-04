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
import MESSAGES from '../../../../constants/artifacts_messages.constants';

class jfXrayController {
    constructor($scope, JFrogEventBus, ArtifactXrayDao, ArtifactActionsDao, JFrogModal, ArtifactoryFeatures,ArtifactPropertyDao) {

        this.$scope = $scope;
        this.JFrogEventBus = JFrogEventBus;
        this.ArtifactXrayDao = ArtifactXrayDao;
        this.artifactActionsDao = ArtifactActionsDao;
        this.MESSAGES = MESSAGES;
        this.modal = JFrogModal;
        this.features = ArtifactoryFeatures;
        this.artifactPropertyDao = ArtifactPropertyDao.getInstance();

    }

    $onInit() {
        this._registerEvents();
        this._getXrayData();

    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode !== node) {
                this.currentNode = node;
                self._getXrayData();
            }
        });
    }

    _getXrayData() {
        if (this.features.isJCRDemo()) {
            console.log("Its Show Time!! ")
            this.artifactPropertyDao.get({
                path: this.currentNode.data.path,
                repoKey: this.currentNode.data.repoKey

            }).$promise.then(({artifactProperties})=> {
                console.log('currentProperty=',artifactProperties);
                this.version =  _.findWhere(artifactProperties, {name: "docker.manifest"}).value;
                this.package_id =  `docker://${ _.findWhere(artifactProperties, {name: "docker.repoName"}).value}`;
                this.showWidget = true
            });

        }else{
            this.ArtifactXrayDao.getData(
                    {repoKey: this.currentNode.data.repoKey, path: this.currentNode.data.path}).$promise.then(
                    (response) => {
                        this.artifactXrayData = response.data;
                    });
        }

    }

    _doAllowDownload() {
        this.modal.confirm(
                "Download will be allowed until Xray runs another scan that generates an alert for this artifact.",
                'Allow download')
                .then(() => {
                    this.artifactActionsDao.perform({
                        action: 'allowDownload',
                        params: 'true',
                        repoKey: this.currentNode.data.repoKey,
                        path: this.currentNode.data.path
                    }).$promise.then((data) => {
                        this._getXrayData();
                    })
                });
    }

    xrayAlertMessage() {
        if (this.artifactXrayData.allowBlockedArtifacts) {
            return this.MESSAGES.xray_tab.blocked_artifact_ignored;
        }
        return this.MESSAGES.xray_tab.blocked_artifact;
    }

}

export function jfXray() {
    return {
        restrict: 'EA',
        controller: jfXrayController,
        controllerAs: 'jfXray',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_xray.html'
    }
}
