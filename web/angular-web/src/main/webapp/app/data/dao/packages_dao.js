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
export class PackagesDAO {
    constructor(RESOURCE, ArtifactoryDaoFactory) {
        let path  = `${RESOURCE.API_URL}/v1/native`;
        let pathV2  = `${RESOURCE.API_URL}/v2/native`;
        return ArtifactoryDaoFactory()
                .setPath(path)
                .setCustomActions({
                    'showExtraInfo': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/packages/show_extra_info`,
                    },
                    'getPackages': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType`,
                    },
                    'getPackagesMds': {
                        method: 'POST',
                        notifications: false,
                        url: `${pathV2}/packages/:packageType`,
                    },
                    'getPackagesCount': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType/count_packages`,
                        params: {
                            packageType: '@packageType',
                        },
                    },
                    'getRepos': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/repos/:packageType`,
                        params: {
                            packageType: '@packageType',
                        },
                    },
                    'getPackage': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/versions/:packageType`,
                        params: {
                            packageType: '@packageType',
                        }
                    },

                    'getPackageMds': {
                        method: 'POST',
                        notifications: false,
                        url: `${pathV2}/versions/:packageType`,
                        params: {
                            packageType: '@packageType',
                        }
                    },
                    'getPackageSummary': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType/summary`,
                        params: {
                            packageType: '@packageType',
                        }
                    },
                    'getPackageSummaryExtraInfo': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType/summary/extra_info`,
                        params: {
                            packageType: '@packageType',
                        }
                    },
                    'getPackageDownloadsCount': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/packages/:packageType/total_downloads`,
                        params: {
                            packageType: '@packageType'
                        }
                    },
                    'getDockerPackageExtraInfo': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/packages/:packageType/extra_info`,
                        params: {
                            packageType: '@packageType'
                        }
                    },
                    'getPackageExtraInfo': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/packages/:packageType/extra_info`,
                        params: {
                            packageType: '@packageType'
                        },
                        cancellable: true
                    },
                    'getVersionDownloadsCount': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/:packageType/total_downloads/:repo`,
                        params: {
                            packageType: '@packageType',
	                        repo: '@repo'
                        }
                    },
                    'getVersion': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/:packageType/:repo`,
                        params: {
                            packageType: '@packageType',
                            repo: '@repo'
                        },
                        urlOptions: {encodeUri: false}
                    },
                    'getVersionMds': {
                        method: 'GET',
                        notifications: false,
                        url: `${pathV2}/versions/:packageType/:repo`,
                        params: {
                            packageType: '@packageType',
                            repo: '@repo'
                        },
                        urlOptions: {encodeUri: false}
                    },
                    'getVersionData': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/:packageType/:dataType`,
                        params: {
                            packageType: '@packageType',
                            dataType: '@dataType'
                        }
                    },
                    'getVersionExtraInfo': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/versions/:packageType/extra_info`,
                        params: {
                            packageType: '@packageType'
                        },
                        cancellable: true
                    },
                    'getVersionSummary': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/versions/:packageType/summary`,
                        params: {
                            packageType: '@packageType',
                        }
                    },
                    'getVersionSummaryExtraInfo': {
                        method: 'POST',
                        notifications: false,
                        url: `${path}/versions/:packageType/summary/extra_info`,
                        params: {
                            packageType: '@packageType',
                        }
                    },
                    'getManifest': {
                        method: 'GET',
                        notifications: false,
                        url: `${path}/versions/:packageType/:repo`,
                        params: {
                            packageType: '@packageType',
                            repoKey: '@repo'
                        },
                    }
                })
                .getInstance();
    }
}

