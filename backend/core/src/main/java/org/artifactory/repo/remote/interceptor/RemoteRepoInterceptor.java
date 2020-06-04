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

package org.artifactory.repo.remote.interceptor;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.repo.HttpRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.HeadersMultiMap;
import org.artifactory.request.Request;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Gidi Shabat
 */
public interface RemoteRepoInterceptor {

    /**
     * Called just before an attempt to download a resource from remote repository. All the interceptors must return
     * true in order for the download to proceed.
     *
     * @param descriptor     The remote repository descriptor
     * @param remoteRepoPath Repo path of the remote resource
     * @return True if downloading the specified resource is allowed
     */
    default boolean isRemoteDownloadAllowed(RemoteRepoDescriptor descriptor, RepoPath remoteRepoPath) {
        return true;
    }

    /**
     * Called at the end of a successful remote download
     */
    default void afterRemoteDownload(RepoResource remoteResource) throws RepoRejectException {
        //noop
    }

    /**
     * Provides a chance to modify a remote request and it's headers before the execution in
     * {@link HttpRepo#retrieveInfo} and {@link HttpRepo#downloadResource}
     * @param outgoingRequest The request about to get executed by the remote repo
     * @param incomingRequest The client request that initiated this remote download flow
     * @param headers The request headers
     * @param repoPath
     */
    default void beforeRemoteHttpMethodExecution(HttpRequestBase outgoingRequest, @Nullable Request incomingRequest,
            HeadersMultiMap headers, RepoPath repoPath) {
        // noop by default
    }

    /**
     * Provides a chance to populate checksum information from a {@link HttpRepo#retrieveInfo} response.
     * insert checksum info into {@param checksums} and take care! only 'original' checksum information is plausible
     * and regarded at this point.
     *
     * This interception is only called if the response didn't contain any Standard checksum headers
     * {@link ArtifactoryRequest#CHECKSUM_SHA1} {@link ArtifactoryRequest#CHECKSUM_SHA256} {@link ArtifactoryRequest#CHECKSUM_MD5}
     */
    default void resolveChecksumInformation(RepoPath repoPath, CloseableHttpResponse response, Set<ChecksumInfo> checksums) {
        //noop
    }

    /**
     * Allows adding additional non-default urls to the blacklist that governs whether or not sync property requests
     * are executed.
     * see {@link HttpRepo#isSynchronizeProperties}
     */
    default void addAdditionalDefaultUrlsToReplicationBlacklist(Set<String> defaultUrls) {
        //noop
    }
}
