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
/**
 * Created by idannaim on 8/6/15.
 */
import API from '../../constants/api.constants';
import TOOLTIP from '../../constants/artifact_tooltip.constant';

class jfMultiDeployController {


    constructor($scope, ArtifactoryUploaderFactory, ArtifactDeployDao, ArtifactoryState,
                JFrogNotifications, GoogleAnalytics) {

        this.$scope = $scope;
        this.artifactDeployDao = ArtifactDeployDao;
        this.artifactoryNotifications = JFrogNotifications;
        this.artifactoryState = ArtifactoryState;
        this.GoogleAnalytics = GoogleAnalytics;
        this.artifactoryUploaderFactory = ArtifactoryUploaderFactory;
        this.errorQueue = [];
        this.multiSuccessMessage = '';
        this.multiSuccessMessageCount = 0;
        this.TOOLTIP = TOOLTIP.artifacts.deploy;
        this.originalDeployPath = '';
    }

    $onInit() {
        this.comm.setController(this);
        this._initDeploy();
    }

    /**
     * create uploader instance,
     * set methods callback
     * set path and file type
     * @private
     */
    _initDeploy() {
        let UPLOAD_REST_URL = `${API.API_URL}/artifact/upload`;

        this.deployMultiUploader = this.artifactoryUploaderFactory.getUploaderInstance(this)
                .setUrl(UPLOAD_REST_URL)
                .setOnSuccessItem(this.onSuccessItem)
                .setOnAfterAddingAll(this.onAfterAddingAll)
                .setOnAfterAddingFile(this.onAfterAddingFile)
                .setOnErrorItem(this.onUploadError)
                .setOnCompleteAll(this.onCompleteAll)
                .setOnProgressAll(this.onProgressAll);

        this.deployMultiUploader.getUploader().headers = {'X-Requested-With': 'artUI'};

        this._setPathAndFileType(this.node.data.path);
    }

    /**
     * check if path includes file/archive if yes cut it from the path and set .
     * check if the current repo is local else clean path.
     * Reset garbage deployFile if exists and fields.
     * @param targetPath
     * @private
     */
    _setPathAndFileType(targetPath) {
        if (this.node.data.isInsideArchive()) {
            targetPath = "";
        }
        else {
            if (this.node.data.type == "file" || this.node.data.type == 'archive') {
                if (targetPath.indexOf('/') > -1) {
                    targetPath = targetPath.substr(0, targetPath.lastIndexOf('/'))
                }
                else if (targetPath.indexOf('\\') > -1) {
                    targetPath = targetPath.substr(0, targetPath.lastIndexOf('\\'))
                }
                else {
                    targetPath = "";
                }
            }
        }
        if (this.firstInit) {
            if (localRepo) {
                this.deployFile = {
                    repoDeploy: localRepo,
                    targetPath: targetPath
                }
            } else {
                this.deployFile = {
                    repoDeploy: this.node.data.type == 'local' ? this.node.this.reposList[0] : '',
                    targetPath: targetPath
                }
            }
        } else {
            //Reset garbage deployFile if exists
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
                this.deployFile.unitInfo.mavenArtifact = false;
            }
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
                this.deployFile.unitInfo.debianArtifact = false;
            }
            this.deployFile.unitInfo = {};
            this.deployFile.fileName = '';
            this.deployMultiUploader.clearQueue();
            this.deployFile.targetPath = targetPath;
        }
        this.uploadCompleted = false;
        this.firstInit = false;
    }

    onSuccessItem(fileDetails, response) {
        this.deployFile.unitInfo = response.unitInfo;
        this.deployFile.unitConfigFileContent = response.unitConfigFileContent;
        //MavenArtifact causes 'deploy as' checkbox to be lit -> change deployment path according to GAVC
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
            this.originalDeployPath = this.deployFile.targetPath;
        }
        if (response.repoKey && response.artifactPath) {
            let msg = this.comm.createNotification(response);
            this.multiSuccessMessage += msg.body + '<br>';
            this.multiSuccessMessageCount++;
        }
    }

    /**
     * check if queue have files to upload
     */
    multiUploadItemRemoved() {
        if (!this.deployMultiUploader.getQueue() || !this.deployMultiUploader.getQueue().length) {
            this.uploadCompleted = false;
        }
    }

    /**
     * when upload item error push to queue for notifications
     * @param item
     * @param response
     */
    onUploadError(item, response) {

        this.errorQueue.push({item: item, response: response});

    }

    /**
     * upload complete check if 'error queue' if empty if not show all failed files
     * else show success notification
     */
    onCompleteAll() {
        let numberOfUploadedFiles = this.deployMultiUploader.getQueue().length;
        this.uploadCompleted = true;
        this.progress = false;
        let body = '<ul>';
        this.artifactoryNotifications.clear();
        if (this.errorQueue.length) {
            this.errorQueue.forEach((error)=> {
                body += '<li>"' + error.item.file.name + '" ' + error.response.error + '</li>'
            })
            body += '</ul>';
            this.artifactoryNotifications.createMessageWithHtml({type: 'error', body: body, timeout: 10000});
            this.deployMultiUploader.clearQueue();
            this.errorQueue = [];
            this.GoogleAnalytics.trackEvent('Artifacts' , 'Deploy succeed' , this.node.data.repoPkgType , this.errorQueue.length , this.node.data.repoType , 'multi' , numberOfUploadedFiles)
        }
        else {
            this.artifactoryNotifications.createMessageWithHtml({type: 'success', body: `Successfully deployed ${this.multiSuccessMessageCount} files`});
            this.GoogleAnalytics.trackEvent('Artifacts' , 'Deploy succeed' , this.node.data.repoPkgType , 0 , this.node.data.repoType , 'multi' , numberOfUploadedFiles)
        }
        if (this.onSuccess && typeof this.onSuccess === 'function') {
            this.onSuccess();
        }

    }

    /**
     * onAfterAddingAll check for only 20 files  upload
     * @param fileItems
     */
    onAfterAddingAll(fileItems) {
        if (fileItems.length > 20) {
            this.artifactoryNotifications.create({error: "You can only deploy up to 20 files at a time"});
            this.deployMultiUploader.clearQueue();
            return;
        }
        //Enable the "deploy" button after all files were added.
        this.uploadCompleted = true;
        let uploadAll = true;

        fileItems.forEach((item)=> {
            if (!item.okToUploadFile) {

                uploadAll = false;
                return;
            }
        });
    }

    /**
     * check if missing fields to disable deploy button
     * @returns {boolean|*}
     */
    isReady() {
        let ok = true;
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
            ok = this.deployFile.unitInfo.distribution && this.deployFile.unitInfo.component && this.deployFile.unitInfo.architecture;
        }
        return ok && this.uploadCompleted && this.deployFile.repoDeploy;
    }

    onAfterAddingFile(fileItem) {
        if (fileItem.file.size < 0) {
            fileItem.okToUploadFile = false;
            this.deployMultiUploader.removeFileFromQueue(fileItem);
        }
        else if(this.isExceedingSizeLimit(fileItem)) {
            fileItem.okToUploadFile = false;
            this.cancelFileUploadExceedingLimit(fileItem);
        }
        else {
            // Save original for display
            fileItem.file.originalName = fileItem.file.name;
            // Encode filename to support UTF-8 strings (server does decode)
            fileItem.file.name = encodeURIComponent(fileItem.file.name);
            fileItem.okToUploadFile = true;
        }
    }

    /**
     * test if file exceeds the size limit
     * @param fileItem
     */
    isExceedingSizeLimit(fileItem){
        return this.fileSizeLimit > 0 &&
               fileItem.file.size > this.toBytes(this.fileSizeLimit);
    }
    /**
     * cancel an upload of a file that exceeds the size limit
     * @param fileItem
     */
    cancelFileUploadExceedingLimit(fileItem){
        let errorMsg = 'File size exceeds the limit of '+this.fileSizeLimit+' MB';
        this.artifactoryNotifications.create({error: errorMsg});

        this.deployMultiUploader.removeFileFromQueue(fileItem);
    }

    /**
     * convert file size from MB to Bytes
     * @param fileSizeInMB
     */
    toBytes(fileSizeInMB){
        return fileSizeInMB*1000000;
    }

    onProgressAll(progressPercent) {
        if (!this.progress) {
            this.progress = true;
            this.artifactoryNotifications.createMessageWithHtml({
                type: 'success',
                body: '<div id="deploy-progress-percent">Deploy in progress... (0%)</div>' +
                      '<div id="deploy-progress-frame"><div id="deploy-progress-line"></div></div>',
                timeout: 60 * 60000 * 10
            });
            if (this.onSuccess && typeof this.onSuccess === 'function') {
                this.onSuccess();
            }
        }
        else {
            let percElem = $('#deploy-progress-percent');
            let lineElem = $('#deploy-progress-line');
            percElem.text(`Deploy in progress... (${progressPercent}%)`)
            lineElem.css('width',`${progressPercent}%`);
        }
    }

    /**
     * if exist char '&' need to be replace to  '%26' before upload
     * @param name
     * @returns {*}
     * @private
     */
    _fixUrlPath(name) {
        name = name.replace(/&/g, '%26');
        var find = '&';
        var re = new RegExp(find, 'g');
        return name.replace(re, '%26');
    }

    /**
     * set url to deploy for each file and deploy when ready
     */
    deployArtifacts() {

        let DEPLOY_REST_URL = `${API.API_URL}/artifact/deploy/multi`;

        if ((!this.deployFile.targetPath.endsWith("/"))) {
            this.deployFile.targetPath += "/";
        }

        this.deployMultiUploader.getQueue().forEach((item)=> {
            item.url = DEPLOY_REST_URL + '?repoKey=' + this.deployFile.repoDeploy.repoKey + '&path=' +
            (this.deployFile.targetPath || '') + this._fixUrlPath(item.file.name);
        });
        this.deployMultiUploader.getUploader().uploadAll();
    }


}
export function jfMultiDeploy() {
    return {
        restrict: 'EA',
        scope: {
            node: '=',
            deploy: '&',
            comm: '=',
            deployFile: '=',
            onSuccess: '&',
            fileSizeLimit: '='
        },
        controller: jfMultiDeployController,
        controllerAs: 'jfMultiDeploy',
        bindToController: true,
        templateUrl: 'directives/jf_deploy/jf_multi_deploy.html'
    }
}