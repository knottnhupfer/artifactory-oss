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
import { assign } from 'lodash';
import CONFIG_MESSAGES from '../../constants/configuration_messages.constants';

export class AllBuildsController {
    constructor($scope, $q, $timeout, JFrogGridFactory, BuildsDao, JFrogEventBus, JFrogModal,
            JFrogNotifications, JFrogTableViewOptions, User, GoogleAnalytics) {

        this.$scope = $scope;
        // this.uiGridConstants = uiGridConstants;
        this.JFrogTableViewOptions = JFrogTableViewOptions;
        this.user = User.getCurrent();
        this.$timeout = $timeout;
        this.$q = $q;
        this.sortState = {
            direction: 'DESC',
            orderBy: 'build_date'
        };
        this.buildsDao = BuildsDao;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.GoogleAnalytics = GoogleAnalytics;
        this.artifactoryNotifications = JFrogNotifications;
        this.modal = JFrogModal;
        this.firstFetch = true;
        this.CONFIG_MESSAGES = CONFIG_MESSAGES.builds;
        this.allBuildsGridOptions = {};
    }

    $onInit() {
        this._createGrid();
    }

    // _getBuildsData(dontTrack) {
    //     this.buildsDao.get().$promise.then((data) => {
    //         let numOfBuilds = data && data.data ? data.data.length : 0;
    //         if (!dontTrack) this.GoogleAnalytics.trackEvent('Builds' , 'Number of builds' , numOfBuilds , numOfBuilds);
    //         this.buildsData = data.data || [];
    //
    //         this.tableViewOptions.setData(this.buildsData);
    //     });
    // }

    getPaginatedBuildsData(params, dontTrack) {
        return this.buildsDao.get(params).$promise.then((data) => {
            //TODO: Ask what to do here -> num of builds ?
            // const numOfBuilds = data && data.data ? data.data.length : 0;
            // if (!dontTrack) {
            //     this.GoogleAnalytics.trackEvent('Builds', 'Number of builds', numOfBuilds, numOfBuilds);
            // }
            return data
        });
    }

    _createGrid() {
        const paginationCallback = ({offset, numOfRows}) => {
            if(this.sorting){
                this.continueState = null;
                this.sorting = false;
            }
            const queryParams = assign({}, {
                limit: 50,
                continueState: this.continueState
            }, this.sortState);
            return this.getPaginatedBuildsData(queryParams)
                    .then((response => {
                        this.buildsData = response.data.length;
                        this.showNoDataMessage = !response.data.length;
                        this.continueState = response.continueState;
                        return assign({}, response, {
                            hasMore: !!response.continueState
                        });
                    }))

        };

        const externalSortCallback = (orderBy, direction) => {
            switch (orderBy) {
                case 'buildName':
                    orderBy='build_name';
                    break;
                case 'buildNumber':
                    orderBy='build_number';
                    break;
                case 'time':
                default:
                    orderBy='build_date';
                    break;
            }

            this.sortState.orderBy = orderBy;
            this.sortState.direction = direction.toUpperCase();
            this.sorting = true;
        };


        this.tableViewOptions = new this.JFrogTableViewOptions(this.$scope);
        this.tableViewOptions.setColumns(this._getColumns())
                .setRowsPerPage('auto')
                .setSelection(this.tableViewOptions.MULTI_SELECTION)
                .setActions(this._getActions())
                .setBatchActions(this.getBatchActions())
                .setEmptyTableText(this.CONFIG_MESSAGES.noBuildsDataMessage)
                .setEmptyTableAction(this.onEmptyTableAction.bind(this))
                .setEmptyTableCallToAction(this.CONFIG_MESSAGES.callToAction)
                .setPaginationMode(this.tableViewOptions.INFINITE_VIRTUAL_SCROLL, paginationCallback)
                .setSortable(true)
                .sortBy('time')
                .reverseSortingDir()
                .useExternalSortCallback(externalSortCallback);


        this.tableViewOptions.isRowSelectable = (row) => {
            return row.entity.canDelete;
        }
    }
    onEmptyTableAction() {
        window.open(this.CONFIG_MESSAGES.actionUrl);
    }
    onFilterChange(){
        this.continueState = null;
        this.sortState.searchStr = this.filter;
        this.tableViewOptions.dirCtrl.vsApi.reset();
        this.tableViewOptions.setData([]);
        this.tableViewOptions.sendInfiniteScrollRequest();
    }

    _getColumns() {
        let nameCellTemplate = '<div class="ui-grid-cell-contents"><a href class="jf-link" ui-sref="builds.build_page({buildName:row.entity.buildName,buildNumber:row.entity.buildNumber,startTime:row.entity.time})">{{row.entity.buildName}}</a></div>';
        let numberCellTemplate = `<div class="ui-grid-cell-contents">
                                       <a href="" class="jf-link" id="last-build-id" ui-sref="builds.build_page({buildName:row.entity.buildName,buildNumber:row.entity.buildNumber,startTime:row.entity.time})" >{{row.entity.buildNumber}}</a>
                                    </div>`;

        let timeCellTemplate = '<div class="ui-grid-cell-contents" id="last-build-time">{{row.entity.time | date: \'d MMMM, yyyy - HH:mm:ss Z\'}}</div>';

        return [
            {
                header: "Build Name",
                field: "buildName",
                cellTemplate: nameCellTemplate,
                filterable: true
            },
            {
                header: "Last Build ID",
                field: "buildNumber",
                cellTemplate: numberCellTemplate,
                width: '10%'
            },
            {
                header: "Last Build Time",
                cellTemplate: timeCellTemplate,
                field: "time",
                width: '15%'
            }
        ]
    }

    getBatchActions() {
        return [
            {
                icon: 'clear',
                name: 'Delete',
                callback: (selected) => this.bulkDelete(selected)
            }
        ]
    }

    bulkDelete(selected) {

        let confirmMessage = `Are you sure you wish to delete ${selected.length}`;
        confirmMessage += selected.length > 1 ? ' build projects?' : ' build project?';

        this.modal.confirm(confirmMessage)
                .then(() => {
                    let buildsToDelete = selected.map(build => {
                        return {buildName: build.buildName}
                    });
                    this._deleteBuilds({buildsCoordinates: buildsToDelete});
                });
    }

    deleteBuild(row) {
        this.modal.confirm("Are you sure you wish to delete all the builds '" + row.buildName + "'?")
                .then(() => {
                    this._deleteBuilds({buildsCoordinates: [{buildName: row.buildName}]});
                })
    }

    _deleteBuilds(json) {
        this.buildsDao.deleteAll(json).$promise.then(() => {
            this.tableViewOptions.dirCtrl.vsApi.reset();
            this.tableViewOptions.setData([]);
            this.tableViewOptions.sendInfiniteScrollRequest();
        });
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: row => this.deleteBuild(row),
                visibleWhen: (row) => row.canDelete
            }
        ];
    }
}
