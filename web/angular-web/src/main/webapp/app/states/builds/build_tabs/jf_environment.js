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

class jfEnvironmentController {
    constructor($scope, BuildsDao, JFrogGridFactory, $stateParams, uiGridConstants, JFrogEventBus) {
        this.$scope = $scope;
        this.$stateParams = $stateParams;
        this.buildsDao = BuildsDao;
        this.uiGridConstants = uiGridConstants;
        this.environmentGridOptions = {};
        this.systemGridOptions = {};
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.DICTIONARY = DICTIONARY.generalInfo;
        this._getEnvironmentData();
        this._createGrids();

        this.JFrogEventBus.registerOnScope(this.$scope, this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH, () => {
            this._getEnvironmentData();
        })

    }

    _getEnvironmentData() {
        this._getEnvVars();
        this._getSysVars();
    }

    _getEnvVars() {
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildProps',
            subAction: 'env',
            orderBy: 'key',
            numOfRows: 25,
            pageNum: 1,
            direction: 'asc'
        }).$promise.then((data) => {
                this.environmentGridOptions.setGridData(data.pagingData || []);
            });
    }

    _getSysVars() {
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildProps',
            subAction: 'system',
            orderBy: 'key',
            numOfRows: 25,
            pageNum: 1,
            direction: 'asc'
        }).$promise.then((data) => {
                this.systemGridOptions.setGridData(data.pagingData || []);
            });
    }

    _createGrids() {

        this.environmentGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setGridData([]);

        this.systemGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this._getColumns())
            .setRowTemplate('default')
            .setGridData([]);
    }

    _getColumns() {
        return [
            {
                name: "Key",
                displayName: "Key",
                field: "key",
                sort: {
                    direction: this.uiGridConstants.ASC
                }
            },
            {
                name: "Value",
                displayName: "Value",
                field: "value"
            }
        ]
    }
}

export function jfEnvironment() {
    return {
        restrict: 'EA',
        controller: jfEnvironmentController,
        controllerAs: 'jfEnvironment',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/build_tabs/jf_environment.html'
    }
}