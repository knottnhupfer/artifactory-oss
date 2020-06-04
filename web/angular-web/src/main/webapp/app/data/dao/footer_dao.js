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
import EVENTS from '../../constants/artifacts_events.constants';

const VERSION_INFO_KEY = 'VERSION_INFO';
export class FooterDao {
    constructor(RESOURCE, ArtifactoryDaoFactory, ArtifactoryStorage, $timeout, JFrogEventBus) {
		this.storage = ArtifactoryStorage;
        this.$timeout = $timeout;
        this.JFrogEventBus = JFrogEventBus;
    	this._resource = ArtifactoryDaoFactory()
            .setPath(RESOURCE.FOOTER)
            .getInstance();
        this.retries = 0;
    }

    get(force = false) {

        if (this.retries >= 10) return this.cached;

        if (!this.cached || force) {
            this.cached = this._resource.get().$promise
                    .then(info => this._info = info);
        }

        //Fix for RTFACT-9873
        if (!this._info) {
            this.$timeout(()=> {
                if (!this._info) {
                    this.retries++;
                    this.get(true).then(()=> {
                        this.JFrogEventBus.dispatch(EVENTS.FOOTER_DATA_UPDATED);
                    });
                }
                else if (this._info) {
                    this.retries = 0;
                }
            }, 400);
        }
        else if (this._info) {
            this.retries = 0;
        }

        return this.cached;
    }

    getInfo() {
        return this._info;
    }
}
