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
import DICTIONARY from "./../constants/builds.constants";

const defaultPagination = {
    pageNum: 1,
    numOfRows: 25,
    direction: "asc",
    orderBy: "name"
}


class jfPublishedModulesController {
    constructor($scope, $state, $stateParams, $q, BuildsDao, ArtifactBuildsDao, ArtifactActionsDao, JFrogGridFactory,
                JFrogDownload, JFrogModal, ArtifactoryFeatures, JFrogEventBus, $timeout, uiGridConstants,
                commonGridColumns,
                User, ArtifactoryStorage) {
        this.$timeout = $timeout;
        this.$q = $q;
        this.$stateParams = $stateParams;
        this.$state = $state;
        this.$scope = $scope;
        this.uiGridConstants = uiGridConstants;
        this.commonGridColumns = commonGridColumns;
        this.initialModuleId = $stateParams.moduleID;
        this.buildsDao = BuildsDao;
        this.artifactActionsDao = ArtifactActionsDao;
        this.artifactBuildsDao = ArtifactBuildsDao.getInstance();
        this.download = JFrogDownload;
        this.modal = JFrogModal;
        this.modulesGridOptions = {};
        this.artifactsGridOptions = {};
        this.dependenciesGridOptions = {};
        this.artifactoryGridFactory = JFrogGridFactory;
        this.JFrogEventBus = JFrogEventBus;
        this.user = User;
        this.modulesCount = 0;
        this.artifactsCount = 0;
        this.dependenciesCount = 0;
        this.DICTIONARY = DICTIONARY.generalInfo;
        this.selectedModule = null;
        this.artifactoryStorage = ArtifactoryStorage;
        this.comparableNumbers = [''];
        this.selectedBuildNumber = '';
        this.artifactoryFeatures = ArtifactoryFeatures;

        if (this.$stateParams.startTime) {
            this.init();
        }
        else {
            this.getBuildTime().then(()=>{
                this.init();
            })
        }

        this.JFrogEventBus.registerOnScope(this.$scope, this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH, () => {
            this.init();
        })
    }

    getBuildTime() {
        let defer = this.$q.defer();
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            action:'buildInfo'
        }).$promise.then((data) => {
            this.$stateParams.startTime = data.time;
            this.$state.transitionTo('.', this.$stateParams, { location: 'replace', inherit: true, relative: this.$state.$current, notify: false })
            defer.resolve();
        });
        return defer.promise;
    }

    init() {
        this._getComparableBuildNumbers();
        this._createGrids();

        if (this.$stateParams.moduleID) {
            this.selectedModule = this.$stateParams.moduleID;
            this.getSubData();
        }
        else {
            this.selectedModule = null;
        }
    }

    showArtifactInTree(row) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') browser = 'tree';
        let path = row.repoKey + '/' + row.path;
        this.$state.go('artifacts.browsers.path', {
            tab: "General",
            artifact: path,
            browser: browser
        });
    }

    downloadArtifact(row) {
        this.download(row.downloadLink);
    }

    viewCodeArtifact(row) {
        this.artifactActionsDao.perform(
                {action: 'view'},
                {
                    repoKey: row.repoKey,
                    path: row.path
                })
                .$promise.then((result) => {
                    this.modal.launchCodeModal(row.name, result.data.fileContent,
                            {name: row.type, json: true});
                });
    }

    selectModule(entity) {
        if (!this.artifactoryFeatures.isDisabled("publishedmodule")) {
            _.extend(this.$stateParams, {
                buildName: this.$stateParams.buildName,
                buildNumber: this.$stateParams.buildNumber,
                startTime: this.$stateParams.startTime,
                tab: 'published ',//this.$stateParams.tab,
                moduleID: entity.moduleId
            });
            this.$state.go('.', this.$stateParams, { location: true, inherit: true, relative: this.$state.$current, notify: false, reload: true }).then(() => {
                this.init();
            })
        }

    }

    getSubData() {
        if (this.compare && this.selectedBuildNumber && this.selectedBuildNumber.buildNumber) {
            this._getArtifactsDiff();
            this._getDependenciesDiff();
        }
        else {
            this._getArtifacts();
            this._getDependencies();
        }
    }

    onCompareChanged() {
        if (!(this.selectedBuildNumber && this.selectedBuildNumber.buildNumber)) {
            // Don't get data if haven't selected build number yet
            return;
        }
        this.getSubData();
    }

    _getModulesData() {
        this.buildsDao.getData(defaultPagination, {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'publishedModules'
        })
                .$promise.then((data) => {
                    this.modulesCount = data.pagingData.length;
                    this.modulesGridOptions.setGridData(data.pagingData);
                    if (this.initialModuleId) {
                        this.initialModuleId = null;
                        let module = _.findWhere(data.pagingData, {moduleId: this.$stateParams.moduleID})
                        //                        this.modulesGridOptions.selectItem(module);
                    }
                });
    }

    _getArtifacts() {
        this.buildsDao.getData(defaultPagination, {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'modulesArtifact',
            moduleId: this.selectedModule
        }).$promise.then((data) => {
                    this.artifactsCount = data.pagingData.length;
                    this.artifactsGridOptions.setGridData(data.pagingData);
                });
    }

    _getDependencies() {

        let defaultPagination = {
            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "id"
        };
        this.buildsDao.getData(defaultPagination, {
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'modulesDependency',
            moduleId: this.selectedModule
        }).$promise.then((data) => {
                    this.dependenciesCount = data.pagingData.length;
                    this.dependenciesGridOptions.setGridData(data.pagingData);
                });

    }

    _getArtifactsDiff() {
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'artifactDiff',
            moduleId: this.selectedModule,

            otherNumber: this.selectedBuildNumber.buildNumber,
            otherDate: this.selectedBuildNumber.time,

            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "name",
        }).$promise.then((data) => {
                    this.artifactsCount = data.pagingData.length;
                    this.artifactsGridOptions.setGridData(data.pagingData);
                });
        ;
    }

    _getDependenciesDiff() {

        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'dependencyDiff',
            moduleId: this.selectedModule,
            otherNumber: this.selectedBuildNumber.buildNumber,
            otherDate: this.selectedBuildNumber.time,

            pageNum: 1,
            numOfRows: 25,
            direction: "asc",
            orderBy: "id"
        }).$promise.then((data) => {
                    this.dependenciesCount = data.pagingData.length;//data.totalItems ? data.totalItems : 0;
                    this.dependenciesGridOptions.setGridData(data.pagingData);
                });

    }

    _getComparableBuildNumbers() {

        this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'prevBuild'
        }).$promise.then((data) => {
            this.comparableBuildNumbers = data;
                })

    }


    _createGrids() {

        this.modulesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getModulesColumns())
                .setRowTemplate('default');

        this.modulesGridOptions.onSelectionChange = (data) => {
            this.selectModule(data.entity);
        }

        this.artifactsGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getArtifactsColumns())
                .setRowTemplate('default')

        this.dependenciesGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this._getDependenciesColumns())
                .setRowTemplate('default');

        this._getModulesData();

        let featureLink = this.artifactoryFeatures.getFeatureLink('publishedmodule');
        let featureName = this.artifactoryFeatures.getFeatureName('publishedmodule');

        if (this.artifactoryFeatures.isDisabled("publishedmodule")) {
            this.$timeout(() => {
                //$('.grid-counter').css('color', 'red');
                $('.grid-counter').append('<span class="license-required"></span>');
                $('.grid-counter').find('.license-required').tooltipster({
                    animation: 'fade',
                    contentAsHTML : 'true',
                    trigger: 'hover',
                    onlyOne: 'true',
                    interactive: 'true',
                    interactiveTolerance: 150,
                    position: 'top',
                    theme: 'tooltipster-default top',
                    content: `Learn more about the <a href="${featureLink}" target="_blank">${featureName}</a> feature`
                });
            })
        }
    }

    _getModulesColumns() {
        let cellTemplate;
        if (this.artifactoryFeatures.isNonCommercial()) {
            cellTemplate = '<div class="ui-grid-cell-contents"><span>{{row.entity.moduleId}}</span></div>';
        } else {
            cellTemplate = '<div ng-click="grid.appScope.jfPublishedModules.selectModule(row.entity)" class="ui-grid-cell-contents"><a href class="jf-link">{{row.entity.moduleId}}</a></div>';
        }

        return [
            {
                name: "Module ID",
                displayName: "Module ID",
                field: "moduleId",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                cellTemplate: cellTemplate,
                width: '60%'
            },
            {
                name: "Number Of Artifacts",
                displayName: "Number Of Artifacts",
                field: "numOfArtifacts",
                width: '20%'
            },
            {
                name: "Number Of Dependencies",
                displayName: "Number Of Dependencies",
                field: "numOfDependencies",
                width: '20%'
            }

        ]
    }

    _getArtifactsColumns() {

        let typeCellTemplate = '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.type}}</div>';

        return [
            {
                name: "Artifact Name",
                displayName: "Artifact Name",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '30%',
                cellTemplate: this.commonGridColumns.downloadableColumn('status-{{(row.entity.status).toLowerCase()}}'),
                customActions: [{
                    icon: 'icon icon-view',
                    tooltip: 'View',
                    callback: row => this.viewCodeArtifact(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "View"})
                }],
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => _.findWhere(row.actions, {name: "Download"})
                    }
                }
            },
            {
                name: "Type",
                displayName: "Type",
                field: "type",
                cellTemplate: typeCellTemplate,
                allowGrouping
            :
                true,
                grouped: true,
                width: '10%'
            },
            {
                name: "Repo Path",
                displayName: "Repo Path",
                field: "repoPath",
                width: '50%',
                cellTemplate: this.commonGridColumns.repoPathColumn('status-{{(row.entity.status).toLowerCase()}}'),
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showArtifactInTree(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "ShowInTree"})
                }]
            }

        ];
    }

    _getDependenciesColumns() {

        let typeCellTemplate = '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.type}}</div>';
        let scopeCellTemplate = '<div class="ui-grid-cell-contents status-{{(row.entity.status).toLowerCase()}}">{{row.entity.scope}}</div>';

        return [
            {
                name: "Dependency ID",
                displayName: "Dependency ID",
                field: "name",
                sort: {
                    direction: this.uiGridConstants.ASC
                },
                width: '25%',
                cellTemplate: this.commonGridColumns.downloadableColumn('status-{{(row.entity.status).toLowerCase()}}'),
                actions: {
                    download: {
                        callback: row => this.downloadArtifact(row),
                        visibleWhen: row => _.findWhere(row.actions, {name: "Download"})
                    }
                }
            },
            {
                name: "Scope",
                displayName: "Scope",
                field: "scope",
                cellTemplate: scopeCellTemplate,
                allowGrouping
            :
                true,
                grouped: true,
                width: '15%'
            },
            {
                name: "Type",
                displayName: "Type",
                field: "type",
                cellTemplate: typeCellTemplate,
                allowGrouping
            :
                true,
                grouped: true,
                width: '15%'
            },
            {
                name: "Repo Path",
                displayName: "Repo Path",
                field: "repoPath",
                width: '45%',
                cellTemplate: this.commonGridColumns.repoPathColumn('status-{{(row.entity.status).toLowerCase()}}'),
                customActions: [{
                    icon: 'icon icon-show-in-tree',
                    tooltip: 'Show In Tree',
                    callback: row => this.showArtifactInTree(row),
                    visibleWhen: row => _.findWhere(row.actions, {name: "ShowInTree"})
                }]
            }
        ];
    }

    _installWatchers() {
        this.$scope.$watch('jfPublishedModules.selectedBuildNumber', (val) => {
            if (val.length) {
                this.getSubData();
            }
        });
        this.$scope.$watch('jfPublishedModules.compare', (val) => {
            if (val !== undefined) {
                this.getSubData();
            }
        });
    }

    backToModules() {
        _.extend(this.$stateParams, {
            buildName: this.$stateParams.buildName,
            buildNumber: this.$stateParams.buildNumber,
            startTime: this.$stateParams.startTime,
            tab: 'published',
            moduleID: null
        });

        this.$state.go('.', this.$stateParams, { location: true, inherit: true, relative: this.$state.$current, notify: false, reload: true }).then(() => {
            this.selectedModule = null;
        })

    }

}


export function jfPublishedModules() {
    return {
        restrict: 'EA',
        controller: jfPublishedModulesController,
        controllerAs: 'jfPublishedModules',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/build_tabs/jf_published_modules.html'
    }
}