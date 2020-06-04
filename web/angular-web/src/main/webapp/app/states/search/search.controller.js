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
import EVENTS from "../../constants/artifacts_events.constants";
import TOOLTIP from "../../constants/artifact_tooltip.constant";

export class SearchStateController {

	constructor($q, $state, $scope, $window, $stateParams, $compile, $timeout, JFrogNotifications, uiGridConstants, commonGridColumns, ArtifactSearchDao, ArtifactPackageSearchDao, JFrogModal,
	            JFrogGridFactory, FooterDao, RepositoriesDao, ArtifactActions, RepoDataDao, ArtifactActionsDao, BasicConfigDao, User, UserProfileDao, JFrogEventBus, $injector, StashResultsDao,
	            GoogleAnalytics, ArtifactoryFeatures, ArtifactViewSourceDao) {
		this.$q = $q;
		this.inject                 = $injector.get;
		this.modal = JFrogModal;
		this.artifactSearchDao      = ArtifactSearchDao;
		this.stashResultsDao        = StashResultsDao;
		this.repositoriesDao        = RepositoriesDao;
		this.notifications          = JFrogNotifications;
        this.artifactViewSourceDao  = ArtifactViewSourceDao.getInstance();
        this.$state                 = $state;
		this.$scope                 = $scope;
		this.$window                = $window;
		this.$timeout               = $timeout;
		this.$compile               = $compile;
		this.$stateParams           = $stateParams;
		this.title                  = 'Search Artifacts';
		this.uiGridConstants        = uiGridConstants;
		this.commonGridColumns      = commonGridColumns;
		this.artifactoryGridFactory = JFrogGridFactory;
		this.artifactActionsDao     = ArtifactActionsDao;
		this.actions                = ArtifactActions;
		this.repoDataDao            = RepoDataDao;
		this.footerDao              = FooterDao;
        this.currentUser 			= User.getCurrent();
		this.userProfileDao         = UserProfileDao;
		this.JFrogEventBus          = JFrogEventBus;
		this.GoogleAnalytics        = GoogleAnalytics;
		this.artifactPackageSearchDao = ArtifactPackageSearchDao;
		this.basicConfigDao 	    = BasicConfigDao;
        this.features				= ArtifactoryFeatures;
		this.options                = ['quick', 'package', 'archive', 'property', 'checksum', 'remote', 'trash'];
		this.currentSearch          = this.$stateParams.searchType || localStorage.lastSearchType || 'quick';
		this.resultsMsg             = 'Search Results';
		this.TOOLTIP = TOOLTIP.artifacts.search;
		this.gridOptions            = {};
		this.repoList               = [];
		this.query                  = this.$stateParams.query ? JSON.parse(atob(this.$stateParams.query)) : {};

		if (this.features.isConanCE() || this.features.isJCR() || this.features.isEdgeNode()) {
            this.options = _.filter(this.options, (i) => {return i != 'remote'});

		}

		this.JFrogEventBus.registerOnScope(this.$scope, EVENTS.REFRESH_PAGE_CONTENT,()=>{
			this.refreshRepoList();
		});

		this.packageTypesDefer = $q.defer();
		this._initSearch();

		if (localStorage.lastSearchType === 'package') {
			this.packageTypesDefer.promise.then(()=>{
				if (localStorage.lastPackageSearch) {
					let packageType = _.find(this.availablePackageTypes,{id: localStorage.lastPackageSearch});
					this.selectedPackageType = packageType;
				}
				this.onPackageTypeChange();
			})
		}

		//Check if Trash Can search should be available
		this.currentUser = User.getCurrent();
		let trashDisabled = this.footerDao.get().then((footerData)=>{
			if (footerData.trashDisabled || !this.currentUser.isAdmin()) {
				this.options.pop()
				if (this.currentSearch === 'trash') this.currentSearch = 'quick';
			}
		});

		//Check if jCenter is configured
		if (!this.features.isConanCE() && !this.features.isJCR() && !this.features.isEdgeNode()) {
            this.repositoriesDao.isJcenterRepoConfigured().$promise
                    .then(() => this.isJcenterExists = true, () => this.isJcenterExists = false);
        }

		// init the query when the search type change
		$scope.$watch( () => this.currentSearch, () => {

			this.updateUrl();
			localStorage.lastSearchType = this.currentSearch;

			if (this.currentSearch === 'package') {
				this._createPackageSearchColumnsObject();

				//Focus on the package type dropdown
				this.$timeout(()=>{
					let e = angular.element($('.package-type-selection').children('.ui-select-container')[0]);
					let c = e.controller('uiSelect');
					c.focusser[0].focus();

					this.onPackageTypeChange();
				})

			}
			else {
				if (this.query.selectedPackageType) {
					delete this.query.selectedPackageType;
					this.updateUrl();
				}
				this.refreshGrid();
			}
		});

		this._updateStashStatus();


	}

	updateUrl() {
		let queryClone = _.cloneDeep(this.query);
		delete queryClone.search;
		this.$state.transitionTo('.', {searchType: this.currentSearch, query: !_.isEmpty(queryClone) ? btoa(JSON.stringify(this.query)) : ''}, { location: this.$stateParams.searchType ? true : 'replace', inherit: true, relative: this.$state.$current, notify: false })
	}

	search() {
		this.lastQuery = _.cloneDeep(this.query);
		this.updateUrl();
		this._trackGaEvent(this.currentSearch);
		if (this.currentSearch === 'package' && this.query.search !== 'gavc') {
			let repos = _.find(this.query.packagePayload, {id: 'repo'});
			if (repos && repos.values && repos.values.length === 0) {
				this.notifications.create({warn: "Select at least one repository to search"});
			}
			else {
				this.artifactPackageSearchDao.runQuery({},this._getQueryWithImplicitWildCard(this.query.packagePayload)).$promise.then((result)=>{
					result = result.data;

					_.map(result.results, (result)=>{
						if (result.extraFields) {
							for (let key in result.extraFields) {
								result['extraField_'+key] = result.extraFields[key].join(', ');
							}
							delete result.extraFields;
						}
					});

					this.resultsMsg = result.message;
					this.gridOptions.setGridData(result.results);
					this.results = result.results;
					this.savedToStash = false;
					if (result.searchExpression) {
						this.cleanAql = result.searchExpression;
						this._updateAQL();
						this.showAQL = false;
						this.$timeout(()=>{
							let showAqlButtonElem = $('#show-aql-button-orig');
							let aqlViewerElem = $('#aql-viewer-orig').clone();
							let gridFilterElem = $('jf-grid-filter');
							let gridActionElem = $('.wrapper-grid-actions');
							let clone = showAqlButtonElem.clone();
							clone.attr('id', 'show-aql-button');
							aqlViewerElem.attr('id','aql-viewer');
							gridFilterElem.append(clone);
							clone.css('display','block');
							this.$compile(clone)(this.$scope);

							gridActionElem.after(aqlViewerElem);
							aqlViewerElem.css('display','block');
							let clipCopyElement = aqlViewerElem.find('jf-clip-copy')
							clipCopyElement.addClass('code-mirror-copy');
							this.$compile(aqlViewerElem)(this.$scope);
						})
					}
				});
			}
		}
		else {
			let payloadQuery = _.cloneDeep(this.query);
			if (this.currentSearch === 'property') {
				if (payloadQuery.propertySetKeyValues) {
					if (!payloadQuery.propertyKeyValues) payloadQuery.propertyKeyValues = [];
					payloadQuery.propertySetKeyValues.forEach((propSet) => {
						payloadQuery.propertyKeyValues.push(propSet);
					});
					delete payloadQuery.propertySetKeyValues;
				}

			}
			else if (this.currentSearch === 'trash') {
				payloadQuery.isChecksum = payloadQuery.isChecksum || false;
			}

			if (payloadQuery.selectedRepositories && payloadQuery.selectedRepositories.length ==0) {
				this.notifications.create({warn: "Select at least one repository to search"});
				this.resultsMsg = 'Search Results';
				this.gridOptions.setGridData([]);
				this.results      = [];
			}
			else {
				this.artifactSearchDao.fetch(this._getQueryWithImplicitWildCard(payloadQuery)).$promise.then((result) => {
					this.resultsMsg = result.data.message;
					this.gridOptions.setGridData(result.data.results);
					this.results      = result.data.results;
					this.savedToStash = false;
				})
			}
		}
	}
	reSearch() {
		this.query = this.lastQuery;
		this.search();
	}

	_getQueryWithImplicitWildCard(query) {

		let getNewVal = (oldVal) => {

			if (!oldVal) return oldVal;

			oldVal = oldVal.trim();
			let newVal;
			if (oldVal.startsWith('"') && oldVal.endsWith('"')) {
				newVal = oldVal.substr(1, oldVal.length - 2);
			}
			else if (!oldVal.endsWith('*')) {
				newVal = oldVal + '*';
			}
			else {
				newVal = oldVal;
			}

			return newVal;
		};

		let newQuery = _.cloneDeep(query);

		if (this.currentSearch === 'package') {
			if (newQuery.search === 'gavc') {
				newQuery.artifactID = getNewVal(newQuery.artifactID);
				newQuery.classifier = getNewVal(newQuery.classifier);
				newQuery.groupID    = getNewVal(newQuery.groupID);
				newQuery.version    = getNewVal(newQuery.version);
			}
			else { //standard package search, not gavc
				newQuery.forEach((cond) => {
					if (cond.id !== 'repo') {
						let newVals = [];
						cond.values.forEach((val) => {
							newVals.push(getNewVal(val));
						})
						cond.values = newVals;
					}
				});
			}
		}
		else {
			for (let key in newQuery) {
				let val = newQuery[key];
				if (_.isString(val) && key !== 'search') {
					if (!(key === 'checksum' ||
							(this.currentSearch === 'trash' && key === 'query' && newQuery.isChecksum))) {
						newQuery[key] = getNewVal(val);
					}
				}
				else if (key === 'propertyKeyValues' || key === 'propertySetKeyValues') {
					val.forEach((prop)=>{
						if (prop.key) prop.key = getNewVal(prop.key);
						if (prop.values) {
							for (let i in prop.values) {
								prop.values[i] = getNewVal(prop.values[i]);
							}
						}
					})
				}
			}
		}

		return newQuery;

	}

	_updateAQL() {
		if (this.cleanAql) {
			this.aql = `curl -H 'Content-Type:text/plain' -H 'X-JFrog-Art-Api: <YOUR_API_KEY> -X POST ${this.baseUrl}/api/search/aql -d '\n${this.cleanAql}'`
		}
	}

	_initSearch(params) {

		this.artifactPackageSearchDao.availablePackages().$promise.then((data)=> {
			data = _.filter(data, (packageType)=> {
				return !packageType.id.startsWith('docker') && (!this.isOss || packageType.id === "gavc");
			});
			let i = 0;
			_.forEach(data, () => {
				if (data[i].displayName === "GAVC") {
					data[i].displayName = "Maven GAVC";
				}
				i++;
			});
			if (!this.isOss) {
				data.unshift({
					id: 'dockerV2',
					icon: 'docker',
					displayName: 'Docker'
				});
			}

			if (this.features.isConanCE()) {
				data = _.filter(data, (pack) => {
					return pack.id === 'conan';
				});
			}
			else if (this.features.isJCR()) {
				data = _.filter(data, (pack) => {
					return pack.id === 'Docker' || pack.id === 'Helm';
				});
			}

			this.availablePackageTypes = _.sortBy(data, (pack) => {
				return pack.displayName;
			});
			this.packageTypesDefer.resolve();
		})

		if (!this.repoList.length) {
			this.refreshRepoList();
		}

		if (!this.propertiesOptions) this.getProperties();

		if (params && params.params) {
			this.query = JSON.parse(atob(params.params));

			if (this.currentSearch === 'package') {
				this.packageSearchColumns = this.query.columns;
			}
			this._getGridData();
		}
		else {
			if (this.currentSearch === 'package') {
				this.packageSearchColumns = ['artifact', 'path', 'repo', 'modified'];
			}
		}

		this.basicConfigDao.get().$promise.then((result)=> {
			this.baseUrl = result.baseUrl;
			this._updateAQL();
		});
		//.authenticate({username: this.currentUser.name, password: this.currentPassword})
		// this.userProfileDao.getApiKey().$promise.then((res)=> {
		// 	this.apiKey = res.apiKey;
		// 	this._updateAQL();
		// });
	}

	refreshRepoList(){
		//            let getFuncName = this.currentSearch === 'package' ? 'getForPackageSearch' : 'getForSearch';
		['getForSearch','getForPackageSearch'].forEach((getFuncName) => {
			this.repoDataDao[getFuncName]().$promise.then((result)=> {
				result.repoTypesList = _.map(result.repoTypesList, (repo)=> {
					repo._iconClass = "icon " + (repo.type === 'local' ? "icon-local-repo" : (repo.type === 'remote' ? "icon-remote-repo" : (repo.type === 'virtual' ? "icon-virtual-repo" : (repo.type === 'distribution' ? "icon-distribution-repo" : "icon-notif-error"))));
					return repo;
				});

				let dists  = _.filter(result.repoTypesList, repo => repo.type === 'distribution');
				let locals = _.filter(result.repoTypesList, repo => repo.type === 'local');
				let caches = _.filter(result.repoTypesList, repo => repo.type === 'remote');

				if (getFuncName === 'getForSearch') this.allRepoList = _.cloneDeep(_.union(dists, locals, caches));
				else if (getFuncName === 'getForPackageSearch') this.allPackageRepoList = _.cloneDeep(_.union(dists, locals, caches));

				let lastIncluded = this.query.selectedRepositories || [];

				if (getFuncName === 'getForSearch') {
					this.repoList = _.filter(this.allRepoList, (repo) => {
						return !_.find(lastIncluded, {repoKey: repo.repoKey});
					});
				}
				else if (getFuncName === 'getForPackageSearch') {
					this.packageRepoList = _.filter(this.allPackageRepoList, (repo) => {
						return !_.find(lastIncluded, {repoKey: repo.repoKey});
					});
				}
			});
		});
	}

	_createGrid() {
		if (this.currentSearch === 'package' && !this.packageSearchColumns) return;

		if (this.currentSearch == "remote" || this.currentSearch == "archive") {
			this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
			                       .setColumns(this._getColumns())
			                       .setRowTemplate('default')
			                       .setGridData([]);
		} else {
			this.gridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
			                       .setColumns(this._getColumns())
			                       .setRowTemplate('default')
			                       .setMultiSelect()
			                       .setBatchActions(this._getBatchActions())
			                       .setGridData([]);
		}

		this.gridOptions.isRowSelectable = (row) => {
			var notRepository = row.entity.relativeDirPath !== '[repo]';
			return notRepository && _.contains(row.entity.actions, 'Delete');
		};
	}

	refreshGrid() {

		let defer = this.$q.defer();

		this.showGrid = false;
		this.$timeout(()=>{
			this._createGrid();

			//This is for recalculating grid columns width
			this.$timeout(()=>{
				try {
					window.dispatchEvent(new Event('resize'));
				}
				catch (e) {
					let resizeEvent = document.createEvent("Event");
					resizeEvent.initEvent("resize", false, true);
					window.dispatchEvent(resizeEvent);
				}
				this.$timeout(()=>{
					this.showGrid = true;
					this.resultsMsg = 'Search Results';
					defer.resolve();
				});
			})
		})

		return defer.promise;
	}

	_getBatchActions() {
		return [
			{
				icon: 'clear',
				name: 'Delete',
				callback: () => this.bulkDelete(this.currentSearch === 'trash')
			}
		]
	}

	bulkDelete(permanent) {
		let selectedRows = this.gridOptions.api.selection.getSelectedRows();
		permanent        = permanent || this.footerDao.getInfo().trashDisabled;
		//console.log(selectedRows);
		// Ask for confirmation before delete and if confirmed then delete bulk of users
		this.modal.confirm(`Are you sure you want to ${permanent ? ' <span class="highlight-alert">permanently</span> ' : ' '} delete ${selectedRows.length} items?`).then(() => {
			this._deleteSingleSelected(selectedRows)
		});
	}
	_deleteSingleSelected(rows){
		//console.log(rows);
		if (rows.length === this.results.length) { //To prevent console error (RTFACT-13554)
			this.gridOptions.api.grouping.clearGrouping()
		}
		let elementsToDelete = _.map(rows, (row) => {
			return {
				name: row.name,
				path: this.currentSearch === 'trash'? ((row.originRepository + '/' + row.relativeDirPath).split('[root]').join('') + '/' + row.name).split('//').join('/') : row.relativePath,
				repoKey: row.repoKey
			}

		});
		this.artifactSearchDao.delete({artifacts:elementsToDelete}).$promise.then(() => {
			// refresh the gridData in any case
		}).finally(()=>{
			this.reSearch();
			this.artifactoryState.setState('refreshTreeNextTime',true);
		});
	}

	_deleteSelected(rows, permanent){
		permanent = permanent || this.footerDao.getInfo().trashDisabled;
		this.modal.confirm(`Are you sure you wish to ${permanent ? ' <span class="highlight-alert">permanently</span> ' : ' '} delete ${rows[0].name}?`)
		    .then(() => this._deleteSingleSelected(rows));
	}

	_getColumns() {
		switch (this.currentSearch) {
			case 'package': {
				return this._getColumnsForPackageSearch(this.packageSearchColumns)
			}
			case 'quick': {
				return [
					{
						name: "Artifact",
						displayName: "Artifact",
						field: "name",
						sort: {
							direction: this.uiGridConstants.ASC
						},
						cellTemplate: this.commonGridColumns.downloadableColumn('autotest-quick-artifact'),
						width: '25%',
						customActions: [{
							icon: 'icon icon-view',
							tooltip: 'View',
							callback: row => this.viewCodeArtifact(row),
							visibleWhen: row => _.contains(row.actions, 'View')
						}],
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: "Path",
						displayName: "Path",
						field: "relativeDirPath",
						allowGrouping: true,
						cellTemplate: '<div class="autotest-quick-path ui-grid-cell-contents">{{ row.entity.relativeDirPath}}</div>',
						width: '40%',
						customActions: [{
							icon: 'icon icon-show-in-tree',
							tooltip: 'Show In Tree',
							callback: row => this.showInTree(row),
							visibleWhen: row => _.contains(row.actions, 'ShowInTree')
						}]
					},
					{
						name: "Repository",
						displayName: "Repository",
						field: "repoKey",
						allowGrouping: true,
						cellTemplate: '<div class="autotest-quick-repository ui-grid-cell-contents">{{ row.entity.repoKey}}</div>',
						width: '15%'
					},
					{
						name: "Modified",
						displayName: "Modified",
						cellTemplate: '<div class="autotest-quick-modified ui-grid-cell-contents">{{ row.entity.modifiedString }}</div>',
						field: "modifiedDate",
						width: '20%',
						actions: {
							delete: {
								callback: row => this._deleteSelected([row]),
								visibleWhen: row => _.contains(row.actions, 'Delete')
							}
						}
					}
				]
			}
			case 'archive': {
				return [
					{
						name: "Name",
						displayName: "Name",
						field: "name",
						sort: {
							direction: this.uiGridConstants.ASC
						},
						width: '30%'
					},

					{
						name: "Artifact",
						displayName: "Artifact",
						field: "archiveName",
						allowGrouping: true,
						width: '20%',
						customActions: [{
							icon: 'icon icon-view',
							tooltip: 'View',
							callback: row => this.viewCodeArtifact(row),
							visibleWhen: row => _.contains(row.actions, 'View')
						}],
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: "Artifact Path",
						displayName: "Artifact Path",
						field: "archivePath",
						allowGrouping: true,
						width: '25%',
						customActions: [{
							icon: 'icon icon-show-in-tree',
							tooltip: 'Show In Tree',
							callback: row => this.showInTree(row),
							visibleWhen: row => _.contains(row.actions, 'ShowInTree')
						}]
					},
					{
						name: "Repository",
						displayName: "Repository",
						field: "repoKey",
						allowGrouping: true,
						width: '10%'
					},
					{
						name: "Modified",
						displayName: "Modified",
						cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.modifiedString }}</div>',
						field: "modifiedDate",
						width: '15%',
						actions: {
							delete: {
								callback: row => this._deleteSelected([row]),
								visibleWhen: row => _.contains(row.actions, 'Delete')
							}
						}
					}
				]
			}
			case 'gavc': {
				return [
					{
						name: 'Artifact',
						displayName: 'Artifact',
						field: 'name',
						sort: {
							direction: this.uiGridConstants.ASC
						},
						width: '20%',
						customActions: [{
							icon: 'icon icon-view',
							tooltip: 'View',
							callback: row => this.viewCodeArtifact(row),
							visibleWhen: row => _.contains(row.actions, 'View')
						}, {
							icon: 'icon icon-show-in-tree',
							tooltip: 'Show In Tree',
							callback: row => this.showInTree(row),
							visibleWhen: row => _.contains(row.actions, 'ShowInTree')
						}],
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: 'Group ID',
						displayName: 'Group ID',
						field: 'groupID',
						allowGrouping: true,
						width: '15%'
					},
					{
						name: 'Artifact ID',
						displayName: 'Artifact ID',
						field: 'artifactID',
						allowGrouping: true,
						width: '17%'
					},
					{
						name: 'Version',
						displayName: 'Version',
						field: 'version',
						allowGrouping: true,
						width: '13%'
					},
					{
						name: 'Classifier',
						displayName: 'Classifier',
						field: 'classifier',
						allowGrouping: true,
						width: '10%'
					},
					{
						name: 'Repository',
						displayName: 'Repository',
						field: 'repoKey',
						allowGrouping: true,
						width: '10%'
					},
					{
						name: "Modified",
						displayName: "Modified",
						cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.modifiedString }}</div>',
						field: "modifiedDate",
						width: '15%',
						actions: {
							delete: {
								callback: row => this._deleteSelected([row]),
								visibleWhen: row => _.contains(row.actions, 'Delete')
							}
						}
					}
				]
			}
			case 'property': {
				return [
					{
						name: "Item",
						displayName: "Item",
						field: "name",
						sort: {
							direction: this.uiGridConstants.ASC
						},
						width: '25%',
						customActions: [{
							icon: 'icon icon-view',
							tooltip: 'View',
							callback: row => this.viewCodeArtifact(row),
							visibleWhen: row => _.contains(row.actions, 'View')
						}],
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: "Type",
						displayName: "Type",
						field: "resultType",
						cellTemplate: '<div class="ui-grid-cell-contents">' +
						'<span jf-tooltip="{{ row.entity.resultType }}" class="icon" ng-class="{ \'icon-local-repo\': row.entity.resultType === \'Repository\', \'icon-folder\': row.entity.resultType === \'Directory\', \'icon-general\': row.entity.resultType === \'File\'}"></span></div>',
						allowGrouping: true,
						width: '8%'
					},
					{
						name: "Path",
						displayName: "Path",
						field: "relativeDirPath",
						allowGrouping: true,
						width: '37%',
						customActions: [{
							icon: 'icon icon-show-in-tree',
							tooltip: 'Show In Tree',
							callback: row => this.showInTree(row),
							visibleWhen: row => _.contains(row.actions, 'ShowInTree')
						}]
					},
					{
						name: "Repository",
						displayName: "Repository",
						field: "repoKey",
						allowGrouping: true,
						width: '15%'
					},
					{
						name: "Modified",
						displayName: "Modified",
						cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.modifiedString }}</div>',
						field: "modifiedDate",
						width: '15%',
						actions: {
							delete: {
								callback: row => this._deleteSelected([row]),
								visibleWhen: row => _.contains(row.actions, 'Delete')
							}
						}
					}
				]
			}
			case 'checksum': {
				return [
					{
						name: "Artifact",
						displayName: "Artifact",
						field: "name",
						sort: {
							direction: this.uiGridConstants.ASC
						},
						width: '25%',
						customActions: [{
							icon: 'icon icon-view',
							tooltip: 'View',
							callback: row => this.viewCodeArtifact(row),
							visibleWhen: row => _.contains(row.actions, 'View')
						}],
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: "Path",
						displayName: "Path",
						field: "relativeDirPath",
						allowGrouping: true,
						width: '45%',
						customActions: [{
							icon: 'icon icon-show-in-tree',
							tooltip: 'Show In Tree',
							callback: row => this.showInTree(row),
							visibleWhen: row => _.contains(row.actions, 'ShowInTree')
						}]
					},
					{
						name: "Repository",
						displayName: "Repository",
						field: "repoKey",
						allowGrouping: true,
						width: '15%'
					},
					{
						name: "Modified",
						displayName: "Modified",
						cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.modifiedString }}</div>',
						field: "modifiedDate",
						width: '15%',
						actions: {
							delete: {
								callback: row => this._deleteSelected([row]),
								visibleWhen: row => _.contains(row.actions, 'Delete')
							}
						}
					}
				]
			}
			case 'remote': {
				return [
					{
						name: "Name",
						displayName: "Name",
						field: "name",
						sort: {
							direction: this.uiGridConstants.ASC
						},
						width: '20%',
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: "Path",
						displayName: "Path",
						field: "path",
						customActions: [{
							icon: 'icon icon-bintray',
							tooltip: 'Show In Bintray',
							callback: row => this.showInBintray(row)
						}],
						width: '30%'
					},
					{
						name: "Package",
						displayName: "Package",
						field: "package",
						width: '25%'
					},
					{
						name: "Released",
						displayName: "Released",
						field: "release",
						width: '15%'
					},
					{
						name: "Cached",
						displayName: "Cached",
						field: "cached",
						cellTemplate: this.commonGridColumns.booleanColumn('MODEL_COL_FIELD'),
						width: '10%'
					}
				]
			}
			case 'trash': {
				return [
					{
						name: "Artifact",
						displayName: "Artifact",
						field: "name",
						sort: {
							direction: this.uiGridConstants.ASC
						},
						cellTemplate: this.commonGridColumns.downloadableColumn('autotest-trash-artifact'),
						width: '25%',
						customActions: [
							{
								icon: 'icon icon-view',
								tooltip: 'View',
								callback: row => this.viewCodeArtifact(row),
								visibleWhen: row => _.contains(row.actions, 'View')
							},
							{
								icon: 'icon icon-trashcan-restore',
								tooltip: 'Restore To Original Path',
								callback: row => this.restoreTrashItem(row),
								visibleWhen: row => _.contains(row.actions, 'Restore')
							}
						],
						actions: {
							download: {
								callback: row => this.downloadSelectedItems(row),
								visibleWhen: row => _.contains(row.actions, 'Download')
							}
						}
					},
					{
						name: "Original Path",
						displayName: "Original Path",
						field: "relativeDirPath",
						allowGrouping: true,
						cellTemplate: '<div class="autotest-trash-origin-path ui-grid-cell-contents">{{ row.entity.relativeDirPath}}</div>',
						width: '40%',
						customActions: [{
							icon: 'icon icon-show-in-tree',
							tooltip: 'Show In Tree',
							callback: row => this.showInTree(row),
							visibleWhen: row => _.contains(row.actions, 'ShowInTree')
						}]
					},
					{
						name: "Original Repository",
						displayName: "Original Repository",
						field: "originRepository",
						allowGrouping: true,
						cellTemplate: '<div class="autotest-trash-origin-repository ui-grid-cell-contents">{{ row.entity.originRepository}}</div>',
						width: '15%'
					},
					{
						name: "Deleted Time",
						displayName: "Deleted Time",
						cellTemplate: '<div class="autotest-trash-deleted ui-grid-cell-contents">{{ row.entity.deletedTimeString }}</div>',
						field: "deletedTime",
						width: '20%',
						customActions: [
							{
								icon: 'icon icon-clear',
								tooltip: ' Delete Permanently ', //Spaces are there to prevent text from splitting to two lines (RTFACT-13526)
								callback: row => this._deleteSelected([row], true),
								visibleWhen: row => _.contains(row.actions, 'Delete'),
							}
						]
					}
				]
			}

		}
	}

	_createPackageSearchColumnsObject() {
		this.packageSearchColumnsObject = {
			artifact: {
				name: "Artifact",
				displayName: "Artifact",
				field: "name",
				sort: {
					direction: this.uiGridConstants.ASC
				},
				cellTemplate: this.commonGridColumns.downloadableColumn(),
				width: '25%',
				customActions: [{
					icon: 'icon icon-view',
					tooltip: 'View',
					callback: row => this.viewCodeArtifact(row),
					visibleWhen: row => _.contains(row.actions, 'View')
				}],
				actions: {
					download: {
						callback: row => this.downloadSelectedItems(row),
						visibleWhen: row => _.contains(row.actions, 'Download')
					}
				}
			},
			path: {
				name: "Path",
				displayName: "Path",
				field: "relativePath",
				allowGrouping: true,
				width: '40%',
				customActions: [{
					icon: 'icon icon-show-in-tree',
					tooltip: 'Show In Tree',
					callback: row => this.showInTree(row),
					visibleWhen: row => _.contains(row.actions, 'ShowInTree')
				}]
			},
			repo: {
				name: "Repository",
				displayName: "Repository",
				field: "repoKey",
				allowGrouping: true,
				width: '15%'
			},
			modified: {
				name: "Modified",
				displayName: "Modified",
				cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.modifiedString }}</div>',
				field: "modifiedDate",
				width: '20%',
				actions: {
					delete: {
						callback: row => this._deleteSelected([row]),
						visibleWhen: row => _.contains(row.actions, 'Delete')
					}
				}
			},
			groupID: {
				name: 'Group ID',
				displayName: 'Group ID',
				field: 'groupID',
				allowGrouping: true,
				width: '18%'
			},
			artifactID: {
				name: 'Artifact ID',
				displayName: 'Artifact ID',
				field: 'artifactID',
				allowGrouping: true,
				width: '18%'
			},
			version: {
				name: 'Version',
				displayName: 'Version',
				field: 'version',
				allowGrouping: true,
				width: '18%'
			},
			classifier: {
				name: 'Classifier',
				displayName: 'Classifier',
				field: 'classifier',
				allowGrouping: true,
				width: '18%'
			}
		}

	}

	_getColumnsForPackageSearch(columns) {
		let columnsArray = [];
		columns.forEach((column)=> {
			if (!_.contains(column, '*')) {
				columnsArray.push(_.cloneDeep(this.packageSearchColumnsObject[column]));
			}
			else {
				let groupable = false;
				let width;
				if (_.contains(column, '@')) {
					column    = column.split('@').join('');
					groupable = true;
				}
				if (_.contains(column, '!')) {
					let splitted = column.split('!');
					column       = splitted[0];
					width        = splitted[1];
				}

				let splitted = column.split('*');
				let field    = splitted[0];
				let name     = splitted[1];
				columnsArray.push({
					name: name,
					displayName: name,
					field: 'extraField_' + field,
					width: width || '18%',
					allowGrouping: groupable
				});
			}
		});

		this._normalizeGridColumnWidths(columnsArray);

		if (!columnsArray[0].actions) columnsArray[0].actions = {};
		if (!columnsArray[0].actions.download) {
			columnsArray[0].actions.download = {
				callback: row => this.downloadSelectedItems(row),
				visibleWhen: row => _.contains(row.actions, 'Download')
			}
		}

		//If no path field add 'show in tree' action to first column
		if (_.findIndex(columnsArray, 'name', 'Path') < 0) {
			if (!columnsArray[0].customActions) columnsArray[0].customActions = [];
			columnsArray[0].customActions = [{
				icon: 'icon icon-show-in-tree',
				tooltip: 'Show In Tree',
				callback: row => this.showInTree(row),
				visibleWhen: row => _.contains(row.actions, 'ShowInTree')
			}]
		}
		return columnsArray;
	}

	_normalizeGridColumnWidths(columnsArray) {
		let totalWidth = 0;
		for (let key in columnsArray) {
			let obj = columnsArray[key];
			totalWidth += parseInt(obj.width);
		}
		let scale = 100/totalWidth;
		for (let key in columnsArray) {
			let obj = columnsArray[key];
			let origWidth = parseInt(obj.width);
			obj.width = (origWidth*scale) + '%';
		}

	}


	showInTree(row) {
		let relativePath;
		let artifactPath;
		if (this.currentSearch === 'trash') {
			relativePath = (row.originRepository + "/" + row.relativeDirPath).split('[root]').join('');
			artifactPath = (row.repoKey + "/" + relativePath + "/" + row.name).split('//').join('/');
		}
		else {
			relativePath = row.relativePath ? (row.relativePath.startsWith('./') ? row.relativePath.substr(2) : row.relativePath) : '';
			artifactPath = row.repoKey + "/" + (relativePath || row.path);
		}


		let archivePath = '';
		if (row.archiveName) {
			if(row.archivePath === '[root]') {
				row.archivePath = '';
			}
			archivePath = row.repoKey + "/" + row.archivePath + row.archiveName;
		}
		let path = (archivePath || artifactPath );
		this.$state.go('artifacts.browsers.path', {
			"browser": "tree",
			"tab": "General",
			"artifact": path
		});

	}

	_updateStashStatus() {
		this.stashResultsDao.get({name:'stash'}).$promise.then((data)=>{
			this.stashedItemsCount = data.length;
			this.showStashBox = true;
			if (data.length === 0) {
				this.showStashBox = false;
				this.savedToStash = false;
			}
		});
	}

	_doStashAction(action) {

		let payload = this._buildPayloadForStash();
		this.stashResultsDao[action]({name: 'stash'},payload).$promise.then((response)=>{
			if (action === 'save' && response.status === 200) {
				this.savedToStash = true;
				this.duringStashAnimation = false;
			}
			this._updateStashStatus();
		});
	}

	saveToStash() {
		this.showStashBox = true;


		/*let distanceRight = $('.repo-list-dropdown').width() + $('.repo-list-dropdown').offset().left - $('.repo-dnd-title').width() - $('.repo-dnd-title').offset().left;
		 $('#stash-animation').css('right', distanceRight);*/
		let distanceRight = $(document).width() - $('#stash-results-button').offset().left - $('#stash-results-button').width();
		$('#stash-animation').css({
			'right' : distanceRight,
			'animation-duration' : $(document).width() / 960
		});

		this.$timeout(()=>{
			this.duringStashAnimation = true;
			$('#stash-container').addClass('animate');

			this.duringStashAnimation = true;

			this._doStashAction('save');
		});

	}

	addToStash() {
		this._doStashAction('add');
	}

	subtractFromStash() {
		this._doStashAction('subtract');
	}

	intersectWithStash() {
		this._doStashAction('intersect');
	}

	gotoStash() {
		this.JFrogEventBus.dispatch(EVENTS.ACTION_REFRESH_STASH);
		this.$state.go('artifacts.browsers.path', {browser: 'stash', artifact: '', tab: 'StashInfo'});
	}

	clearStash() {
		this.modal.confirm('Are you sure you want to clear stashed results? All items will be removed from stash.','Clear Stashed Results', {confirm: 'Clear'})
		    .then(() => {
			    this.stashResultsDao.delete({name: 'stash'}).$promise.then((response)=> {
				    this.JFrogEventBus.dispatch(EVENTS.ACTION_DISCARD_STASH);
				    if (response.status === 200) {
					    this.savedToStash = false;
					    this._updateStashStatus();
					    $('#stash-container').removeClass('animate');
				    }
			    });
		    });
	}
	_buildPayloadForStash() {
		let searchType = this.currentSearch;
		if (searchType === 'checksum') searchType='quick';
		let selectedRows = this.gridOptions.api.selection.getSelectedRows();
		let rawResults = selectedRows.length ? selectedRows : this.results;

		rawResults = _.filter(rawResults, (result)=> {
			return !result.resultType || result.resultType == 'File';
		});

		let payload = _.map(rawResults, (result)=>{
			let retObj = {};
			retObj.type = searchType === 'archive' ? 'class' : searchType;
			retObj.repoKey = result.repoKey;

			if (searchType === 'archive') {
				if (result.archivePath==='[root]') result.archivePath = '';
				retObj.name = result.name;
				retObj.archivePath = result.archivePath + result.archiveName;
			}
			else {
				if (result.relativePath==='[root]') result.relativePath = '';
				retObj.relativePath = result.relativePath;
			}

			return retObj;
		});

		return payload;
	}

	setShowAQL(show) {
		this.showAQL = show;
		if (show) {
			let temp = this.aql;
			this.aql = '';
			this.$timeout(()=>{
				this.aql = temp;
			})
		}
	}

	downloadSelectedItems(row) {
		//        this.download(row.downloadLink);
	}

	getProperties() {
		this.artifactSearchDao.get({search: "property", action: "keyvalue"}).$promise.then((_propeties)=> {
			this.propertiesOptions = _propeties.data;
		});
	}

	showInBintray(row) {
		this.$window.open('https://bintray.com/bintray/jcenter/' + row.package, '')
	}
	viewCodeArtifact(row) {
		let name = row.name;
		if(_.startsWith(name, './')) {
			name = name.slice(2);
		}
		if (row.archiveName) {
			if(row.archivePath === '[root]') {
				row.archivePath = '';
			}
			this.artifactViewSourceDao.fetch({
				archivePath: row.archivePath + row.archiveName,
				repoKey: row.repoKey,
				sourcePath: name
			}).$promise
			    .then((result) => {
				    this.modal.launchCodeModal(row.name, result.source,
					    {name: row.type, json: true});
			    })
		} else {
			let data = {
				repoKey: row.repoKey,
				path: (row.relativePath || row.path)
			};
			this.artifactActionsDao.perform({action: 'view'}, data).$promise
			    .then((result) => {
				    this.modal.launchCodeModal(row.name, result.data.fileContent,
					    {name: row.type, json: true});
			    });
		}
	}

	restoreTrashItem(row) {
		this.actions.perform({name: 'RestoreToOriginalPath'},
			{
				data: {
					path: (row.originRepository + '/' + row.relativeDirPath + '/' + row.name).split('[root]').join(''),
					repoKey: row.repoKey
				}
			}
		)
		    .then(()=>{
			    this.reSearch();
		    })
	}

	getPrettySearchType(type) {
		if (type === 'trash') return 'Trash Can';
		if (type === 'remote') return 'JCenter';
		if (type === 'archive') return 'Archive Entries';
		else return _.capitalize(type);
	}

	createJcenter() {
		if(!this.currentUser.isAdmin()) {
			this.notifications.create({warn: 'Only an admin user can create repositories.'});
			return false;
		}
		this.modal.confirm('A remote repository pointing to JCenter with default configuration values is about to be created.<br/>' +
			'If you wish to change it\'s configuration you can do so from the Remote Repositories menu in the Admin section',
			'Creating JCenter remote repository')
		    .then(()=> {
				    this.repositoriesDao.createDefaultJcenterRepo().$promise
				        .then(() => this.isJcenterExists = true , () => '');
			    }
		    );
	}

	onPackageTypeChange() {
		if (this.selectedPackageType) {
			this.$timeout(() => {
				if (this.packageController) this.packageController.onPackageTypeChange(this.selectedPackageType)
			}, 100);
			localStorage.lastPackageSearch = this.selectedPackageType.id;
		}
	}

	_trackGaEvent(searchType) {
		switch (searchType) {
			case 'quick' :
				if (this.$stateParams.fromHome) {
					this.GoogleAnalytics.trackEvent('Homepage', 'Quick search');
					this.$stateParams.fromHome = false;
				} else {
					this.GoogleAnalytics.trackEvent('Search', 'Quick');
				}
				break;
			case 'package' :
				this.GoogleAnalytics.trackEvent('Search', 'Package', this.query.selectedPackageType.displayName);
				break;
			case 'archive' :
				this.GoogleAnalytics.trackEvent('Search', 'Archive');
				break;
			case 'property' :
				this.GoogleAnalytics.trackEvent('Search', 'Property');
				break;
			case 'checksum' :
				this.GoogleAnalytics.trackEvent('Search', 'Checksum');
				break;
			case 'remote' :
				this.GoogleAnalytics.trackEvent('Search', 'Remote');
				break;
			case 'trash' :
				this.GoogleAnalytics.trackEvent('Search', 'Trash');
				break;
			default : {
				this.GoogleAnalytics.trackEvent('Search', 'Unknown');
				break;
			}
		}
	}

}
