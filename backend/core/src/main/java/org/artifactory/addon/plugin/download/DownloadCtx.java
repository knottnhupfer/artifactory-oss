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

package org.artifactory.addon.plugin.download;

import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Download context filled up by a {@link BeforeDownloadRequestAction} plugin extension
 *
 * @author Shay Yaakov
 */
public class DownloadCtx {

    private boolean expired;
    private Request clientRequest;
    private RepoPath modifiedRepoPath;

    public DownloadCtx() {
        this.clientRequest = null;
        HttpServletRequest request = RequestThreadLocal.getRequest();
        if (request != null) {
            try {
                this.clientRequest = new HttpArtifactoryRequest(request);
            } catch (UnsupportedEncodingException e) {
                LoggerFactory.getLogger(DownloadCtx.class).warn("Creating download context partially failed, client request set to null.");
            }
        }
    }

    /**
     * @return true if the resource being downloaded is marked as an expired resource,
     * see {@link #setExpired(boolean)} for full details about expired resources.
     */
    public boolean isExpired() {
        return expired;
    }

    /**
     * Whether the resource being downloaded is marked as expired by a user plugin.
     * When true, the cache expiry mechanism will treat this resource as expired regardless of it's last updated time.
     * This should be treated with caution, as it means both another database hit (for updating the last updated time)
     * as well as network overhead since if the resource is expired, a remote download will occur to re-download it to the cache.
     *
     * @param expired True if the resource being downloaded should be treated as expired
     */
    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    /**
     * The modified repo path provided by the user plugin for a given query
     *
     * @return a modified repo path used inside Artifactory to answer the given request
     */
    public RepoPath getModifiedRepoPath() {
        return modifiedRepoPath;
    }

    /**
     * Override the actual repo path used inside the Artifactory download process to answer the given download request.
     *
     * @param modifiedRepoPath The new repo path to use
     */
    public void setModifiedRepoPath(RepoPath modifiedRepoPath) {
        this.modifiedRepoPath = modifiedRepoPath;
    }
}
