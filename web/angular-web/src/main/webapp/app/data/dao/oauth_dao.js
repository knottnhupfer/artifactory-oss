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

export function OAuthDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.OAUTH + "/:p1/:p2/:p3/:p4")
            .setCustomActions({
                'get':{
                    method: 'GET'
                },
                'update':{
                    method: 'POST',
                    notifications: true
                },
                'createProvider':{
                    method: 'PUT',
                    params: {p1: 'provider'},
                    notifications: true
                },
                'updateProvider':{
                    method: 'POST',
                    params: {p1: 'provider'},
                    notifications: true
                },
                'deleteProvider':{
                    method: 'DELETE',
                    params: {p1: 'provider', p2: '@provider'},
                    notifications: true
                },
                'getUserTokens':{
                    method: 'GET',
                    isArray: true,
                    params: {p1: 'user', p2: 'tokens'}
                },
                'deleteUserToken':{
                    method: 'DELETE',
                    params: {p1: 'user', p2: 'tokens', p3: '@username', p4: '@provider'}
                }
            })
            .getInstance();
}