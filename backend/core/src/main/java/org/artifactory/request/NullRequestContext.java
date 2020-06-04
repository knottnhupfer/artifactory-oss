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

package org.artifactory.request;

import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import static org.artifactory.api.request.InternalRequestFactory.createInternalRequestDisableRedirect;

/**
 * Dummy implementation of the request context. It doesn't contain the original request. Used in internal requests and
 * for testing.
 *
 * @author Yossi Shaul
 */
public class NullRequestContext extends BaseRequestContext {

    public NullRequestContext(RepoPath repoPath) {
        super(createInternalRequestDisableRedirect(repoPath));
    }

    @Override
    public boolean isFromAnotherArtifactory() {
        return false;
    }

    @Override
    public String getResourcePath() {
        final RepoPath repoPath = request.getRepoPath();
        return repoPath.getPath();
    }

    @Override
    public Properties getProperties() {
        return (Properties) InfoFactoryHolder.get().createProperties();
    }

    public void setServletContextUrl(String servletContextUrl) {
        ((InternalArtifactoryRequest)getRequest()).setServletContextUrl(servletContextUrl);
    }

    @Override
    public boolean isForceExpiryCheck() {
        return false;
    }
}
