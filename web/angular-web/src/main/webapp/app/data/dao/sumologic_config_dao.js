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
export function SumoLogicConfigDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.SUMOLOGIC + '/:action')
        .setCustomActions({
            'registerSumoLogicApplication': {
                method: 'POST',
                params: {action: 'registerSumoLogicApplication'},
                notifications: true
            },
            'setupSumoLogicApplication': {
                method: 'POST',
                params: {action: 'setupSumoLogicApplication'},
                notifications: true
            },
            'refreshToken': {
                method: 'POST',
                params: {action: 'refreshToken'},
                notifications: true
            },
            'reset': {
                method: 'POST',
                params: {action: 'reset'},
                notifications: true
            }
        })
        .getInstance();
}