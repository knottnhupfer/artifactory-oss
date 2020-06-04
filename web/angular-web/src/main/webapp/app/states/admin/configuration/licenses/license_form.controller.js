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

export class AdminConfigurationLicenseFormController {
    constructor($stateParams, LicensesDao, $state, ArtifactoryState, ArtifactoryModelSaver) {
    	this.state = $state;
    	this.isNew = !$stateParams.licenseName;
    	this.licensesDao = LicensesDao;
		this.artifactoryState = ArtifactoryState;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['license']);

        this.TOOLTIP = TOOLTIP.admin.configuration.licenseForm;

    	if (this.isNew) {
    		this.license = new LicensesDao();
    	}
    	else {
            this.license = LicensesDao.getSingleLicense({name: $stateParams.licenseName});
            this.license.$promise.then((data)=>{
                this.ArtifactoryModelSaver.save();
            });
    	}
    }

    save() {

        if (this.savePending) return;

        this.savePending = true;

		let whenSaved = this.isNew ? this.license.$create() : this.license.$update();
        whenSaved.then(() => {
            this.savePending = false;
            this.ArtifactoryModelSaver.save();
            this._end()
        }).catch(()=>this.savePending = false);

    }
	cancel() {
        this._end();
    }
    _end() {
        let prevState = this.artifactoryState.getState('prevState');
        if (prevState) {
            this.state.go(prevState.state,prevState.params);
        }
        else {
            this.state.go('^.licenses');
        }
    }

    testRegex(value) {

        let regex = new RegExp('^[A-Za-z0-9\._-]*$');
        return regex.test(value);
    }

}