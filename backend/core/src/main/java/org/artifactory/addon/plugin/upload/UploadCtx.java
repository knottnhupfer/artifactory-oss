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

package org.artifactory.addon.plugin.upload;

import org.artifactory.addon.plugin.download.DownloadCtx;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;
import org.artifactory.request.RequestThreadLocal;
import org.artifactory.webapp.servlet.HttpArtifactoryRequest;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Upload context filled up by a {@link BeforeUploadRequestAction} plugin extension
 *
 * @author Rotem Kfir
 */
public class UploadCtx {

    private RepoPath modifiedRepoPath;
    private Request clientRequest;

    public UploadCtx() {
        this.clientRequest = null;
        HttpServletRequest request = RequestThreadLocal.getRequest();
        if (request != null) {
            try {
                this.clientRequest = new HttpArtifactoryRequest(request);
            } catch (UnsupportedEncodingException e) {
                LoggerFactory.getLogger(DownloadCtx.class).warn("Creating upload context partially failed, client request set to null.");
            }
        }

    }

    /**
     * The modified repo path provided by the user plugin for a given query
     * @return a modified repo path used inside Artifactory to answer the given request
     */
    public RepoPath getModifiedRepoPath() {
        return modifiedRepoPath;
    }

    /**
     * Override the actual repo path used inside the Artifactory upload process to answer the given request.
     * @param modifiedRepoPath The new repo path to use
     */
    public void setModifiedRepoPath(RepoPath modifiedRepoPath) {
        this.modifiedRepoPath = modifiedRepoPath;
    }
}
