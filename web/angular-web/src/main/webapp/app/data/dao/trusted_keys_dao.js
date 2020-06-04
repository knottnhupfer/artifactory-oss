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

export function TrustedKeysDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.TRUSTEDKEYS)
        .setCustomActions({
            'getTrustedKeys': {
                method: 'GET',
	            isArray: true
            },
            'AddTrustedKey': {
                method: 'POST',
                params: {'public_key': '@key', 'alias': '@alias'},
	            notifications: true
            },
            'deleteTrustedKey': {
            	path: RESOURCE.TRUSTEDKEYS + '/delete',
	            method: 'POST',
	            params: {'key_id': '@key_id'},
	            notifications: true
            }
        })
        .getInstance();
}