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

package org.artifactory.api.request;

import org.artifactory.repo.RepoPath;

/**
 * An internal factory for creating {@link InternalArtifactoryRequest} objects.
 *
 * Read the documentation carefully. Always ask someone if you are not sure.
 *
 * @author Yuval Reches
 */
public abstract class InternalRequestFactory {

    /**
     * Use this if the request is made for internal usage (indexing, extracting, get info, etc.)
     *
     * if a repository is configured with redirect download requests --> the specific request won't be redirected.
     * Stream will be handled instead.
     *
     * See DownloadServiceImpl#shouldSendArtifactDownloadRedirect for the full list of conditions for redirect
     */
    public static InternalArtifactoryRequest createInternalRequestDisableRedirect(RepoPath repoPath) {
        return new InternalArtifactoryRequest(repoPath, true);
    }

    /**
     * Use this if the request is made for external usage (serving the response directly to a client)
     *
     * Note:
     * The response.getStatus might not be HTTP OK 200 if an artifact exits.
     * If redirect is enabled the status will be HTTP 302 with 'location' header.
     *
     * if a repository is configured with redirect download requests --> the specific request will be redirected.
     * Otherwise it will be served as stream.
     *
     * See DownloadServiceImpl#shouldSendArtifactDownloadRedirect for the full list of conditions for redirect
     */
    public static InternalArtifactoryRequest createInternalRequestEnableRedirect(RepoPath repoPath) {
        return new InternalArtifactoryRequest(repoPath, false);
    }
}
