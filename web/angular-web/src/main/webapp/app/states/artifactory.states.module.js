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
import AdminState from './admin/admin.module';
import ArtifactState from './artifacts/artifacts.module';
import BuildsState from './builds/builds.module';
import BundlesState from './bundles/bundles.module';
import HomeModule from './home/home.module';
import OAuthErrorModule from './oauth_error/oauth_error.module';
import notFound404 from './not_found_404/not_found_404.module';
import Forbidden403 from './forbidden_403/forbidden_403.module';
import Login from './login/login.module';
import ChangePassword from './change_password/change_password.module';
import ForgotPassword from './forgot_password/forgot_password.module';
import UserProfile from './user_profile/user_profile.module';
import ServerError5XX from './server_error_5XX/server_error_5XX.module';
import ServerDown from './server_down/server_down.module';
import BaseState from './base/base.module';
import Search from './search/search.module';
import {MODULE_PACKAGE_NATIVE} from "./package_native/native.module";

angular.module('artifactory.states', [
    AdminState.name,
    ArtifactState.name,
    BuildsState.name,
    BundlesState.name,
    HomeModule.name,
    OAuthErrorModule.name,
    notFound404.name,
    Forbidden403.name,
    Login.name,
    ChangePassword.name,
    ForgotPassword.name,
    UserProfile.name,
    ServerDown.name,
    ServerError5XX.name,
    BaseState.name,
    Search.name,
    MODULE_PACKAGE_NATIVE,
    'artifactory.services',
    'artifactory.dao',
    'cfp.hotkeys',
    'ui.router'
    ])
    .run(changeStateHook);

function changeStateHook(User, $rootScope, $q, JFrogNotifications, $location, $timeout, $state, TreeBrowserDao,
                         ArtifactoryFeatures, FooterDao, ArtifactoryState, JFrogEventBus, OnBoardingWizard) {

    let EVENTS = JFrogEventBus.getEventsDefinition();

    JFrogEventBus.register(EVENTS.USER_LOGOUT, (confirmDiscard) => {
        if (confirmDiscard === true) {
            checkDiscardConfirmation($q).then(()=>{
                JFrogEventBus.dispatch(EVENTS.USER_LOGOUT);
            })
        }
    });

    $rootScope.$on('$locationChangeStart', (e,newUrl)=>{
        if (ArtifactoryState.getState('confirmDiscardModalOpen')) {
            e.preventDefault();
        }
    });

    $rootScope.$on('$locationChangeSuccess', () => {
        if (window.ga) {
            let gaTrackPage = ArtifactoryState.getState('gaTrackPage');
            if (gaTrackPage && typeof gaTrackPage === 'function') gaTrackPage();
        }
    })

    let pendingOnboarding = false;
    let firstStateChange = true;
    $rootScope.$on('$stateChangeStart', (e, toState, toParams, fromState, fromParams) => {
        if (firstStateChange) {
            firstStateChange = false;
            pendingOnboarding = true;
            let pending = {toState, toParams, search: $location.search()};
            OnBoardingWizard.isSystemOnboarding().then((onBoarding) => {
                pendingOnboarding = false;
                let redirectFromBintray = !!(pending.search.client_id && pending.search.code && pending.search.scope);
                let ssoRedirect = !!pending.search.redirectTo && pending.toState.name === 'login';
                let resetPassword = !!(pending.toState.name === 'reset-password' && pending.search.key);
                if (onBoarding && !redirectFromBintray && !resetPassword && !ssoRedirect) {
                    $state.go('home').then(()=>{
                        OnBoardingWizard.show();
                    })
                }
                else {
                    $state.go(pending.toState.name,pending.toParams).then(()=>{
                        $location.search(pending.search);
                    })
                }
            })
            e.preventDefault();
            return;
        }
        let onboardingWizardOpen = ArtifactoryState.getState('onboardingWizardOpen')
        if (toState.name !== 'home' && (pendingOnboarding || onboardingWizardOpen === true)) {
            e.preventDefault();
            return;
        }


        if (fromState.name.startsWith('admin.') || fromState.name === 'user_profile') {
            if (!ArtifactoryState.getState('confirmDiscardModalOpen')) {
                checkDiscardConfirmation($q, e).then(()=> {
                    $state.go(toState.name, toParams);
                });
            }
            else {
                e.preventDefault();
                return;
            }
        }
        let saveAdminState = ArtifactoryState.getState('saveAdminState');

        if (toState.name.startsWith('admin.') && !toState.name.match(/(?:.new|.edit)\b/) && saveAdminState && !e.defaultPrevented) {
            ArtifactoryState.setState('lastAdminState', toState);
            ArtifactoryState.setState('lastAdminStateParams', toParams);
            ArtifactoryState.removeState('saveAdminState');
        }
        else if (saveAdminState && !e.defaultPrevented) {
            ArtifactoryState.removeState('saveAdminState');
        }


        if (fromState.name && toState.name && fromState.name != toState.name) {
            JFrogEventBus.dispatch(EVENTS.CANCEL_SPINNER);
        }

        if (toState.name === 'artifacts.browsers.search') {
            //MOVED FROM artifacts.module.js to prevent error message (ui-router bug workaround)
            JFrogEventBus.dispatch(EVENTS.SEARCH_URL_CHANGED, toParams);
        }
        else if (fromState.name === 'artifacts.browsers.search') {
            JFrogEventBus.dispatch(EVENTS.CLEAR_SEARCH);
        }

        if (fromState.name.startsWith('artifacts.browsers.')) {
            TreeBrowserDao.invalidateRoots();
        }

        if (toState.name === 'change-password' && !toParams.username) {
            e.preventDefault();
            $state.go('login');
        }

        if (toState.name === 'oauth_error') {
            e.preventDefault();

            let message = $location.search().message;
            let gotoState = localStorage.stateBeforeOAuth;

            if (gotoState === 'login') {
                $state.go(gotoState,{oauthError: message, location: "replace"});
            }
            else if (gotoState === 'user_profile') {
                JFrogNotifications.create({error: message});
                $state.go(gotoState,{location: "replace"});
            }
            else {
                JFrogNotifications.create({error: message});
                $state.go('home',{location: "replace"});
            }
        }

        if (toState.name.match(/^builds/) && !User.getCurrent().getCanDeploy()) {
            toParams.tab = 'published';
        }

        if (toState.name === 'login' && $location.path() !== '/login' && $location.path() !== '/forgot-password' && $location.path() !== '/change-password' && $location.path() !== '/oauth_error' && !$location.path().startsWith('/resetpassword') && !$location.path().startsWith('/404') && !$location.path().startsWith('/403')) {
            let afterLogin = ArtifactoryState.getState('urlAfterLogin');
            if (!afterLogin) ArtifactoryState.setState('urlAfterLogin', $location.path());
        }


        // Permissions:

        if (!User.getCurrent().canView(toState.name, toParams)) {
            if (User.getCurrent().isProWithoutLicense()) {
                $timeout(() => $location.path('admin/configuration/register_pro'));
            }else {
                if ($location.path() !== '/login') ArtifactoryState.setState('urlAfterLogin', $location.path());
                e.preventDefault();
                if (User.getCurrent().name === 'anonymous') {
                    JFrogNotifications.create({error: 'You are not authorized to view this page'});
                    $timeout(() => $location.path('/login'));
                }
                else {
                    $timeout(() => $location.path('/403'));
                }
            }
        }
        // Features per license:
        else {
            let feature = toParams.feature;
            // Must verify footer data is available before checking (for initial page load)
            FooterDao.get().then(() => {
                if (ArtifactoryFeatures.isDisabled(feature) || ArtifactoryFeatures.isHidden(feature)) {
                JFrogNotifications.create({error: 'Page unavailable'});
                    e.preventDefault();
                    $timeout(() => $location.path('/home'));
                }
            });
        }

        if (!e.defaultPrevented) {
            JFrogEventBus.dispatch(EVENTS.CLOSE_MODAL);
            if (ArtifactoryState.getState('clearErrorsOnStateChange')) {
                ArtifactoryState.removeState('clearErrorsOnStateChange');
                JFrogEventBus.dispatch(EVENTS.CLOSE_NOTIFICATIONS);
            }
        }
    })
}


function checkDiscardConfirmation($q, e) {

    let defer = $q.defer();
    let forms = $('form');
    let changeDiscovered = false;
    for (let i = 0; i< forms.length; i++) {
        let form = forms[i];
        let controller = angular.element(form).controller();
        if (controller && controller._$modelSaver$_ && controller._$modelSaver$_.confirmOnLeave && !controller._$modelSaver$_.isModelSaved()) {
            changeDiscovered = true;

            controller._$modelSaver$_.ask().then(()=>{
                controller._$modelSaver$_.confirmOnLeave =   false;
                defer.resolve();
            });

            break;
        }
    }

    if (!changeDiscovered && !e) {
        defer.resolve();
    }
    else if (changeDiscovered && e) {
        e.preventDefault();
    }

    return defer.promise;

}
