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
export class BundlePageController {
    constructor($scope, $timeout, $filter, $state, $stateParams, $location, BundlesDao, JFrogUIUtils, JFrogModal, JFrogNotifications) {

    	this.$scope = $scope;
    	this.$filter = $filter;
        this.$state = $state;
        this.$timeout = $timeout;
        this.$stateParams = $stateParams;
        this.BundleDao = BundlesDao;
        this.$location = $location;
        this.JFrogUIUtils = JFrogUIUtils;
        this.BundlesDao = BundlesDao;
        this.artifactoryModal = JFrogModal;
        this.notifications = JFrogNotifications;
    }

    $onInit() {
	    this._getBundleVersions();
	    this.fileInfo = [];
	    this.activeItem = '';
    }

    _getBundleVersions() {
        this.BundleDao.getBundleVersions({
            type: this.$stateParams.type,
            name: this.$stateParams.bundleName
        }).$promise.then((data) => {

            // Go to bundle list page if no versions exist
            if (_.isEmpty(data.versions)) {
                this.backToBundles(this.$stateParams.type);
                return;
            }

	        this.versions = _.sortBy(data.versions, i => -i.created);

            // Go to first version if version isn't exist
            let versionsList = this.versions.map(version => version.version);
            if (!_.contains(versionsList, this.$stateParams.version)) this.goToVersion(this.versions[0]);

            this._getBundleData(this.$stateParams.type, this.$stateParams.bundleName, this.$stateParams.version);

            this.scrollToVersion();
        });
    }

    _getBundleData(type, name, version) {
        this.BundleDao.getBundleData({type,name,version}).$promise.then((data) => {
            let artifacts = data.artifacts;
            this.artifactsList = [];

        	// Group by component name
            artifacts = _.groupBy(artifacts, 'component_name');

            // sorting the artifacts object alphabetic
            Object.keys(artifacts).sort().forEach(key => {
                if (key !== 'undefined') {
                    this.artifactsList.push({key, value: artifacts[key]});
                }
            });

            this.artifactsList.push({key: 'undefined', value: artifacts['undefined']});

            // find the first item in first drawer and select it by default
            const firstItem = this.artifactsList[0].value[0]
            if (firstItem) this.updateFileInfo(firstItem, true);

            this.summaryColumns = this.getSummaryColumns();
	        this.bundleData = data;
        })
    }

	updateFileInfo(fileInfo, scrpllTop = false) {
		this.activeItem = fileInfo.name;
		this.fileInfo = [
			{label: 'Name', value: fileInfo.name || ''},
			{label: 'Path', value: fileInfo.path || '', copy: true},
			{label: 'Created', value: fileInfo.created || ''},
			{label: 'Version', value: fileInfo.component_version || ''},
			{label: 'Path', value: fileInfo.path || ''},
            {label: 'Size', value: this.formatFileSize(fileInfo.size) || ''}
		]

        if (scrpllTop) {
            $('.inner-data-wrapper > div').scrollTop(0);
        }
	}

	getSummaryColumns() {
		return [{
			label: 'Version ID',
			class: 'name',
			template: '{{BundlePage.bundleData.version}}',
			isActive: true
		},
		{
			label: 'Short Description',
			class: 'short-description',
			template: '{{BundlePage.bundleData.desc}}',
            isActive: '{{!!BundlePage.bundleData.desc.length}}',
            width: '2fr'
		},
		{
			label: 'Creation Date',
			class: 'creation-date',
			template: '{{BundlePage.bundleData.created | date:"d MMMM, yyyy HH:mm:ss"}}',
			isActive: true
		},
		{
			label: 'Size',
			class: 'size',
			template: '{{BundlePage.bundleData.size | filesize}}',
			isActive: true
		}]
	}

    deleteBundleVersion(e,version) {

        e.stopPropagation();

        let bundleName = this.$stateParams.bundleName;
        let bundleVersion = version.version;
        let index = _.findIndex(this.versions, (version) => version.version === bundleVersion);
        let isDeleteingActiveVersion = bundleVersion === this.$stateParams.version;

        this.artifactoryModal.confirm(`Are you sure you want to delete version ${bundleVersion} of the ${bundleName} release bundle?`, `Delete Release Bundle`)
        	.then(() => {
                    this.BundlesDao.deleteBundles({
	                    type: this.$stateParams.type,
	                    name: bundleName,
	                    version: bundleVersion}).$promise.then(() => {
                        if (index === 0 && this.versions.length === 1) {
                            this.backToBundles('bundles.list',this.$stateParams.type);
                        } else if (isDeleteingActiveVersion) {
                            this.versions.splice(index, 1);
                            if (index === 0) {
                                this.goToVersion(this.versions[0]);
                            } else {
                                this.goToVersion(this.versions[index - 1]);
                            }
                        } else {
                            this.versions.splice(index, 1);
                        }
                    this.notifications.create({info: `You successfully removed version ${bundleVersion} from ${bundleName} bundle`});
        	});
	    });
    }

	goToVersion(version) {
		_.extend(this.$stateParams, {
		    type: this.$stateParams.type,
		    version: version.version,
		    bundleName: this.$state.params.bundleName
    	});

        this.$state.go('.', this.$stateParams, { location: true, inherit: true, relative: this.$state.$current, notify: false, reload: true })
					.then(() => {
                        this._getBundleData(this.$stateParams.type, this.$stateParams.bundleName, this.$stateParams.version)
					});
	}

    formatFileSize(bytes) {
        return this.$filter('filesize')(bytes);
    }

    scrollToVersion() {
		this.$timeout(() => {
            $('.versions-wrapper').animate({ scrollTop: $('.versions-wrapper .active').offset().top - 350 }, 350);
		});
	}

    backToBundles(type) {
        this.$state.go('bundles.list',{tab: type});
    }
}