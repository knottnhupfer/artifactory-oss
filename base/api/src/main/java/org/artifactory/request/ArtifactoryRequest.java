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

/**
 * @author Fred Simon
 */
package org.artifactory.request;

import org.artifactory.checksum.ChecksumType;

public interface ArtifactoryRequest extends Request {
    @Deprecated
    String ORIGIN_ARTIFACTORY = "Origin-Artifactory";

    String ARTIFACTORY_ORIGINATED = "X-Artifactory-Originated";

    String ARTIFACTORY_OVERRIDE_BASE_URL = "X-Artifactory-Override-Base-Url";

    String JFROG_OVERRIDE_BASE_URL = "X-JFrog-Override-Base-Url";

    String CHECKSUM_SHA1 = "X-Checksum-Sha1";

    String CHECKSUM_SHA256 = "X-Checksum-Sha256";

    //Should be used for incoming values, not sure there's a lot of sense in sending this out?
    String CHECKSUM = "X-Checksum";

    String CHECKSUM_MD5 = "X-Checksum-Md5";

    String ACCEPT_RANGES = "Accept-Ranges";

    String FILE_NAME = "X-Artifactory-Filename";

    /**
     * An header to trigger checksum deploy (when the value is true). Request must also include
     * {@link org.artifactory.request.ArtifactoryRequest#CHECKSUM_SHA1}.
     */
    String CHECKSUM_DEPLOY = "X-Checksum-Deploy";

    /**
     * if set to true - will retrieve the actual binary from the binary store (if it exists)
     */
    String CHECK_BINARY_EXISTENCE_IN_FILESTORE = "X-Check-Binary-Existence-In-Filestore";

    /**
     * Header to trigger bundle archive deployment (supports zip/tar/tar.gz)
     */
    String EXPLODE_ARCHIVE = "X-Explode-Archive";

    String EXPLODE_ARCHIVE_ATOMIC = "X-Explode-Archive-Atomic";

    String RESULT_DETAIL = "X-Result-Detail";

    String PARAM_SKIP_JAR_INDEXING = "artifactory.skipJarIndexing";

    String PARAM_FORCE_DOWNLOAD_IF_NEWER = "artifactory.forceDownloadIfNewer";

    /**
     * Causes the download service to ignore repository configuration for download redirect
     */
    String PARAM_FORCE_GET_STREAM = "artifactory.disableRedirect";

    String PARAM_SEARCH_FOR_EXISTING_RESOURCE_ON_REMOTE_REQUEST = "artifactory.searchForExistingResourceOnRemoteRequest";

    String PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL = "artifactory.alternativeRemoteDownloadUrl";

    String PARAM_ALTERNATIVE_REMOTE_SITE_URL = "artifactory.alternativeRemoteSiteUrl";

    String PARAM_REPLICATION_DOWNLOAD_REQUEST = "artifactory.replicationDownloadRequest";

    /**
     * Signifies request seems to have originated in another Artifactory and was directed at a package api replication endpoint
     */
    String PARAM_REPLICATION_ORIGINATED_DOWNLOAD_REQUEST = "artifactory.replicationOriginatedDownloadRequest";

    String PARAM_FOLDER_REDIRECT_ASSERTION = "artifactory.disableFolderRedirectAssertion";

    /**
     * Will replace the HEAD request in RetrieveInfo with a GET request
     */
    String PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET = "artifactory.replaceHeadInRetrieveInfoWithGet";

    /**
     * Will skip the encoding of the alternative remote download url, assumes it's already encoded
     */
    String PARAM_SKIP_ENCODING = "artifactory.skipEncoding";

    /**
     * The path prefix name for list browsing.
     */
    String LIST_BROWSING_PATH = "list";

    /**
     * The path prefix name for simple browsing.
     */
    String SIMPLE_BROWSING_PATH = "simple";

    String LAST_MODIFIED = "X-Artifactory-Last-Modified";

    String CREATED = "X-Artifactory-Created";

    String MODIFIED_BY = "X-Artifactory-Modified-By";

    String CREATED_BY = "X-Artifactory-Created-By";

    String getRepoKey();

    String getPath();

    boolean isMetadata();

    /**
     * Indicates whether the request is coming back to the same proxy as a result of reverse mirroring
     */
    boolean isRecursive();

    long getModificationTime();

    String getName();

    /**
     * Indicates whether the request if for a directory instead of a file
     *
     * @return True if the request uri if for a directory
     */
    boolean isDirectoryRequest();

    static String headerForChecksum(ChecksumType checksumType) {
        switch (checksumType) {
            case sha1:
                return CHECKSUM_SHA1;
            case sha256:
                return CHECKSUM_SHA256;
            case md5:
                return CHECKSUM_MD5;
            default:
                return "";
        }
    }
}
