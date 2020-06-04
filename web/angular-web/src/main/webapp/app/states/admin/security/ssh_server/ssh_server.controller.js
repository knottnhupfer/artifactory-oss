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
import API from '../../../../constants/api.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminSecuritySshServerController {

    constructor($timeout, $scope, $state, FileUploader, SshServerDao, JFrogEventBus, ArtifactoryModelSaver, JFrogNotifications, JFrogModal) {
        this.$timeout = $timeout;
        this.$scope = $scope;
        this.$state = $state;
        this.FileUploader = FileUploader;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryNotifications = JFrogNotifications;
        this.SshServerDao = SshServerDao.getInstance();
        this.TOOLTIP = TOOLTIP.admin.security.SSHSERVER;
        this.modal = JFrogModal;

        // Set flags for case added server keys (prior to upload)
        this.serverKeysStatuses = {
            addedPublicKeyFile: false,
            addedPrivateKeyFile: false
        };

        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['sshServer','serverKeysStatuses']);
        this.publicKeyValue = 'No public key installed';
        this.privateKeyValue = 'No private key installed';
        this.NO_VALUE_STRING = '** NO VALUE **';
        this.EVENTS = JFrogEventBus.getEventsDefinition();
        this.getSshData();
        this.initKeys();
    }

    initKeys() {
        this.uploaderPublicKey = new this.FileUploader();
        this.uploaderPublicKey.onSuccessItem = this.onUploadPublicKeySuccess.bind(this);
        this.uploaderPublicKey.url = `${API.API_URL}/sshserver/install?public=true`;
        this.uploaderPublicKey.headers = {'X-Requested-With': 'artUI'};
        this.uploaderPublicKey.removeAfterUpload = true;

        // Raise flag when adding a public key to input
        this.uploaderPublicKey.onAfterAddingAll = (addedItems)=>{
            this.serverKeysStatuses.addedPublicKeyFile = true;
        };

        this.uploaderPrivateKey = new this.FileUploader();
        this.uploaderPrivateKey.url = `${API.API_URL}/sshserver/install?public=false`;
        this.uploaderPrivateKey.headers = {'X-Requested-With': 'artUI'};
        this.uploaderPrivateKey.onSuccessItem = this.onUploadPrivateKeySuccess.bind(this);
        this.uploaderPrivateKey.removeAfterUpload = true;

        // Raise flag when adding a private key to input
        this.uploaderPrivateKey.onAfterAddingAll = (addedItems)=>{
            this.serverKeysStatuses.addedPrivateKeyFile = true;
        };

    }

    getSshData(updateKeysStateOnly = false) {
        this.SshServerDao.get().$promise.then((sshServer)=> {
            if (!updateKeysStateOnly) this.sshServer = sshServer;
            this.publicKeyInstalled = sshServer.serverKey && sshServer.serverKey.publicKeyInstalled;
            this.privateKeyInstalled = sshServer.serverKey && sshServer.serverKey.privateKeyInstalled;
            this.publicKeyValue = this.publicKeyInstalled ? 'Public key is installed' : 'No public key installed';
            this.privateKeyValue = this.privateKeyInstalled ? 'Private key is installed' : 'No private key installed';
            this.publicKeyLink = sshServer.serverKey ? sshServer.serverKey.publicKeyLink : undefined;
            this.passPhrase = sshServer.serverKey ? sshServer.serverKey.passPhrase : undefined;
            this.sshServer.customUrlBase = this.sshServer.customUrlBase || this.NO_VALUE_STRING;
            if (!updateKeysStateOnly) this.ArtifactoryModelSaver.save();
            this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
        });
    }

    clearServerKeysInputs(){
        this.uploaderPrivateKey.clearQueue();
        this.serverKeysStatuses.addedPrivateKeyFile = false;
        this.uploaderPublicKey.clearQueue();
        this.serverKeysStatuses.addedPublicKeyFile = false;
    }

    reset(){
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this.getSshData();
            this.clearServerKeysInputs();
        });
    }
    save() {
        if (this.sshServer.enableSshServer && this.sshServer.customUrlBase === this.NO_VALUE_STRING) {
            this._showUrlBaseAlert().then((gotoGenConf)=>{
                if (gotoGenConf) {
                    this.SshServerDao.update(this.sshServer).$promise.then(()=> {
                        this.ArtifactoryModelSaver.save();
                        this.$state.go('admin.configuration.general', {focusOnBaseUrl: true});
                    });
                }
                else {
                    this.SshServerDao.update(this.sshServer).$promise.then(()=>{
                        this.ArtifactoryModelSaver.save();
                    });
                }
            });
        }
        else {
            this.SshServerDao.update(this.sshServer).$promise.then(()=>{
                this.ArtifactoryModelSaver.save();
            });
        }
    }

    _showUrlBaseAlert() {
        let modalScope = this.$scope.$new();
        modalScope.context='ssh';
        return this.modal.launchModal('base_url_alert_modal', modalScope, 'md').result;
    }

    onUploadPublicKeySuccess(fileDetails, response) {
        this.getSshData(true);
        this.artifactoryNotifications.create(response.feedbackMsg);
        this.serverKeysStatuses.addedPublicKeyFile = false;
    }

    onUploadPrivateKeySuccess(fileDetails, response) {
        this.getSshData(true);
        this.artifactoryNotifications.create(response.feedbackMsg);
        this.serverKeysStatuses.addedPrivateKeyFile = false;
    }

    upload(type) {
        if (type === 'public') {
            this.uploaderPublicKey.queue[0].upload();
        }
        if (type === 'private') {
            this.uploaderPrivateKey.queue[0].upload();
        }
    }

    removeKey(isPublic) {
        this.SshServerDao.delete({public: isPublic}).$promise.then((result) => this.getSshData(true));
    }

    verifyPhrase(shouldNotify = true) {
        let method = shouldNotify ? 'post' : 'postWithoutNotifications';
        if (this.SshServerDao[method])
            return this.SshServerDao[method]({action: 'verify', passPhrase: this.passPhrase}).$promise;
    }

    updatePhrase() {
        let verifyPromise = this.verifyPhrase(false);
        if (verifyPromise) {
            verifyPromise
                    .then(() => {
                        this.SshServerDao.put({action: 'update', passPhrase: this.passPhrase});
                    })
                    .catch((response) => this.artifactoryNotifications.create(response.data));
            ;
        }
    }

    canUploadSshKey(uploader) {
        return this[uploader].queue.length;
    }

    canUpdatePhrase() {
        return this.publicKeyInstalled && this.privateKeyInstalled && this.passPhrase;
    }
}