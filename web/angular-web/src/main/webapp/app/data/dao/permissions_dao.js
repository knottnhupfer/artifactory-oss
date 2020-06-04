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
import {ArtifactoryDao} from '../artifactory_dao';

export class PermissionsDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor)
    {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.PERMISSION_TARGETS + '/:action/:name');

        this.setCustomActions({
            getAll: {
                method: 'GET'
            },
            getEntity:{
                method: 'GET',
                params:{action:'@action',name:'@name'},
                isArray: true
            },
            getResource:{
                method: 'GET',
                params:{action:'@action',name:'@name'},
            },
            getAllUsersAndGroups: {
                method: 'GET',
                params: {action: 'allUsersGroups'}
            },
            getPermission: {
                method: 'GET',
                params: {name: '@name'}
            },
            deletePermission: {
                method: 'POST',
                params: {action:'delete'},
                notifications: true
            },
            update: {
                method: 'PUT',
                params: {name: '@name'}
            },
            create: {
                method: 'POST',
                notifications: true
            },
            getEntityPermissions: {
                method: 'POST',
                isArray: true,
                params: {action: '@entity_type',name: '@name'}
            },
            buildPatterns: {
                method: 'POST',
                params: {action: 'buildPatterns'}
            },
            getBuildGlobalBasicReadAllowed: {
                method: 'GET',
                params: {action: 'getBuildGlobalBasicReadAllowed'}
            }
        });
    }
}