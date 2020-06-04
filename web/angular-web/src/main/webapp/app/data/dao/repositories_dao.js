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
export function RepositoriesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.REPOSITORIES)
            .setCustomActions({
                getRepositories: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/:type/info',
                    isArray: true,
                    params: {type: '@repoType'}
                },
                getRepository: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/:type/:repoKey',
                    params: {type: '@repoType', repoKey: '@repoKey'}
                },
                deleteRepository: {
                    method: 'DELETE',
                    path: RESOURCE.REPOSITORIES + '/:repoKey/delete',
                    params: {repoKey: '@repoKey'},
                    notifications: true
                },
                getAvailableChoicesOptions: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/availablechoices'
                },
                getDefaultValues: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/defaultvalues'
                },
                repoKeyValidator: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/validatereponame'
                },
                testRemoteUrl: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testremote',
                    notifications: true
                },
                detectSmartRepository: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/discoversmartrepocapabilities',
                },
                testLocalReplication: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testlocalreplication',
                    notifications: true
                },
                testRemoteReplication: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testremotereplication',
                    notifications: true
                },
                executeReplicationNow: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/executereplicationnow',
                    params: {replicationUrl: '@replicationUrl', repoKey: '@repoKey'},
                    notifications: true
                },
                executeRemoteReplicationNow: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/exeucteremotereplication',
                    params: {replicationUrl: '@replicationUrl', repoKey: '@repoKey'},
                    notifications: true
                },
                runNowReplications: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/executeall', params: {repoKey: '@repoKey'},
                    notifications: true
                },
                remoteUrlToRepoMap: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/remoteUrlMap'
                },
                availableRepositoriesByType: {
                    method: 'GET',
                    params: {type: '@type', repoKey: '@repoKey'},
                    path: RESOURCE.REPOSITORIES + '/availablerepositories'
                },
                indexerAvailableRepositories: {
                    method: 'GET',
                    params: {type: '@type', layout: '@layout'},
                    path: RESOURCE.REPOSITORIES + '/indexeravailablerepositories'
                },
                getResolvedRepositories: {
                    method: 'POST',
                    isArray: true,
                    params: {type: '@repoType', repoKey: '@repoKey'},
                    path: RESOURCE.REPOSITORIES + '/resolvedrepositories'
                },
                isReplicationValid: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/validatelocalreplication',
                    notifications: true
                },
                reorderRepositories: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/:repoType/reorderrepositories',
                    params: {
                        repoType: '@repoType',
                        $no_spinner: true
                    }
                },
                createDefaultJcenterRepo: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/createdefaultjcenterrepo',
                    notifications: true,
                    params: {
                        $no_spinner: false
                    }
                },
                isJcenterRepoConfigured: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/isjcenterconfigured',
                    notifications: false,
                    params: {
                        $no_spinner: true
                    }
                },
                saveBintrayOauthConfig: {
                    method: 'PUT',
                    path: RESOURCE.REPOSITORIES + '/savebintrayoauthconfig',
                    notifications: true
                },
                checkBintrayAuthentication: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/checkbintrayrepauth',
                    notifications: false
                },
                testDistributionRules: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testdistributionrule',
                    notifications: true,
                    params: {testPath : '@testPath', productName: '@productName'}
                }
            })
            .extendPrototype({
                isType: function (...types) {
                    return this.typeSpecific && this.typeSpecific.repoType && _.contains(types,
                                    this.typeSpecific.repoType.toLowerCase());
                },
                isGitProvider: function (gitProvider) {
                    return this.typeSpecific && this.typeSpecific.gitProvider && gitProvider == this.typeSpecific.gitProvider.toLowerCase();
                }
            })
            .getInstance();
}
