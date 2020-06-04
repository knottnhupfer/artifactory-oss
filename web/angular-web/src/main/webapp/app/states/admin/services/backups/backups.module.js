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
import {AdminServicesBackupsController} from './backups.controller';
import {AdminServicesBackupFormController} from './backup_form.controller';

function backupsConfig($stateProvider) {

    $stateProvider
            .state('admin.services.backups', {
                params: {feature: 'backups'},
                url: '/backups',
                templateUrl: 'states/admin/services/backups/backups.html',
                controller: 'AdminServicesBackupsController as AdminServicesBackups'
            })
            .state('admin.services.backups.new', {
                params: {feature: 'backups'},
                parent: 'admin.services',
                url: '/backups/new',
                templateUrl: 'states/admin/services/backups/backup_form.html',
                controller: 'AdminServicesBackupFormController as BackupForm'
            })
            .state('admin.services.backups.edit', {
                params: {feature: 'backups'},
                parent: 'admin.services',
                url: '/backups/:backupKey/edit',
                templateUrl: 'states/admin/services/backups/backup_form.html',
                controller: 'AdminServicesBackupFormController as BackupForm'
            })
}

export default angular.module('backups', [])
        .config(backupsConfig)
        .controller('AdminServicesBackupsController', AdminServicesBackupsController)
        .controller('AdminServicesBackupFormController', AdminServicesBackupFormController)