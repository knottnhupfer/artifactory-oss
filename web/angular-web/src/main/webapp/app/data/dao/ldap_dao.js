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

export function LdapDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.LDAP + "/:action/:key")
            .setCustomActions({
                'save':{
                    method: 'POST'
                },
                'get':{
                    method: 'GET',
                    params: {key: '@key'}
                },
                'update':{
                    method: 'PUT',
                    params: {key: '@key'}
                },
                'delete':{
                    method: 'DELETE',
                    params: {key: '@key'}
                },
                'test':{
                    method: 'POST',
                    params: {key: '@key', action: 'test'},
                    notifications: true
                },
                'reorder':{
                    method: 'POST',
                    path: RESOURCE.LDAP + "/reorder"
                }
            })
            .getInstance();
}