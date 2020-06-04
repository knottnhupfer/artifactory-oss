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

export class AdminAdvancedConfigDescriptorController {

    constructor($scope,$timeout, ArtifactoryHttpClient, JFrogNotifications, RESOURCE, ArtifactoryModelSaver, JFrogEventBus) {
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.RESOURCE = RESOURCE;
        this.artifactoryNotifications = JFrogNotifications;
        this.artifactoryHttpClient = ArtifactoryHttpClient;
        this.configDescriptor = '';
        this.apiAccess = {};
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['configDescriptor']);
        this.JFrogEventBus = JFrogEventBus;

        this._getData();

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.REFRESH_PAGE_CONTENT,()=>{
            this._getData();
        });
    }

    _getData() {
        this.artifactoryHttpClient.get(this.RESOURCE.CONFIG_DESCRIPTOR).then((response) => {
                this.configDescriptor = response.data;
        this.ArtifactoryModelSaver.save();
                this.$timeout(()=> {
                    this.apiAccess.api.clearHistory();
                });
            }
        );
    }

    save(configXml) {
        this.artifactoryHttpClient.put(this.RESOURCE.CONFIG_DESCRIPTOR, {configXml})
            .then(response => {
                this.ArtifactoryModelSaver.save();
                this.artifactoryNotifications.create(response.data);
            })
            .catch(response => {
                if (response.data.errors && response.data.errors.length) {
                    this.artifactoryNotifications.create(angular.fromJson(response.data.errors[0].message));
                }
            });
    }

    cancel() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this._getData();
        });
    }

}
