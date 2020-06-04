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

export function SetMeUpDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.SET_ME_UP)
        .setCustomActions({
            fetch: {
                notifications: true
            },
            maven : {
                path : RESOURCE.SET_ME_UP_MAVEN
            },
            gradle : {
                path : RESOURCE.SET_ME_UP_GRADLE
            },
            ivy : {
                path : RESOURCE.SET_ME_UP_IVY
            },
            maven_distribution : {
              path : RESOURCE.SET_ME_UP_MAVEN_DISTRIBUTION,
              method : "GET"
            },
            maven_snippet : {
                path : RESOURCE.SET_ME_UP_MAVEN_SNIPPET,
                method : "POST"
            },
            gradle_snippet : {
                path : RESOURCE.SET_ME_UP_GRADLE_SNIPPET,
                method : "POST"
            },
            ivy_snippet : {
                path : RESOURCE.SET_ME_UP_IVY_SNIPPET,
                method : "POST"
            },
            reverse_proxy_data : {
                path : RESOURCE.SET_ME_UP_REVERSE_PROXY_DATA,
                method : "GET"
            }
        }).getInstance();
}