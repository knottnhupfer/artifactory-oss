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
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";
import MESSAGES from "../../../../constants/configuration_messages.constants";

/*
* TODO:
* - if proxy is change or on load thre's default proxy - update the model
* */
export class AdminAdvancedLogAnalyticsController {

    constructor(SumoLogicConfigDao, $scope, $state, $location, $timeout, $interval, $window, JFrogModal, ArtifactoryModelSaver,
            ProxiesDao, ArtifactoryFeatures) {
        this.sumoLogicConfigDao = SumoLogicConfigDao;
        this.$scope = $scope;
        this.$state = $state;
        this.$location = $location;
        this.$timeout = $timeout;
        this.$interval = $interval;
        this.$window = $window;
        this.modal = JFrogModal;
        this.proxiesDao = ProxiesDao;
        this.artifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['sumologic']);
        this.enableExistingSettings = false;
        this.features = ArtifactoryFeatures;
        this.TOOLTIP = TOOLTIP.admin.logAnalytics;
        this.MESSAGES = MESSAGES.admin.advanced.logAnalytics;
        this.getSumoLogicData(false,true);
        this.proxies = [""];

        this.proxiesDao.get().$promise.then((proxies)=> {
            _.forEach(proxies, (o) => {
                this.proxies.push(o.key);
                if (o.defaultProxy && !this.proxy) {
                    this.proxy = o.key;
                }
            });
        });


        this.$scope.$on('$destroy', () => {
            this.cancelInterval();
        });
    }

    disabledIntegration() {
        if (this.enableExistingSettings === false) {
                this.modal.confirm("If disabled, logs will not be sent to your Sumo Logic account. <br /> You can enable the integration for this instance later. <br /><br /> Are you sure you want to disable the integration for this instance?", 'Disable Sumo Logic Integration', {confirm: 'Disable'})
                    .then(()=> {
                        this.cancelInterval();
                        this.enableDisableSumoLogic(false);
                    }).catch(() => {
                        this.enableExistingSettings = !this.enableExistingSettings;
                    });
        } else {
            if (!this.sumologic.email) {
                this.modal.confirm(`To enable the Sumo Logic integration, you must provide ${this.features.getGlobalName()} with your email address by adding it to your User Profile.`, 'Email address required', {confirm: 'Go to User Profile'})
                    .then(()=> {
                        this.$state.go('user_profile');
                    }).catch(() => {
                        this.enableExistingSettings = !this.enableExistingSettings;
                    });
            } else {
                if(this.sumologic.clientId && this.sumologic.secret) {
                this.modal.confirm(`If enabled, your logs will be populated and sent to your Sumo Logic ${this.features.getGlobalName()} dashboard. <br /><br /> Are you sure you want to enable the integration for this instance?`, 'Enable Sumo Logic Integration', {confirm: 'Enable'})
                    .then(()=> {
                        this.enableDisableSumoLogic(true);
                    }).catch(() => {
                        this.enableExistingSettings = !this.enableExistingSettings;
                    });
                } else {
                    this.enableDisableSumoLogic(true);
                }
            }
        }
    }

    enableDisableSumoLogic(action) {
        this.sumologic.enabled = action;
        this.updateModel();
    }

    changeListener() {
        if (this.connectionMethod === 'existing') {
            this.disableRegisterButton = false;
        }
    }

    requireAuth() {
        return this.sumologic.clientId != this.origSumologicData.clientId || this.sumologic.secret != this.origSumologicData.secret;
    }

    changeConnectionMethod(value) {
        if (value === 'new') {
            if (this.sumologic.dashboardUrl || this.isPullingRunning) {
                this.modal.confirm("Creating a new connection will disconnect the current connection. When you access your dashboard with a new connection for the first time, a new Client ID and Secret will be created. <br /><br />Are you sure you want to create a new connection?", 'Create New Connection with Sumo Logic', {confirm: 'Create'})
                    .then(()=> {
                        this.cancelInterval();
                        this.sumoLogicConfigDao.reset().$promise.then((sumologic) => {
                            this.connectionMethod = 'new';
                            this.statusConnected = false;
                            this.disableRegisterButton = false;
                            this.sumologic = sumologic;
                            this.artifactoryModelSaver.save();
                        }).catch(() => {
                            this.cancelInterval();
                        });


                    }).catch(() => {
                    this.connectionMethod = 'existing';
                });

            } else {
                this.sumologic.clientId = null;
                this.sumologic.secret = null;
            }
        }
    }

    registerSumoLogicApplication() {
        this.disableRegisterButton = true;
        this.sumologic.dashboardUrl = null;
        this.sumoLogicConfigDao.registerSumoLogicApplication().$promise.then(() => {
            this.getSumoLogicData(true);
            if (!angular.isDefined(this.getDataInterval)) {
                this.getDataInterval = this.$interval(() => {
                    this.isPullingRunning = true;
                    this.getSumoLogicData();
                }, 5000);
            }
        }).catch(() => {
            this.cancelInterval();
        });
    }

    authenticateWithSumo(setupNewConnection) {
        this.sumologic.dashboardUrl = null;
        let setupTypeSuffix = setupNewConnection ? '/new_app' : '/existing_app';
        let redirectUrl = encodeURIComponent(this.sumologic.redirectUrl + setupTypeSuffix);
        let email = this.sumologic.email;
        let url = this.sumologic.sumoBaseUrl + '/partner/oauth/authorize?response_type=code&email=' + encodeURIComponent(email)
            + '&license_type=' + this.sumologic.licenseType + '&client_id=' + this.sumologic.clientId + '&redirect_uri=' + redirectUrl + '';

        var popup = this.$window.open(url, "_blank");

        if (!angular.isDefined(this.getDataInterval)) {
            this.getDataInterval = this.$interval(() => {
                this.isPullingRunning = true;
                this.getSumoLogicData();
            }, 5000);
        }


        this.$timeout(() => {
            if(!popup || popup.outerHeight === 0) {
                let modalScope = this.$scope.$new();
                modalScope.url = url;
                this.modalInstance = this.modal.launchModal("popup_block_notice", modalScope ,'sm');
            }
        }, 100);
    }
    
    getSumoLogicData(setupNewConnection, firstLoad) {
        this.sumoLogicConfigDao.get().$promise.then((sumologic)=> {
            this.sumologic = sumologic;
            if (sumologic.proxy) this.proxy = sumologic.proxy;
            this.origSumologicData = {
                "clientId" : this.sumologic.clientId,
                "secret" : this.sumologic.secret
            };
            this.enableExistingSettings = this.sumologic.enabled;

            if (this.sumologic.dashboardUrl) {
                this.cancelInterval();
                if (firstLoad && this.enableExistingSettings) {
                    this.sumoLogicConfigDao.refreshToken().$promise.then((refreshResponseData)=> {
                        this.sumologic.dashboardUrl = refreshResponseData.dashboardUrl;
                        this.statusConnected = true;
                        this.updateModel();
                    }).catch(() => {
                        if (angular.isDefined(this.getDataInterval)) {
                            this.$interval.cancel(this.getDataInterval);
                        }
                    });
                }

            }

            if (this.sumologic.clientId || this.sumologic.secret) {
                this.connectionMethod = 'existing';
            } else {
                this.connectionMethod = 'new';
            }

            if (setupNewConnection) {
                this.authenticateWithSumo(true);
                this.updateModel();
            }
            this.artifactoryModelSaver.save();
        }).catch(() => {
            this.cancelInterval();
        });
    }
    cancelInterval() {
        if (angular.isDefined(this.getDataInterval)) {
            this.isPullingRunning = false;
            this.$interval.cancel(this.getDataInterval);
            delete this.getDataInterval;
        }
    }
    updateProxy() {
        this.updateModel();
    }
    updateModel() {
        if (this.proxy) this.sumologic.proxy = this.proxy;
        this.sumoLogicConfigDao.update(this.sumologic).$promise.then(()=> {
            this.artifactoryModelSaver.save();
        });
    }
}