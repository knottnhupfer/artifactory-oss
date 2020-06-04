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
import org.artifactory.addon.plugin.ResponseCtx;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.Request;

/**
 * Intercept download responses, either found, not-modified or head.
 * The user is given a {@link ResponseCtx} to set response headers, and in case of found resource to set response status/content.
 *
 * @author Rotem Kfir
 */
public interface AltAllResponsesAction extends PluginAction {
    void altAllResponses(Request request, RepoPath responseRepoPath);
}