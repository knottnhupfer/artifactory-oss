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

export class AdminImportExportSystemController {
    constructor(BrowseFilesDao, ExportDao, ImportDao, JFrogNotifications, JFrogModal, JFrogEventBus,
            ArtifactoryFeatures) {
        this.features = ArtifactoryFeatures;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.JFrogEventBus = JFrogEventBus;
        this.systemExportDao = ExportDao;
        this.systemImportDao = ImportDao;
        this.artifactoryNotifications = JFrogNotifications;
        this.modal = JFrogModal;
        this.TOOLTIP = TOOLTIP.admin.import_export.system;
        this.EVENTS = JFrogEventBus.getEventsDefinition();

        this.exportFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export',
            pathLabel: 'Path to export',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        };
        this.importFileBrowserOptions = {
            canSelectFiles: true,
            selectionLabel: 'Directory Or Zip File To Import',
            pathLabel: 'Path to import',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: false
        };


        this.exportOptions = {
            path: '',
            excludeContent: false,
            excludeMetadata: false,
            m2: false,
            createArchive: false,
            verbose: false
        };

        this.importOptions = {
            path: '',
            excludeContent: false,
            excludeMetadata: false,
            verbose: false
        };

        this._getRootPath();
    }

    _getRootPath() {
        this.browseFilesDao.query({path: '/'}).$promise.then((result) => {
            if (result) {
                this.defaultRootPath = result.roots[0] || '/';
                this.roots = result.roots;
            }
        });
    }

    clearValidations() {
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

    updateImportFolderPath(directory) {
        this.importOptions.path = directory;
    }

    updateExportFolderPath(directory) {
        this.exportOptions.path = directory;
    }

    import() {
        if (this.importForm.$valid) {
            this.confirmImport();
        }
    }

    doImport() {
        this.importOptions.zip = _.endsWith(this.importOptions.path, '.zip');
        this.importOptions.action = "system";
        this.systemImportDao.save(this.importOptions).$promise.then((response) => {
            if (response.data.errors) {
                this.artifactoryNotifications.create(
                        {error: 'The import has failed. Check Artifactory logs for details.'});
            }
        });
    }

    export() {
        if (this.exportForm.$valid) {
            this.exportOptions.action = "system";
            this.systemExportDao.save(this.exportOptions).$promise.then((res) => {
                if (res.status = 200) {
                    document.querySelector('#export-path').focus();
                    this.exportOptions.path = '';
                }
            });
        }
    }

    confirmImport() {
        this.modal.confirm(
                'Full system import deletes all existing Artifactory content. <br /> Are you sure you want to continue?')
                .then(() => this.doImport());
    }

}
