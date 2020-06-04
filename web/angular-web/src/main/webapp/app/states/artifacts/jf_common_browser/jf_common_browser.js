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
import KEYS from "../../../constants/keys.constants";
import ACTIONS from "../../../constants/artifacts_actions.constants";
import {REPO_FORM_CONSTANTS} from '../../admin/repositories/repository_form.constants';

const REGEXP = /(pkg|repo)\:\ *([^;]+)(?:;|$)\ */g;

export default class JFCommonBrowser {
    constructor(JFTreeApi, $scope, ArtifactActions, ArtifactoryState, JFrogUIUtils) {

        this.ArtifactoryState = ArtifactoryState;
        this.artifactActions = ArtifactActions;
        this.activeFilter = false;

        this.utils = JFrogUIUtils;
        this.treeApi = new JFTreeApi($scope);
        this.treeApi.showLines();
        this.treeApi.on('item.dblClicked', node => {
            this.treeApi.toggleExpansion(node);
        })

        if (this.browserController) {
            this.activeFilter = this.browserController.activeFilter || false;
            this.searchText = this.browserController.searchText || '';
            if (this.searchText.endsWith('*')) this.searchText = this.searchText.substr(0,this.searchText.length-1);
        }

        if (!localStorage.favoritesRepos) localStorage.setItem('favoritesRepos', '{"favoritesRepos" : []}');

//        this._initTreeSorting();

        this.packageTypesReplacingArchives = ['bower','npm','cocoapods','gitlfs','opkg','composer','pypi','vagrant', 'helm', 'chef', 'go', 'cran', 'conda'];
    }

    /****************************
     * Context menu items
     ****************************/

    _getContextMenuItems(obj, cb) {
        let actionItems = {};
        if (obj.data) {
            let node = obj.data;
            node.load()
                    .then(() => node.refreshWatchActions())
                    .then(() => node.getDownloadPath())
                    .then(() => {
                        if (node.actions) {
                            node.actions.forEach((actionObj) => {
                                let name = actionObj.name;
                                let action = angular.copy(ACTIONS[name]);

                                if (name == 'Favorites' && localStorage.favoritesRepos) {
                                    let favoritesRepos = JSON.parse(localStorage.favoritesRepos);
                                    action.icon = _.contains(favoritesRepos.favoritesRepos, node.text) ? 'icon-star-full' : 'icon-star';
                                    action.title = _.contains(favoritesRepos.favoritesRepos, node.repoKey) ? 'Remove from Favorites' : 'Add to Favorites';
                                }

                                if (!action) {
                                    console.log("Unrecognized action", name);
                                    return true;
                                }
                                action._class = 'menu-item-' + action.icon;
                                action.icon = 'action-icon icon ' + action.icon;
                                action.name = action.title;

                                if (actionObj.name === 'Download') {
                                    action.link = node.actualDownloadPath;
                                }
                                else {
                                    action.callback = () => {
                                        this.artifactActions.perform(actionObj, obj, true);
                                    }
                                }
                                if (actionObj.name === 'Distribute' && node.repoType === 'distribution') {
                                    action = angular.copy(ACTIONS['Redistribute']);
                                    ;
                                }
                                actionItems[name] = action;
                            });

                            cb(actionItems);
                        }
                        else {
                            cb([]);
                        }
                    });
        }
        else {
            cb([]);
        }
    }

    _focusOnTree() {
        this.treeApi.focus();
    }

    _getScoreObjectFromOrderArray(order) {
        let repoScore = {};
        let score = 100000;

        order.forEach((repoType) => {
            repoScore[repoType.toLowerCase()] = score;
            if (repoType === 'REMOTE') {
                repoScore['cached'] = score;
                score = score / 10;
            }
            score = score / 10;
        });
        return repoScore;
    }

    _initTreeSorting() {

        let repoScore, repoOrder;
        this.treeApi.setSortingFunction((a,b) => {
            if (!repoOrder) {
                repoOrder = this.ArtifactoryState.getState('repoOrder');
                repoScore = this._getScoreObjectFromOrderArray(repoOrder || ['VIRTUAL','DISTRIBUTION', 'LOCAL', 'REMOTE']);
            }

            let aNode = a;
            let bNode = b;

            let aText = aNode.data ? aNode.data.text.toLowerCase() : '*';
            let bText = bNode.data ? bNode.data.text.toLowerCase() : '*';

            let aType = aNode.data ? aNode.data.type : '*';
            let bType = bNode.data ? bNode.data.type : '*';
            let aRepoType = aNode.data ? aNode.data.repoType : '*';
            let bRepoType = bNode.data ? bNode.data.repoType : '*';
            let aPkgType = aNode.data ? aNode.data.repoPkgType : '*';
            let bPkgType = bNode.data ? bNode.data.repoPkgType : '*';

            let aScore=0,bScore=0;

            if (aNode.data && aNode.data.isTrashcan && aNode.data.isTrashcan() && aNode.text !== '..') return 1;
            else if (bNode.data && bNode.data.isTrashcan && bNode.data.isTrashcan() && bNode.text !== '..') return -1;
            else if ((aType === 'repository' || aType === 'virtualRemoteRepository') &&
                    (bType === 'repository' || bType === 'virtualRemoteRepository')) {
                //both repos - top level sort

                if (localStorage.treeSortingMethod  === 'pkg_type') {
                    aPkgType = (aPkgType === REPO_FORM_CONSTANTS.DISTRIBUTION_REPO_TYPES.RELEASE_BUNDLES) ? REPO_FORM_CONSTANTS.DISTRIBUTION_REPO_TYPES.BINTRAY : aPkgType;
                    bPkgType = (bPkgType === REPO_FORM_CONSTANTS.DISTRIBUTION_REPO_TYPES.RELEASE_BUNDLES) ? REPO_FORM_CONSTANTS.DISTRIBUTION_REPO_TYPES.BINTRAY : bPkgType;

                    if (aPkgType < bPkgType) aScore+=1000000;
                    if (bPkgType < aPkgType) bScore+=1000000;
                }

                if (localStorage.treeSortingMethod  === 'repo_type' || localStorage.treeSortingMethod  === 'pkg_type' || !localStorage.treeSortingMethod) {
                    if (aRepoType === 'distribution' || aRepoType === 'releaseBundles') aScore += repoScore.distribution;
                    if (bRepoType === 'distribution' || bRepoType === 'releaseBundles') bScore += repoScore.distribution;

                    if (aRepoType==='local') aScore+=repoScore.local;
                    if (bRepoType==='local') bScore+=repoScore.local;

                    if (aRepoType==='cached') aScore+=repoScore.cached;
                    if (bRepoType==='cached') bScore+=repoScore.cached;

                    if (aRepoType==='remote') aScore+=repoScore.remote;
                    if (bRepoType==='remote') bScore+=repoScore.remote;

                    if (aRepoType==='virtual') aScore+=repoScore.virtual;
                    if (bRepoType==='virtual') bScore+=repoScore.virtual;
                }

                if (aText<bText) aScore++;
                if (aText>bText) bScore++;

                return aScore<bScore?1:-1;
            }
            else if ((aType !== 'repository' && aType !== 'virtualRemoteRepository') &&
                    (bType !== 'repository' && bType !== 'virtualRemoteRepository')) {
                //both files or folders

                if (aType==='folder') aScore+=10000;
                if (bType==='folder') bScore+=10000;

                if (aNode.text === '..') aScore+=100000;
                if (bNode.text === '..') aScore+=100000;

                let aHasNumVal = !_.isNaN(parseInt(aText));
                let bHasNumVal = !_.isNaN(parseInt(bText));

                if (aHasNumVal && bHasNumVal) {

                    let versionCompareResult = this.utils.compareVersions(aText,bText);

                    if (versionCompareResult === -1) aScore += 100;
                    if (versionCompareResult === 1) bScore += 100;
                }
                else {

                    let aDigitIndex = aText.search(/\d/);
                    let bDigitIndex = bText.search(/\d/);

                    if (aDigitIndex === bDigitIndex && aDigitIndex !== -1) {
                        let aBeforeNum = aText.substr(0,aDigitIndex);
                        let bBeforeNum = bText.substr(0,bDigitIndex);
                        if (aBeforeNum === bBeforeNum) {
                            let aFromNum = aText.substr(aDigitIndex);
                            let bFromNum = bText.substr(bDigitIndex);

                            let versionCompareResult = this.utils.compareVersions(aFromNum,bFromNum);

                            if (versionCompareResult === -1) aScore += 100;
                            if (versionCompareResult === 1) bScore += 100;

                        }
                    }

                    if (aText<bText) aScore++;
                    if (aText>bText) bScore++;
                }
                return aScore<bScore?1:-1;
            }
            else {
                if (!aNode.data) return -1; //special node
                else if (!bNode.data) return 1; //special node
                else if ((aType === 'repository' || aType === 'virtualRemoteRepository')) return -1;
                else if ((bType === 'repository' || bType === 'virtualRemoteRepository')) return 1;
                else return aText>bText?1:-1;
            }
        });
    }

    _getFileExtension(path){
        return (path.indexOf('.') === -1 ? '' : path.split('.').pop());
    }

    _iconsShouldBeReplacedWithRepoPkgTypeIcon(nodeType,repoPkgType,fullPath){
        let repotype = repoPkgType && repoPkgType.toLocaleLowerCase();
        return ((nodeType === 'archive' &&
                ($.inArray(repotype,this.packageTypesReplacingArchives) !== -1))
                ||
                (repoPkgType === 'Gradle' &&
                        this._getFileExtension(fullPath) === 'gradle'));
    }

    filterCallback(node) {
        let treeNode = node.data;
        let rootRepo = node && node.data && node.data.getRoot ? node.data.getRoot() : null;

        let isFilterMatch = (str, filterText) => {
            if (filterText.indexOf(',') === -1) {
                return str.toLowerCase().trim().indexOf(filterText.toLowerCase().trim()) != -1;
            }
            else {
                let filterArray = filterText.split(',');
                return filterArray.reduce((acc,val)=>{return acc || (val && str.toLowerCase().trim().indexOf(val.trim().toLowerCase()) != -1)},false)
            }
        }

        let checkFavorites = (treeNode) => {
            if (!localStorage.filterFavorites || !localStorage.favoritesRepos) return true;
            let favoritesRepos = JSON.parse(localStorage.favoritesRepos);
            if (((treeNode.isTrashcan() || treeNode.isInTrashcan()) && this.type === 'tree') || _.includes(favoritesRepos.favoritesRepos, rootRepo.repoKey)) return true;
            else return false;
        }

        let combinedFilter = this.getCombinedFilter(this.searchText);
        let filterRegexp = new RegExp(REGEXP);
        let matches = filterRegexp.exec(combinedFilter);
        if (matches) {
            let ret = true;
            while (matches) {
                if (matches[2].trim()) {
                    let filterType = matches[1].trim();
                    let filterText = matches[2].trim();
                    switch(filterType) {
                        case 'pkg':
                            ret = ret && (
                                    (treeNode.isRepo()
                                            && treeNode.repoPkgType
                                            && isFilterMatch(treeNode.repoPkgType, filterText)
                                    )
                                    ||
                                    (!treeNode.isRepo()
                                            && rootRepo.isRepo()
                                            && isFilterMatch(rootRepo.repoPkgType, filterText))
                                    ||
                                    (
                                            (
                                                    (treeNode.isTrashcan
                                                            && treeNode.isTrashcan())
                                                    ||
                                                    (treeNode.isInTrashcan
                                                            && treeNode.isInTrashcan())
                                            )
                                            && localStorage.pinnedTrash
                                            && this.type === 'tree'
                                    )
                            );
                            break;
                        case 'repo':
                            ret = ret && (
                                    (treeNode.isRepo()
                                            && isFilterMatch(treeNode.repoType === _.camelCase(REPO_FORM_CONSTANTS.DISTRIBUTION_REPO_TYPES.RELEASE_BUNDLES) ? 'distribution' : treeNode.repoType, filterText))
                                    ||
                                    (!treeNode.isRepo()
                                            && rootRepo.isRepo()
                                            && isFilterMatch(treeNode.repoType === _.camelCase(REPO_FORM_CONSTANTS.DISTRIBUTION_REPO_TYPES.RELEASE_BUNDLES) ? 'distribution' : treeNode.repoType, filterText))
                                    ||
                                    (
                                            (
                                                    (treeNode.isTrashcan
                                                            && treeNode.isTrashcan())
                                                    ||
                                                    (treeNode.isInTrashcan
                                                            && treeNode.isInTrashcan())
                                            )
                                            &&
                                            localStorage.pinnedTrash
                                            && this.type === 'tree'
                                    )
                            );
                            break;
                    }
                }
                matches = filterRegexp.exec(combinedFilter);
            }
            return ret && checkFavorites(treeNode);
        }
        else return checkFavorites(treeNode);
    }

    /****************************
     * Searching the tree
     ****************************/
    _searchTree(text) {
        this.searchText = text || '';
        this.treeApi.refreshFilter();
        let showOnlyMatches = text ? text.match(new RegExp(REGEXP)) || false : false;
        this._jsQuickFind(this.searchText);
    }

    _jsQuickFind(searchText) {
        this.treeApi.quickFind(searchText);
    }

    _isInActiveFilterMode(checkIfMatchesFound = false) {
        if (this.searchText.match(new RegExp(REGEXP))) {
            let justSearchTerm = this.searchText.substr(this.searchText.indexOf(':')+1).trim();
            if (justSearchTerm) {
                if (checkIfMatchesFound) {
                    let matchesFound = !!this.treeApi.getViewPane().getFilteredNodesCount();
                    return matchesFound ? true : 'no results';
                }
                else {
                    return true;
                }
            }
            else return 'empty';
        }
        else return false;
    }

    _searchTreeKeyDown(key) {
        if (key == KEYS.ENTER) {
            //manually set the model to the input element's value (because the model is debounced...)
            this.searchText = $('.jf-tree-search').val();

            let isInActiveFilterMode = this._isInActiveFilterMode(true);

            if (isInActiveFilterMode === true) {
                this.activeFilter = true;
                if (this.browserController) {
                    this.browserController.activeFilter = true;
                    this.browserController.searchText = this.searchText + '*';
                }
                this._searchTree(this.searchText);
                this._focusOnTree();

                let selected = this.treeApi.getSelectedNode();
                if (!selected || !this.treeApi.isNodeFiltered(selected)) {
                    this.treeApi.selectFirst();
                }
            }
            else if (isInActiveFilterMode === 'no results') {
                if (this.artifactoryNotifications) this.artifactoryNotifications.create({warn: "No repositories matches the filter expression"});
                this._clear_search();
            }
            else {
                this.activeFilter = false;
                if (this.browserController) this.browserController.activeFilter = false;
                //                this._selectCurrentSearchResult();
                this.treeApi.selectPreSelected();
                this.treeApi.openSelected();
                this._clear_search();
                this._focusOnTree();
                this.currentResult = null;
            }
        }
        else if (key == KEYS.ESC) {
            this.activeFilter = false;
            if (this.browserController) this.browserController.activeFilter = false;
            this._clear_search();
            this._focusOnTree();
            this.currentResult = null;
        }
    }

    _clear_search() {
        this.activeFilter = false;
        if (this.browserController) this.browserController.activeFilter = false;

        this.searchNodes = null;
        this.searchParentNodes = null;
        this.nomatchNodes = null;
        this.nomatchParentNodes = null;
        this.searchText = '';
        this.treeApi.quickFind('');
        this.treeApi.refreshFilter();
    }

    getPersistentFilter() {
        let persistentFilter = localStorage.getItem("treeFiltering");
        if (persistentFilter) {
            persistentFilter = JSON.parse(persistentFilter);
            let filterArray = [];
            if (persistentFilter.pkg.length) filterArray.push(`pkg:${persistentFilter.pkg.join(',')}`);
            if (persistentFilter.repo.length) filterArray.push(`repo:${persistentFilter.repo.join(',')}`);
            let filterString = filterArray.join(';');

            return filterString || (localStorage.filterFavorites && localStorage.favoritesRepos ? '*' : '');
        }
        return localStorage.filterFavorites && localStorage.favoritesRepos ? '*' : '';
    }

    getCombinedFilter(tempFilter) {
        let persistentFilter = this.getPersistentFilter();
        if (!tempFilter) {
            return persistentFilter;
        }
        else {

            let persistentObj = this._filterStringToFilterObject(persistentFilter);
            let tempObj = this._filterStringToFilterObject(tempFilter);

            let pkgArray = persistentObj.pkg ? _.filter(persistentObj.pkg,val=>{
                return _.find(tempObj.pkg,tempVal=>val.toLowerCase().indexOf(tempVal) !== - 1);
            }) : tempObj.pkg || [];
            let repoArray = persistentObj.repo ? _.filter(persistentObj.repo,val=>{
                return _.find(tempObj.repo,tempVal=>val.toLowerCase().indexOf(tempVal) !== - 1);
            }) : tempObj.repo || [];

            if (!pkgArray.length) pkgArray = persistentObj.pkg;
            if (!repoArray.length) repoArray = persistentObj.repo;

            let combinedArray = [];
            if (pkgArray && pkgArray.length) combinedArray.push(`pkg:${pkgArray.join(',')}`);
            if (repoArray && repoArray.length) combinedArray.push(`repo:${repoArray.join(',')}`);
            let combinedString = combinedArray.join(';');

            return combinedString;
        }
    }

    _filterStringToFilterObject(filterString) {

        if (!filterString) return {};

        let filterRegexp = new RegExp(REGEXP);

        let part1 = filterRegexp.exec((filterString.match(filterRegexp) || [])[0]) || [];
        let part2 = filterRegexp.exec((filterString.match(filterRegexp) || [])[1]) || [];

        let obj = {
            [part1[1]]: (part1[2] || '').split(','),
            [part2[1]]: (part2[2] || '').split(',')
        }

        delete obj.undefined;
        return obj;

    }

}
