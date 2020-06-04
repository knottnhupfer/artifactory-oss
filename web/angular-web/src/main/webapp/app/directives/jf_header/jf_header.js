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
import EVENTS from '../../constants/artifacts_events.constants';
import API from '../../constants/api.constants';
import HELP from '../../constants/artifactory_help.constants';
import HELP_JCR from '../../constants/jcr_help.constants';


class jfHeaderController {
    constructor($scope, $q, User, $state, $stateParams, $timeout, $window, GeneralConfigDao, FooterDao, JFrogEventBus, ArtifactoryFeatures,
                $rootScope, $location, $http, ArtifactoryState, ArtifactoryHttpClient, OnBoardingWizard, GoogleAnalytics) {
        this.$scope = $scope;
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.User = User;
        this.generalConfigDao = GeneralConfigDao;
        this.footerDao = FooterDao;
        this.artifactoryState = ArtifactoryState;
        this.JFrogEventBus = JFrogEventBus;
        this.user = User;
        this.features = ArtifactoryFeatures;
        this.GoogleAnalytics = GoogleAnalytics;
        this.state = $state;
        this.$timeout = $timeout;
        this.$window = $window;
        this.$q = $q;
        this.logoEndPoint = `${API.API_URL}/auth/screen/logo`;
        this.defaultLogoUrl = 'images/artifactory_logo.svg';
        this.HELP = this.features.isJCR() ? HELP_JCR : HELP;
        this.OnBoardingWizard = OnBoardingWizard;

        this.ArtifactoryHttpClient = ArtifactoryHttpClient;

        //$.getJSON('artifactory_help.json', (jsonRes) => {
        //    this.HELP = jsonRes;
        //    this._refreshHelpMenu($location.path());
        //})
        //        .fail((errRes) => {
        //            if (errRes.status != 404) {
        //                let body = `Cannot parse the local help links file 'artifactory_help.json'.<br>The default file will be loaded instead.`;
        //                JFrogNotifications.createMessageWithHtml({type: 'error', body: body, timeout: 0});
        //            }
        //        });

        let unregister = $rootScope.$watch(() => {
                return $location.path();
            },
            (currentURL) => {
                this._refreshHelpMenu(currentURL);
            }
        );

        $scope.$on('$destroy',()=>{
            unregister();
        })

        this._registerEvents();
        $timeout(()=>{
            this._getFooterData();
        })
    }

    _registerEvents() {
        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.FOOTER_DATA_UPDATED, () => this._getFooterData(true)
    )
        ;
    }

    _getFooterData(force) {
        this.footerDao.get(force).then(footerData => {
            this.$window.document.title = footerData.serverName ? footerData.serverName : this.features.getGlobalName();

            this.helpLinksEnabled = footerData.helpLinksEnabled;

            this.samlRedirectEnabled = footerData.samlRedirectEnabled;

            if (footerData.userLogo) {
                this.logoUrl = '';
                this.$timeout(()=> {
                    this.logoUrl = this.logoEndPoint;
                });
            }
            else if (footerData.logoUrl)
                this.logoUrl = footerData.logoUrl;
            else if (this.features.isJCR()) {
                this.logoUrl = 'images/jcr_logo.svg';
            }
            else if (this.features.isConanCE()) {
                this.logoUrl = 'images/artifactory_conan_ce.svg';
            }
            else if (this.features.isJCR()) {
                // todo:JCR: get proper logo...
                this.logoUrl = this.defaultLogoUrl;
            }
            else if (this.features.isEdgeNode()) {
                this.logoUrl = 'images/artifactory_edge.svg';
            }
            else
                this.logoUrl = this.defaultLogoUrl;

            if ((this.user.currentUser.name !== 'anonymous' || this.user.currentUser.anonAccessEnabled) && (footerData.systemMessage || footerData.systemMessageTitle))
                this.artifactoryState.setState('systemMessage', {
                    enabled: footerData.systemMessageEnabled,
                    color: footerData.systemMessageTitleColor,
                    title: footerData.systemMessageTitle,
                    message: footerData.systemMessage,
                    inAllPages: footerData.showSystemMessageOnAllPages
                });
            else
                this.artifactoryState.setState('systemMessage',undefined);
        });
    }

    _refreshHelpMenu(currentURL) {
        this.helpLinks = [];

        for (let key in this.HELP)
            if (currentURL == key || (key.indexOf('**') != -1 && currentURL.indexOf(key.replace('**', '')) != -1))
                for (let i = 0; i < this.HELP[key].length; i++)
                    this.helpLinks.push(this.HELP[key][i]);
    }

    login() {

        if (this.samlRedirectEnabled) {
            this.user.getLoginData(this.$stateParams.redirectTo).then((res) => {
                if (res.ssoProviderLink) {
                    this.$window.open(res.ssoProviderLink, "_self");
                }
                else {
                    this.state.go('login');
                }
            });
        }
        else {
            this.state.go('login');
        }

    }
    /**
     * Logout is dispatching an event.
     * The handler also checks if the current state is one of the admin states.
     * This is done in order to make sure that logout happens only after all open admin states (windows) are closed.
     * Otherwise the user could be stuck with an unresponsive screen.
     * */
    logout() {
        this.JFrogEventBus.dispatch(EVENTS.USER_LOGOUT, this.state.current.name.startsWith('admin.') || this.state.current.name === 'user_profile');

/*
        this.user.logout()
                .then(() => {
                    this.state.go("home");
                });
*/
    }

    initQuickActions(actionsController) {

        this.quickActions = actionsController;
        let dictionary = {
            'quickRepo':     {title: 'Quick Setup'},
            'createLocal':   {title: 'Local Repository'},
            'createRemote':  {title: 'Remote Repository'},
            'createVirtual': {title: 'Virtual Repository'},
            'createDist':    {title: 'Distribution Repository'},
            'createUser':    {title: 'Add User'},
            'createGroup':   {title: 'Add Group'},
            'createPerm':    {title: 'Add Permission'},
            'userProfile':   {title: 'Edit Profile'},
            'logout':        {title: 'Log Out'},
        }

        let onEveryAction = (action) => {
            // make sure dropdown is closed (strangely, sometimes it does not)
            this.$timeout(()=>{
                if (this.quickActions.isDropdownOpen) {
                    this.quickActions.hideDropdown();
                }
                this.artifactoryState.setState('clearErrorsOnStateChange',true)
                this.GoogleAnalytics.trackEvent('Top Bar' , 'Quick Actions' , action);
            },100)
        };

        actionsController.setActionsDictionary(dictionary);
        actionsController.setActions([
            {
                name:'quickRepo',
                icon: 'icon-quicksetup',
                action: () => {
                    onEveryAction('Quick Wizard');
                    this.launchQuickSetup();
                }
            },
            {
                name:'createLocal',
                icon: 'icon-local-repo',
                action: () => {
                    onEveryAction('Local repository');
                    this.$state.go('admin.repositories.list.new',{repoType: 'local'});
                }
            },
            {
                name:'createRemote',
                icon: 'icon-remote-repo',
                action: () => {
                    onEveryAction('Remote repository');
                    this.$state.go('admin.repositories.list.new',{repoType: 'remote'});
                },
                visibleWhen: () => !this.features.isEdgeNode()
            },
            {
                name:'createVirtual',
                icon: 'icon-virtual-repo',
                action: () => {
                    onEveryAction('Virtual repository');
                    this.$state.go('admin.repositories.list.new',{repoType: 'virtual'});
                }
            },
            {
                name:'createDist',
                icon: 'icon-distribution-repo',
                action: () => {
                    onEveryAction('Distribution repository');
                    this.$state.go('admin.repositories.list',{repoType: 'distribution', action: 'openCreationDropdowns'});
                },
                visibleWhen: () => !this.features.isEdgeNode()
            },
            {
                name:'createUser',
                action: () => {
                    onEveryAction('Add User');
                    this.$state.go('admin.security.users.new');
                },
                itemClass: 'top-sep'
            },
            {
                name:'createGroup',
                action: () => {
                    onEveryAction('Add Group');
                    this.$state.go('admin.security.groups.new');
                }
            },
            {
                name:'createPerm',
                action: () => {
                    onEveryAction('Add Permission');
                    this.$state.go('admin.security.permissions.new');
                }
            },
            {
                name:'userProfile',
                icon: 'icon-artifactory-edit',
                action: () => {
                    onEveryAction('User Profile');
                    this.$state.go('user_profile');
                },
                itemClass: 'top-sep'
            },
            {
                name:'logout',
                icon: 'icon-logout',
                action: () => {
                    onEveryAction('Logout');
                    this.logout();
                }
            }
        ])

        $('body').on('click',(e)=>{
            if (!$(e.target).hasClass('user-header-section') && !$(e.target).hasClass('icon-more') && !$(e.target).hasClass('actions-more') && !$(e.target).hasClass('action-button')) {
                actionsController.hideDropdown();
            }
        })
    }

    isQuickActionsAvailable() {
        this.currentUser = this.User.getCurrent();
        let initStatus = this.artifactoryState.getState('initStatus');
        return this.currentUser.isAdmin() && initStatus && (initStatus.hasLicenseAlready || this.features.isNonCommercial());
    }

    launchQuickSetup() {
        //this.GoogleAnalytics.trackEvent('Homepage','Quick repository setup')
        this.OnBoardingWizard.show(true);
    }

    onClickUserHeaderSection(e) {
        if (!this.isQuickActionsAvailable()) return;

        if ($(e.target).hasClass('icon-more') || $(e.target).hasClass('actions-more') || $(e.target).hasClass('icon-small-arrow-down')) {
            e.preventDefault();
            e.stopPropagation();
            return;
        }


        if (!this.quickActions.isDropdownOpen) {
            this.quickActions.showDropdown();
        }
        else {
            this.quickActions.hideDropdown();
        }
    }

    onClickUserProfile(e) {
        if (!this.isQuickActionsAvailable()) {
            this.$state.go('user_profile');
        }
        else {
            this.$timeout(()=>$('.user-header-section').click());
            e.preventDefault();
            e.stopPropagation();
        }
    }

    isRedirecting() {
        return this.$state.current.name === 'login' && angular.isDefined(this.$stateParams.redirectTo)
    }

    isSSOMode() {
        return this.$state.current.name === 'login' && !this.features.isNonCommercial();
    }

}

export function jfHeader() {
    return {
        scope: {
            hideSearch: '@'
        },
        controller: jfHeaderController,
        controllerAs: 'jfHeader',
        bindToController: true,
        templateUrl: 'directives/jf_header/jf_header.html'
    }
}
