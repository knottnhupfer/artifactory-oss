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
export class BaseController {
    constructor(FooterDao,ArtifactorySidebarDriver,$timeout) {

        this.FooterDao = FooterDao;
        this.$timeout = $timeout;

        this.getFooterData();

        this.sidebarDriver = ArtifactorySidebarDriver;
    }

    getFooterData(force = false) {
        // Ensure page is not displayed before we get the footer data
        this.FooterDao.get(force).then(footerData => this.footerData = footerData);


        // Check that we have the footer data, solve RTFACT-13069 (Happens inconsistently when restarting server / starting vanilla)
        this.$timeout(()=>{
            if (!this.footerData) {
                this.getFooterData(true);
            }
        },100)
    }
}