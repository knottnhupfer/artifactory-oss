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
export function RepositoriesLayoutsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.REPOSITORIES_LAYOUTS + '/:action/:name')
        .setCustomActions({
            getLayouts: {
                method: 'GET',
                isArray: true
            },
            getLayoutData: {
                method: 'GET',
                params: {name: '@layoutName'}
            },
            deleteLayout: {
                method: 'DELETE',
                params: {name: '@layoutName'},
                notifications: true
            },
            testArtifactPath: {
                method: 'POST',
                params: {action: 'testArtPath'},
                notifications: true
            },
            resolveRegex: {
                method: 'POST',
                params: {action: 'resolveRegex'}
            }

        })
        .getInstance();
}
