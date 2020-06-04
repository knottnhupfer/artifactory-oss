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

package org.artifactory.addon.docker;

import com.jfrog.bintray.client.api.BintrayCallException;
import org.artifactory.addon.Addon;
import org.artifactory.addon.docker.rest.DockerTokenCacheKey;
import org.artifactory.api.bintray.docker.BintrayDockerPushRequest;
import org.artifactory.repo.RepoPath;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author Shay Yaakov
 */
public interface DockerAddon extends Addon {

    /**
     * clean docker temp "_uploads" folder that has files that are older than 1 day
     */
    default void cleanup(String repoId, String uploadsPath, boolean async) {

    }

    /**
     * search for docker temp folders ("_uploads") and initiate cleanup
     * for files that are older than 1 day
     */
    default void searchAndCleanupTempFolders(String repoId) {

    }


    /**
     * Pushes the image:tag from {@param repoKey} given in {@param request} to Bintray, can optionally use a
     * distribution repository's client instead of a client that's created for the current user
     * if {@param distRepoKey} is specified.
     */
    default void pushTagToBintray(String repoKey, BintrayDockerPushRequest request, @Nullable String distRepoKey)
            throws BintrayCallException {
    }

    default DockerV2InfoModel getDockerV2Model(RepoPath manifestPath, boolean convertSizeToHumanReadable)
            throws IOException {
        return null;
    }

    /**
     * Retrieves a new auth token for the {@param tokenCacheKey} passed to it.
     */
    default String fetchDockerAuthToken(DockerTokenCacheKey tokenCacheKey) {
        return null;
    }

    /**
     * @param repoKey   Repo to promote
     * @param promotion Contains all relevant promotion details
     */
    default void dockerV2promote(String repoKey, DockerV2Promotion promotion) {
    }
}