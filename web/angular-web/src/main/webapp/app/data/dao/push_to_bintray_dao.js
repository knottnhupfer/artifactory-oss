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

export function PushToBintrayDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.PUSH_TO_BINTRAY + "/:param1/:param2/:param3/:param4")
        .setCustomActions({
            'getBuildRepos': {
                method: 'GET'
            },
            'getBuildPacks': {
                method: 'GET',
                params: {param1: 'build', param2: 'pkg'}
            },
            'getBuildVersions': {
                method: 'GET',
                params: {param1: 'build', param2: 'versions'}
            },
            'pushBuildToBintray': {
                method: 'POST',
                notifications: true,
                params: {param1: 'build', param2: '@buildName', param3: '@buildNumber', param4: '@buildTime'}
            },
            'getArtifactData': {
                method: 'GET',
                params: {param1: 'artifact'}
            },
            'pushArtifactToBintray': {
                method: 'POST',
                notifications: true,
                params: {param1: 'artifact', param2: '@repoKey', param3: '@path'}
            },
            'pushDockerTagToBintray': {
                method: 'POST',
                notifications: true,
                params: {param1: 'dockerTag', param2: '@repoKey', param3: '@path'}
            }
        })
        .getInstance();
}