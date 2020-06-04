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
/**
 * Created by danny on 01/06/15.
 */
class jfAccordionController {

    constructor($state, $scope, $rootScope, ArtifactoryState) {
        this.state = $state;
        this.currentAccordion = null;
        this.artifactoryState = ArtifactoryState;
        this.scope = $scope;
        this.openItemByCurrentState();

        $rootScope.$on('$stateChangeSuccess', () => this.openItemByCurrentState());


        this._initWatchers();
    }

    openItemByCurrentState() {

        let item = _.find(this.items, (item) => {
            return _.contains(this.state.current.name, item.state);
        });
        if (item) {
            item.isOpen = true;
        }

        this.currentAccordion && this.artifactoryState.setState(this.currentAccordion, this.state.current);

    }

    isCurrentAccordion(item) {
        return this.currentAccordion === item.label;
    }

    getCurrentAccordion() {
        return this.currentAccordion;
    }

    setCurrentAccordion(item) {

        if (item) {
            if (!this.currentAccordion) {
                this.artifactoryState.setState(item.label, this.state.current);
            }
            this.currentAccordion = item.label;
            let state = this.artifactoryState.getState(this.currentAccordion);
            if (state) {
                this.state.go(state);
            }
        }
        //this.currentAccordion == item.label?this.currentAccordion = '':this.currentAccordion = item.label;
    }

    _initWatchers() {
        for (let i in this.items) {
            this.scope.$watch('jfAccordion.items[' + i + '].isOpen', (isOpen) => {
                if (isOpen) {
                    //console.log('current accordion: '+this.items[i].label);
                    this.setCurrentAccordion(this.items[i]);
                }
                else {
                    this.setCurrentAccordion(null);
                }
            });
        }
    }

}

export function jfAccordion() {

    return {
        restrict: 'EA',
        scope: {items: '='},
        controller: jfAccordionController,
        controllerAs: 'jfAccordion',
        templateUrl: 'directives/jf_accordion/jf_accordion.html',
        bindToController: true
    };
}

