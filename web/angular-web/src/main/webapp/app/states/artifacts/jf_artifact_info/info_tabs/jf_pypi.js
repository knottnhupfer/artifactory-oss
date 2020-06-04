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

class jfPyPiController {
    constructor($scope, ArtifactViewsDao, JFrogEventBus, JFrogGridFactory) {
        this.artifactViewsDao = ArtifactViewsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.DICTIONARY = DICTIONARY.pyPi;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.pyPiData = {};
        this.$scope = $scope;
        this.categoriesGridOptions = {};
    }

    $onInit() {
        this._initPyPi();
        this._createGrid();
    }

    _initPyPi() {
        this._registerEvents();
        this.getPyPiData();
    }

    _createGrid() {
        this.categoriesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getCategoriesColumns())
            .setRowTemplate('default');
    }

    _getCategoriesColumns() {
        return [
            {
                name: "Category",
                displayName: "Category",
                field: "category"
//                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity}}</div>'
            }
        ]
    }

    isValidUrl(str) {
        var regex = /^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?$/i;
        return regex.test(str);
    }

    getPyPiData() {
        //Temp fix for preventing fetching data for non-file nodes (occurred when pressing "Artifacts" on sidebar)
        if (!this.currentNode.data.path) {
            return;
        }

        this.artifactViewsDao.fetch({
            "view": "pypi",
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
            .then((data) => {
                this.pyPiData = data;
                this.categoriesGridOptions.setGridData(data.categories.map((cat) => {
                    return {category:cat};
                }));
            });
    }

    _registerEvents() {
        let self = this;

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (self.currentNode != node) {
                self.currentNode = node;
                self.getPyPiData();
            }
        });
    }

}
export function jfPyPi() {
    return {
        restrict: 'EA',
        controller: jfPyPiController,
        controllerAs: 'jfPyPi',
        scope: {
            currentNode: '='
        },
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_pypi.html'
    }
}