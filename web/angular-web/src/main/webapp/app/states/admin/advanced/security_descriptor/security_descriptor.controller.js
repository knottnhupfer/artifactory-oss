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
export class AdminAdvancedSecurityDescriptorController {
    constructor($timeout, ArtifactoryHttpClient, JFrogNotifications, RESOURCE, ArtifactoryModelSaver) {
        this.artifactoryHttpClient = ArtifactoryHttpClient;
        this.artifactoryNotifications = JFrogNotifications;
        this.RESOURCE = RESOURCE;
        this.$timeout = $timeout;
        this.securityDescriptor = '';
        this.apiAccess = {};
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['securityDescriptor']);

        this._getData();
    }

    _getData() {
        this.artifactoryHttpClient.get(this.RESOURCE.SECURITY_DESCRIPTOR).then((response) => {
            this.securityDescriptor = response.data;
        this.ArtifactoryModelSaver.save();
            this.$timeout(()=> {
                this.apiAccess.api.clearHistory();
            });
        });
    }

    save(securityXML) {
        this.artifactoryHttpClient.put(this.RESOURCE.SECURITY_DESCRIPTOR, {securityXML}).
            success((response) => {
            this.ArtifactoryModelSaver.save();
                    this.artifactoryNotifications.create(response);
                }
        )
            .error((response) => {
                if (response.errors && response.errors.length) {
                    this.artifactoryNotifications.create(angular.fromJson(response.errors[0].message));
                }
            });
    }

    cancel() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this._getData();
        });
    }
}