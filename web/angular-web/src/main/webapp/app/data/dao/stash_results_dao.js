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
export function StashResultsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'GET'})
            .setPath(RESOURCE.STASH_RESULTS)
            .setCustomActions({
                'get': {
                    method: 'GET',
                    isArray: true
                },
                'save': {
                    method: 'POST',
                    notifications: true
                },
                'delete': {
                    method: 'DELETE'
                },
                'add': {
                    path: RESOURCE.STASH_RESULTS + "/add",
                    notifications: true,
                    method: 'POST'
                },
                'subtract': {
                    path: RESOURCE.STASH_RESULTS + "/subtract",
                    notifications: true,
                    method: 'POST'
                },
                'intersect': {
                    path: RESOURCE.STASH_RESULTS + "/intersect",
                    notifications: true,
                    method: 'POST'
                },
                'export': {
                    path: RESOURCE.STASH_RESULTS + "/export",
                    notifications: true,
                    method: 'POST'
                },
                'discard': {
                    path: RESOURCE.STASH_RESULTS + "/discard",
                    notifications: true,
                    method: 'POST'
                },
                'copy': {
                    path: RESOURCE.STASH_RESULTS + "/copy",
                    notifications: true,
                    method: 'POST'
                },
                'move': {
                    path: RESOURCE.STASH_RESULTS + "/move",
                    notifications: true,
                    method: 'POST'
                },
                'silentCopy': {
                    path: RESOURCE.STASH_RESULTS + "/copy",
                    method: 'POST'
                },
                'silentMove': {
                    path: RESOURCE.STASH_RESULTS + "/move",
                    method: 'POST'
                }
            })
            .getInstance();
}