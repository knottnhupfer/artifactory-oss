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
export class ArtifactCountController {
    constructor(HomePageDao) {
        this.homePageDao = HomePageDao;
        this.initHomePage();
        this.updateCount();
    }

    initHomePage() {
        this.homePageDao.get({widgetName:'artifactCount'}).$promise.then((data)=> {
            this.homepageData = data.widgetData;
            this.$widgetObject.showSpinner = false;
        });
    }

    updateCount() {
        this.$widgetObject.showSpinner = true;
        this.initHomePage();

    }

}