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

export class AdminAdvancedMaintenanceController {
    constructor(MaintenanceDao, JFrogNotifications, JFrogEventBus, JFrogModal, ArtifactoryModelSaver) {
        this.maintenanceDao = MaintenanceDao;
        this.artifactoryNotifications = JFrogNotifications;
        this.JFrogEventBus = JFrogEventBus;
        this.artifactoryModal = JFrogModal;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['maintenanceSettings']);
        this.maintenanceSettings = {};
        this.TOOLTIP = TOOLTIP.admin.advanced.maintenance;
        this.EVENTS = JFrogEventBus.getEventsDefinition();

        this._getData();
    }

    _getData() {
        this.maintenanceDao.get().$promise.then(data => {
            this.backupMaintance = angular.copy(data);
            this.maintenanceSettings.cleanUnusedCachedCron = data.cleanUnusedCachedCron;
            this.maintenanceSettings.cleanVirtualRepoCron = data.cleanVirtualRepoCron;
            this.maintenanceSettings.garbageCollectorCron = data.garbageCollectorCron;
            this.maintenanceSettings.quotaControl = data.quotaControl;
            this.maintenanceSettings.storageLimit = data.storageLimit;
            this.maintenanceSettings.storageWarning = data.storageWarning;
        this.ArtifactoryModelSaver.save();
        });
    }

    save() {
        if (this.maintenanceForm.$valid) {
            this.maintenanceDao.update(this.maintenanceSettings).$promise.then(()=>{
                this.ArtifactoryModelSaver.save();
            });
        }
    }

    clear() {
        this.ArtifactoryModelSaver.ask(true).then(() => {
            this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
            this._getData();
        });
    }

    resetQuotaFields() {
        if (!this.maintenanceSettings.quotaControl) {
            this.maintenanceSettings.storageLimit = this.backupMaintance.storageLimit;
            this.maintenanceSettings.storageWarning = this.backupMaintance.storageWarning;
        }
    }

    _runAction(name) {
        this.maintenanceDao.perform({module: name});
    }

    runGarbageCollection() {
        this._runAction('garbageCollection');
    }

    runUnusedCachedArtifactsCleanup() {
        this._runAction('cleanUnusedCache');
    }

    compressInternalDatabase() {
        this.artifactoryModal.confirm('Are you sure you want to compress the internal database?')
            .then(() => this._runAction('compress'));
    }

    pruneUnreferencedData() {
        this._runAction('prune');
    }

    cleanVirtualRepositories() {
        this._runAction('cleanVirtualRepo');
    }

}