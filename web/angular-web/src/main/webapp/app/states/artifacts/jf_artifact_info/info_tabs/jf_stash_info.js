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

class jfStashInfoController {
    constructor($state, BrowseFilesDao, StashResultsDao, JFrogEventBus) {
        this.$state = $state;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.stashResultsDao = StashResultsDao;
        this.JFrogEventBus = JFrogEventBus;
        this.EVENTS = JFrogEventBus.getEventsDefinition();

        this.TOOLTIP = TOOLTIP.admin.import_export.stash;

        this.exportOptions = {};

        this.exportFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export',
            pathLabel: 'Path to export',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        };


    }

    updateExportFolderPath(directory) {
        this.exportOptions.path = directory;
    }

    clearValidations() {
        this.JFrogEventBus.dispatch(this.EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

    export() {
        let payload = {
            path: this.exportOptions.path,
            excludeMetadata: this.exportOptions.excludeMetadata || false,
            m2: this.exportOptions.createM2CompatibleExport || false,
            createArchive: this.exportOptions.createArchive || false,
            verbose: this.exportOptions.verbose || false
        };

        this.stashResultsDao.export({name: 'stash'},payload).$promise.then((response)=>{
//            console.log(response);
        });
    }

    gotoSearch() {
        this.$state.go('search',{searchType: 'quick'});
    }

}

export function jfStashInfo() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '=',
            allowExport: '='
        },
        controller: jfStashInfoController,
        controllerAs: 'jfStashInfo',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_stash_info.html'
    }
}