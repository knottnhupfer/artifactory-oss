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

package org.artifactory.repo.service.deploy;

import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import java.io.InputStream;

/**
 * @author Tomer Cohen
 */
public class ArtifactoryDeployRequest extends InternalArtifactoryRequest {

    private InputStream inputStream;
    private long contentLength;
    private long lastModified;
    private Properties properties;

    /**
     * Use {@link ArtifactoryDeployRequestBuilder} to instantiate this object
     */
    protected ArtifactoryDeployRequest(RepoPath pathToUpload, InputStream inputStream, long contentLength, long lastModified,
            Properties properties, boolean trustServerChecksums) {
        super(pathToUpload, true);
        this.inputStream = inputStream;
        this.contentLength = contentLength;
        this.lastModified = lastModified;
        this.properties = properties != null ? properties : (Properties) InfoFactoryHolder.get().createProperties();
        setTrustServerChecksums(trustServerChecksums);
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean hasProperties() {
        return !properties.isEmpty();
    }
}
