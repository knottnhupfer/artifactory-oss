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

export class SigningKeysController {

    constructor($timeout, FileUploader, JFrogEventBus, SigningKeysDao, KeystoreDao, JFrogNotifications) {
        this.$timeout = $timeout;
        this.signingKeysDao = SigningKeysDao;
        this.keystoreDao = KeystoreDao;
        this.keyStore = {};
        this.FileUploader = FileUploader;
        this.artifactoryNotifications = JFrogNotifications;
        this.JFrogEventBus = JFrogEventBus;
        this.EVENTS = JFrogEventBus.getEventsDefinition();

        this.TOOLTIP = TOOLTIP.admin.security.signingKeys;
        this.publicKeyValue = 'No public key installed';
        this.privateKeyValue = 'No private key installed';
        this.initSigningKeys();
    }

    initSigningKeys() {

        this.getSigningKeysData();
        this.keyPairNames = [];
        this.keystoreFileUploaded = false;
        this.getKeyStoreData();
        //-----public key-----//
        this.uploaderPublicKey = new this.FileUploader();
        this.uploaderPublicKey.onSuccessItem = this.onUploadPublicKeySuccess.bind(this);
        this.uploaderPublicKey.onErrorItem = this.onUploadPublicKeyFail.bind(this);
        this.uploaderPublicKey.url = `${API.API_URL}/signingkeys/install?public=true`;
        this.uploaderPublicKey.headers = {'X-Requested-With': 'artUI'};
        this.uploaderPublicKey.removeAfterUpload = true;
        //-----private key-----//
        this.uploaderPrivateKey = new this.FileUploader();
        this.uploaderPrivateKey.url = `${API.API_URL}/signingkeys/install?public=false`;
        this.uploaderPrivateKey.headers = {'X-Requested-With': 'artUI'};
        this.uploaderPrivateKey.onSuccessItem = this.onUploadPrivateKeySuccess.bind(this);
        this.uploaderPrivateKey.onErrorItem = this.onUploadPrivateKeyFail.bind(this);
        this.uploaderPrivateKey.removeAfterUpload = true;
        //------key pair-----//
        this.uploaderKeyStore = new this.FileUploader();
        this.uploaderKeyStore.onSuccessItem = this.onUploadKeyStoreSuccess.bind(this);
        this.uploaderKeyStore.onErrorItem = this.onUploadKeyStoreFail.bind(this);
        this.uploaderKeyStore.onAfterAddingAll = this.onUploadKeyFileAdded.bind(this);
        this.uploaderKeyStore.url = `${API.API_URL}/keystore/upload?pass=`;
        this.uploaderKeyStore.headers = {'X-Requested-With': 'artUI'};
        this.uploaderKeyStore.removeAfterUpload = true;
    }

    getSigningKeysData() {
        this.signingKeysDao.get().$promise.then((result) => {
            this.publicKeyInstalled = result.publicKeyInstalled;
            this.privateKeyInstalled = result.privateKeyInstalled;
            this.publicKeyValue = result.publicKeyInstalled ? 'Public key is installed' : 'No public key installed';
            this.privateKeyValue = result.privateKeyInstalled ? 'Private key is installed' : 'No private key installed';
            this.publicKeyLink = result.publicKeyLink;
            this.passPhrase = result.passPhrase;
        });
    }

    getKeyStoreData() {
        this.keystoreDao.get().$promise.then((keyStore) => {
            this.keyStoreExist = keyStore.keyStoreExist;
            _.map(keyStore.keyStorePairNames, (keypairName) => {
                this.keyPairNames.push(keypairName);
            })
        });
    }

    onUploadPublicKeySuccess(fileDetails, response) {
        this.getSigningKeysData();
        this.artifactoryNotifications.create(response.feedbackMsg);
    }

    onUploadPrivateKeySuccess(fileDetails, response) {
        this.getSigningKeysData();
        this.artifactoryNotifications.create(response.feedbackMsg);
    }

    onUploadPublicKeyFail(fileDetails, response) {
        this.artifactoryNotifications.create(response);
    }
    onUploadPrivateKeyFail(fileDetails, response) {
        this.artifactoryNotifications.create(response);
    }

    upload(type) {
        if (type === 'public') {
            this.uploaderPublicKey.queue[0].upload();
        }
        if (type === 'private') {
            this.uploaderPrivateKey.queue[0].upload();
        }
        if (type === 'keyStore') {
            this.uploaderKeyStore.queue[0].url = `${API.API_URL}/keystore/upload?pass=${this.keyPair.keyStorePassword}`;
            this.uploaderKeyStore.queue[0].upload();
        }
    }

    removeKey(isPublic) {
        this.signingKeysDao.delete({public: isPublic}).$promise.then((result) => this.getSigningKeysData());
    }

    verifyPhrase(shouldNotify = true) {
        //this.signingKeysDao.setNotification('post', shouldNotify);
        let method = shouldNotify ? 'post' : 'postWithoutNotifications';
        if (this.signingKeysDao[method])
            return this.signingKeysDao[method]({action: 'verify', passPhrase: this.passPhrase}).$promise;
    }

    updatePhrase() {
        let verifyPromise = this.verifyPhrase(false);
        if (verifyPromise) {
            verifyPromise
                .then(() => {
                    this.signingKeysDao.put({action: 'update', passPhrase: this.passPhrase});
                })
                .catch((response) => this.artifactoryNotifications.create(response.data));
            ;
        }
    }

    checkMatchingPasswords() {
        this.$timeout(() => {
            if (this.signingKeysForm.password.$valid && this.signingKeysForm.repeatPassword.$valid) {
            this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION);
            }
        });
    }

    updatePassword() {
        this.keystoreDao.updatePassword({action: 'updatePass'}, {password: this.user.password}).$promise
            .then(() => {
                this.keyStoreExist = true;
            });
    }

    removeKeyStore() {
        this.keystoreDao.removeKeystore({action: 'password'}).$promise
            .then(() => {
                //_.forEach(this.keyPairNames, this.removeKeypair, this);
                this.keyStoreExist = false;
                this.keyPairNames = [];
                this.keyPairName = '';
                this.user.password = '';
                this.repeatPassword = '';
            })
    }

    onUploadKeyFileAdded() {
        this.keyStorePassRequired = true;
    }
    onUploadKeyStoreSuccess(fileDetails, keyStore) {
        this.keyStorePassRequired = false;
        this.keystoreFileUploaded = true;
        this.keyStore = keyStore;
        this.aliases = keyStore.availableAliases;
        this.alias = this.aliases[0];
        this.keyPair.keyStorePassword = '';
        this.artifactoryNotifications.create(keyStore.feedbackMsg);
    }

    onUploadKeyStoreFail(fileDetails, response) {
        this.keyStorePassRequired = false;
        this.artifactoryNotifications.create(response);
    }

    saveKeypair() {
        let payload = _.cloneDeep(this.keyStore);
        delete payload.feedbackMsg;
        delete payload.availableAliases;
        payload.alias = this.alias;

        this.keystoreDao.save({action: 'add'}, payload).$promise
            .then((response) => {
                this.keyPairNames.push(this.keyStore.keyPairName);
                this.keyStore.keyPairName = '';
                this.keyStore.privateKeyPassword = '';
                this.alias = '';
                this.aliases = [];
            });
        //.finally(() => this.keystoreFileUploaded = false);
    }

    removeKeypair() {
        this.keystoreDao.removeKeypair({name: this.keyPairName}).$promise.then((response) => {
            _.pull(this.keyPairNames, this.keyPairName);
            this.keyPairName = '';
        }).catch((response) => {
            if (response.error) {
                let keyPairNames = _.words(response.error);
                _.pull(this.keyPairNames, keyPairNames.pop());
            }
        });
    }

    cancelKeypairUpload() {
        //this.keystoreFileUploaded = false;
        this.keyStore.keyPairName = '';
        this.keyStore.privateKeyPassword = '';
    }

    canUpdatePassword() {
        return this.signingKeysForm.password.$valid && this.signingKeysForm.repeatPassword.$valid;
    }

    canUploadKeystore() {
        return this.keyStoreExist &&
            this.signingKeysForm.keyStorePassword.$valid &&
            this.uploaderKeyStore.queue.length;
    }

    canUploadDebianKey(uploader) {
        return this[uploader].queue.length;
    }

    canRemoveKeyPairs() {
        return this.keyStoreExist && this.keyPairNames.length && this.keyPairName;
    }

    canUpdatePhrase() {
        return this.publicKeyInstalled && this.privateKeyInstalled && this.passPhrase;
    }
}
