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
import EVENTS from "../../../../constants/artifacts_events.constants";

let $state, $stateParams, RepoDataDao, BackupDao;

export class AdminServicesBackupFormController {
    constructor($scope,_$state_, _$stateParams_, _RepoDataDao_, _BackupDao_, BrowseFilesDao,
            ArtifactoryModelSaver,JFrogEventBus) {
        $state = _$state_;
        this.$scope = $scope;
        $stateParams = _$stateParams_;
        RepoDataDao = _RepoDataDao_;
        BackupDao = _BackupDao_;
        this.JFrogEventBus = JFrogEventBus;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['backup']);

        this.isNew = !$stateParams.backupKey;
        this.TOOLTIP = TOOLTIP.admin.services.backupsForm;
        this.formTitle = `${this.isNew ? 'New' : 'Edit ' + $stateParams.backupKey} Backup`;
        this._initBackup();

        this.fileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export Backup',
            pathLabel: 'Path to Export Backup',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        }

        this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.REFRESH_PAGE_CONTENT,()=>{
            RepoDataDao.getForBackup().$promise.then((repoData) => {
                this.addRepos(repoData.repoList);
            });
        });
    }

    addRepos(repoList){
        repoList.forEach((repo)=> {
            if (this.backup.excludeRepos.indexOf(repo) == -1
                    && this.backup.includeRepos.indexOf(repo) == -1) {
                this.backup.includeRepos.push(repo);
            }
        });

        this.ArtifactoryModelSaver.save();
    }

    _initBackup() {
        if (this.isNew) {
            RepoDataDao.getForBackup().$promise.then((repoData) => {
                this.backup = {
                    enabled: true,
                    sendMailOnError: true,
                    retentionPeriodHours: 168,
                    includeRepos: repoData.repoList,
                    excludeRepos: [],
                    precalculate: false
                };

                this.ArtifactoryModelSaver.save();
            });
        }
        else {
            BackupDao.get({key: $stateParams.backupKey}).$promise
            .then((backup) => {
                this.backup = backup;
                this.ArtifactoryModelSaver.save();
            });
        }
    }

    changeIncremental() {
        if (this.backup.precalculate) {
            this.backup.precalculate = !this.backup.precalculate;
        }
    }

    updateFolderPath(directory) {
        this.backup.dir = directory;
    }

    save() {
        if (this.savePending) return;

        this.savePending = true;

        let whenSaved = this.isNew ? BackupDao.save(this.backup) : BackupDao.update(this.backup);
        whenSaved.$promise.then(() => {
            this.savePending = false;
            this.ArtifactoryModelSaver.save();
            this._end()
        }).catch(()=>this.savePending = false);
    }

    cancel() {
        this._end();
    }

    _end() {
        $state.go('^.backups');
    }

    onClickIncremental() {
        if (this.backup.incremental) {
            this.backup.retentionPeriodHours = 0;
            this.backup.createArchive=false;
        }
    }
    onClickZip() {
        if (this.backup.createArchive) {
            this.backup.incremental = false;
        }
    }
/*
    MOVED TO MAIN BACKUPS GRID
    runNow() {
        BackupDao.runNow({},this.backup).$promise.then((res)=>{
           //console.log(res);
        });
    }
*/
}
