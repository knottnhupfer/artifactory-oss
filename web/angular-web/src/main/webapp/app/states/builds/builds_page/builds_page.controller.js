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
import DICTIONARY from "../constants/builds.constants";
import MESSAGES from '../../../constants/artifacts_messages.constants';

export class BuildsPageController {
    constructor($scope, $stateParams, BuildsDao, User, ArtifactoryFeatures, $state, $filter, JFrogEventBus,
            DistributionDao, JFrogModal) {
        this.$scope = $scope;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.JFrogEventBus = JFrogEventBus;
        this.$filter = $filter;
        this.distributionDao = DistributionDao;
        this.modal = JFrogModal;

        this.buildsDao = BuildsDao;
        this.DICTIONARY = DICTIONARY.tabs;

        this.buildTitle = this.$stateParams.buildName;

        this.user = User.getCurrent();
        this.features = ArtifactoryFeatures;
        this.userCanDistribute = false;
        this.continueState = null;
        this.tabs = [
            {name: 'published'},
            {name: 'environment'},
            {name: 'issues'},
            {name: 'licenses', feature: 'licenses'},
            {name: 'diff', feature: 'diff'},
            {name: 'history'},
            {name: 'json'},
            {name: 'effectivePermission'}
        ];
        this.fetchInitialBuildsHistoryData();

    }

    deleteBuildVersion(build) {

        let json = {
            buildsCoordinates:[
                {
                    buildName: this.$stateParams.buildName,
                    buildNumber: build.buildNumber,
                    date: build.time
                }
            ]
        };


        this.modal.confirm(`Are you sure you wish to delete ${build.buildNumber} version of ${this.$stateParams.buildName} build?`)
                    .then(() => {
                        this.buildsDao.delete(json).$promise.then(() => {
                            this._getBuildsData();
                        });
                    });
    }

    _isTabDisabled(tab) {
        if (tab.name === 'effectivePermission' && !this.canManage && !this.user.isAdmin()) return true;
        return !(tab.name === 'published' || this.buildFullView);
    }

    fetchInitialBuildsHistoryData(){
        this.loading = true;
        this.buildsDao.getData({
            action: 'history',
            name: this.$stateParams.buildName,
            limit:50
        })
            .$promise.then((res) => {
            this.builds = res.data;
            this.continueState = res.continueState;
            this.ciUrl = _.find(this.builds, {buildNumber: this.$stateParams.buildNumber}).ciUrl;
            this.loading = false;
            this._getBuildData();
        });
    }

    fetchMoreBuildsData(){
        if(!this.continueState || this.loading) return;
        this.loading = true;
        const payload = {
            action: 'history',
            name: this.$stateParams.buildName,
            limit:50,
            continueState:  this.continueState
        };
        this.buildsDao.getData(payload).$promise.then((res) => {
            this.builds.push(...res.data);
            this.continueState = res.continueState;
            this.loading = false;
        });
    }

    setOnScroll() {
        const EDGE = 10;
        setTimeout(() => {
            const scrollParent = $('.list-content-scrolling-container');
            this.scrollParent = scrollParent;
            scrollParent.on('scroll', (e) => {
                if (scrollParent[0].scrollHeight - scrollParent.scrollTop() <= scrollParent[0].clientHeight + EDGE) {
                    if(!this.loading){
                        this.fetchMoreBuildsData();
                    }
                }

            });
        });
    }


    // Get list of builds
    _getBuildsData() {
        this.buildsDao.getData({
            action: 'history',
            name: this.$stateParams.buildName,
            limit:20
        })
        .$promise.then((res) => {
            this.builds = _.map(_.sortByOrder(res.data,['time'],false));
            this.ciUrl = _.find(this.builds, {buildNumber: this.$stateParams.buildNumber}).ciUrl;
            this._getBuildData();
        });
    }

    _getBuildData() {
        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action:'buildInfo'
        }).$promise.then((data) => {
            this.buildFullView = data.buildFullView;
            this.userCanDistribute = data.userCanDistribute;
            this.canDelete = data.canDelete;
            this.canManage = data.canManage;
            this.$stateParams.startTime = data.time;
            this.buildData = data;
            this.summaryData = this.getSummaryColumns();

            this.tabs.forEach((tab) => {
                tab.isDisabled = this._isTabDisabled(tab);
            });
            this.setOnScroll();
        });
    }

    getSummaryColumns() {
        let agentTemplate = (this.ciUrl) ? `<a class="jf-link" ng-href="{{BuildsPage.buildData.url}}" target="_blank">{{BuildsPage.buildData.agent}}</a> <a ng-href="{{BuildsPage.buildData.url}}" target="_blank"><i class="icon-linked" jf-tooltip='<a href="{{BuildsPage.buildData.url}}">{{BuildsPage.buildData.url}}</a>'></i></a>` : `{{BuildsPage.buildData.agent}}`;
        return [
            {
                label: 'Agent',
                template: agentTemplate,
                isActive: true
            },
            {
                label: 'Build Agent',
                template: '{{BuildsPage.buildData.buildAgent}}',
                isActive: true
            },
            {
                label: 'Started',
                template: '<div class="text-truncate" jf-tooltip-on-overflow>{{BuildsPage.buildData.lastBuildTime | date:"d MMMM, yyyy"}} {{BuildsPage.buildData.lastBuildTime | date:"HH:mm:ss"}}</div>',
                isActive: true
            },
            {
                label: 'Duration',
                template: `{{BuildsPage.buildData.duration || '-'}}`,
                isActive: true
            },
            {
                label: 'Principal',
                template: `{{BuildsPage.buildData.principal || '-'}}`,
                isActive: true
            },
            {
                label: 'Artifactory Principal',
                template: `{{BuildsPage.buildData.artifactoryPrincipal || '-'}}`,
                isActive: true,
                width: '1.2fr'
            }
        ]
    }

    goToBuildNumber(build) {
        this.ciUrl = build.ciUrl;
        _.extend(this.$stateParams, {buildNumber: build.buildNumber,startTime: build.time, tab: this.$state.params.tab, moduleID: null});
        this.$state.go('.', this.$stateParams, { location: true, inherit: true, relative: this.$state.$current, notify: false, reload: true })
                .then(() => {
                    this._getBuildData();
                    this.JFrogEventBus.dispatch(this.JFrogEventBus.getEventsDefinition().BUILDS_TAB_REFRESH)
                })
    }

    deleteBuildId(e, buildId) {
        e.stopPropagation();
        let buildName = this.$stateParams.buildName;
        let buildNumber = buildId;
        let date = this.$stateParams.startTime;
        let index = _.findIndex(this.builds, (build) => build.buildNumber === buildNumber);
        let isDeleteingActiveBuildNumber = buildNumber === this.$stateParams.buildNumber;

        let json = {
            buildsCoordinates: [{
                buildName,
                buildNumber,
                date
            }]
        };

        this.modal.confirm(`Are you sure you want to delete build number ${buildNumber} in ${buildName}?`)
                .then(() => {
                    this.buildsDao.delete(json).$promise.then(() => {
                        if (index === 0 && this.builds.length === 1) {
                            this.backToBuilds();
                        } else if (isDeleteingActiveBuildNumber) {
                            this.builds.splice(index, 1);
                            if (index === 0) {
                                this.goToBuildNumber(this.builds[0]);
                            } else {
                                this.goToBuildNumber(this.builds[index - 1]);
                            }
                        } else {
                            this.builds.splice(index, 1);
                        }
                    })
                })
    }

    distribute() {
        this.distributionDao.getAvailableDistributionRepos({}).$promise.then((data)=>{
            let modalInstance;
            this.distributeModalScope = this.$scope.$new();

            this.distributeModalScope.title = "Distribute " + this.$stateParams.buildName + " #" + this.$stateParams.buildNumber;
            this.distributeModalScope.distributionRepositoriesOptions = _.map(data.data.availableDistributionRepos, 'repoKey');

            this.distributeModalScope.data = {};

            if (!this.distributeModalScope.distributionRepositoriesOptions.length) {
                this.distributeModalScope.errorMessage = MESSAGES.actions.distribute.noRepos.nonAdmin.message;
                this.distributeModalScope.messageType = MESSAGES.actions.distribute.noRepos.nonAdmin.messageType;
            }

            this.distributeModalScope.data.async = true;
            this.distributeModalScope.data.publish = true;
            this.distributeModalScope.data.publish = true;
            this.distributeModalScope.data.overrideExistingFiles = false;
            this.distributeModalScope.data.selectedRepo = null;
            this.distributeModalScope.distribute = () => {
                this._resetChanges();
                this.distributionDao.distributeBuild({
                    targetRepo: this.distributeModalScope.data.selectedRepo,
                    async: this.distributeModalScope.data.async,
                    overrideExistingFiles: this.distributeModalScope.data.overrideExistingFiles,
                    date: this.$stateParams.startTime
                },{
                    buildName: this.$stateParams.buildName,
                    buildNumber: this.$stateParams.buildNumber,
                    date: this.$stateParams.startTime
                }).$promise.then((res)=>{
                    // Success
                    if (this.distributeModalScope.data.async) {
                        modalInstance.close();
                    } else {
                        this._runRulesTest(res);
                    }
                });
            };

            // DRY RUN
            this.distributeModalScope.dryRun = () => {
                this._resetChanges();
                this.distributionDao.distributeBuild({
                            targetRepo: this.distributeModalScope.data.selectedRepo,
                            async: this.distributeModalScope.data.async,
                            publish: this.distributeModalScope.data.publish,
                            overrideExistingFiles: this.distributeModalScope.data.overrideExistingFiles,
                            dryRun: true
                        }, {
                            buildName: this.$stateParams.buildName,
                            buildNumber: this.$stateParams.buildNumber,
                            date: this.$stateParams.startTime
                        }
                ).$promise.then((res)=>{
                    this._runRulesTest(res);
                });
            };

            modalInstance = this.modal.launchModal('distribute_modal', this.distributeModalScope, 650);
        });
    }

    _runRulesTest(res) {
        let ind = 0;
        let result = res.data;
        _.forEach(result, (value,key) => {
            if (key == 'distributed') {
                let distributed = result[key];

                _.forEach(distributed, (value,key) => {
                    distributed[key].customId = "dis" + ind;
                    ind++;

                    let packages = distributed[key].packages;

                    _.forEach(packages, (value,key) => {
                        packages[key].customId = "pac" + ind;
                        ind++;

                        let versions = packages[key].versions;
                        _.forEach(versions, (value,key) => {
                            versions[key].customId = "ver" + ind;
                            ind++;
                        });

                    });
                });
            }
        });
        this.distributeModalScope.data.dryRunResults = result;

        _.forEach(result.messagesByPath, (value) => {
            if (value.warnings) {
                this.distributeModalScope.data.warningExist = value.warnings.length ? true : false;
            }
            if (value.errors) {
                this.distributeModalScope.data.errorsExist = value.errors.length ? true : false;
            }
        });

        this._expandModal();
    }

    _expandModal(){
        $('form[name="distributeRepo"]')
                .closest('.modal-dialog')
                .animate({
                    maxWidth:'850px'
                },500);
    }

    _resetChanges() {
        // RESET
        this.distributeModalScope.data.dryRunResults = null;
        this.distributeModalScope.data.toggleSuccessTitle = null;
        this.distributeModalScope.data.toggleWarnTitle = null;
        this.distributeModalScope.data.toggleErrorTitle = null;
        this.distributeModalScope.data.warningExist = null;
        this.distributeModalScope.data.errorsExist = null;
    }

	backToBuilds() {
        this.$state.go('builds.all');
    }
}