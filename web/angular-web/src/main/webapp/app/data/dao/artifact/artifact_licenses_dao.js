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

export function ArtifactLicensesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.GENERAL_TAB_LICENSES + '/:action')
        .setCustomActions({
            'getLicenses': {
                method: 'GET',
                isArray: true
            },
            setLicenses: {
                method: 'PUT',
                isArray: false,
                path: RESOURCE.GENERAL_TAB_LICENSES + '/setLicensesOnPath',
                notifications: true
            },
            scanArtifact: {
                method: 'GET',
                isArray: true,
                path: RESOURCE.GENERAL_TAB_LICENSES + '/scanArtifact',
                notifications: true
            },
            queryCodeCenter: {
                method: 'POST',
                params: {
                    repoKey: '@repoKey',
                    path: '@path'
                },
                path: RESOURCE.GENERAL_TAB_LICENSES + '/queryCodeCenter',
                notifications: true
            },
            getArchiveLicenseFile: {
                method: 'GET',
                path: RESOURCE.GENERAL_TAB_LICENSES + '/getArchiveLicenseFile',
                transformResponse:(data)=>{ return {data: data} }
            }
        })
        .getInstance();
}