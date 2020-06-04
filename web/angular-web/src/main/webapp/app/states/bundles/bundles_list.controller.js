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
export class BundlesListController {
    constructor($scope, $q, BundlesDao, JFrogTableViewOptions, ArtifactoryFeatures, $stateParams, $state) {

        this.$scope = $scope;
        this.$q = $q;
        this.BundlesDao = BundlesDao;
        this.JFrogTableViewOptions = JFrogTableViewOptions;
        this.allBundlesGridOptions = {};
        this.ArtifactoryFeatures = ArtifactoryFeatures;
        this.$stateParams = $stateParams;
        this.$state = $state;
	    this._createGrids();
        this._getBundlesData();
    }

    $onInit() {
	    this.DICTIONARY = {
		    target: 'Received',
		    source: 'Distributable',
	    };
	    this.tabs = [
		    {name: 'target'},
		    {name: 'source'},
	    ];
    }

    _getBundlesData() {
        this.BundlesDao.getData({type : 'target'}).$promise.then((results) => {
	        this.targetBundles = results.bundles;
            this.targetTableViewOptions.setData(this.targetBundles)
        });
        this.BundlesDao.getData({type : 'source'}).$promise.then((results) => {
            this.sourceBundles = results.bundles;
            this.sourceTableViewOptions.setData(this.sourceBundles)
        });
    }

    _createGrids() {
        this.targetTableViewOptions = new this.JFrogTableViewOptions(this.$scope);
        this.targetTableViewOptions.setColumns(this._getColumns())
                .setRowsPerPage(20)
                .setEmptyTableText('No release bundles have been distributed to Artifactory.');
        this.sourceTableViewOptions = new this.JFrogTableViewOptions(this.$scope);
        this.sourceTableViewOptions.setColumns(this._getColumns())
                .setRowsPerPage(20)
                .setEmptyTableText('No release bundles have been distributed from Artifactory.');
    }

    goToBundle(name, version) {
        let type = this.$state.params.tab;
	    this.$state.go('bundles.bundle_page', {
	        type: type,
            bundleName: name,
            version: version
        });
    }

    _getColumns() {
        return [
            {
                header: "Bundle Name",
                field: "name",
                cellTemplate: `<div class="ui-grid-cell-contents">
                                   <a href class="no-cm-action jf-link" 
                                      ng-click="appScope.BundlesList.goToBundle(row.entity.name,row.entity.latestVersion)">
                                     {{row.entity.name}}
                                   </a>
                               </div>`,
                width: '55%',
                filterable: true
            },
            {
                header: "Last Version",
                field: "latestVersion",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.latestVersion}}</div>',
                width: '20%',
                filterable: true
            },
            {
                header: "Created",
                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.created | date: \'d MMMM, yyyy HH:mm:ss Z\'}}</div>',
                field: "created",
                width: '25%'
            }
        ]
    }
}