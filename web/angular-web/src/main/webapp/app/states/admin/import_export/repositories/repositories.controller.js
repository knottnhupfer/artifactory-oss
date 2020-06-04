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

export class ImportExportRepositoriesController {
    constructor($scope, BrowseFilesDao, ExportDao, ImportDao, JFrogNotifications, FileUploader, RepoDataDao,
            JFrogEventBus, ArtifactoryFeatures) {
        this.$scope = $scope;
        this.features = ArtifactoryFeatures;
        this.repoDataDao = RepoDataDao;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.JFrogEventBus = JFrogEventBus;
        this.FileUploader = FileUploader;
        this.EVENTS = JFrogEventBus.getEventsDefinition();

        this.exportDao = ExportDao;
        this.importDao = ImportDao;
        this.TOOLTIP = TOOLTIP.admin.import_export.repositories;


        this.exportFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export',
            pathLabel: 'Path to export',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        };
        this.importFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Import',
            pathLabel: 'Path to import',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: false
        };

        this.uploadZip = {};
        this.uploadSuccess = false;
        this.exportOptions = {
            action: 'repository',
            repository: 'All Repositories',
            path: '',
            excludeMetadata: false,
            m2: false,
            verbose: false
        };
        this.importOptions = {
            action: 'repository',
            repository: 'All Repositories',
            path: '',
            excludeMetadata: false,
            verbose: false
        };
        this.zipOptions = {
            action: 'repository',
            repository: 'All Repositories',
            path: '',
            verbose: false
        };
        this.artifactoryNotifications = JFrogNotifications;
        this._initImportExportRepo();

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.REFRESH_PAGE_CONTENT, () => {
            this.getAllReposList();
        });
    }

    _getRootPath() {
        this.browseFilesDao.query({path: '/'}).$promise.then((result) => {
            if (result) {
                this.rootPath = result.roots[0] || '/';
                this.roots = result.roots;
            }
        });
    }

    getAllReposList() {
        this.repoDataDao.getForBackup().$promise.then((result) => {
            this.reposList = _.sortBy(result.repoList, (repo) => repo);
            this.reposList.unshift('All Repositories');
        });
    }

    _initImportExportRepo() {
        this.uploader = new this.FileUploader();
        this.uploader.url = `${API.API_URL}/artifactimport/upload`;
        this.uploader.headers = {'X-Requested-With': 'artUI'};
        this.uploader.onSuccessItem = this.onUploadSuccess.bind(this);
        this.uploader.onErrorItem = this.onUploadError.bind(this);
        this.uploader.onAfterAddingFile = this.onAddingfile.bind(this);

        this.uploader.removeAfterUpload = true;
        this.getAllReposList();

        this._getRootPath();
    }

    onUploadError(fileDetails, response) {
        this.artifactoryNotifications.create(response);
    }

    onUploadSuccess(fileDetails, response) {
        this.uploadSuccess = true;
        this.zipOptions.path = response.path;
    }

    onAddingfile(fileItem) {
        if (fileItem.file.size < 0) {
            fileItem.okToUploadFile = false;
            this.uploader.removeFromQueue(fileItem);
        } else {
            fileItem.okToUploadFile = true;
        }
    }

    updateExportFolderPath(directory) {
        this.exportOptions.path = directory;
    }

    updateImportFolderPath(directory) {
        this.importOptions.path = directory;
    }

    clearValidations() {
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

    export(form) {
        if (form.$valid) {
            this.exportDao.save(this.exportOptions);
        }
    }


    import(form) {
        if (form.$valid) {
            this.importDao.save(this.importOptions);
        }
    }

    importUploadZip() {
        let importDetails = {
            path: this.zipOptions.path,
            verbose: this.zipOptions.verbose,
            repository: this.zipOptions.repository,
            zip: true
        };
        this.importDao.save({action: 'repository'}, importDetails).$promise
                .finally(() => this.uploadSuccess = false);
    }

    upload() {
        if (this.uploader.queue[0]) {
            this.uploader.queue[0].upload();
        }
    }

}
