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

import org.artifactory.addon.plugin.PluginAction;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;

/**
 * Intercept any before download request (from any repo), the user is given a {@link DownloadCtx} to set
 * custom properties on it to be used as part of the download action.
 *
 * @author Shay Yaakov
 * @see DownloadCtx
 */
public interface BeforeDownloadRequestAction extends PluginAction {
    void beforeDownloadRequest(Request request, RepoPath repoPath);
}