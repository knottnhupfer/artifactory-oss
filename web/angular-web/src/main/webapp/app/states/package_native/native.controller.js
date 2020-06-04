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
import _ from 'lodash';
import field_options from '../../constants/field_options.constats';
import {
    packagesFieldsAdapter,
    versionsFieldsAdapter,
    versionExtraInfoAdapter,
} from './packagesMdsDaoAdapter';

export default class PackagesNativeController {
    constructor($state, $scope, PackagesDAO, $q, FooterDao, ArtifactoryFeatures, User, ArtifactoryStorage) {
        this.$state = $state;
        this.$scope = $scope;
        this.$q = $q;
        this.packagesDAO = PackagesDAO;
        this.footerDao = FooterDao;
        this.artifactoryStorage = ArtifactoryStorage;
        this.useMdsApi = null;

        this.mdsCache = {
            packages: null,
            versions: null
        };

        if (User.currentUser.name === 'anonymous' && !User.currentUser.anonAccessEnabled) {
            $state.go('login');
            return;
        }
        if (ArtifactoryFeatures.isOss()) {
            $state.go('home');
            return;
        }

         this.disabledPackageTypes = [];
        // if (!localStorage._enableNativeNpm) this.disabledPackageTypes.push('npm');

        if (ArtifactoryFeatures.isJCR()) {
            this.disabledPackageTypes.push('npm');
        }

        const mdsApiSwitcher = (functionName) => {
            return this.$q((resolve, reject) => {
                this.isMdsEnabled().then((isMdsEnabled) => {
                    if (isMdsEnabled && this[`${functionName}Mds`] && _.isFunction(this[`${functionName}Mds`])) {
                        resolve(this[`${functionName}Mds`].bind(this));
                    }
                    resolve(this[functionName].bind(this));
                })
            });
        };

        this.hostData = {
            getPackageTypes: () => this.getPackageTypes(),
            isXrayEnabled: () => this.isXrayEnabled(),
            // showExtraInfo: () => mdsApiSwitcher('showExtraInfo')(),
            showExtraInfo: () => this.showExtraInfo(),

            getRepos: (params) => this.getRepos(params),
            getPackages: (params) => mdsApiSwitcher('getPackages').then(fn => fn(params)),
            getPackagesCount: (params) => this.getPackagesCount(params),
            getPackage: (params) => mdsApiSwitcher('getPackage').then(fn => fn(params)),
            // getVersion: (params) => mdsApiSwitcher('getVersion')(params),
            getVersion: (params) => this.getVersion(params),
            getVersionData: (params) => this.getVersionData(params),
            getPackageExtraInfo: (params) => mdsApiSwitcher('getPackageExtraInfo').then(fn => fn(params)),
            getVersionExtraInfo: (params) => mdsApiSwitcher('getVersionExtraInfo').then(fn => fn(params)),
            getPackageSummary: (params) => this.getPackageSummary(params),
            getVersionSummary: (params) => this.getVersionSummary(params),
            getPackageSummaryExtraInfo: (params) => mdsApiSwitcher('getPackageSummaryExtraInfo').then(fn => fn(params)),
            getVersionSummaryExtraInfo: (params) => mdsApiSwitcher('getVersionSummaryExtraInfo').then(fn => fn(params)),
            getPackageDownloadsCount: (params) => mdsApiSwitcher('getPackageDownloadsCount').then(fn => fn(params)),
            getVersionDownloadsCount: (params) => mdsApiSwitcher('getVersionDownloadsCount').then(fn => fn(params)),
            getManifest: (params) => this.getManifest(params),

            cancelPackageExtraInfo: () => this.cancelPackageExtraInfo(),
            cancelVersionExtraInfo: () => this.cancelVersionExtraInfo(),

            showInTree: (params) => this.showInTree(params),
        }
    }

    getPackageTypes() {
        let defferd = this.$q.defer();
        let pkgTypes = field_options.repoPackageTypes.map((t) => {
            return {
                value: t.value,
                displayText: t.text,
                iconClass: `icon-${t.icon}`
            };
        });
        defferd.resolve(pkgTypes);
        return defferd.promise;
    }

    getPackages(params) {
        return this.packagesDAO.getPackages(params.pathAndQuery, params.payload).$promise;
    }

    getPackagesMds(params) {
        return this.$q((resolve, reject) => {
            this.packagesDAO.getPackagesMds(params.pathAndQuery, params.payload).$promise
                .then(res => {
                    this.mdsCache.packages = res.packages;
                    const packagesAdapted = packagesFieldsAdapter(res);
                    resolve(packagesAdapted)
                }).catch(e => {
                console.log(e);
            });
        })
    }

    getPackagesCount(params) {
        return this.packagesDAO.getPackagesCount(_.extend(params.pathAndQuery, {$no_spinner: true}),
            params.payload).$promise;
    }

    getRepos(params) {
        return this.packagesDAO.getRepos(params.pathAndQuery).$promise;
    }

    getPackage(params) {
        return this.packagesDAO.getPackage(params.pathAndQuery, params.payload).$promise;
    }

    getPackageMds(params) {
        return this.$q((resolve, reject) => {
            const {packageType, packageName} = params.pathAndQuery;

            this.packagesDAO.getPackageMds(params.pathAndQuery, params.payload).$promise
                .then(res => {
                    const versions = res.versions.filter(v => v.pkgid === `${packageType}:${packageName}`);

                    const versionsAdapted = versionsFieldsAdapter(versions, params.pathAndQuery);
                    this.mdsCache.versions = versionsAdapted;

                    if (this.resolvePackageDownloadCount) {
                        this.resolvePackageDownloadCount(
                            {
                                totalDownloads: (res.versions.length &&
                                    res.versions[0].stats &&
                                    res.versions[0].stats.download_count) ?
                                    res.versions[0].stats.download_count : 0
                            });
                        this.resolvePackageDownloadCount = null;
                    }

                    resolve(versionsAdapted);
                });

        });
    }

    getPackageSummary(params) {
        return this.packagesDAO.getPackageSummary(params.pathAndQuery, params.payload).$promise;
    }

    getPackageSummaryExtraInfo(params) {
        return this.packagesDAO.getPackageSummaryExtraInfo(_.extend(params.pathAndQuery, {$no_spinner: true}),
            params.payload).$promise;
    }


    getPackageExtraInfo(params) {
        if (params.pathAndQuery.packageType === 'docker') {
            return this.packagesDAO.getDockerPackageExtraInfo(
                _.extend(params.pathAndQuery, {$no_spinner: true})).$promise;
        }
        else {
            return this.packagesDAO.getPackageExtraInfo(_.extend(params.pathAndQuery, {$no_spinner: true}),
                params.payload).$promise;
        }
    }

    getPackageExtraInfoMds(params) {
        return this.$q((resolve, reject) => {
            if (params.pathAndQuery.packageType === 'docker') {
                if (this.mdsCache.packages) {
                    const packageData = this.getPackageFromCache(params);
                    if (packageData) {
                        resolve({
                            totalDownloads: packageData.numberOfDownloads || 0,
                            totalVersions: packageData.numberOfVersions || 0,
                            lastModified: moment(packageData.modified).format('X')
                        })
                    }
                }
                else {
                    return this.packagesDAO.getDockerPackageExtraInfo(
                        _.extend(params.pathAndQuery, {$no_spinner: true})).$promise.then(res => resolve(res));
                }
            }
            else {
                if (this.mdsCache.packages) {
                    const packageData = this.getPackageFromCache(params);
                    if (packageData.length === 1) {
                        resolve({
                            totalDownloads: packageData[0].downloadCount,
                            keywords: packageData.tags
                        })
                    }
                }
                return this.packagesDAO.getPackageExtraInfo(_.extend(params.pathAndQuery, {$no_spinner: true}),
                    params.payload).$promise.then(res => resolve(res));
            }
        });

    }

    getPackageFromCache(params) {
        const packageData = this.mdsCache.packages.find(p => p.name === params.pathAndQuery.packageName);
        return packageData;
    }

    getVersionExtraInfo(params) {
        return this.packagesDAO.getVersionExtraInfo(_.extend(params.pathAndQuery, {$no_spinner: true}),
            params.payload).$promise;
    }

    getVersionExtraInfoMds(params) {
        const {packageName, packageType, versionName} = params.pathAndQuery;
        if (this.mdsCache.packages) {
            const packageData = this.mdsCache.packages.filter(p => p.name === packageName);
            if (packageData.length === 1) {
                return new Promise((resolve, reject) => {
                    const version = packageData.versions.filter(v => v.name === packageName);
                    resolve(versionExtraInfoAdapter(version[0]));
                });
            }
        }
        if (this.mdsCache.versions) {
            return new Promise((resolve, reject) => {
                const version = this.mdsCache.versions
                    .filter(v => v.name === versionName
                        && v.pkgid === `${packageType}:${packageName}`);
                resolve(versionExtraInfoAdapter(version[0]));
            })
        }
        return this.packagesDAO.getVersionExtraInfo(_.extend(params.pathAndQuery, {$no_spinner: true}),
            params.payload).$promise;
    }

    cancelPackageExtraInfo() {
        this.packagesDAO.$cancelAll('getPackageExtraInfo');
    }

    cancelVersionExtraInfo() {
        this.packagesDAO.$cancelAll('getVersionExtraInfo');
    }

    getPackageDownloadsCount(params) {
        return this.packagesDAO.getPackageDownloadsCount(_.extend(params.pathAndQuery, {$no_spinner: true})).$promise;
    }

    getPackageDownloadsCountMds(params) {
        return this.$q((resolve, reject) => {
            this.resolvePackageDownloadCount = resolve;
        })
    }

    getVersionDownloadsCount(params) {
        return this.packagesDAO.getVersionDownloadsCount(_.extend(params.pathAndQuery, {$no_spinner: true})).$promise;
    }

    getVersionDownloadsCountMds(params) {
        return this.$q((resolve, reject) => {
            if (this.mdsCache.versions) {
                const {versionName, packageName, packageType} = params.pathAndQuery;
                const version = this.findVersionByNameTypeVersion(packageType, packageName, versionName);
                resolve({
                    totalDownloads: version.totalDownloads
                })
            }
        })
    }

    getManifest(params) {
        return this.packagesDAO.getManifest(params.pathAndQuery).$promise;
    }

    getVersion(params) {
        return this.packagesDAO.getVersion(params.pathAndQuery).$promise;
    }

    getVersionMds(params) {
        const {versionName, packageName, packageType} = params.pathAndQuery;
        return new Promise((resolve, reject) => {
            if (!this.mdsCache.versions) {
                this.getPackageMds(params)
                    .then(() => {
                        const versionData = this.findVersionByNameTypeVersion(packageType, packageName, versionName);
                        if (versionData) {
                            return resolve(versionData);
                        }
                    })
            }
            else {
                const versionData = this.findVersionByNameTypeVersion(packageType, packageName, versionName);
                if (versionData) {
                    return resolve(versionData);
                }
            }
        })
    }

    findVersionByNameTypeVersion(packageType, packageName, versionName) {
        const versionsKey = packageType === 'docker' ? 'versions' : 'results';
        return this.mdsCache.versions[versionsKey].find(v => v.name === versionName);
    }

    getVersionData(params) {
        return this.packagesDAO.getVersionData(_.extend(params.pathAndQuery, {$no_spinner: true})).$promise;
    }

    getVersionSummary(params) {
        return this.packagesDAO.getVersionSummary(params.pathAndQuery, params.payload).$promise;
    }

    getVersionSummaryExtraInfo(params) {
        return this.packagesDAO.getVersionSummaryExtraInfo(_.extend(params.pathAndQuery, {$no_spinner: true}),
            params.payload).$promise;
    }

    getVersionSummaryExtraInfoMds(params) {
        return this.$q((resolve, reject) => {
            this.getPackageMds(params)
                .then(() => {
                    const {versionName, packageName, packageType} = params.pathAndQuery;
                    const versionData = this.findVersionByNameTypeVersion(packageType, packageName, versionName);
                    if (versionData) {
                        return resolve({
                            license: versionData.licenses,
                            numOfDownloads: (versionData.stats && versionData.stats.download_count) ? versionData.stats.download_count : 0
                        });
                    }
                })
        })
    }

    // TODO: When the product team will decide on phase 2 (adding xray) , replace the 'false' value
    isXrayEnabled() {
        return this.footerDao.get(true).then((footerData) => {
            return footerData.xrayEnabled && footerData.xrayConfigured && footerData.xrayLicense;
        });
    }

    isMdsEnabled() {
        return new Promise((resolve, reject) => {
            if (this.useMdsApi === null) {
                return this.footerDao.get(true).then((footerData) => {
                    this.useMdsApi= !!footerData.mdsPackageNativeUI;
                    resolve(this.useMdsApi);
                });
            }
            else {
                resolve(this.useMdsApi);
            }
        })
    }

    showExtraInfo() {
        //		return new Promise.when({showExtraInfo:true})
        return this.packagesDAO.showExtraInfo().$promise;
    }

    showExtraInfoMds() {
        //		return new Promise.when({showExtraInfo:true})
        return new Promise((res) => res({showExtraInfo: true}));
    }

    showInTree(pathParams) {
        let browser = this.artifactoryStorage.getItem('BROWSER') || 'tree';
        if (browser === 'stash') {
            browser = 'tree';
        }

        let path = pathParams.fullpath || `${pathParams.repo}/${pathParams.package}/${pathParams.version}`;

        this.$state.go('artifacts.browsers.path', {
            tab: 'General',
            artifact: path,
            browser: browser
        });
    }
}
