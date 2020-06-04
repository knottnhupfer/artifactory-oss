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
export class LastDeployedController {
    constructor(HomePageDao, $timeout, ArtifactoryDeployModal, GoogleAnalytics) {

        this.homePageDao = HomePageDao;
        this.GoogleAnalytics = GoogleAnalytics;
        this.deployModal = ArtifactoryDeployModal;
        this.data = {};
        this.getData();
    }

    getData() {
        this.lastDeployed;

        this.homePageDao.get({widgetName: 'latestBuilds'}).$promise.then((data)=> {
            this.data = data.widgetData;
            this.lastDeployed = true;
            if (this.lastDeployed) {
                this.$widgetObject.showSpinner = false;
            }
        });
        this.dateTime = (new Date).getTime();
    }

    refresh() {
        this.$widgetObject.showSpinner = true;
        this.getData();
    }

    itemClick() {
        this.GoogleAnalytics.trackEvent('Homepage', 'Last deployed item click');
    }
}