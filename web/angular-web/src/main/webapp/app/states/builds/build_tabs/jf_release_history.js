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
import DICTIONARY from './../constants/builds.constants';

class jfReleaseHistoryController {
    constructor($stateParams, $scope, BuildsDao, JFrogEventBus) {
        this.$stateParams = $stateParams;
        this.buildsDao = BuildsDao;
        this.historyData = null;
        this.DICTIONARY = DICTIONARY.releaseHistory;
        this.JFrogEventBus = JFrogEventBus;
        this.$scope = $scope;

        this._getData();

        this.JFrogEventBus.registerOnScope(this.$scope, this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH, () => {
            this._getData();
        })

    }

    _getData() {
        return this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action:'releaseHistory'
        }).$promise.then((data) => {
            this.historyData = data;
        }).catch(() => {
            this.historyData = [];
        });
    }
}

export function jfReleaseHistory() {
    return {
        restrict: 'EA',
        controller: jfReleaseHistoryController,
        controllerAs: 'jfReleaseHistory',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/build_tabs/jf_release_history.html'
    }
}