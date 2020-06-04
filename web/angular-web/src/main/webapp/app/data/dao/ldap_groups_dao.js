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

export function LdapGroupsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.LDAP_GROUPS + "/:name/:action/:username")
            .setCustomActions({
                'get':{
                    params: {name: '@name'}
                },
                'update':{
                    params: {name: '@name'}
                },
                'delete':{
                    params: {name: '@name'}
                },
                'refresh':{
                    method: 'POST',
                    isArray: true,
                    params: {name: '@name', action: 'refresh', username: '@username'},
                    notifications: true
                },
                'import':{
                    method: 'POST',
                    params: {name: '@name', action: 'import'},
                    notifications: true
                },
                'getstrategy':{
                    method: 'GET',
                    params: {name: '@name', action: 'strategy'}
                }
            })
            .getInstance();
}