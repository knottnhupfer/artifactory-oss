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
import TOOLTIPS from '../../constants/artifact_tooltip.constant';

export class UserProfileController {

    constructor($state, $scope, UserProfileDao, ArtifactoryFeatures, BintrayDao, SshClientDao, JFrogNotifications,
            User, JFrogEventBus, ArtifactoryModelSaver, OAuthDao, JFrogGridFactory, JFrogModal, ArtifactoryState) {
        this.$scope = $scope;
        this.$state = $state;
        this.passwordRank = 0;
        this.userProfileDao = UserProfileDao;
        this.bintrayDao = BintrayDao.getInstance();
        this.sshClientDao = SshClientDao;
        this.artifactoryNotifications = JFrogNotifications;
        this.User = User;
        this.currentUser = User.getCurrent();
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryGridFactory = JFrogGridFactory;
        this.OAuthDao = OAuthDao;
        this.features = ArtifactoryFeatures;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['userInfo'], ['apiKey']);
        this.modal = JFrogModal;
        this.EVENTS = JFrogEventBus.getEventsDefinition();

        this.userInfo = {};
        this.currentPassword = null;
        this.showUserApiKey = false;
        this.showBintrayApiKey = false;
        this.profileLocked = true;

        this.tooltips = TOOLTIPS.userProfile;
        this.artifactoryState = ArtifactoryState;
        this.emailChanged=false;

        // Reloading is necessary since the User object is cached,
        // and once the session is over the data becomes deprecated
        this.User.loadUser(true).then(()=> {
            if(this.currentUser.name == 'anonymous'){
                $state.go('login');
            }
            $('body').addClass('load-complete');
        });

        if (this.User.currentUser.requireProfileUnlock === false && this.User.currentUser.existsInDB) {
            this.unlock();
        }
    }

    unlock() {
        this.userProfileDao.fetch({password: this.User.currentUser.requireProfileUnlock === true ? this.currentPassword : ''}).$promise
            .then(response => {
                this.userInfo = response.data;
        this.ArtifactoryModelSaver.save();
                //console.log(this.userInfo);
                this.profileLocked = false;

                if (!this.features.isNonCommercial()) {
                    this._initOAuthData();
                }

                this._getApiKey();

            });
    }

    _getApiKey() {
        if (this.User.currentUser.requireProfileUnlock && this.User.currentUser.requireProfilePassword) {
            this.userProfileDao.getApiKey.authenticate({username: this.currentUser.name, password: this.currentPassword});
        }

        this.userProfileDao.getApiKey().$promise.then((res)=>{
            this.userInfo.apiKey = res.apiKey;
        });
    }


    revokeApiKey() {
        this.modal.confirm(`Are you sure you want to revoke your API key?`)
                .then(() => {
                    this.userProfileDao.revokeApiKey.authenticate({username: this.currentUser.name, password: this.currentPassword})
                    this.userProfileDao.revokeApiKey().$promise.then((res)=> {
                        this._getApiKey();
                        this.artifactoryState.removeState('setMeUpUserData');
                    });
                });
    }
    regenerateApiKey() {
        //
        this.modal.confirm(`Are you sure you want to regenerate your API key?`)
            .then(() => {
                this.userProfileDao.regenerateApiKey.authenticate({username: this.currentUser.name, password: this.currentPassword});
                this.userProfileDao.regenerateApiKey({username: this.currentUser.name}).$promise.then((res)=>{
                    if (res.apiKey) {
                        this.artifactoryNotifications.create({info: 'Successfully regenerated API key'});
                        this.userInfo.apiKey = res.apiKey;
                        let oldSMUUserData = this.artifactoryState.getState('setMeUpUserData');
                        if (oldSMUUserData) {
                            oldSMUUserData.apiKey = this.userInfo.apiKey;
                        }
                    }
                    else {
                        this.artifactoryNotifications.create({error: 'Failed to regenerate API key'});
                    }
                });
            });

    }
    generateApiKey() {
        this.userProfileDao.getAndCreateApiKey.authenticate({username: this.currentUser.name, password: this.currentPassword});
        this.userProfileDao.getAndCreateApiKey({username: this.currentUser.name}).$promise.then((res)=>{
            if (res.apiKey) {
                this.artifactoryNotifications.create({info: 'Successfully generated API key'});
                this.userInfo.apiKey = res.apiKey;
            }
            else {
                this.artifactoryNotifications.create({error: 'Failed to generate API key'});
            }
        });
    }

    _initOAuthData() {

        this.oauth = {};

        this.User.getOAuthLoginData().then((response) => {
            this.oauth.providers = response;

            this.OAuthDao.getUserTokens().$promise.then((data)=>{
                data.forEach((providerName) => {
                    let provider = _.findWhere(this.oauth.providers, {name: providerName});
                    if (provider) {
                        provider.binded = true;
                    }
                });
            });
        });

    }

    unbindOAuthProvider(providerName) {
        this.OAuthDao.deleteUserToken({},{username: this.currentUser.name, provider: providerName}).$promise.then((res)=>{
            this._initOAuthData();
        });
    }
    onEmailChanged() {
        this.emailChanged = true;
    }
    save() {
        if (this.userInfo.user.newPassword && this.userInfo.user.newPassword !== this.userInfo.user.retypePassword) {
            this.artifactoryNotifications.create({error: 'Passwords do not match'});
            return;
        }

        let params = {
            user: {
                email: this.userInfo.user.email,
                password: this.userInfo.user.newPassword
            },
            bintray: this.userInfo.bintray,
            ssh: this.userInfo.ssh
        };

        if(this.emailChanged) {
            this.artifactoryState.removeState('setMeUpUserData');
        }

        this.userProfileDao.update(params).$promise.then(()=>{
            this.changePassword = false;
            this.clearPasswordFields();
            this.User.reload();
            this.ArtifactoryModelSaver.save();
        });
    }

    clearPasswordFields() {
        delete this.userInfo.user.newPassword;
        delete this.userInfo.user.retypePassword;
    }

    testBintray() {
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_SUBMITTED, this.bintrayForm.$name);
        this.bintrayDao.fetch(this.userInfo.bintray);
    }

    isOAuthEnabled() {
        return this.oauth && this.oauth.providers && this.oauth.providers.length > 0 && this.userInfo.user.realm === 'internal';
    }

    onGotoOAuth() {
        localStorage.stateBeforeOAuth = this.$state.current.name;
    }

}