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
import {ArtifactoryDao} from "../artifactory_dao";

export class UserDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.USERS + '/:prefix/:name');

        this.setCustomActions({
            'getAll': {
                method: 'GET',
                isArray: true
            },
            'getSingle': {
                method: 'GET',
                params: {name: '@name'},
                notifications: true
            },
            'create': {
                method: 'POST',
                notifications: true
            },
            'update': {
                method: 'PUT',
                params: {name: '@name'},
                notifications: true
            },
            'delete': {
                method: 'POST',
                params: {prefix: 'userDelete'},
                notifications: true
            },
            'getAllGroups': {
                method: 'GET',
                params: {prefix: 'groups'},
                isArray: true
            },
            'getPermissions': {
                method: 'GET',
                params: {name: '@name', prefix: 'permissions'},
            },
            'checkExternalStatus': {
                method: 'POST',
                params: {prefix: 'externalStatus'},
                notifications: true
            },
            'changePassword': {
                path: '/auth/changePassword',
                method: 'POST',
                params: {prefix: 'changePassword'},
                notifications: true
            },
            'expirePassword': {
                method: 'POST',
                params: {prefix: '@username', name: 'expirePassword'},
                notifications: true
            },
            'unExpirePassword': {
                method: 'POST',
                params: {prefix: '@username', name: 'unexpirePassword'},
                notifications: true
            },
            'expireAllPassword': {
                method: 'POST',
                params: {prefix: 'expirePasswordForAllUsers'},
                notifications: true
            },
            'unExpireAllPassword': {
                method: 'POST',
                params: {prefix: 'unexpirePasswordForAllUsers'},
                notifications: true
            }

        })
    }
}
