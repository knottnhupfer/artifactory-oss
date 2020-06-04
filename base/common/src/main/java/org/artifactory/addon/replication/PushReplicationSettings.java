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

package org.artifactory.addon.replication;

import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public class PushReplicationSettings extends ReplicationBaseSettings {

    private final String username;
    private final String password;
    private final ProxyDescriptor proxyDescriptor;
    private final boolean includeStatistics;
    private String replicationKey;
    public long eventLogUnstableDurationMillis = -1; // -1 for uninitialized. replicate would use default.

    public PushReplicationSettings(LocalReplicationDescriptor descriptor) {
        this(descriptor.getRepoPath(), descriptor.getUrl(), descriptor.getProxy(), descriptor.getSocketTimeoutMillis(),
                descriptor.getUsername(), descriptor.getPassword(), descriptor.isSyncDeletes(),
                descriptor.isSyncProperties(), descriptor.isCheckBinaryExistenceInFilestore(),
                descriptor.isSyncStatistics(), descriptor.getReplicationKey());
    }

    /**
     * <B>NOTE<B>: Try to refrain from using this constructor directly and use the builder instead
     */
    public PushReplicationSettings(RepoPath repoPath, String url, ProxyDescriptor proxyDescriptor,
            int socketTimeoutMillis, String username, String password, boolean deleteExisting,
            boolean includeProperties, boolean checkBinaryExistenceInFilestore, boolean includeStatistics, String replicationKey) {
        super(repoPath, deleteExisting, includeProperties, checkBinaryExistenceInFilestore, url, socketTimeoutMillis);
        this.proxyDescriptor = proxyDescriptor;
        this.username = username;
        this.password = password;
        this.includeStatistics = includeStatistics;
        this.replicationKey = replicationKey;
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PushReplicationSettings)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        PushReplicationSettings that = (PushReplicationSettings) o;

        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }
        if (proxyDescriptor != null ? !proxyDescriptor.equals(that.proxyDescriptor) : that.proxyDescriptor != null) {
            return false;
        }
        if (!username.equals(that.username)) {
            return false;
        }
        return includeStatistics == that.includeStatistics;
    }

    public String getReplicationKey() {
        return replicationKey;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (proxyDescriptor != null ? proxyDescriptor.hashCode() : 0);
        result = 31 * result + (includeStatistics ? 1 : 0);
        return result;
    }
}
