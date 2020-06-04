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
import TOOLTIPS from '../../../constants/artifact_tooltip.constant';

'use strict';
/**
 * launch a modal that prompts the user to select a target repo & path to do move / copy
 *
 * @param action:String - either 'copy' or 'move'
 * @returns promise - resolved with Object({targetRepoKey: String, targetPath: String}) if the user confirmed, rejected otherwise
 */
export function selectTargetPathFactory(ArtifactActionsDao, $q, $rootScope, JFrogModal, RepoDataDao) {
    return function selectTargetPath(action, node, useNodePath, customDryRun, defaultValues) {

        if (useNodePath === undefined) useNodePath = true;

        let deferred = $q.defer();
        let modalInstance;

        // init modal scope
        let modalScope = $rootScope.$new();
        modalScope.action = action;
        modalScope.node = node;
        modalScope.target = {
            repoList: [],
            repoKey: defaultValues ? defaultValues.targetRepoKey : '',
            path: useNodePath ? angular.copy(node.data.path) : (defaultValues ? defaultValues.targetPath : '/'),
            isCustomPath: false
        };
        modalScope.tooltips = TOOLTIPS.selectTargetPathModal;

        // get local repo list that match the original's repo pkg type
        let isMavinish = (pkgType) => {
            return _.contains(['maven','ivy','gradle','sbt'],pkgType.toLowerCase());
        };
        RepoDataDao.get({user: true}).$promise.then((result)=> {
            if (node.data.getRoot && node.data.getRoot().repoPkgType && action !== "restore") {
                result.repoTypesList = _.filter(result.repoTypesList,(repo)=>{
                    return repo.repoType === node.data.getRoot().repoPkgType || repo.repoType === 'Generic' || (isMavinish(repo.repoType) && isMavinish(node.data.getRoot().repoPkgType));
                });
            }
            else {

            }
            modalScope.target.repoList = result.repoTypesList.map(repo => {return {value: repo.repoKey, text: repo.repoKey}});
        });

        if (action === "restore") modalScope.noDryRun = true;

        // scope functions for modal
        modalScope.cancel = () => {
            modalInstance.close();
            deferred.reject();
        };

        modalScope.changeTargetRepository = () => {
            if (modalScope.target.repoKey === 'artifactory-build-info') {
                modalScope.target.isCustomPath = false;
                modalScope.target.path = '/';
            }
        };

        modalScope.confirm = () => {
            deferred.resolve({
                target: {
                    targetRepoKey: modalScope.target.repoKey,
                    targetPath: modalScope.getTargetPath()
                },
                onSuccess: ()=>{
                    modalInstance.close();
                    deferred = $q.defer();
                    deferred.resolve();
                    return deferred.promise;
                },
                onFail: (msg)=>{
                    modalScope.resultError = true;
                    modalScope.dryRunResults = msg;
                    deferred = $q.defer();
                    return deferred.promise;
                }
            });
        };
        modalScope.getTargetPath = () => {
            return modalScope.target.isCustomPath && modalScope.target.path || modalScope.target.path
        };
        modalScope.dryRun = customDryRun || (() => {
            var data = {
                repoKey: node.data.repoKey,
                path: node.data.path,
                targetRepoKey: modalScope.target.repoKey,
                targetPath: modalScope.getTargetPath(),
                dryRun: true
            };
            var params = {action: action};
            ArtifactActionsDao.dryRun(params, data).$promise
                    .then((response) => {
                        modalScope.resultError = false;
                        modalScope.dryRunResults = [response.info];
                    }).catch((response) => {
                        modalScope.resultError = true;
                        modalScope.dryRunResults = response.data.errors;
                    });
        });

        if (customDryRun) customDryRun.scope = modalScope;

        // Launch modal
        modalInstance = JFrogModal.launchModal('select_target_path', modalScope, 'sm');
        return deferred.promise;
    }
}