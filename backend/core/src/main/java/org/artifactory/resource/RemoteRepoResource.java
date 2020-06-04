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

package org.artifactory.resource;

import org.apache.http.Header;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;

import java.util.Set;

/**
 * A barebone resource representing minimal file information retrieved from a remote source
 *
 * @author Noam Y. Tenne
 */
public class RemoteRepoResource implements RepoResource {

    private final MutableRepoResourceInfo info;
    private RepoPath responseRepoPath;
    private boolean expirable;

    private Header[] allHeaders;
    private String etag;

    public RemoteRepoResource(RepoPath repoPath, long lastModified, String etag, long size, Set<ChecksumInfo> checksums,
            Header[] allHeaders) {
        this.allHeaders = allHeaders;
        this.etag = etag;
        if (NamingUtils.isMetadata(repoPath.getPath())) {
            //TODO: [by YS] remove if not used by the replication for properties
            info = InfoFactoryHolder.get().createMetadata(repoPath);
        } else {
            info = InfoFactoryHolder.get().createFileInfo(repoPath);
        }
        info.setLastModified(lastModified);
        info.setSize(size);
        info.setChecksums(checksums);
    }

    /**
     * Constructing the object again, it's important to keep the original instance but replace the fields inside.
     * @param other is the new resource to build from
     */
    public void reConstruct(RemoteRepoResource other) {
        reConstruct(other.getLastModified(), other.getEtag(),
                other.getSize(), other.getChecksums(), other.getAllHeaders());
    }

    /**
     *  Constructing the object again, it's important to keep the original instance but replace the fields inside.
     *  The Parameters should match the constructor parameters to make sure all data fulfilled.
     */
    private void reConstruct(long lastModified, String etag, long size, Set<ChecksumInfo> checksums,
            Header[] allHeaders) {

        this.allHeaders = allHeaders;
        this.etag = etag;
        if (info != null) {
            info.setLastModified(lastModified);
            info.setSize(size);
            info.setChecksums(checksums);
        }
    }

    @Override
    public RepoPath getRepoPath() {
        return info.getRepoPath();
    }

    @Override
    public RepoPath getResponseRepoPath() {
        return (responseRepoPath != null) ? responseRepoPath : getRepoPath();
    }

    @Override
    public void setResponseRepoPath(RepoPath responsePath) {
        responseRepoPath = responsePath;
    }

    @Override
    public MutableRepoResourceInfo getInfo() {
        return info;
    }

    @Override
    public boolean isFound() {
        return true;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    @Override
    public boolean isExactQueryMatch() {
        return true;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isMetadata() {
        return NamingUtils.isMetadata(getRepoPath().getPath());
    }

    @Override
    public long getSize() {
        return info.getSize();
    }

    @Override
    public long getCacheAge() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return info.getLastModified();
    }

    @Override
    public String getEtag() {
        return etag;
    }

    @Override
    public String getMimeType() {
        return NamingUtils.getMimeType(getRepoPath().getPath()).getType();
    }

    @Override
    public boolean isExpirable() {
        return expirable;
    }

    @Override
    public void expirable() {
        expirable = true;
    }

    public Header[] getAllHeaders() {
        return allHeaders;
    }

    public Set<ChecksumInfo> getChecksums() {
        return info.getChecksums();
    }

    @Override
    public String toString() {
        return "RemoteRepoResource{" +
                "info=" + info +
                ", responseRepoPath=" + responseRepoPath +
                '}';
    }
}
