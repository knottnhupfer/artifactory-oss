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

export function ArtifactSearchDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.ARTIFACT_SEARCH + '/:search/:action')
            .setCustomActions({
                'fetch': {
                    params: {search: '@search'},
                    notifications:true
                },
                'get': {
                    method: 'GET',
                    isArray: true,
                    params: {
                        action: '@action',
                        search: '@search'
                    },
                    notifications:true
                },
                'delete': {
                    method: 'POST',
                    params: {search: 'deleteArtifact'}
                }
            })
            .getInstance();
}