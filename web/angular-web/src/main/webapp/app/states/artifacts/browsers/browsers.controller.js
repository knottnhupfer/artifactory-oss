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
import EVENTS from '../../../constants/artifacts_events.constants';
import FIELD_OPTIONS from '../../../constants/field_options.constats';

export class BrowsersController {
    constructor($timeout, $scope, $stateParams, $state, TreeBrowserDao, JFrogEventBus, hotkeys, ArtifactoryState,
                ArtifactoryStorage, ArtifactoryFeatures, FooterDao) {
        this.JFrogEventBus = JFrogEventBus;
        this.stateParams = $stateParams;
        this.state = $state;
        this.treeBrowserDao = TreeBrowserDao;
        this.artifactoryState = ArtifactoryState;
        this.ArtifactoryStorage = ArtifactoryStorage;
        this.features = ArtifactoryFeatures;
        this.compactFolders = TreeBrowserDao.getCompactFolders();
        this.$scope = $scope;
        this.EVENTS = EVENTS;
        this.$timeout = $timeout;
        this.hotkeys = hotkeys;
        this.activeSortingMenu = false;
        this._setupKeyHints();
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TREE_NODE_SELECT, node => this.selectedNode = node);

        let activeFilter = this.artifactoryState.getState('activeFilter');
        this.activeFilter = activeFilter ? true : false;
        this.searchText = activeFilter || '';

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_RUNNING, (running) => {
            this.searchInAction = running;
        });

        this.sortMethod = localStorage.treeSortingMethod || 'repo_type'; // sort from local storage or default if not exist

        let savedFilter = localStorage.treeFiltering ? JSON.parse(localStorage.treeFiltering) : {};
        let savedPkgFilter = savedFilter.pkg || [];
        let savedRepoFilter = savedFilter.repo || [];

        this.selectionRepoPkgList = _.map(FIELD_OPTIONS.repoPackageTypes, (repo) => {
            return {
                text: repo.text,
                serverEnumName: repo.serverEnumName,
                iconClass: 'icon-' + repo.icon,
                isSelected: savedPkgFilter.indexOf(repo.serverEnumName) !== -1
            }
        });

        // Filter only Aritfactory Community Edition for C/C++ supported packages
        if (this.features.isConanCE()) {
            this.selectionRepoPkgList = _.filter(this.selectionRepoPkgList, (pkg) => {
                return pkg.serverEnumName === 'Conan' || pkg.serverEnumName === 'Generic';
            });
        }
        // Filter only JCR supported packages
        else if (this.features.isJCR()) {
            this.selectionRepoPkgList = _.filter(this.selectionRepoPkgList, (pkg) => {
                return pkg.serverEnumName === 'Docker' || pkg.serverEnumName === 'Helm' || pkg.serverEnumName === 'Generic';
            });
        }

        // Filter only Aritfactory OSS supported packages
        else if (this.features.isOss()) {
            this.selectionRepoPkgList = _.filter(this.selectionRepoPkgList, (pkg) => {
                return _.includes(['Maven', 'Gradle', 'Ivy', 'SBT', 'Generic', 'BuildInfo'], pkg.serverEnumName)
            });
        }

        //this.selectionTypeList = _.values(FIELD_OPTIONS.REPO_TYPE).toObject();

        this.selectionRepoTypeList = _.map(_.extend({},FIELD_OPTIONS.REPO_TYPE,{CACHE: 'cache'}), (value) => {
            let repoType = value.charAt(0).toUpperCase() + value.slice(1);
            return {
                text: repoType,
                iconClass: 'icon-' + value,
                isSelected: savedRepoFilter.indexOf(repoType) !== -1
            }
        });

        if (this.features.isConanCE()) {
            this.selectionRepoTypeList = _.filter(this.selectionRepoTypeList, (type) => {
                return type.text != 'Remote';
            })
        }

        if (this.features.isEdgeNode()) {
	        this.selectionRepoTypeList = _.filter(this.selectionRepoTypeList, (type)=> {
	            return type.text != 'Distribution' && type.text != 'Remote';
            })
        }

        this.sendWindowResizeEvent();


        let footerData = FooterDao.getInfo();
        if (footerData.treebrowserFolderCompact) this.allowCompactFolders = true;
    }

    get dropDownOpened() {
        return !!(this.repoDropDownOpened || this.packageDropDownOpened);
    }

    toggleCompactFolders() {
        this.treeBrowserDao.setCompactFolders(this.compactFolders);
        this.JFrogEventBus.dispatch(EVENTS.TREE_COMPACT, this.compactFolders);
    }
    toggleSortAndFilterMenu() {
        if (!this.dropDownOpened) {
            this.activeSortingMenu = !this.activeSortingMenu;
        }
    }

    changeSortingMethod() {
        localStorage.setItem("treeSortingMethod", this.sortMethod);
        this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH_SORTING);
    }

    changeFilter() {
        let filter = {
            pkg: _.map(_.filter(this.selectionRepoPkgList, {isSelected: true}),'serverEnumName'),
            repo: _.map(_.filter(this.selectionRepoTypeList, {isSelected: true}),'text')
        };

        if (!filter.pkg.length && !filter.repo.length) {
            localStorage.removeItem('treeFiltering');
        }
        else {
            localStorage.setItem("treeFiltering", JSON.stringify(filter));
        }

        this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH_FILTER);
    }

    filterFavorites() {
        if (localStorage.filterFavorites) {
            localStorage.removeItem('filterFavorites');
        }
        else {
            localStorage.setItem('filterFavorites', true);
        }
        this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH_FILTER);
    }

    isFiltersOn() {
        return localStorage.treeFiltering ? true : false;
    }

    isFavoritesOn() {
        return localStorage.filterFavorites ? true : false;
    }

    isFavoritesEnabled() {
        let favRepos = this.ArtifactoryStorage.getItem('favoritesRepos');
        return !!((favRepos && favRepos.favoritesRepos && favRepos.favoritesRepos.length) || localStorage.filterFavorites);
    }

    resetFilters() {
        _.filter(this.selectionRepoTypeList,{isSelected: true}).forEach(i=>i.isSelected = false);
        _.filter(this.selectionRepoPkgList,{isSelected: true}).forEach(i=>i.isSelected = false);
        localStorage.removeItem("treeFiltering");
        this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH_SORTING);
    }

    resetAllFilters() {
        localStorage.removeItem("filterFavorites");
        this.resetFilters();
        this.filterHasNoMatches = false;
    }

    showTreeSearch() {
        this.JFrogEventBus.dispatch(EVENTS.ACTIVATE_TREE_SEARCH);
    }

    switchBrowser(browser) {
        this.artifactoryState.setState('activeFilter', this.activeFilter ? this.searchText : undefined);

        // Reclicking simple browser when we are already in simple browser - go to root
        if (browser === 'simple' && this.state.params.browser === 'simple') {
/*
            let repo = this.selectedNode.data.getRoot();
            // Make sure roots are visible:
            this.artifactoryState.setState('tree_touched', false);
            // Use forceLoad as a Date to ensure state transition even if it's the same as before
            this.state.go(this.state.current.name, {browser: browser, artifact: repo.fullpath, forceLoad: new Date()});
*/
        }
        else if (browser === 'tree' && this.state.params.browser === 'tree') {
//            this.JFrogEventBus.dispatch(EVENTS.TREE_COLLAPSE_ALL);
        }
        else if (browser != this.state.params.browser) {
//            this.state.transitionTo(this.state.current.name, _.extend({}, this.state.params, {browser: browser}), {reload: true, notify: false});
            this.state.go(this.state.current, _.extend({}, this.state.params, {browser: browser}), {notify: false, reload: this.state.current});
//            this.treeBrowserDao.invalidateRoots();
            // console.log(" from "+this.stateParams.browser+" to "+browser);
        }
    }

    _setupKeyHints() {
        this.hotkeys.bindTo(this.$scope).add({
            combo: 'Enter',
            description: 'Select node'
        }).add({
            combo: 'Esc',
            description: 'Cancel search / deselect node'
        }).add({
            combo: 'Down',
            description: 'Navigate down in tree / in search results'
        }).add({
            combo: 'Up',
            description: 'Navigate up in tree / in search results'
        }).add({
            combo: 'Right',
            description: 'Expand folder'
        }).add({
            combo: 'Left',
            description: 'Collapse folder'
        });
    }

    clearFilter() {
        this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
    }

    editFilter() {
        this.JFrogEventBus.dispatch(EVENTS.TREE_SEARCH_EDIT);
    }

    sendWindowResizeEvent() {
        let resizeEvent = document.createEvent("Event");
        resizeEvent.initEvent("resize", false, true);
        let doSafeResize = () => {
            try {
                window.dispatchEvent(new Event('resize'));
            }
            catch (e) {
                window.dispatchEvent(resizeEvent);
            }
        }

        this.$timeout(()=>{
            this.$timeout(()=>{
                doSafeResize();
            })
        })
    }
}
