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
export function XrayDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.XRAY + '/:action')
            .setCustomActions({
                'getNoneIndex': {
                    method: 'GET',
                    params: {action: 'getNoneIndex'},
                    isArray: true
                },
                'getIndex': {
                    method: 'GET',
                    params: {action: 'getIndex'},
                    isArray: true
                },
                'addIndex': {
                    method: 'POST',
                    params: {action: 'addIndex'}
                },
                'removeIndex': {
                    method: 'POST',
                    params: {action: 'removeIndex'}
                },
                'updateRepositories': {
                    method: 'PUT',
                    params: {action: 'indexRepos'}
                },
                'getConf': {
                    path: RESOURCE.XRAY_CONFIG,
                    method: 'GET'
                },
                'getIntegrationConfig': {
                    method: 'GET',
                    params: {action: 'getIntegrationConfig'}
                },
                'setXrayEnabled': {
                    method: 'POST',
                    params: {action: 'setXrayEnabled'}
                },
                'xrayAllowWhenUnavailable': {
                    method: 'POST',
                    params: {action: 'setAllowWhenUnavailable'}
                },
                'xrayAllowBlocked': {
                    method: 'POST',
                    params: {action: 'setAllowBlockedArtifactsDownload'}
                },
                'setBypassDefaultProxy': {
                    method: 'POST',
                    params: {action: 'setBypassDefaultProxy'}
                },
                'blockUnscannedTimeout': {
                    method: 'POST',
                    params: {action: 'setBlockUnscannedArtifactsDownloadTimeout', seconds: '@seconds'},
                    notifications: true
                },
                'updateXrayProxy': {
                    method: 'POST',
                    params: {action: 'updateProxy', proxyKey: 'proxyKey'}
                }
            })
            .getInstance();
}