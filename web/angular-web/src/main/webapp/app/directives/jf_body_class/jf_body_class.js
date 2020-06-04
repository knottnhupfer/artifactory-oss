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
export function jfBodyClass() {
    return {
        restrict: 'A',
        controller: jfBodyClassController,
        controllerAs: 'jfBodyClass',
        bindToController: true
    }
}

class jfBodyClassController {
    constructor($element, $state, $scope, ArtifactoryFeatures) {
        this.$element = $element;
        this.$state = $state;
        this.$scope = $scope;
        this.features = ArtifactoryFeatures;

        this._registerEvents();
    }

    $onInit() {
        this.features.whenReady.then(() => {
            if (this.features.isAol()) {
                this.loadWalkMeScript();
            }
        })
    }

    _registerEvents() {
        this.$scope.$on('$stateChangeSuccess', () => {
            this._setBodyClass()
        });
    }

    _setBodyClass() {
        this.$element.attr('class', this._formatCssClass(this.$state.$current.name) + (this.isLoadCompleted() ? ' load-complete' : ''));
    }

    _formatCssClass(stateName) {
        return stateName.replace(/\./g, '-');
    }

    isLoadCompleted() {

        return window.angular && angular.element(document.body).injector() && angular.element(document.body).injector().get("$http").pendingRequests.length == 0;
    }

    loadWalkMeScript() {
        let scriptTagString = `<script type="text/javascript">(function() {var walkme = document.createElement('script'); walkme.type = 'text/javascript'; walkme.async = true; walkme.src = 'https://cdn.walkme.com/users/0a2384c11b1c4515a35a67fe08b1b2c9/walkme_0a2384c11b1c4515a35a67fe08b1b2c9_https.js'; var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(walkme, s); window._walkmeConfig = {smartLoad:true}; })();</script>`;
        let scriptTag = $(scriptTagString);
        $(document.head).append(scriptTag)
    }
}