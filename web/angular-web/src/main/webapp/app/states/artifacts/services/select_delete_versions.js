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
'use strict';
/**
 * launch a modal that prompts the user to select a target repo & path to do move / copy
 *
 * @param action:String - either 'copy' or 'move'
 * @returns promise - resolved with Object({targetRepoKey: String, targetPath: String}) if the user confirmed, rejected otherwise
 */
export function selectDeleteVersionsFactory($q, ArtifactActionsDao, $rootScope, JFrogModal, JFrogGridFactory) {
    return function selectDeleteVersions(node) {
        let modalInstance;
        let modalScope = $rootScope.$new();
        modalScope.noData = false;

        // Grid
        modalScope.versionsGridOptions = JFrogGridFactory
                .getGridInstance(modalScope)
                .setColumns(
                        [
                            {
                                name: 'Group ID',
                                displayName: 'Group ID',
                                field: 'groupId'
                            },
                            {
                                name: 'Version',
                                displayName: 'Version',
                                field: 'version'
                            },
                            {
                                name: 'Directories Count',
                                displayName: 'Directories Count',
                                field: 'directoriesCount'
                            }
                        ])
                .setRowTemplate('default')
                .setMultiSelect();

        // Scope functions
        modalScope.selectedVersions = () => {
            return (modalScope.versions && modalScope.versionsGridOptions.api) && modalScope.versionsGridOptions.api.selection.getSelectedRows() || [];
        };

        modalScope.close = (version) => {
            modalInstance.close(version);
        };

        let defer = $q.defer();

        ArtifactActionsDao.getDeleteVersions({repoKey: node.data.repoKey, path: node.data.path})
                .$promise.then((versions) => {
                    modalScope.versions = versions.data.versions;
                    modalScope.versionsGridOptions.setGridData(versions.data.versions);
                    if (versions.data.versions.length == 0) {
                        modalScope.noData = true;
                    }
                })
                .finally(()=>{
                    // Launch modal
                    let modalSize = (modalScope.noData ? 'sm' : 'lg');
                    modalInstance = JFrogModal.launchModal('select_delete_versions', modalScope, modalSize);
                    modalInstance.result.then(versions=>defer.resolve(versions));
                });

        return defer.promise;
    }
}
