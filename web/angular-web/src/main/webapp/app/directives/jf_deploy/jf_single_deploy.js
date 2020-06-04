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
import API from "../../constants/api.constants";
import TOOLTIP from "../../constants/artifact_tooltip.constant";

class jfSingleDeployController {
    constructor($scope, ArtifactoryUploaderFactory, ArtifactDeployDao, ArtifactoryState,
                JFrogNotifications, GoogleAnalytics) {

        this.$scope = $scope;
        this.artifactDeployDao = ArtifactDeployDao;
        this.GoogleAnalytics = GoogleAnalytics;
        this.artifactoryNotifications = JFrogNotifications;
        this.artifactoryState = ArtifactoryState;
        this.artifactoryUploaderFactory = ArtifactoryUploaderFactory;
        this.errorQueue = [];
        this.multiSuccessMessage = '';
        this.TOOLTIP = TOOLTIP.artifacts.deploy;
        this.originalDeployPath = '';
        this.firstInit = true;
        this.uploadCompleted = false;
        this.isComposerExtention = false;

        this.REGEX_REPLACER = '@@@@';
    }

    $onInit() {
        this.comm.setController(this);
        this._initDeploy();
        this._initEvent();
    }

    /**
     * create uploader instance,
     * set methods callback
     * set path and file type
     * @private
     */
    _initDeploy() {
        let UPLOAD_REST_URL = `${API.API_URL}/artifact/upload`;
        this.deploySingleUploader = this.artifactoryUploaderFactory.getUploaderInstance(this)
                .setUrl(UPLOAD_REST_URL)
                .setOnSuccessItem(this.onSuccessItem)
                .setOnAfterAddingAll(this.onAfterAddingAll)
                .setOnAfterAddingFile(this.onAfterAddingFile)
                .setOnErrorItem(this.onUploadError)
                .setOnCompleteAll(this.onCompleteAll);
        this._setPathAndFileType(this.node ? this.node.data.path : '');
        this.deploySingleUploader.getUploader().headers = {'X-Requested-With': 'artUI', 'X-ARTIFACTORY-REPOTYPE' : this.deployFile.repoDeploy.repoType};
    }

    _initEvent() {
        this.$scope.$on('$destroy', this.onRemoveSingle.bind(this));
    }

    /**
     *  On file successfully uploaded: setting path for deploy.
     *  if maven repo set fields and path
     * @param fileDetails
     * @param response
     */
    onSuccessItem(fileDetails, response) {
        if (this.deployFile.repoDeploy.repoType !== 'Maven' && response.unitInfo.artifactType === 'maven') {
            response.unitInfo.artifactType = "base";
        }
        response.unitInfo.origArtifactType = response.unitInfo.artifactType;
        response.unitInfo.debianArtifact = response.unitInfo.artifactType==='debian';
        response.unitInfo.mavenArtifact = response.unitInfo.artifactType==='maven';
        response.unitInfo.vagrantArtifact = response.unitInfo.artifactType==='vagrant';
        response.unitInfo.composerArtifact = this.deployFile.repoDeploy.repoType==='Composer' && this.isComposerExtention;
        response.unitInfo.cranArtifact = this.deployFile.repoDeploy.repoType === 'CRAN';
//        response.unitInfo.originalMaven = response.unitInfo.artifactType==='maven';

        let tempBundle = this.deployFile.unitInfo ? this.deployFile.unitInfo.bundle : false;
        this.deployFile.unitInfo = response.unitInfo;
        this.deployFile.unitInfo.bundle = tempBundle;
        this.deployFile.unitInfo.type = fileDetails.file.name.substr(fileDetails.file.name.lastIndexOf('.')+1);
        //HA support
        this.deployFile.handlingNode = response.handlingNode;
        this.deployFile.unitConfigFileContent = response.unitConfigFileContent || "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns=\"http://maven.apache.org/POM/4.0.0\"\n    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n  <modelVersion>4.0.0</modelVersion>\n  <groupId></groupId>\n  <artifactId></artifactId>\n  <version></version>\n  <description>Artifactory auto generated POM</description>\n</project>\n";

        //MavenArtifact causes 'deploy as' checkbox to be lit -> change deployment path according to GAVC
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
            this.originalDeployPath = this.deployFile.targetPath;
            this.updateMavenTargetPath()
        }
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
            this.originalDeployPath = this.deployFile.targetPath;
            this.updateDebianTargetPath()
        }
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.cranArtifact) {
            this.originalDeployPath = this.deployFile.targetPath;
            if (this.deployFile.unitInfo.type === 'gz') {
                this.deployFile.targetPath = 'src/contrib' + this.deployFile.targetPath;
            }
        }
        if (this.comm) {
            this.needToCancel = true;
        }
    }

    /**
     * check if path includes file/archive if yes cut it from the path and set .
     * check if the current repo is local else clean path.
     * Reset garbage deployFile and fields if exists.
     * @param targetPath
     * @private
     */
    _setPathAndFileType(targetPath) {
        _.forEach(this.comm.reposList, (item) => {
           if (item.repoType === 'YUM') item.repoText = 'RPM';
        });

        if (this.node && this.node.data.isInsideArchive()) {
            targetPath = "";
        }
        else {
            if (this.node && (this.node.data.isFile() || this.node.data.isArchive())) {
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
            if (this.comm && this.comm.localRepo) {
                this.deployFile = {
                    repoDeploy: this.comm.localRepo,
                    targetPath: targetPath
                }
            } else {
                this.deployFile = {
                    repoDeploy: this.node && this.node.data.type == 'local' ? this.comm.reposList[0] : '',
                    targetPath: targetPath
                }
            }
        } else {
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact) {
                this.deployFile.unitInfo.mavenArtifact = false;
            }
            if (this.deployFile && this.deployFile.unitInfo && this.deployFile.unitInfo.debianArtifact) {
                this.deployFile.unitInfo.debianArtifact = false;
            }
            this.deployFile.unitInfo = {};
            this.deployFile.fileName = '';
            this.deploySingleUploader.clearQueue();
            this.deployFile.targetPath = targetPath;
        }
        this.uploadCompleted = false;
        this.firstInit = false;
    }

    /**
     * if maven file upload
     * update path by gavc (onChange)
     */
    updateMavenTargetPath() {
        let newPath = '';
        if (this.deployFile.unitInfo.groupId) {
            newPath += this.deployFile.unitInfo.groupId.replace(/\./g, '/');
        }
        newPath += '/' + (this.deployFile.unitInfo.artifactId || '');
        newPath += '/' + (this.deployFile.unitInfo.version || '');
        newPath += '/' + (this.deployFile.unitInfo.artifactId || '');
        newPath += '-' + (this.deployFile.unitInfo.version || '');
        if (this.deployFile.unitInfo.classifier) {
            newPath += '-' + this.deployFile.unitInfo.classifier;
        }
        newPath += '.' + (this.deployFile.unitInfo.type || '');

        this.deployFile.targetPath = newPath;
        this._bindToPomXml();
    }

    /**
     * bind and update maven  xml (depend on updateMavenTargetPath)
     * @private
     */
    _bindToPomXml() {
        if (typeof window.DOMParser != 'undefined' && typeof window.XMLSerializer != 'undefined'
                && this.deployFile.unitConfigFileContent) {
            //Parse the code mirror model into xml object and modify based on input fields
            let parser = new DOMParser();
            let pomXml = parser.parseFromString(this.deployFile.unitConfigFileContent, "text/xml");
            let groupId = pomXml.getElementsByTagName('groupId');
            if (groupId.length) {
                if (groupId[0].hasChildNodes()) {
                    groupId[0].childNodes[0].nodeValue = this.deployFile.unitInfo.groupId;
                } else {
                    groupId[0].textContent = this.deployFile.unitInfo.groupId;
                }
            }
            var artifactId = pomXml.getElementsByTagName('artifactId');
            if (artifactId.length) {
                if (artifactId[0].hasChildNodes()) {
                    artifactId[0].childNodes[0].nodeValue = this.deployFile.unitInfo.artifactId;
                } else {
                    artifactId[0].textContent = this.deployFile.unitInfo.artifactId;
                }
            }
            var version = pomXml.getElementsByTagName('version');
            if (version.length) {
                if (version[0].hasChildNodes()) {
                    version[0].childNodes[0].nodeValue = this.deployFile.unitInfo.version;
                } else {
                    version[0].textContent = this.deployFile.unitInfo.version;
                }
            }
            //Serialize updated pom xml back to string and re-set as model
            let backToText = new XMLSerializer();
            this.deployFile.unitConfigFileContent = backToText.serializeToString(pomXml);
        }
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
        if (this.deployFile.unitInfo && this.deployFile.unitInfo.cranArtifact
            && (this.deployFile.unitInfo.type === 'zip' || this.deployFile.unitInfo.type === 'tgz')) {
            ok = !!this.deployFile.unitInfo.distribution && !!this.deployFile.unitInfo.rVersion;
        }
        return ok && this.uploadCompleted && this.deployFile.repoDeploy && (!this.layoutTokensForm || this.layoutTokensForm.$valid) && (!this.mavenArtifactForm || this.mavenArtifactForm.$valid);
    }

    /**
     * if debian file upload
     * update path onChange
     */
    updateDebianTargetPath() {
        let path;
        if (this.deployFile.targetPath.indexOf(';') != -1) {
            path = this.deployFile.targetPath.substring(0, this.deployFile.targetPath.indexOf(';'));
        }
        else {
            path = this.deployFile.targetPath;
        }
        let newPath = '';
        newPath += ( path || '');
        if (this.deployFile.unitInfo.distribution) {
            let dist=this.deployFile.unitInfo.distribution ? this.deployFile.unitInfo.distribution.split(';').join(';deb.distribution=') : null;
            newPath += ";deb.distribution=" + (dist || '');
        }
        if (this.deployFile.unitInfo.component) {
            let comp=this.deployFile.unitInfo.component ? this.deployFile.unitInfo.component.split(';').join(';deb.component=') : null;
            newPath += ";deb.component=" + (comp || '');
        }
        if (this.deployFile.unitInfo.architecture) {
            let arch=this.deployFile.unitInfo.architecture ? this.deployFile.unitInfo.architecture.split(';').join(';deb.architecture=') : null;
            newPath += ";deb.architecture=" + (arch || '');
        }
        this.deployFile.targetPath = '';
        this.deployFile.targetPath = newPath;
    }

    /**
     * if vagrant box file upload
     * update path onChange
     */
    updateVagrantTargetPath() {
        let path;
        if (this.deployFile.targetPath.indexOf(';') != -1) {
            path = this.deployFile.targetPath.substring(0, this.deployFile.targetPath.indexOf(';'));
        }
        else {
            path = this.deployFile.targetPath;
        }
        let newPath = '';
        newPath += ( path || '');
        if (this.deployFile.unitInfo.boxName) {
            let name=this.deployFile.unitInfo.boxName ? this.deployFile.unitInfo.boxName.split(';').join(';box_name=') : null;
            newPath += ";box_name=" + (name || '');
        }
        if (this.deployFile.unitInfo.boxProvider) {
            let prov=this.deployFile.unitInfo.boxProvider ? this.deployFile.unitInfo.boxProvider.split(';').join(';box_provider=') : null;
            newPath += ";box_provider=" + (prov || '');
        }
        if (this.deployFile.unitInfo.boxVersion) {
            let ver=this.deployFile.unitInfo.boxVersion ? this.deployFile.unitInfo.boxVersion.split(';').join(';box_version=') : null;
            newPath += ";box_version=" + (ver || '');
        }
        this.deployFile.targetPath = '';
        this.deployFile.targetPath = newPath;
    }

    updateComposerTargetPath() {
        let path;
        if (this.deployFile.targetPath.indexOf(';') != -1) {
            path = this.deployFile.targetPath.substring(0, this.deployFile.targetPath.indexOf(';'));
        }
        else {
            path = this.deployFile.targetPath;
        }
        let newPath = '';
        newPath += ( path || '');
        if (this.deployFile.unitInfo.version) {
            let name=this.deployFile.unitInfo.version ? this.deployFile.unitInfo.version.split(';').join(';composer.version=') : null;
            newPath += ";composer.version=" + (name || '');
        }

        this.deployFile.targetPath = '';
        this.deployFile.targetPath = newPath;
    }
    /**
     *onAfterAddingAll Verifies upload only one file
     * @param fileItems
     */
    onAfterAddingAll(fileItems) {
        if (fileItems.length > 1) {
            this.artifactoryNotifications.create({error: "You can only deploy one file"});
            this.deploySingleUploader.clearQueue();
            return;
        }

        let uploadAll = true;

        fileItems.forEach((item)=> {
            if (!item.okToUploadFile) {
                uploadAll = false;
                return;
            }
        });
        if (uploadAll) {
            this.deploySingleUploader.uploadAll();
        }
        else {
            return;
        }
    }

    onAfterAddingFile(fileItem) {
        this.isBundle = _.endsWith(fileItem.file.name, 'zip') ||
                        _.endsWith(fileItem.file.name, 'tar') ||
                        _.endsWith(fileItem.file.name, 'tgz') ||
                        _.endsWith(fileItem.file.name, '7z') ||
                        _.endsWith(fileItem.file.name, 'tar.gz') ;
        this.isComposerExtention = (/\.(zip|gz|tar|rar|phar|tar.gz|xz)$/i).test(fileItem.file.name);
        this.deployFile.fileName = fileItem.file.name;
        if (this.deployFile.targetPath.slice(-1) != "/") {
            this.deployFile.targetPath += "/";
        }
        this.deployFile.targetPath += fileItem.file.name;

        if (fileItem.file.size < 0) {
            fileItem.okToUploadFile = false;
            this.deploySingleUploader.removeFileFromQueue(fileItem);
        } else if (this.isExceedingSizeLimit(fileItem)) {
            fileItem.okToUploadFile = false;
            this.cancelFileUploadExceedingLimit(fileItem);
        } else {
            // Save original for display
            fileItem.file.originalName = fileItem.file.name;
            // Encode filename to support UTF-8 strings (server does decode)
            fileItem.file.name = encodeURIComponent(fileItem.file.name);
            fileItem.okToUploadFile = true;
            // this.deploySingleUploader.getUploader().headers['X-ARTIFACTORY-REPOTYPE'] = this.node.data.repoPkgType;
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

        this.deploySingleUploader.removeFileFromQueue(fileItem);
        this.clearPath();
    }

    /**
     * convert file size from MB to Bytes
     * @param fileSizeInMB
     */
    toBytes(fileSizeInMB){
        return fileSizeInMB*1000000;
    }

    /**
     * when upload item error push to queue for notifications
     * @param item
     * @param response
     */
    onUploadError(item, response) {
        this.errorQueue.push({item: item, response: response});
        this.artifactoryNotifications.create(response);
        this.deploySingleUploader.removeFileFromQueue(item);

        this.deployFile.unitInfo = {};
        this.clearPath();
    }

    /**
     * trigger if user checked for edit maven gavc
     * if not set artifactType = origArtifactType
     */
    changeMavenFileType() {
        if (!this.deployFile.unitInfo.mavenArtifact) {
            this.deployFile.unitInfo.maven = false;
            this.deployFile.unitInfo.artifactType = this.deployFile.unitInfo.origArtifactType === 'maven' ? 'base' : this.deployFile.unitInfo.origArtifactType;
            if (this.originalDeployPath) {
                this.deployFile.targetPath = angular.copy(this.originalDeployPath);
            }
            this.tempUnitConfigFileContent = this.deployFile.unitConfigFileContent;
            delete this.deployFile.unitConfigFileContent;
        }
        else {
            this.deployFile.unitInfo.maven = true;
            this.deployFile.unitInfo.artifactType = 'maven';
            this.originalDeployPath = angular.copy(this.deployFile.targetPath);
            if (this.tempUnitConfigFileContent) {
                this.deployFile.unitConfigFileContent = this.tempUnitConfigFileContent;
            }
            this.updateMavenTargetPath();
        }
    }

    /**
     * trigger if user checked for edit debian
     * if not set artifactType = origArtifactType
     */

    changeDebianFileType() {
        if (!this.deployFile.unitInfo.debianArtifact) {
            this.deployFile.unitInfo.artifactType = this.deployFile.unitInfo.origArtifactType === 'debian' ? 'base' : this.deployFile.unitInfo.origArtifactType;
            this.deployFile.unitInfo.debian = false;
            if (this.originalDeployPath) {
                this.deployFile.targetPath = angular.copy(this.originalDeployPath);
            }
        }
        else {
            this.deployFile.unitInfo.debian = true;
            this.deployFile.unitInfo.artifactType = 'debian';
            this.originalDeployPath = angular.copy(this.deployFile.targetPath);
            this.updateDebianTargetPath();
        }
    }

    onBundleDeploy() {
        if (this.deployFile.unitInfo.bundle) {
            this.tempPathBeforeBundle = this.deployFile.targetPath;
            if (!this.targetChanged) {
                this.deployFile.targetPath = this.deployFile.targetPath.substr(0,this.deployFile.targetPath.lastIndexOf('/')+1);
            }
        }
        else if (!this.deployFile.unitInfo.bundle) {
            this.deployFile.targetPath = this.tempPathBeforeBundle;
        }
    }

    onToggleDeployByLayout() {
        if (this.deployFile.unitInfo.deployByLayout) {
            this.tempPathBeforeUsingLayout = this.deployFile.targetPath;
            if (!this.layoutTokens) {
                this.extractTokensFromLayout();
            }
            this.deployFile.targetPath = this.getPathFromLayoutTokens();
        }
        else {
            this.deployFile.targetPath = this.tempPathBeforeUsingLayout;
        }
    }

    onCompleteAll() {
        this.progress = false;
        let body = '<ul>';
        this.artifactoryNotifications.clear();
        if (this.errorQueue.length) {
            this.errorQueue.forEach((error)=> {
                body += '<li>"' + error.item.file.name + '" ' + error.response.error + '</li>'
            })
            body += '</ul>';
            this.artifactoryNotifications.createMessageWithHtml({type: 'error', body: body, timeout: 10000});
            this.deploySingleUploader.clearQueue();
            this.errorQueue = [];
        }
        else { //only when no errors
            this.uploadCompleted = true;
        }
    }

    /**
     * when user removed selected file 'clearPath' is calling
     */
    clearPath() {
        if (this.node && (this.node.data.isFolder() || this.node.data.isRepo())) {
            this.deployFile.targetPath = this.node.data.path;
        } else {
            this.deployFile.targetPath = this.deployFile.targetPath.replace("/" + this.deployFile.fileName, "");
        }
        delete this.layoutTokens;
        this.targetChanged = false;
        this.uploadCompleted = false;
    }

    /**
     *
     * cancel file upload remove from server stock
     */
    onRemoveSingle() {
        if (this.needToCancel) {
            this.artifactDeployDao.cancelUpload({fileName: this.deployFile.fileName});
            this.needToCancel = false;
        }
    }

    /**
     * deploy after adding file to queue
     */
    deployArtifacts() {
        let singleDeploy = {};
        singleDeploy.action = "deploy";
        singleDeploy.unitInfo = this.deployFile.unitInfo;
        singleDeploy.unitInfo.path = angular.copy(this.deployFile.targetPath);
        singleDeploy.fileName = this.deployFile.fileName;
        singleDeploy.repoKey = this.deployFile.repoDeploy.repoKey;
        singleDeploy.handlingNode = this.deployFile.handlingNode;

        if (this.deployFile.unitInfo.Internal && this.deployFile.unitConfigFileContent) {
            singleDeploy.publishUnitConfigFile = true;
            singleDeploy.unitConfigFileContent = this.deployFile.unitConfigFileContent;
        }
        if (!this.deployFile.unitInfo.bundle) {
            this.artifactDeployDao.post(singleDeploy).$promise.then((result)=> {
                if (result.data) {
                    this.deploySuccess(result.data);
                }
            });
        }
        else {
            this.artifactDeployDao.postBundle(singleDeploy).$promise.then((result)=> {
                if (result.data) {
                    this.deploySuccess(result.data);
                }
            });
        }
    }

    deploySuccess(data) {
        this.artifactoryNotifications.createMessageWithHtml(this.comm.createNotification(data));
        this.needToCancel = false;
        this.onSuccess();
        this.GoogleAnalytics.trackEvent('Artifacts', 'Deploy succeed' , this.node.data.repoPkgType , null , this.node.data.repoType, 'single');
    }

    isMavenCheckBoxVisible() {
//        return this.deployFile.unitInfo && (this.deployFile.unitInfo.originalMaven || (this.deployFile.unitInfo.valid && this.deployFile.repoDeploy.repoType === 'Maven'));
        return this.deployFile.unitInfo && (this.deployFile.unitInfo.valid && this.deployFile.repoDeploy.repoType === 'Maven');
    }
    isMavenSectionVisible() {
        return this.deployFile.unitInfo && this.deployFile.unitInfo.mavenArtifact && this.isMavenCheckBoxVisible();// && (this.deployFile.unitInfo.maven || (this.deployFile.unitInfo.valid && this.deployFile.repoDeploy.repoType === 'Maven'));
    }


    extractTokensFromLayout() {
        let allRegexes;

        let extractToken = (tokenMatch,customSegment) => {
            let tokenKey = tokenMatch[1];
            let tokenExact = tokenMatch[0];

            if (_.contains(tokenKey,this.REGEX_REPLACER)) {
                tokenKey = tokenKey.replace(this.REGEX_REPLACER,allRegexes[0])
                tokenExact = tokenExact.replace(this.REGEX_REPLACER,allRegexes[0])
                if (customSegment) customSegment.regex = allRegexes[0];
                allRegexes.shift();
            }

            let tokenRegex = tokenKey.match(/\<(.*?)\>/);
            if (tokenRegex) {
                tokenKey = tokenKey.split(tokenRegex[0]).join('');
            }

            if (customSegment) customSegment.tokens.push(tokenKey);
            this.layoutTokens[tokenKey] = {
                exactString: tokenExact,
                customSegment: customSegment,
                regex: tokenKey==='folderItegRev' ? this.deployFile.repoDeploy.layoutFolderItegRevRegex : tokenKey==='fileItegRev' ? this.deployFile.repoDeploy.layoutFileItegRevRegex : tokenRegex ? tokenRegex[1] : undefined,
                value: tokenKey==='ext' ? this.deployFile.unitInfo.type : ''
            };
        };

        let pattern = this.deployFile.repoDeploy.layoutPattern;
        this.layoutTokens = {};

        //Temporarily remove '[' and ']' inside custom regex
        allRegexes = pattern.match(/\<(.*?)\>/g);
        pattern = pattern.replace(/\<(.*?)\>/g,this.REGEX_REPLACER);
        //

        let nextToken = pattern.match(/\[(.*?)\]/);
        while (nextToken) {
            let nextTokenIndex = pattern.search(/\[(.*?)\]/);
            let nextCustomSegmentIndex = pattern.search(/\((.*?)\)/);

            if (nextTokenIndex < nextCustomSegmentIndex || nextCustomSegmentIndex === -1) {
                extractToken(nextToken);
                pattern = pattern.replace(nextToken[0],'');
                nextToken = pattern.match(/\[(.*?)\]/);
            }
            else {
                let customSegment = {
                    match: pattern.match(/\((.*?)\)/),
                    tokens: []
                };
                let customPattern = customSegment.match[1];
                let nextCustomToken = customPattern.match(/\[(.*?)\]/);
                while (nextCustomToken) {
                    extractToken(nextCustomToken,customSegment);
                    customPattern = customPattern.replace(nextCustomToken[0],'');
                    nextCustomToken = customPattern.match(/\[(.*?)\]/);
                }
                pattern = pattern.replace(customSegment.match[0],'');
                nextToken = pattern.match(/\[(.*?)\]/);
            }
        }

    }

    getPathFromLayoutTokens() {
        let pattern = this.deployFile.repoDeploy.layoutPattern;
        for (let token in this.layoutTokens) {
            let customSegment = this.layoutTokens[token].customSegment;
            if (customSegment) {
                let allFilled = true;
                for (let i in customSegment.tokens) {
                    if (!this.layoutTokens[customSegment.tokens[i]].value) {
                        allFilled = false;
                        break;
                    }
                }
                if (allFilled) {
                    pattern = pattern.split(customSegment.match[0].replace(this.REGEX_REPLACER,customSegment.regex)).join(customSegment.match[1]).replace(this.REGEX_REPLACER,customSegment.regex);
                    pattern = pattern.split(this.layoutTokens[token].exactString).join(this.layoutTokens[token].value);
                }
                else {
                    pattern = pattern.split(customSegment.match[0].replace(this.REGEX_REPLACER,customSegment.regex)).join('');
                }
            }
            else {
                if (this.layoutTokens[token].value) pattern = pattern.split(this.layoutTokens[token].exactString).join(this.layoutTokens[token].value);
            }
        }
        return pattern;
    }

    updatePathFromLayoutTokens() {
        this.deployFile.targetPath = this.getPathFromLayoutTokens();
    }

    isTokenValueValid(tokenKey,value) {
        return !value || !this.layoutTokens[tokenKey].regex || (new RegExp('^'+this.layoutTokens[tokenKey].regex+'$')).test(value);
    }

    getTokenRegexError(tokenKey) {
        let showErrors = this.layoutTokensForm[`token-${tokenKey}`].showErrors;
        let errObj = this.layoutTokensForm[`token-${tokenKey}`].$error;
        if (showErrors && errObj.tokenRegexValidator && !errObj.required) {
            return `Value do not match regex: ${this.layoutTokens[tokenKey].regex}`
        }
    }

}
export function jfSingleDeploy() {
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
        controller: jfSingleDeployController,
        controllerAs: 'jfSingleDeploy',
        bindToController: true,
        templateUrl: 'directives/jf_deploy/jf_single_deploy.html'
    }
}
