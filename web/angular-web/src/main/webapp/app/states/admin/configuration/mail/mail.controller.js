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
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminConfigurationMailController {

    constructor(MailDao, JFrogEventBus, $timeout, ArtifactoryModelSaver) {
        this.mailDao = MailDao.getInstance();
        this.JFrogEventBus = JFrogEventBus;
        this.getMailData();
        this.mailSettingsForm = null;
        this.testReceiptForm = null;
        this.TOOLTIP = TOOLTIP.admin.configuration.mail;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['mail']);
        this.$timeout = $timeout;
        this.EVENTS = JFrogEventBus.getEventsDefinition();
    }

    getMailData() {
        this.mailDao.get().$promise.then((mail)=> {
            this.mail = mail;
        this.ArtifactoryModelSaver.save();
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
        });
    }

    save(form) {
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_SUBMITTED, form.$name);
        if (this.mailSettingsForm.$valid) {
            this.mailDao.update(this.mail).$promise.then(()=>{
                this.ArtifactoryModelSaver.save();
            });
        }
    }

    reset() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this.getMailData();
        });

    }
    testReceipt(form) {
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_SUBMITTED, form.$name);
        if (this.testReceiptForm.$valid) {
            this.mailDao.save(this.mail);
        }
    }
}