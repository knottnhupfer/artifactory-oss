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
import TOOLTIP from '../constants/artifact_tooltip.constant';

export class PushToBintrayModal {
    constructor($stateParams, $rootScope, $q, JFrogModal, PushToBintrayDao, JFrogNotifications) {

        this.JFrogNotifications = JFrogNotifications;
        this.$rootScope = $rootScope;
        this.$stateParams = $stateParams;
        this.$q = $q;
        this.modal = JFrogModal;
        this.ptbDao = PushToBintrayDao;
    }

    _getBuildBintrayRepositories() {
        this.ptbDao.getBuildRepos().$promise.then((data) => {
            this.modalScope.data.bintrayRepos = _.map(data.binTrayRepositories,(repo) => {return {text:repo ,value: repo}});

        })
            .catch((err) => {
                if (err.data && err.data.feedbackMsg && err.data.feedbackMsg.error) {
                    let msg = err.data.feedbackMsg.error;
            this.JFrogNotifications.create({error: msg});
                }
            });

    }

    _getBuildBintrayPackages() {
        this.ptbDao.getBuildPacks({key: this.modalScope.selection.bintrayRepo}).$promise.then((data) => {
            data.binTrayPackages = _.filter(data.binTrayPackages,(pack) => {return pack!=='_'});
            this.modalScope.data.bintrayPackages = _.map(data.binTrayPackages,(pack) => {return {text:pack ,value: pack}});
            if (data.binTrayPackages && data.binTrayPackages.length) {
                if (!this.modalScope.selection.bintrayPackageName) this.modalScope.selection.bintrayPackageName = data.binTrayPackages[0];
            }
            if (this.modalScope.selection.bintrayPackageName) {
                this._getBuildBintrayVersions();
            }
            else {
                this.modalScope.data.bintrayPackageVersions = [{text:'1.0' ,value: '1.0'}];
                this.modalScope.selection.bintrayPackageVersion = '1.0';
            }
        });
    }

    _getBuildBintrayVersions() {
        this.ptbDao.getBuildVersions({
            key: this.modalScope.selection.bintrayRepo,
            id: this.modalScope.selection.bintrayPackageName
        }).$promise.then((data) => {
                    this.modalScope.data.bintrayPackageVersions = _.map(data.binTrayVersions,(ver) => {return {text:ver ,value: ver}});
                    if (data.binTrayVersions && data.binTrayVersions.length && !this.modalScope.selection.bintrayPackageVersion) {
                        this.modalScope.selection.bintrayPackageVersion = data.binTrayVersions[0];
                    }

                })
        .catch(()=>{
                    this.modalScope.data.bintrayPackageVersions = [{text:'1.0' ,value: '1.0'}];
                    this.modalScope.selection.bintrayPackageVersion = '1.0';
                })
    }

    _pushBuildToBintray(backgroundPush) {

        let payload = {

            buildName: this.$stateParams.buildName,
            buildNumber: this.$stateParams.buildNumber,
            buildTime: this.$stateParams.startTime,
            bintrayParams: {
                useExistingProps: this.modalScope.selection.useSpecificProperties,
                notify: this.modalScope.selection.sendEmail,
                repo: this.modalScope.selection.bintrayRepo,
                packageId: this.modalScope.selection.bintrayPackageName,
                version: this.modalScope.selection.bintrayPackageVersion
            }
        };

        this.ptbDao.pushBuildToBintray({background: backgroundPush}, payload).$promise.then((response)=> {
            this.createPushToBintrayResponse(response);
        }).finally(() => this.modalInstance.close());
    }

    _pushArtifactToBintray() {

        let payload = {
            bintrayParams: this.bintrayParams
        };

        payload.bintrayParams.repo = this.modalScope.selection.bintrayRepo;
        payload.bintrayParams.packageId = this.modalScope.selection.bintrayPackageName;
        payload.bintrayParams.version = this.modalScope.selection.bintrayPackageVersion;
        payload.bintrayParams.path = this.modalScope.selection.filePath;

        this.ptbDao.pushArtifactToBintray({
            repoKey: this.params.repoKey,
            path: this.params.path
        }, payload).$promise.then((response)=> {
                this.createPushToBintrayResponse(response);
            }).finally(() => this.modalInstance.close());

    }


    _getArtifactBintrayData() {

        this.ptbDao.getArtifactData({repoKey: this.params.repoKey, path: this.params.path}).$promise.then((data) => {
            this.bintrayParams = data.bintrayParams;
            this.modalScope.selection.bintrayRepo = data.bintrayParams.repo;
            this.modalScope.selection.bintrayPackageName = data.bintrayParams.packageId;
            this.modalScope.selection.filePath = data.bintrayParams.path;
            this.modalScope.selection.bintrayPackageVersion = data.bintrayParams.version;

            this.modalScope.data.bintrayRepos = _.map(data.binTrayRepositories, (repo) => {
                return {text: repo, value: repo}
            });//data.binTrayRepositories;
            if (data.bintrayParams.packageId) this.modalScope.data.bintrayPackages = [data.bintrayParams.packageId];
            if (data.bintrayParams.version) this.modalScope.data.bintrayPackageVersions = [{
                text: data.bintrayParams.version,
                value: data.bintrayParams.version
            }];

            if (this.modalScope.selection.bintrayRepo) this._getBuildBintrayPackages();
        })
            .catch((err) => {
                if (err.data && err.data.feedbackMsg && err.data.feedbackMsg.error) {
                    let msg = err.data.feedbackMsg.error;
            this.JFrogNotifications.create({error: msg});
                }
            });
    }


    _pushDockerTagToBintray() {
        let payload = {
            bintrayParams: this.bintrayParams
        };

        payload.bintrayParams.repo = this.modalScope.selection.bintrayRepo;
        payload.bintrayParams.path = this.modalScope.selection.filePath;

        this.ptbDao.pushDockerTagToBintray({
            repoKey: this.params.repoKey,
            path: this.params.path
        }, payload).$promise.then((response)=> {
            this.createPushToBintrayResponse(response);
        }).finally(() => this.modalInstance.close());
    }

    launchModal(type, params) {

        let deferred = this.$q.defer();
        this.modalScope = this.$rootScope.$new();
        this.modalScope.selection = {};
        this.modalScope.data = {};
        this.modalScope.tooltip = TOOLTIP.artifacts.pushToBintray;

        this.modalScope.cancel = () => {
            this.modalInstance.close();
            deferred.reject();
        };

        this.modalScope.onRepoSelect = () => {
            if (type !== 'docker') this._getBuildBintrayPackages();
        };
        this.modalScope.onPackageSelect = () => {
            this._getBuildBintrayVersions();
        };

        this.modalScope.selectizeConfigAdd = {
            sortField: 'number',
            create: true,
            maxItems: 1
        };
        this.modalScope.selectizeConfigNoAdd = {
            sortField: 'number',
            create: false,
            maxItems: 1
        };


        this.modalScope.pushType = type;

        switch(type) {
            case 'build': {
                this.modalScope.push = () => {
                    this._pushBuildToBintray(false);
                };

                this.modalScope.backgroundPush = () => {

                    this._pushBuildToBintray(true);
                };

                this._getBuildBintrayRepositories();

                break;
            }
            case 'artifact': {
                this.modalScope.push = () => {
                    this._pushArtifactToBintray();
                };

                let repoPath = params.repoPath;
                let arr = repoPath.split(':');
                let repoKey = arr[0];
                let path = arr[1];

                this.params = {repoKey: repoKey, path: path};

                this._getArtifactBintrayData();

                break;
            }
            case 'docker': {
                this.modalScope.push = () => {
                    this._pushDockerTagToBintray();
                };

                let repoPath = params.repoPath;
                let arr = repoPath.split(':');
                let repoKey = arr[0];
                let path = arr[1];

                let pathArr = path.split('/');
                this.modalScope.docker = {
                    tagName: pathArr.splice(pathArr.length - 1, 1),
                    packageName: pathArr.join(':')
                };

                this.params = {repoKey: repoKey, path: path};

                this._getArtifactBintrayData();

                break;
            }
        }
        this.modalInstance = this.modal.launchModal("push_to_bintray", this.modalScope);

        return deferred.promise;

    }

    createPushToBintrayResponse(response) {
        if (response.data.error) {
            this.createPushToBintrayErrorResponse(response.data);
            return;
        }
        let artifactBintrayUrl = response.data.url;
        if (artifactBintrayUrl) {
            this.JFrogNotifications.createMessageWithHtml({
                type: 'success',
                body: `${response.data.info} <a href="${artifactBintrayUrl}" target="_blank">${artifactBintrayUrl}</a>`
            });
        }
    }

    createPushToBintrayErrorResponse(response) {
        if (response.error) {
            this.JFrogNotifications.create(response);
        }
    }
}