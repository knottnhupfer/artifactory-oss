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
import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';

class jfConanPackageController {
    constructor($scope, $element, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.$scope = $scope;
        this.$element = $element;
        this.artifactViewsDao = ArtifactViewsDao;
        this.DICTIONARY = DICTIONARY.conan;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.conanPackageData = {};
        this.labelGridOptions = {};

    }

    $onInit() {
        this._getConanPackageData();
        this._registerEvents();
    }

    _getConanPackageData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "conan_package",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise.then((data) => {
            this.conanPackageData = data;
            if (this.layersController)
                this.layersController.refreshView();
        });
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            this.currentNode = node;
            this._getConanPackageData();
        });
    }

    isNonEmptyObject(val) {
        return val && Object.keys(val).length > 0;
    }

    hasSettings() {
        if (this.isNonEmptyObject(this.conanPackageData.settings)) {
            return true;
        }
        var knownSettings = this.knownSettings();
        for (var i in knownSettings) {
            if (this.conanPackageData[knownSettings[i]]) {
                return true;
            }
        }
        return false;
    }

    knownSettings() {
        return ['os', 'arch', 'buildType', 'compiler', 'compilerVersion', 'compilerRuntime'];
    }
}

export function jfConanPackage() {
    return {
        restrict: 'EA',
        controller: jfConanPackageController,
        controllerAs: 'jfConanPackage',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_conan_package.html'
    }
}