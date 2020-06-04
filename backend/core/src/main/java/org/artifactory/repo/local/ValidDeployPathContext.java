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

package org.artifactory.repo.local;

import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;

/**
 * Object containing method params to use in {@link org.artifactory.repo.service.InternalRepositoryService#assertValidDeployPathAndPermissions(ValidDeployPathContext)}
 *
 * @author Shay Yaakov
 */
public class ValidDeployPathContext {

    private LocalRepo repo;
    private RepoPath repoPath;
    private long contentLength;
    private String requestSha1;
    private String requestSha2;
    private boolean forceExpiryCheck;

    private ValidDeployPathContext(LocalRepo repo, RepoPath repoPath, long contentLength, String requestSha1,
            String requestSha2, boolean forceExpiryCheck) {
        this.repo = repo;
        this.repoPath = repoPath;
        this.contentLength = contentLength;
        this.requestSha1 = requestSha1;
        this.requestSha2 = requestSha2;
        this.forceExpiryCheck = forceExpiryCheck;
    }

    public LocalRepo getRepo() {
        return repo;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getRequestSha1() {
        return requestSha1;
    }

    public String getRequestSha2() {
        return requestSha2;
    }

    public boolean isForceExpiryCheck() {
        return forceExpiryCheck;
    }

    public static class Builder {

        private LocalRepo repo;
        private RepoPath repoPath;
        private long contentLength;
        private String requestSha1;
        private String requestSha2;
        private boolean forceExpiryCheck;

        /**
         * @param repo     The storing repository (cache or local) to deploy to
         * @param repoPath The repo path for deployment (the repo key is always taken from the repo parameter)
         */
        public Builder(LocalRepo repo, RepoPath repoPath) {
            this.repo = repo;
            this.repoPath = repoPath;
        }

        /**
         * @param contentLength The length in bytes of the resource to deploy
         */
        public Builder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        /**
         * @param requestSha1 sha1 checksum of the deployed file
         */
        public Builder requestSha1(String requestSha1) {
            this.requestSha1 = requestSha1;
            return this;
        }

        /**
         * @param requestSha2 sha256 checksum of the deployed file
         */
        public Builder requestSha2(String requestSha2) {
            this.requestSha2 = requestSha2;
            return this;
        }

        /**
         * @param forceExpiryCheck Force expiry on existed cached resources
         */
        public Builder forceExpiryCheck(boolean forceExpiryCheck) {
            this.forceExpiryCheck = forceExpiryCheck;
            return this;
        }

        public ValidDeployPathContext build() {
            return new ValidDeployPathContext(repo, repoPath, contentLength, requestSha1, requestSha2, forceExpiryCheck);
        }
    }
}