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
import API from '../constants/api.constants';
import EVENTS from '../constants/artifacts_events.constants';
import ACTIONS from '../constants/artifacts_actions.constants';
import MESSAGES from '../constants/artifacts_messages.constants';

export class ArtifactActions {
    constructor(JFrogEventBus, ArtifactActionsDao, StashResultsDao, $state, $window, $rootScope, $timeout, JFrogNotifications, FooterDao, $sce,
                JFrogModal, selectTargetPath, selectDeleteVersions, PushToBintrayModal, DistributionDao, $q, JFrogIFrameDownload, NativeBrowser,
                User, GoogleAnalytics, ArtifactXrayDao) {
        this.$q = $q;
        this.$state = $state;
        this.$timeout = $timeout;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryNotifications = JFrogNotifications;
        this.footerDao = FooterDao;
        this.artifactActionsDao = ArtifactActionsDao;
        this.stashResultsDao = StashResultsDao;
        this.pushToBintrayModal = PushToBintrayModal;
        this.distributionDao = DistributionDao;
        this.modal = JFrogModal;
        this.selectTargetPath = selectTargetPath;
        this.selectDeleteVersions = selectDeleteVersions;
        this.$window = $window;
        this.$rootScope = $rootScope;
        this.iframeDownload = JFrogIFrameDownload;
        this.nativeBrowser = NativeBrowser;
        this.$sce = $sce;
        this.userService = User;
        this.GoogleAnalytics = GoogleAnalytics;
        this.ArtifactXrayDao = ArtifactXrayDao;
    }

    perform(actionObj, node, context) {
        let actionData = {
            actionTitle: ACTIONS[actionObj.name] ? ACTIONS[actionObj.name].title : actionObj.name,
            repoType: node.data.repoType,
            packageType: node.data.repoPkgType,
            triggeredFrom: context ? 'context menu' : 'drop down'
        }

        this.GoogleAnalytics.trackEvent('Artifacts' , actionData.triggeredFrom , actionData.actionTitle , null , actionData.packageType , actionData.repoType);

        return this['_do' + actionObj.name](node);
    }

    _doDownload(actionObj) {
        if (actionObj.data.xrayShouldValidate) {
	        this.ArtifactXrayDao.isBlocked({repoKey: actionObj.data.repoKey, path: actionObj.data.path}).$promise.then((response) => {
		        if (response.status === 200) {
			        this.iframeDownload(actionObj.data.actualDownloadPath);
		        }
	        })
        }
    }

    _doRefresh(node) {
        this.JFrogEventBus.dispatch(EVENTS.ACTION_REFRESH, node);
    }

    _doCopy(node, useNodePath) {

        let target;
        let onActionDone;
        onActionDone = (retData) => {
            target = retData.target;
            this._performActionInServer('copy', node, target)
                    .then(()=>{
                        retData.onSuccess().then((response) => {
                            this.JFrogEventBus.dispatch(EVENTS.ACTION_COPY,{node: node, target: target});
                        });
                    })
                    .catch((err)=>{
                        retData.onFail(err.data.errors).then(onActionDone);
                    });
        }
        this.selectTargetPath('copy', node, useNodePath).then(onActionDone)
    }

    _doMove(node, useNodePath) {
        let target;
        let onActionDone;
        onActionDone = (retData) => {
            target = retData.target;
            this._performActionInServer('move', node, target)
                    .then((data)=>{
                        retData.onSuccess().then((response) => {
                            this.JFrogEventBus.dispatch(EVENTS.ACTION_MOVE,{node: node, target: target});
                        });
                    })
                    .catch((err)=>{
                        retData.onFail(err.data.errors).then(onActionDone);
                    });
        }
        this.selectTargetPath('move', node, useNodePath).then(onActionDone);
    }

    _doStashAction(node,action) {
        let target;
        let onActionDone;
        let dryRun;
        onActionDone = (retData) => {
            target = retData.target;
            this.stashResultsDao[action]({name: 'stash', repoKey: target.targetRepoKey},{}).$promise
                .then(()=>{
                    retData.onSuccess().then((response) => {
                        this.JFrogEventBus.dispatch(EVENTS.ACTION_MOVE_STASH,{node: node, target:{targetPath: '/', targetRepoKey: target.targetRepoKey}});
                    });
                })
                .catch((err)=>{
                    this.JFrogEventBus.dispatch(EVENTS.ACTION_REFRESH_STASH);
                    retData.onFail(err.data.errors).then(onActionDone);
                });
        };

        dryRun = ()=>{
            let modalScope = dryRun.scope;
            this.stashResultsDao['silent'+action.charAt(0).toUpperCase()+action.substring(1)]({name: 'stash', repoKey: modalScope.target.repoKey, dryRun: true},{}).$promise
                    .then((response)=>{
                        modalScope.resultError = false;
                        modalScope.dryRunResults = [response.info];
                    })
                    .catch((response) => {
                        modalScope.resultError = true;
                        modalScope.dryRunResults = response.data.errors;
                    });
        };

        this.selectTargetPath(action+'Stash', node, false, dryRun).then(onActionDone)
    }

    _doCopyStash(node) {
        this._doStashAction(node,'copy');
    }

    _doMoveStash(node) {
        this._doStashAction(node,'move');
    }

    _doDiscardFromStash(node) {
        let doAction = () => {
            this.stashResultsDao.discard({
                name:'stash',
                repoKey:node.data.repoKey,
                path:node.data.path
            },{}).$promise.then((res)=>{
                        if (res.status === 200) {
                            this.JFrogEventBus.dispatch(EVENTS.ACTION_DISCARD_FROM_STASH,node);
                        }
                    });
        };

        if (!node.alreadyDeleted) {
            this.modal.confirm('Are you sure you wish to discard \'' + node.text + '\' from stashed search results?',
                    'Discard from stash', {confirm: 'Discard'})
                    .then(doAction);
        }
        else doAction();


    }

    _doDiscardStash() {
        this.modal.confirm('Are you sure you wish to discard stashed search results?','Discard search results', {confirm: 'Discard'})
                .then(() => {
                    this.stashResultsDao.delete({name:'stash'}).$promise.then(()=>{
                        this.JFrogEventBus.dispatch(EVENTS.ACTION_DISCARD_STASH);
                    });
                });
    }

    _doShowInTree(node) { //stash
        this.JFrogEventBus.dispatch(EVENTS.ACTION_EXIT_STASH,node);
    }

    _doUploadToBintray(node) {
        this.pushToBintrayModal.launchModal(node.type === 'docker' ? 'docker' : 'artifact', {
            repoPath: node.data.repoKey + ':' + node.data.path
        });
    }

    _doRedistribute(node) {
        this._doDistribute(node, true);
    }

    stripHtml(htmlStrig) {
        let tmp = document.createElement("DIV");
        tmp.innerHTML = htmlStrig;
        return tmp.textContent || tmp.innerText || htmlStrig;
    }

    hasHtmlElements(someString){
        return /<[a-z][\s\S]*>/i.test(someString);
    }

    _doDistribute(node,redistribute = false) {
        this.distributionDao.getAvailableDistributionRepos({}).$promise.then((data)=>{

            let modalInstance;
            this.distributeModalScope = this.$rootScope.$new();

            let nodeText = node.text ? node.text : node.data.text;
            if(nodeText.indexOf('<span')!==-1 || this.hasHtmlElements(nodeText)){
                nodeText = this.stripHtml(nodeText);
            }

            let position = nodeText.split('/').length - 1;
            this.nodeText = nodeText.split('/')[position];

            this.distributeModalScope.title = "Distribute " + this.nodeText;

            this.distributeModalScope.data = {};

            let response = data.data;
            if(response.offlineMode) {
                this.distributeModalScope.errorMessage = MESSAGES.actions.distribute.inOfflineMode.message;
                this.distributeModalScope.messageType = MESSAGES.actions.distribute.inOfflineMode.messageType;
            }
            else if(!response.distributionRepoConfigured) {
                if(this.userService.getCurrent().isAdmin()){
                    this.distributeModalScope.errorMessage = MESSAGES.actions.distribute.noRepos.admin.message;
                    this.distributeModalScope.messageType =  MESSAGES.actions.distribute.noRepos.admin.messageType;
                }
                else{
                    this.distributeModalScope.errorMessage = MESSAGES.actions.distribute.noRepos.nonAdmin.message;
                    this.distributeModalScope.messageType =  MESSAGES.actions.distribute.noRepos.nonAdmin.messageType;
                }
            }
            else if(response.availableDistributionRepos.length === 0) {
                this.distributeModalScope.errorMessage = MESSAGES.actions.distribute.noPermissions.message;
                this.distributeModalScope.messageType = MESSAGES.actions.distribute.noPermissions.messageType;
            }

            if (redistribute) {
                this.distributeModalScope.distributionRepositoriesOptions = [node.data.repoKey];
                this.distributeModalScope.data.selectedRepo = node.data.repoKey;
                this.distributeModalScope.redistribute = true;
            }
            else {
                this.distributeModalScope.distributionRepositoriesOptions = _.map(data.data.availableDistributionRepos, 'repoKey');
                this.distributeModalScope.data.selectedRepo = null;
            }

            this.distributeModalScope.data.async = true;
            this.distributeModalScope.data.publish = true;
            this.distributeModalScope.data.overrideExistingFiles = false;

            this.distributeModalScope.distribute = () => {
                this._resetChanges();
                this.distributionDao.distributeArtifact({
                    targetRepo: this.distributeModalScope.data.selectedRepo,
                    async: this.distributeModalScope.data.async,
                    publish: this.distributeModalScope.data.publish,
                    overrideExistingFiles: this.distributeModalScope.data.overrideExistingFiles
                }, {repoKey: node.data.repoKey, path: node.data.path}).$promise.then((res)=>{
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
                this.distributionDao.distributeArtifact({
                    targetRepo: this.distributeModalScope.data.selectedRepo,
                    async: this.distributeModalScope.data.async,
                    publish: this.distributeModalScope.data.publish,
                    overrideExistingFiles: this.distributeModalScope.data.overrideExistingFiles,
                    dryRun: true
                }, {repoKey: node.data.repoKey, path: node.data.path}).$promise.then((res)=>{
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

    _doCopyContent(node) {
        this._doCopy(node, false);
    }

    _doMoveContent(node) {
        this._doMove(node, false);
    }

    _doWatch(node) {
        this._performActionInServer('watch', node, {}, {param: 'watch'})
                .then((response) => {
                    node.data.refreshWatchActions().then(() => {
                        this.JFrogEventBus.dispatch(EVENTS.ACTION_WATCH, node);
                    })
                });
    }

    _doUnwatch(node) {
        this._performActionInServer('watch', node, {}, {param: 'unwatch'})
                .then((response) => {
                    node.data.refreshWatchActions().then(() => {
                        this.JFrogEventBus.dispatch(EVENTS.ACTION_UNWATCH, node);
                    })
                });
    }

    _doView(node) {
        this._performActionInServer('view', node)
                .then((response) => {
                    this.modal.launchCodeModal(node.data.text, response.data.fileContent,
                            {name: node.data.mimeType})
                });
    }

    _doDeletePermanently(node) {
        this._doDelete(node);
    }

    _doDelete(node) {
        let permanent = this.footerDao.getInfo().trashDisabled || node.data.isInTrashcan();

        //allowPermDeletes is disabled for now (30.12.15)
        let allowPerm = false;//this.footerDao.getInfo().allowPermDeletes;

        let onPermChange = (perm, scope) => {
            scope.content = this.$sce.trustAsHtml('Are you sure you wish to' + (perm ? ' <span class="highlight-alert">permanently</span> ' : ' ') + 'delete this file?');
        };

        this.modal.confirm('Are you sure you wish to' + (permanent ? ' <span class="highlight-alert">permanently</span> ' : ' ') + 'delete this file?', 'Delete ' + node.data.text,
                {confirm: 'Delete'},allowPerm ? "Delete permanently" : undefined, onPermChange)
                .then((permDelete) => {
                    this._performActionInServer('delete', node, permDelete ? {permDelete: permDelete} : undefined).then(()=>{
                        this.JFrogEventBus.dispatch(EVENTS.ACTION_DELETE, node);
                    })

                })
    }

    _doDeleteContent(node) {
        let permanent = this.footerDao.getInfo().trashDisabled;

        this.modal.confirm('Are you sure you want to delete the content of this repository? All artifacts will be' + (permanent ? ' <span class="highlight-alert">permanently</span> ' : ' ') + 'deleted.', 'Delete Content Of \'' + node.data.text +'\'', {confirm: 'Delete Content'})
                .then(() => this._performActionInServer('delete', node))
                .then((response) => this.JFrogEventBus.dispatch(EVENTS.ACTION_DELETE_CONTENT, node));
    }

    _doDeleteVersions(node) {
        var versions;
        this.selectDeleteVersions(node)
                .then((_versions) => {
                    versions = _versions;
                    return this.modal.confirm('Are you sure you wish to delete '+_versions.length+' selected versions?\n\nThis folder may contain artifacts that are part of the result of or used as dependencies in published build(s).','Delete '+_versions.length+' Versions')
                })
                .then(() => {
                    let promise = this._performActionInServer('deleteversions', null, versions);
                    promise.then(()=>{
                        this._doRefresh(node);
                    });
                    return promise;
                });
    }

    _doZap(node) {
        this._performActionInServer('zap', node).then((data)=> {
//            console.log(data);
        })
    }

    _doZapCaches(node) {
        this._performActionInServer('zapVirtual', node).then((data)=> {
            // console.log(data);
        })
    }

    _doRecalculateIndex(node) {
//        console.log('recalculate index', node);
        this._performActionInServer('calculateIndex', node,
                {"type": node.data.repoPkgType, "repoKey": node.data.repoKey}).then((data)=> {
//            console.log(data);
        })
    }

    _doCalculateDebianCoordinates(node) {
        this._performActionInServer('calculateDebianCoordinates', node,
                {"type": 'Debian-Cache', "repoKey": node.data.repoKey}).then((data)=> {
        })
    }

    _doRestoreToOriginalPath(node) {
        return this._doRestore(node,false)
    }

    _doRestore(node,chooseTarget = true) {

        let defer = this.$q.defer();

        let restoreTarget = {
            targetPath: node.data.path.indexOf('/') !== -1 ? node.data.path.substr(node.data.path.indexOf('/')) : '/',
            targetRepoKey: node.data.path.indexOf('/') !== -1 ? node.data.path.substr(0,node.data.path.indexOf('/')) : node.data.path
        };

        let onActionDone = (retData) => {
            let target = retData.target;
            this._performActionInServer('restore', node, target)
                .then((data)=>{
                    if (retData.onSuccess) retData.onSuccess().then((response) => {
                        this.JFrogEventBus.dispatch(EVENTS.ACTION_MOVE,{node: node, target: target});
                        defer.resolve();
                    });
                    else {
                        defer.resolve();
                    }
                })
                .catch((err)=>{
                    if (retData.onFail) retData.onFail(err.data.errors).then(onActionDone);
                    defer.reject();
                });
        }
        if (chooseTarget) {
            this.selectTargetPath('restore', node, false, false, restoreTarget).then(onActionDone);
        }
        else {
            onActionDone({target: restoreTarget})
        }

        return defer.promise;
/*
        this.modal.confirm('Are you sure you wish to restore this file?', 'Restore ' + node.data.text,
            {confirm: 'Restore'})
            .then(() => this._performActionInServer('restore', node,restoreTarget))
            .then((response) => this.JFrogEventBus.dispatch(EVENTS.ACTION_MOVE,{node: node, target: restoreTarget}));
*/
    }

    _doEmptyTrash(node) {

        this.modal.confirm('Are you sure you want to empty the trash can?', 'Empty Trash',
            {confirm: 'Empty Trash'})
            .then(() => this._performActionInServer('emptytrash', node))
            .then((response) => this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH, node));
    }

    _doSearchTrash(node) {
        this.$state.go('search',{'searchType':'trash'});
    }

    _doDownloadFolder(node) {
        this.artifactActionsDao.performGet({
            action: 'downloadfolderinfo',
            path: node.data.path,
            repoKey: node.data.repoKey
        }).$promise.then((data)=>{
            let modalInstance;
            let modalScope = this.$rootScope.$new();
            modalScope.totalSize = data.data.sizeMB;
            modalScope.filesCount = data.data.totalFiles;
            modalScope.folderName = node.data.text;
            modalScope.archiveTypes = ['zip','tar','tar.gz','tgz'];
            modalScope.selection = {archiveType: 'zip'};
            modalScope.options = {includeChecksumFiles :false};
            modalScope.download = () => {
                this.iframeDownload(`${API.API_URL}/artifactactions/downloadfolder?repoKey=${node.data.repoKey}&path=${encodeURIComponent(node.data.path)}&archiveType=${modalScope.selection.archiveType}&includeChecksumFiles=${modalScope.options.includeChecksumFiles}`,'There are too many folder download requests currently running, try again later.');
                modalInstance.close();
            };

            modalScope.isHighlighted = (ind) => {
                let search = $('.blocked-items-dnd-list').find('li').eq(ind).find('.ui-select-highlight').length;
                return (search ? true : false);
            };

            modalScope.cancel = () => modalInstance.close();
            modalInstance = this.modal.launchModal('download_folder_modal', modalScope, 'sm');
        });
    }

    _doNativeBrowser(node) {
        this.$window.open(this.nativeBrowser.pathFor(node.data),"_blank");
    }

    _doFavorites(node) {
        let repoKey = node.data.repoKey;

        if (!localStorage.favoritesRepos) {
            localStorage.setItem('favoritesRepos', JSON.stringify({favoritesRepos: [repoKey]}));
        } else {
            let favoritesRepos = JSON.parse(localStorage.favoritesRepos);

            let isRepoInFavorites = _.contains(favoritesRepos.favoritesRepos, repoKey);

            if (isRepoInFavorites) {
                favoritesRepos.favoritesRepos = _.remove(favoritesRepos.favoritesRepos, (i) => i !== repoKey); // Remove from favorites
            } else {
                favoritesRepos.favoritesRepos.push(repoKey); // Add to favorites
            }
            localStorage.setItem('favoritesRepos', JSON.stringify({favoritesRepos: favoritesRepos.favoritesRepos}));
            this.JFrogEventBus.dispatch(EVENTS.ACTION_FAVORITES);
            if (localStorage.filterFavorites) this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH_FILTER);

        }
        delete node.$cachedCMItems;

    }

    // Do the actual action on the server via the DAO:
    _performActionInServer(actionName, node, extraData = {}, extraParams = {}) {
        let data;
        if (node) {
            data = angular.extend({
                repoKey: node.data.repoKey,
                path: node.data.path,
                param: extraParams.param
            }, extraData);
        }
        else {
            data = extraData;
        }
        var params = angular.extend({action: actionName}, extraParams);
        return this.artifactActionsDao.perform(params, data).$promise;
    }
}