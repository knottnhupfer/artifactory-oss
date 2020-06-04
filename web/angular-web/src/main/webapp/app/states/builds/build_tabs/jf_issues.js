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
class jfIssuesController {
    constructor($scope, $stateParams, JFrogEventBus, BuildsDao, JFrogGridFactory, uiGridConstants, commonGridColumns) {
        this.$stateParams = $stateParams;
        this.$scope = $scope;
        this.uiGridConstants=uiGridConstants;
        this.buildsDao = BuildsDao;
        this.gridOptions = {};
        this.artifactoryGridFactory = JFrogGridFactory;
        this.commonGridColumns = commonGridColumns;
        this.JFrogEventBus = JFrogEventBus;
        this.noData = false;

        this._createGrid();
        this._getIssuesData();

        this.JFrogEventBus.registerOnScope(this.$scope, this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH, () => {
            this._getIssuesData();
        })

    }

    _getIssuesData() {
        this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildIssues'
        }).$promise.then((data) => {
                if (data.length) {
                    this.gridOptions.setGridData(data);
                }
                else {
                    this.noData = true;
                }

            }).catch(() => {
                this.noData = true;
                this.gridOptions.setGridData([]);
        });
    }

    _createGrid() {

        this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setButtons(this._getActions());

    }

    _getColumns() {
        let cellTemplate = '<div class="ui-grid-cell-contents"><a target="_blank" ng-href="{{row.entity.url}}" class="jf-link" >{{row.entity.key}}</a></div>';

        return [
            {
                name: "Key",
                displayName: "Key",
                field: "key",
                cellTemplate: cellTemplate,
            },
            {
                name: "Summary",
                displayName: "Summary",
                field: "summary"
            },
            {
                name: "Previous Build",
                displayName: "Previous Build",
                field: "aggregated",
                cellTemplate: this.commonGridColumns.booleanColumn('row.entity.aggregated'),
                sort: {
                    direction: this.uiGridConstants.ASC
                }
            }

        ]
    }

    _getActions() {
        return [
        ];
    }

}


export function jfIssues() {
    return {
        restrict: 'EA',
        controller: jfIssuesController,
        controllerAs: 'jfIssues',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/build_tabs/jf_issues.html'
    }
}