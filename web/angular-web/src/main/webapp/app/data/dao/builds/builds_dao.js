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
export function BuildsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()

        .setPath(RESOURCE.BUILDS + "/:action/:subAction/:name/:number/:time/:moduleId")
        .setCustomActions({

            getData: {
                method: 'GET',
                isArray: false,
                params: {action: '@action', subAction: '@subAction',name: '@name', number: '@number', time: '@time', moduleId: '@moduleId'}
            },
            lastBuild: {
                method: 'GET',
                isArray: false,
                params: {action: 'buildInfo', name: '@name', number: '@number'}
            },
            getDataArray: {
                method: 'GET',
                isArray: true,
                params: {action: '@action', subAction: '@subAction', name: '@name', number: '@number', time: '@time', moduleId: '@moduleId'}
            },
            delete:{
                method: 'POST',
                notifications: true,
                params: {action: 'buildsDelete'}
            },
            deleteAll:{
                method: 'POST',
                notifications: true,
                params: {action: 'deleteAllBuilds'}
            },
            overrideLicenses:{
                method: 'PUT',
                params: {action: 'overrideLicenses', name: '@name', number: '@number', time: '@time'}
            },
            buildEffectivePermission: {
                method: 'GET',
                params: {action: 'buildEffectivePermission', name: '@name', number: '@buildNumber', time: '@startTime'}
            },
            getAllBuildsNames: {
                method: 'GET',
                isArray: true,
                params: {action: 'names'}
            }
        })
        .getInstance();
}