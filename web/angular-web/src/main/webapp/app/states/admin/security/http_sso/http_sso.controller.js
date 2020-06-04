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

export class AdminSecurityHttpSSoController {

    constructor(HttpSsoDao, JFrogEventBus, ArtifactoryModelSaver) {
        this.JFrogEventBus = JFrogEventBus;
        this.httpSsoDao = HttpSsoDao.getInstance();
        this.sso = this.getSsoData();
        this.TOOLTIP = TOOLTIP.admin.security.HTTPSSO;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['sso']);
        this.EVENTS = JFrogEventBus.getEventsDefinition();

    }

    getSsoData() {
        this.httpSsoDao.get().$promise.then((sso)=> {
            this.sso = sso;
        this.ArtifactoryModelSaver.save();
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
        });
    }

    reset() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this.getSsoData();
        });
    }
    save(sso) {
        this.httpSsoDao.update(sso).$promise.then(()=>{
            this.ArtifactoryModelSaver.save();
        });
    }
}