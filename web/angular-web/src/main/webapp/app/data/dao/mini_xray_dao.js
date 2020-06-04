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
export function MiniXrayDao(RESOURCE, ArtifactoryDaoFactory) {
	return ArtifactoryDaoFactory()
    	.setPath(RESOURCE.MINI_XRAY + "/:action")
        .setCustomActions({
            'getSecurityVulnerabilites': {
                method : 'post',
                path:'/jcr/xray/paginatedIssues?direction=asc&num_of_rows=1000&order_by=version&page_num=1',
                params : {action: 'paginatedIssues?direction=asc&num_of_rows=1000&order_by=version&page_num=1'}
            },
            'getSecurityImpactGraph': {
                method : 'post',
                params : {action: 'impactPath'}
            },
            'getSecurityDetails': {
                method : 'post',
                params : {action: 'issueDetails'}
            },
        })
        .getInstance();
}
