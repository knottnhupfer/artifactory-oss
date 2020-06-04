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

class jfBreadcrumbController {

    constructor($scope, $state, $stateParams, JFrogEventBus, $timeout) {

        this.$scope = $scope;
        this.$stateParams = $stateParams;
        this.$state = $state;
        this.JFrogEventBus = JFrogEventBus;
        this.$timeout = $timeout;
        this._registerEvents();

        this.updateBreadcrumb();
    }

    updateBreadcrumb() {
        this.$timeout(()=>{
            switch(this.$state.current.name) {
                case 'builds.all':
                    this._initCrumbs();
                    break;
                case 'builds.history':
                    this._initCrumbs();
                    this.crumbs.push({
                        name:this.$stateParams.buildName,
                        state:this.$state.current.name + '(' + JSON.stringify(this.$stateParams) + ')'
                    });
                    break;
                case 'builds.info':
                    this._initCrumbs();
                    this.crumbs.push({
                        name: this.$stateParams.buildName,
                        state: 'builds.history' + '(' + JSON.stringify({buildName:this.$stateParams.buildName}) + ')'
                    });
                    this.crumbs.push({
                        name: this.$stateParams.buildNumber,
                        state: 'builds.info' + '(' + JSON.stringify({buildName: this.$stateParams.buildName ,buildNumber: this.$stateParams.buildNumber}) + ')'                    });
                    break;
            }
        });
    }

    _initCrumbs() {
        this.crumbs = [
            {name:'All Builds',state:'builds.all({})'}
        ];
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.BUILDS_BREADCRUMBS, () => {
            this.updateBreadcrumb();
        });
    }


}

export function jfBreadcrumb() {

    return {
        restrict: 'E',
        scope: {
        },
        controller: jfBreadcrumbController,
        controllerAs: 'jfBreadcrumb',
        templateUrl: 'directives/jf_breadcrumb/jf_breadcrumb.html',
        bindToController: true
    };
}
