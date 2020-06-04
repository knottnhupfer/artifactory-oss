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
import TOOLTIP from '../../../constants/artifact_tooltip.constant';

export class AdminRepositoryLayoutFormController {
    constructor($state,$stateParams, RepositoriesLayoutsDao, ArtifactoryModelSaver) {
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.layoutsDao = RepositoriesLayoutsDao;
        this.TOOLTIP = TOOLTIP.admin.repositories.layoutsForm;
        this.ArtifactoryModelSaver = ArtifactoryModelSaver.createInstance(this, ['layoutData']);

        this.input = {};
        this.testReply = null;
        this.regexViewData = null;

        this.testReplyDictionary = {
            organization: 'Organization',
            module: 'Module',
            baseRevision: 'Base Revision',
            folderIntegrationRevision: 'Folder Integration Revision',
            fileIntegrationRevision: 'File Integration Revision',
            classifier: 'Classifier',
            ext: 'Extension',
            type: 'Type'
        };

        this.viewOnly = ($stateParams.viewOnly === true);

        if ($stateParams.layoutname) {
            this.mode = 'edit';
            this.layoutName = $stateParams.layoutname;
            this.title = 'Edit ' + this.layoutName + ' Repository Layout';
            this._getLayoutData(this.layoutName);
        }
        else if ($stateParams.copyFrom) {
            this.mode = 'create';
            this.title = 'New Repository Layout';
            this._getLayoutData($stateParams.copyFrom);
        }
        else {
            this.mode = 'create';
            this.title = 'New Repository Layout';
            this.layoutData = {};
        }

    }

    save() {

        if (this.savePending) return;

        this.savePending = true;
        if (this.mode == 'edit')
        {
            let payload = angular.copy(this.layoutData);
            delete (payload.repositoryAssociations);

            this.layoutsDao.update({},payload).$promise.then((data)=>{
                this.savePending = false;
                this.ArtifactoryModelSaver.save();
                this.$state.go('^.repo_layouts');
            }).catch(()=>this.savePending = false);
        }

        if (this.mode == 'create')
        {
            this.layoutsDao.save({},this.layoutData).$promise.then((data)=>{
                this.savePending = false;
                this.ArtifactoryModelSaver.save();
                this.$state.go('^.repo_layouts');
            }).catch(()=>this.savePending = false);
        }
    }

    hasAnyAssoc() {
        return this.layoutData &&
               (this.layoutData.repositoryAssociations.localRepositories.length ||
               this.layoutData.repositoryAssociations.remoteRepositories.length ||
               this.layoutData.repositoryAssociations.virtualRepositories.length);
    }

    cancel() {
        this.$state.go('^.repo_layouts');
    }


    test() {
        let payload = angular.copy(this.layoutData);
        delete (payload.repositoryAssociations);
        _.extend(payload,{pathToTest: this.input.testPath});
        this.testReply = null;

        this.layoutsDao.testArtifactPath({},payload).$promise.then((data)=>{
            this.testReply = data.data;
        });
    }

    isSaveDisabled() {
        return this.savePending || !this.layoutForm || this.layoutForm.$invalid;
    }

    resolveRegex() {
        let payload = angular.copy(this.layoutData);
        delete (payload.repositoryAssociations);
        this.regexViewData = null;
        this.layoutsDao.resolveRegex({},payload).$promise.then((data)=>{
            this.regexViewData = data;
        });
    }

    gotoEditRepo(type,repo) {
        this.$state.go('admin.repositories.list.edit',{repoType: type, repoKey: repo});
    }
    _getLayoutData(layoutName) {
        this.layoutsDao.getLayoutData({},{layoutName:layoutName}).$promise.then((data)=>{
            this.layoutData = data;
        this.ArtifactoryModelSaver.save();
            if (this.$stateParams.copyFrom) {
                this.layoutData.name = '';
            }
        });
    }

}
