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
import EVENTS from '../../../../constants/artifacts_events.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';
import API from '../../../../constants/api.constants';

export class AdminSecuritySamlIntegrationController {

    constructor(SamlDao, ArtifactoryModelSaver, JFrogEventBus, JFrogModal, $window, RESOURCE) {
        this.samlDao = SamlDao.getInstance();
        this.TOOLTIP = TOOLTIP.admin.security.SAMLSSOSettings;
        this.artifactoryModelSaver = ArtifactoryModelSaver.createInstance(this,['saml']);
        this.artifactoryEventBus = JFrogEventBus;
        this.modal = JFrogModal;
        this.$window = $window;
        this.RESOURCE = RESOURCE;
        this.downloadEncryptedAssertionCertLink = API.API_URL + this.RESOURCE.SAML_DOWNLOAD_ENCRYPTED_ASSERTION_CERT;
        this._init();
    }

    _init() {
        this.samlDao.get().$promise.then((data) => {
            this.saml = data;
            if (!angular.isDefined(this.saml.noAutoUserCreation)) {
                this.saml.noAutoUserCreation = true;
            }
            this.artifactoryModelSaver.save();
        });
    }

    save() {
        this.samlDao.update(this.saml).$promise.then(()=>{
            this.artifactoryModelSaver.save();
            this.artifactoryEventBus.dispatch(EVENTS.FOOTER_DATA_UPDATED);
        });
    }

    cancel() {
        this.artifactoryModelSaver.ask(true).then(()=>{
            this._init();
        });
    }
    canSave() {
        return this.samlForm.$valid;
    }

    regenerateEncryptedAssertionCert() {
        this.modal.confirm("A new X.509 public certificate will be generated. You will need to download the new " +
                "certificate and upload it to your IDP. Click Confirm to continue.")
                .then(()=> this.samlDao.regenerateCertificate().$promise)
                .then((data) => {
                    console.log(data);
                });
    }
}