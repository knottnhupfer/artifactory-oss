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
export function BundlesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.BUNDLES + "/:type/:name/:version")
        .setCustomActions({
            getData: {
                method: 'GET',
                isArray: false,
                params: {type : '@type', name: '@name'}
            },
            getBundleVersions: {
                method: 'GET',
                isArray: false,
                params: {type : '@type', name: '@name'}
            },
            getBundleData: {
                method: 'GET',
                isArray: false,
                params: {type : '@type', name: '@name', version: '@version'}
            },
            deleteBundles: {
                method: 'DELETE',
                isArray: false,
                params: {type : '@type', name: '@name', version: '@version'}
            }
        })
        .getInstance();
}
