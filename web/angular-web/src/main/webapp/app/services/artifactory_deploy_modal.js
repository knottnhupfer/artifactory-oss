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
import EVENTS from '../constants/artifacts_events.constants.js';
import TOOLTIP from '../constants/artifact_tooltip.constant.js';
import FIELD_OPTIONS from '../constants/field_options.constats.js';
import MESSAGES from '../constants/artifacts_messages.constants.js';

export class ArtifactoryDeployModal {

    constructor($rootScope, RepoDataDao, JFrogEventBus, JFrogModal, $timeout, JFrogUIUtils, ArtifactoryFeatures) {
        this.$rootScope = $rootScope;
        this.$timeout = $timeout;
        this.repoDataDao = RepoDataDao;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryModal = JFrogModal;
        this.jfrogUtils = JFrogUIUtils;
        this.features = ArtifactoryFeatures;
        this.TOOLTIP = TOOLTIP.artifacts.deploy;
        this.MESSAGES = MESSAGES.actions.deploy;
    }


    /**
     * Init the modal scope and launch the modal
     * @param node
     * @returns {modalInstance.result|*}
     */
    launch(node) {
        this.$scope = this.$rootScope.$new();
        this.$scope.Deploy = this;
        this.node = node;
        this.$scope.Deploy.fileSizeLimit = 0;
        this._initDeploy();
        this.modalInstance = this.artifactoryModal.launchModal('deploy_modal', this.$scope, 600);
        return this.modalInstance.result;
    }

    /**
     * get repo list.
     * set controller (single or multi)
     * check if local repo
     * (set  local repo on comm obj)
     * @private
     */
    _initDeploy() {
        this.currentDeploy = 'Single';
        this.deployFile = {};
        this.comm = {};
        this.repo = {};
        this.repoDataDao.get({deploy: 'true'}).$promise.then((result)=> {
            this.fileSizeLimit = result.fileUploadMaxSizeMb;
            this.comm.reposList = result.repoTypesList;
            this.comm.reposList = _.map(this.comm.reposList,(repo)=>{
                if (repo.layoutPattern) {
                    repo.layoutPatternDisplay = repo.layoutPattern;

                    let pattern = repo.layoutPattern;
                    let nextToken = pattern.match(/\[(.*?)\]/);
                    while (nextToken) {
                        let safeHTML = nextToken[1].replace('<','&lt').replace('>','&gt');
                        repo.layoutPatternDisplay = repo.layoutPatternDisplay.split(nextToken[0]).join(`[${safeHTML}]`);
                        pattern = pattern.replace(nextToken[0],'');
                        nextToken = pattern.match(/\[(.*?)\]/);
                    }
                }
                var rowPackageType =_.find(FIELD_OPTIONS.repoPackageTypes, (type) => {
                    return type.serverEnumName == repo.repoType;
                });
                if (rowPackageType)
                    repo.repoTypeIcon = rowPackageType.icon;
                return repo;
            });

            if (this.node) {
                this.comm.localRepo = _.findWhere(this.comm.reposList, {repoKey: this.node.data.repoKey});
                this.repo.repoInList = this.comm.localRepo;
                this.repo.rootRepo = this.node.data.getRoot();
            }
            else {
                if (this.deployController) this.deployController.deployFile.repoDeploy = this.comm.reposList[0];
            }

        });
        this.comm.createNotification = this.createNotification.bind(this);
        this.comm.setController = (controller) => {
            this.deployController = controller;
        };
    }

    /**
     * deploy on selected controller (single or multi)
     */
    deploy() {
        this.deployController.deployArtifacts();
    }

    /**
     * when deploy success refresh node and close modal
     */
    onDeploySuccess() {
        this.dispatchSuccessEvent();
        this.modalInstance.close();
    }

    /**
     * check if current deploy selected
     * @param deploy
     * @returns {boolean}
     */
    isSelectedDeploy(deploy) {
        return this.currentDeploy === deploy;
    }

    /**
     * This builds an appropriate notification for the Deploy action in the UI (with or w/o the Artifact URL)
     *
     * @param response from the server
     * @returns {{type: string, body: string}}
     */
    createNotification(response) {
        let {repoKey, artifactPath} = response;
        artifactPath = _.trim(artifactPath || '', '/');
        let encodedPath = encodeURIComponent(artifactPath).replace(/%2F/g,'/');
        let messageWithUrl = `Successfully deployed <a href="#/artifacts/browse/tree/General/${repoKey}/${encodedPath}">${this.jfrogUtils.getSafeHtml(artifactPath)}</a> to ${repoKey}`;
        let messageWithoutUrl = `Successfully deployed ${this.jfrogUtils.getSafeHtml(artifactPath)}`;
        return {
            type: 'success',
            body: response.showUrl ? messageWithUrl : messageWithoutUrl
        }
    }

    /**
     * after deploy _dispatchSuccessEvent refresh node in tree
     * @private
     */
    dispatchSuccessEvent() {
        this.JFrogEventBus.dispatch(EVENTS.ACTION_DEPLOY, [this.deployFile.repoDeploy.repoKey, this.deployFile]);
    }

    onRepoChange() {
        this.$timeout(()=>{
            if (this.deployController.layoutTokens) {
                delete this.deployController.layoutTokens;
                this.deployController.extractTokensFromLayout();
                this.deployController.updatePathFromLayoutTokens();
            }

            let repo = this.deployController.deployFile.repoDeploy;
            if (repo.repoType !== 'Maven' && this.deployController.deployFile.unitInfo) {
                delete this.deployController.deployFile.unitInfo.maven;
                this.deployController.deployFile.unitInfo.mavenArtifact = false;
                if (this.deployController.originalDeployPath) {
                    this.deployController.deployFile.targetPath = angular.copy(this.deployController.originalDeployPath);
                }
            }

            // Save the new selected localRepo
            this.repo.repoInList = repo;

            if (this.deployController.deploySingleUploader) {
                this.deployController.deploySingleUploader.getUploader().headers['X-ARTIFACTORY-REPOTYPE'] = this.deployController.deployFile.repoDeploy.repoType;
            }
        });
    }

    isTargetRepoSelectOpen(){
       return $('.target-path .ui-select-container').is('.open');
    }

    showDeployWarningMessage(){
        // This check is necessary since ng-show calls this function asynchronously , before _initDeploy()
        // so rootRepo is not loaded yet
        if(this.repo && this.node && !this.repo.rootRepo){
            this.repo.rootRepo = this.node.data.getRoot();
        }
        // Determine if a message should be displayed and determine its type
        if(!this.repo.repoInList) {
            if(this.repo.rootRepo && this.repo.rootRepo.distribution){
                this.warningMessage = this.MESSAGES.deployToDistRepoErrorMessage.message;
                this.messageType = this.MESSAGES.deployToDistRepoErrorMessage.messageType;
            }
            else if(this.repo.rootRepo.repoType === 'virtual' && !this.node.data.hasDefaultDeployRepo){
               this.warningMessage = this.MESSAGES.hasNoDefaultDeployRepo.message;
               this.messageType = this.MESSAGES.hasNoDefaultDeployRepo.messageType;
            }
            else if(this.repo.rootRepo.repoType === 'remote' || this.repo.rootRepo.repoType === 'cached'){
                this.warningMessage = this.MESSAGES.cannotDeployToRemote.message;
                if (this.features.isJCR()) this.warningMessage = this.MESSAGES.cannotDeployToRemote.messageJCR;
                this.messageType = this.MESSAGES.cannotDeployToRemote.messageType;
            }
            else if(this.repo.rootRepo.isTrashcan()){
                this.warningMessage =this.MESSAGES.cannotDeployToTrashCan.message;
                this.messageType = this.MESSAGES.cannotDeployToTrashCan.messageType;
            }
            else {
                this.warningMessage = this.MESSAGES.deployPermissionsErrorMessage.message;
                this.messageType = this.MESSAGES.deployPermissionsErrorMessage.messageType;
            }
            return true;
        }
        return false;
    }

}
