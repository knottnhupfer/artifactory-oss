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
import TOOLTIP from "../../../../constants/artifact_tooltip.constant";
import MESSAGES from "../../../../constants/configuration_messages.constants"

export class AdminSecurityGeneralController {

    constructor(AdminSecurityGeneralDao, PasswordsEncryptionDao, ArtifactoryModelSaver, UserProfileDao, JFrogModal, UserDao, JFrogNotifications, User) {
        this.adminSecurityGeneralDao = AdminSecurityGeneralDao;
        this.passwordsEncryptionDao = PasswordsEncryptionDao.getInstance();
        this.options = [{label:'Supported', value: 'SUPPORTED'},
                        {label:'Unsupported', value: 'UNSUPPORTED'},
                        {label:'Required', value: 'REQUIRED'}];
        this.modal = JFrogModal;
        this.User = User;
        this.TOOLTIP = TOOLTIP.admin.security.general;
        this.MESSAGES = MESSAGES.admin.security.general;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['generalConfig']);
        this.userProfileDao = UserProfileDao;
        this.userDao = UserDao.getInstance();
        this.artifactoryNotifications = JFrogNotifications;


        this.getGeneralConfigObject();
        this.getMasterKeyStatus();
    }

    getEncryptionButtonText() {
        return this.materKeyState.hasMasterKey ? "Decrypt" : "Encrypt";
    }

    getEncryptionStatusText() {
        return this.materKeyState.hasMasterKey ?
                this.MESSAGES.passwordEncrypted :
                this.MESSAGES.passwordDecrypted;
    }

    getGeneralConfigObject() {
        this.adminSecurityGeneralDao.get().$promise.then((data) => {
            this.generalConfig = data;
            this.generalConfig.passwordSettings.encryptionPolicy = _.find(this.options,{value: this.generalConfig.passwordSettings.encryptionPolicy});
        this.ArtifactoryModelSaver.save();
        });
    }

    getMasterKeyStatus() {
        this.materKeyState = this.passwordsEncryptionDao.get();
    }
    forcePassExpForAll() {
        if (!this.generalConfig.passwordSettings.expirationPolicy.enabled) return;
        this.modal.confirm(`Are you sure you want to expire all user's passwords?`)
            .then(() => {
                this.userDao.expireAllPassword();
            });
    }

    unExpireAll() {
        if (!this.generalConfig.passwordSettings.expirationPolicy.enabled) return;
        this.modal.confirm(`Are you sure you want to unexpire all user's expired passwords?`)
            .then(() => {
                this.userDao.unExpireAllPassword();
            });
    }

    toggleEncryption() {
        if (this.materKeyState.hasMasterKey) {
            this.modal.confirm(`Artifactory will decrypt all encrypted data in your configuration files<br/>and user sensitive data, and it will be saved in clear text.<br/>It is recommended to backup your current artifactory.key<br/>(under $ARTIFACTORY_HOME/etc/security).<br/><br/>Are you sure you want to decrypt?`,null,{confirm: 'Decrypt'}).then(() => {
                this.materKeyState.$decrypt().then(() => {
                    this.getMasterKeyStatus();
                });
            });
        } else {
            this.modal.confirm(`Artifactory will create a private key to encrypt your configuration data<br/>and user sensitive data under /etc directory.<br/>Make sure to backup the key (under $ARTIFACTORY_HOME/etc/security)<br/>in a secure private location, since without it Artifactory will not be able to decrypt the encrypted data, such as configuration passwords, API keys, encrypted passwords, etc.<br/><br/>Are you sure you want to encrypt?`,null,{confirm: 'Encrypt'}).then(() => {
                this.materKeyState.$encrypt().then(() => {
                    this.getMasterKeyStatus();
                });
            });
        }
    }

    save() {
        let payload = _.cloneDeep(this.generalConfig);
        payload.passwordSettings.encryptionPolicy = payload.passwordSettings.encryptionPolicy.value;
        this.adminSecurityGeneralDao.update(payload).$promise.then(()=>{
            this.ArtifactoryModelSaver.save();
            this.User.reload();
        });
    }

    cancel() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this.getGeneralConfigObject();
        });
    }

    revokeApiKeys() {
        this.modal.confirm(`Are you sure you want to revoke all users API keys?`)
                .then(() => {
                    this.userProfileDao.revokeApiKey({deleteAll: 1});
                });
    }

    unlockAllUsers() {
        this.adminSecurityGeneralDao.unlockAllUsers();
    }
}