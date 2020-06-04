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
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';
import FIELD_OPTIONS from '../../../../constants/field_options.constats';
import EVENTS from "../../../../constants/artifacts_events.constants";

export class AdminAdvancedStorageSummaryController {
    constructor($scope, $timeout, JFrogGridFactory, uiGridConstants, commonGridColumns, $compile,
                ArtifactoryFeatures, JFrogEventBus, StorageSummaryCachedDao, $interval) {
        this.$scope = $scope;
        this.$compile = $compile;
        this.$timeout = $timeout;
        this.commonGridColumns = commonGridColumns;
        this.storageSummary = {};
        this.gridOption = {};
        this.uiGridConstants = uiGridConstants;
        this.storageSummaryCachedDao = StorageSummaryCachedDao;
        this.features = ArtifactoryFeatures;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.TOOLTIP = TOOLTIP.admin.advanced.storageSummary;
        this.counterTooltip = 'List includes all Local and Virtual repositories, and Remote repositories configured to store artifacts locally.'
        this.binariesKeys = ['binariesSize', 'binariesCount', 'artifactsSize', 'artifactsCount', 'optimization', 'itemsCount'];
        this.JFrogEventBus = JFrogEventBus;

        this.getGridPopulatedWithData();

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.REFRESH_PAGE_CONTENT, () => {
            this.getGridPopulatedWithData();
        });
        this.disableRefresh = true;
        this.$interval = $interval;
        this.fetchStatus();
        this.intervalPromise = this.$interval(this.fetchStatus.bind(this), 5000);
        this.$scope.$on("$destroy", () => {
            this.$interval.cancel(this.intervalPromise);
        });
    }

    getGridPopulatedWithData() {

        this.storageSummaryCachedDao.getStorageInfo().$promise.then((result) => {
            this.storageSummary = result;
            this.binariesSummary = result.binariesSummary;
            // Creates a repository array by running each repo element in the list through the arrow function
            this.storageSummary.repositoriesSummaryList = _.map(this.storageSummary.repositoriesSummaryList, (row) => {
                return this.setRowTemplate(row);
            });

            this.getStorageTableSummaryData();

            // If grid does not exists - create it , else refresh the grid data
            if (!this.gridOption.data) {
                this.createGrid();
            } else {
                this.gridOption.setGridData(this.storageSummary.repositoriesSummaryList);
            }
        });
    }

    refresh() {
        this.storageSummaryCachedDao.refreshStorageSummary();
        this.disableRefresh = true;
        this.calculating = true;
    }

    fetchStatus() {
        if (!this.fetchingStatus) {
            this.fetchingStatus = true;
            this.storageSummaryCachedDao.fetchStatus().$promise.then((result) => {
                this.disableRefresh = result.calculating;
                if(this.calculating && !result.calculating){
                    this.getGridPopulatedWithData();
                }
                this.calculating = result.calculating;
                this.fetchingStatus = false;
            })


        }
    }

    setRowTemplate(row) {

        row = this.getStorageTableSummaryTamplate(row);

        row = this.getDataColumns(row);

        row = this.getPackageTypeColumn(row);

        return row;
    }

    getDataColumns(row) {
        let repoKey = row.repoKey;
        for (let key in row) {
            if (key !== '__doNotCount__' && key !== 'percentageDisplay') {
                row[key] = {value: row[key], repoKey: repoKey, getCtrl: () => this};
            }
        }
        return row;
    }

    getPackageTypeColumn(row) {
        let rowPackageType = _.find(FIELD_OPTIONS.repoPackageTypes, (type) => {
            return ((type.serverEnumName == row.packageType.value) ||
                (type.serverEnumName == 'YUM' && row.packageType.value == 'RPM'));
            // The REST for storage has changed and now returns RPM, while other RESTs returns YUM
        });

        if (rowPackageType) {
            row.typeIcon = rowPackageType.icon;
            // set the correct package name (from FIELD_OPTIONS constants)
            row.packageType.value = rowPackageType.text;
        }

        if (row.packageType.value === 'Trash') {
            row.typeIcon = 'trash';
        }

        if (row.packageType.value === 'Support Bundle') {
            row.typeIcon = 'support';
        }

        if (row.packageType.value === 'Distribution') {
            row.typeIcon = 'distribution-repo';
        }

        return row;
    }

    getStorageTableSummaryTamplate(row, repoKey) {
        if (repoKey === 'TOTAL') {
            row.percentage = 100;
            row.percentageDisplay = '100%';
        } else {
            row.percentage = !_.isNaN(parseFloat(row.percentage)) ? parseFloat(row.percentage) : row.percentage;
            row.percentageDisplay = _.isNumber(row.percentage) ? row.percentage + '%' : 'N/A';
        }

        if (row.repoType === 'NA') row.repoType = 'N/A';
        if (row.packageType === 'NA') row.packageType = 'N/A';

        if (row.repoKey === 'TOTAL' || row.repoKey === 'auto-trashcan' || row.repoKey === 'jfrog-support-bundle') {
            row['__doNotCount__'] = true;
            row.packageType = 'N/A';
            row._specialRow = true;
        }

        if (row.repoKey === 'auto-trashcan') {
            row.trashcan = true;
            row.packageType = 'Trash';
            row.repoKey = "Trash Can";
        }

        if (row.repoKey === 'jfrog-support-bundle') {
            row.packageType = 'Support Bundle';
            row.repoKey = "Support Bundle";
        }

        return row;
    }

    getStorageTableSummaryData() {
        //This is for assuring that even without sorting, total will always be first and trash will be second
        let total = _.findWhere(this.storageSummary.repositoriesSummaryList, {repoKey: {value: 'TOTAL'}});
        let trash = _.findWhere(this.storageSummary.repositoriesSummaryList, {repoKey: {value: 'Trash Can'}});
        let supportBundle = _.findWhere(this.storageSummary.repositoriesSummaryList, {repoKey: {value: 'Support Bundle'}});

        if (supportBundle) {
            let supportBundleIndex = this.storageSummary.repositoriesSummaryList.indexOf(supportBundle);
            this.storageSummary.repositoriesSummaryList.splice(supportBundleIndex, 1);
            this.storageSummary.repositoriesSummaryList.unshift(supportBundle);
        }


        let totalIndex = this.storageSummary.repositoriesSummaryList.indexOf(total);
        this.storageSummary.repositoriesSummaryList.splice(totalIndex, 1);

        let trashIndex = this.storageSummary.repositoriesSummaryList.indexOf(trash);
        this.storageSummary.repositoriesSummaryList.splice(trashIndex, 1);

        this.storageSummary.repositoriesSummaryList.unshift(trash);
        this.storageSummary.repositoriesSummaryList.unshift(total);

        if (this.storageSummary.fileStoreSummary && this.storageSummary.fileStoreSummary.storageDirectory.indexOf(', ') != -1) {
            this.storageSummary.fileStoreSummary.storageDirectory = '<div class="storage-multiple-mounts">' + this.storageSummary.fileStoreSummary.storageDirectory.replace(/, /g, '<br>') + '</div>';
            this.storageSummary.fileStoreSummary.storageType = 'Advanced Configuration';
        }
    }

    createGrid() {
        this.gridOption = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setColumns(this.getColumns())
            .setGridData(this.storageSummary.repositoriesSummaryList)
            .setRowTemplate('default');

        this.gridOption.afterRegister((gridApi) => {
            gridApi.pagination.on.paginationChanged(this.$scope, (pageNumber, pageSize) => {
                let specialsToRemove = $('.ui-grid-row.special-row');
                specialsToRemove.removeClass('special-row');
                this.$timeout(() => {
                    let specials = $('.special-row');
                    specials.parent().parent().addClass('special-row');
                    specials.removeClass('special-row')
                }, 100)
            });
        });

        this.$timeout(() => {
            let counterElem = $('.grid-counter');
            let tooltipElem = $('<jf-help-tooltip html="StorageSummaryController.counterTooltip"></jf-help-tooltip>');
            counterElem.append(tooltipElem);
            this.$compile(tooltipElem)(this.$scope);

            let specials = $('.special-row');
            specials.parent().parent().addClass('special-row');
            specials.removeClass('special-row')

        })
    }

    sortGeneral(a, b, column) {
        let dir = 'asc';
        let ctrl = a.getCtrl();
        if (column) {
            dir = _.findWhere(ctrl.gridOption.api.grid.columns, {field: column}).sort.direction;
        }
        if (a.repoKey === 'TOTAL') return dir === 'desc' ? 1 : -1;
        else if (b.repoKey === 'TOTAL') return dir === 'desc' ? -1 : 1;
        else if (a.repoKey === 'Trash Can') return dir === 'desc' ? 1 : -1;
        else if (b.repoKey === 'Trash Can') return dir === 'desc' ? -1 : 1;
        else if (a.repoKey === 'Support Bundle') return dir === 'desc' ? 1 : -1;
        else if (b.repoKey === 'Support Bundle') return dir === 'desc' ? -1 : 1;
        else return a.value > b.value ? 1 : a.value < b.value ? -1 : 0;
    }

    sortByteSizes(a, b, column) {
        let dir = 'asc';
        let ctrl = a.getCtrl();

        if (column) {
            dir = _.findWhere(ctrl.gridOption.api.grid.columns, {field: column}).sort.direction;
        }

        let res = 0;
        if (a === undefined || b === undefined) return res;

        if (a.repoKey === 'TOTAL') return dir === 'desc' ? 1 : -1;
        else if (b.repoKey === 'TOTAL') return dir === 'desc' ? -1 : 1;
        else if (a.repoKey === 'Trash Can') return dir === 'desc' ? 1 : -1;
        else if (b.repoKey === 'Trash Can') return dir === 'desc' ? -1 : 1;
        else if (a.repoKey === 'Support Bundle') return dir === 'desc' ? 1 : -1;
        else if (b.repoKey === 'Support Bundle') return dir === 'desc' ? -1 : 1;
        else {
            var tb = [a.value.match('TB'), b.value.match('TB')],
                gb = [a.value.match('GB'), b.value.match('GB')],
                mb = [a.value.match('MB'), b.value.match('MB')],
                kb = [a.value.match('KB'), b.value.match('KB')]

            res = (tb[0] && !tb[1]) ? 1 : (tb[1] && !tb[0]) ? -1 :
                (gb[0] && !gb[1]) ? 1 : (gb[1] && !gb[0]) ? -1 :
                    (mb[0] && !mb[1]) ? 1 : (mb[1] && !mb[0]) ? -1 :
                        (kb[0] && !kb[1]) ? 1 : (kb[1] && !kb[0]) ? -1 :
                            (parseFloat(a.value.match(/[+-]?\d+(\.\d+)?/)[0]) > parseFloat(b.value.match(/[+-]?\d+(\.\d+)?/)[0])) ? 1 : -1
        }

        return res;
    }

    getColumns() {
        return [
            {
                field: "repoKey",
                name: "Repository Key",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'repoKey'),
                cellTemplate: '<div class="ui-grid-cell-contents" id="repoKey">{{row.entity.repoKey.value}}</div>',
                displayName: "Repository Key"
            },
            {
                field: "repoType",
                name: "Repository Type",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'repoType'),
                cellTemplate: '<div class="ui-grid-cell-contents" id="repoType">{{row.entity.repoType.value}}</div>',
                displayName: "Repository Type"
            },
            {
                field: "packageType",
                name: "Package Type",
                displayName: "Package Type",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'packageType'),
                cellTemplate: this.commonGridColumns.iconColumn('row.entity.packageType.value', 'row.entity.typeIcon', 'repo-type-icon')
            },
            {
                field: "percentage",
                cellTemplate: '<div class="ui-grid-cell-contents text-center" id="storage-precentage" >{{row.entity.percentageDisplay}}</div>',
                name: "Percentage",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'percentage'),
                displayName: "Percentage"
            },
            {
                field: "usedSpace",
                name: "Used Space",
                displayName: "Artifacts Size",
                cellTemplate: '<div class="ui-grid-cell-contents text-center" id="used-space" >{{row.entity.usedSpace.value}}</div>',
                sortingAlgorithm: (a, b) => this.sortByteSizes(a, b, 'usedSpace'),
                sort: {
                    direction: this.uiGridConstants.DESC
                }
            },
            {
                field: "filesCount",
                name: "Files",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'filesCount'),
                cellTemplate: '<div class="ui-grid-cell-contents" id="files" >  {{row.entity.filesCount.value}}</div>',
                displayName: "Files"
            },
            {
                field: "foldersCount",
                name: "Folders",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'foldersCount'),
                cellTemplate: '<div class="ui-grid-cell-contents" id="folders" >{{row.entity.foldersCount.value}}</div>',
                displayName: "Folders"
            },
            {
                field: "itemsCount",
                name: "Items",
                sortingAlgorithm: (a, b) => this.sortGeneral(a, b, 'itemsCount'),
                cellTemplate: '<div class="ui-grid-cell-contents" id="items" >{{row.entity.itemsCount.value}}</div>',
                displayName: "Items"
            }

        ]
    }
}