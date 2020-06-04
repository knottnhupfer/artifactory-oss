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
import TOOLTIPS from '../../../constants/artifact_tooltip.constant';
import ICONS from '../constants/artifact_browser_icons.constant';
import ACTIONS from '../../../constants/artifacts_actions.constants';

export class ArtifactsController {
    constructor($rootScope,$scope, $state, JFrogEventBus, ArtifactoryState, SetMeUpModal, ArtifactoryDeployModal, User,
                ArtifactActions,JFrogModal, GoogleAnalytics, ArtifactoryFeatures) {

        this.JFrogEventBus = JFrogEventBus;
        this.$state = $state;
        this.$scope = $scope;
        this.$rootScope = $rootScope;
        this.node = null;
        this.ArtifactoryFeatures = ArtifactoryFeatures;
        this.deployModal = ArtifactoryDeployModal;
        this.setMeUpModal = SetMeUpModal;
        this.artifactoryState = ArtifactoryState;
        this.GoogleAnalytics = GoogleAnalytics;
        this.tooltips = TOOLTIPS;
        this.icons = ICONS;
        this.artifactActions = ArtifactActions;

        this.user = User.getCurrent();

        this.initEvents();
        this.modal = JFrogModal;
        this.initNoPermissionsModalScope();
    }

    initNoPermissionsModalScope(){
        this.noPermissionsModalScope = this.$rootScope.$new();
        this.noPermissionsModalScope.close = ()=>{
            // Close and go home...
            this.modalInstance.close();
            this.$state.go('home');
        };
        this.noPermissionsModalScope.modalTitle = "No Access Privileges";
        this.noPermissionsModalScope.modalText = "You do not have permissions defined for any repository.<br/>"+
                                                 "To gain access, make sure you are logged in, or contact your Artifactory administrator.";
    }

    launchNoPermissionsModal(){
        this.modalInstance = this.modal.launchModal('no_permissions_modal', this.noPermissionsModalScope);
        this.modalInstance.result.finally(()=>{
            this.modalInstance.close();
            this.$state.go('home');
        })
    }

    getNodeIcon() {
        if (this.node && this.node.data) {
            let type = this.icons[this.node.data.iconType];
            if (!type) type = this.icons['default'];
            return type && type.icon;
        }
    }

    isFavorite() {
        if (this.node && this.node.data) {
            return this.node.data.isFavorite();
        };
    }

    initNoPermissionsModalScope(){
        this.noPermissionsModalScope = this.$rootScope.$new();
        this.noPermissionsModalScope.modalTitle = "No Access Privileges";
        this.noPermissionsModalScope.modalText = "You do not have permissions defined for any repository.<br/>"+
                                                 "To gain access, make sure you are logged in, or contact your Artifactory administrator.";
    }

    openSetMeUp() {
        this.GoogleAnalytics.trackEvent('Artifacts' , 'Set me up - Open' , this.node.data.repoPkgType, null , this.node.data.repoType);
        this.setMeUpModal.launch(this.node);
    }

    openDeploy() {
        if (this.node && this.node.data) this.GoogleAnalytics.trackEvent('Artifacts' , 'Open deploy' , this.node.data.repoPkgType , null , this.node.data.repoType);
        this.deployModal.launch(this.node);
    }

    initEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TREE_NODE_SELECT, node => {
            this.selectNode(node)
        });

        this.JFrogEventBus.registerOnScope(this.$scope, [EVENTS.ACTION_WATCH, EVENTS.ACTION_UNWATCH], () => {
            this.actionsController.setActions(this.node.data.actions);
            this.JFrogEventBus.dispatch(EVENTS.TREE_NODE_CM_REFRESH, this.node);
        });

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.ACTION_FAVORITES, () => {
            if (!localStorage.favoritesRepos) return;
            let favoritesRepos = JSON.parse(localStorage.favoritesRepos);
            let isMarked = _.contains(favoritesRepos.favoritesRepos, this.node.data.repoKey);

            let item = _.find(this.node.data.actions, {name: 'Favorites'});
            if (!item) return;
            ACTIONS['Favorites'].icon = item.icon = isMarked ? 'icon-star-full' : 'icon-star';
            ACTIONS['Favorites'].title = item.title = isMarked ? 'Remove from Favorites' : 'Add to Favorites';
        });

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.TREE_DATA_IS_SET, (treeHasData)=> {
            this.treeDataIsSet = true;
            if(!treeHasData) {
                this.launchNoPermissionsModal();
            }
        });
    }

    selectNode(node) {
        let previousNode = this.node;
        this.node = node;

        if (node && node.data) {
            this.artifactoryState.setState('repoKey', this.node.data.repoKey);

            if (this.$state.params.artifact !== node.data.fullpath) {
                let location = true;
                if (this.$state.current.name === 'artifacts.browsers.path' && (!previousNode || !this.$state.params.artifact)) {
                    // If no artifact and selecting artifact - replace the location (fix back button bug)
                    location = 'replace';
                }
                this.$state.go(this.$state.current, {artifact: node.data.fullpath}, {location: location, notify: false, reload: this.$state.current});
            } 

            this.actionsController.setCurrentEntity(node);
            this.node.data.getDownloadPath()
                .then(() => {
                    let downloadAction = _.findWhere(node.data.actions,{name: 'Download'});
                    if (downloadAction) {
                        downloadAction.href = node.data.actualDownloadPath;
                        if (node.data.xrayShouldValidate) downloadAction.xrayShouldValidate = node.data.xrayShouldValidate;
                    }
                    this.actionsController.setActions(node.data.actions)
                });

            this.JFrogEventBus.dispatch(EVENTS.ACTION_FAVORITES);
        }
        else {
            this.artifactoryState.removeState('repoKey');
            this.$state.go(this.$state.current, {artifact: ''});
            this.actionsController.setActions([]);
        }
    }

    exitStashState() {
        this.JFrogEventBus.dispatch(EVENTS.ACTION_EXIT_STASH);
    }

    hasData() {
        return this.artifactoryState.getState('hasArtifactsData') !== false;
    }

    initActions(actionsController) {
        this.actionsController = actionsController;
        actionsController.setActionsHandler(this.artifactActions);
        actionsController.setActionsDictionary(ACTIONS);
    }


    deployIsDisabled () {
        if (!this.user.getCanDeploy()) {
            this.disabledTooltip = this.tooltips.artifacts.deploy.noDeployPermission;
            return true;
        }
        return false;
    }

    deployIsAllowedOnEdge() {
        return this.node.data.getRoot().repoPkgType === 'Generic' &&
                this.node.data.repoType === 'local' &&
                this.node.data.repoKey === 'artifactory-edge-uploads';
    }

    toggleFavorites(node) {
        let repoKey = node.data.text;

        if (!localStorage.favoritesRepos) {
            localStorage.setItem('favoritesRepos', JSON.stringify({favoritesRepos: [repoKey]}));
        }
        else {
            let favoritesRepos = JSON.parse(localStorage.favoritesRepos);

            let isRepoInFavorites = _.contains(favoritesRepos.favoritesRepos, repoKey);

            if (isRepoInFavorites) {
                favoritesRepos.favoritesRepos = _.remove(favoritesRepos.favoritesRepos, (i) => i !== repoKey); // Remove from favorites
            } else {
                favoritesRepos.favoritesRepos.push(repoKey); // Add to favorites
            }
            localStorage.setItem('favoritesRepos', JSON.stringify({favoritesRepos: favoritesRepos.favoritesRepos}));
        }
        this.JFrogEventBus.dispatch(EVENTS.ACTION_FAVORITES);

        if (localStorage.filterFavorites) this.JFrogEventBus.dispatch(EVENTS.TREE_REFRESH_FILTER);

        delete node.$cachedCMItems;

    }
}