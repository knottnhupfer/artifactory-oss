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
class jfBuildInfoJsonController {
    constructor($stateParams, $scope, BuildsDao, JFrogEventBus) {
        this.$stateParams = $stateParams;
        this.json = '';
        this.buildsDao = BuildsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.$scope = $scope;

        this._getJson();
        this.JFrogEventBus.registerOnScope(this.$scope, this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH, () => {
            this._getJson();
        });

        $(window).resize(() => {
            setTimeout(() => {
                this._calculateCodeMirrorHeight();
            }, 50);
        });

    }


    _getJson() {

        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildJson'
        }).$promise.then((data) => {
            this.json = data.fileContent;

            setTimeout(() => {
                this._calculateCodeMirrorHeight();
            }, 100);


        })
    }


    _calculateCodeMirrorHeight() {
        let codeMirrorElement = $('.CodeMirror');
        let buildWrapperElement = $('.builds-inner-wrapper');

        let buildWrapperOffset = buildWrapperElement.outerHeight() + buildWrapperElement.offset().top;
        let codeMirrorOffset = codeMirrorElement.outerHeight() + codeMirrorElement.offset().top;

        let codeMirrorHeight = buildWrapperOffset - codeMirrorOffset + codeMirrorElement.outerHeight() - 30;

        codeMirrorElement.css({height: codeMirrorHeight});
    };

}




export function jfBuildInfoJson() {
    return {
        restrict: 'EA',
        controller: jfBuildInfoJsonController,
        controllerAs: 'jfBuildInfoJson',
        scope: {
        },
        bindToController: true,
        templateUrl: 'states/builds/build_tabs/jf_build_info_json.html'
    }
}