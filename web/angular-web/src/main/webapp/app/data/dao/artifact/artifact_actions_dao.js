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

export function ArtifactActionsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.ARTIFACT_ACTIONS + '/:action')
        .setCustomActions({
                perform: {
                    params: {action: '@action'},
                    notifications: true
                },
                performGet: {
                    method: 'GET',
                    params: {action: '@action'},
                    notifications: true
                },
                dryRun: {
                    params: {action: '@action'}
                },
                getDeleteVersions: {
                    method: 'GET',
                    path: RESOURCE.ARTIFACT_ACTIONS + '/deleteversions',
                    isArray: false,
                    notifications: true
                },
                getSha256: {
                    method: 'POST',
                    path: RESOURCE.ARTIFACT_ACTIONS + '/addSha256',
                    notifications: true
                }
        }).getInstance();
}
