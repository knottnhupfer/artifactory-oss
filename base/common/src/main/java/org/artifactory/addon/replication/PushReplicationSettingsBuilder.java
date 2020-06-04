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

import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public class PushReplicationSettingsBuilder {

    private final RepoPath repoPath;
    private final String url;
    private ProxyDescriptor proxyDescriptor;
    private int socketTimeoutMillis;
    private String username;
    private String password;
    private boolean deleteExisting = false;
    private boolean includeProperties = false;
    private boolean includeStatistics = false;
    private boolean checkBinaryExistenceInFilestore = false;
    private String replicationKey;

    public PushReplicationSettingsBuilder(RepoPath repoPath, String url) {
        this.repoPath = repoPath;
        this.url = url;
    }

    public PushReplicationSettingsBuilder proxyDescriptor(ProxyDescriptor proxyDescriptor) {
        this.proxyDescriptor = proxyDescriptor;
        return this;
    }

    public PushReplicationSettingsBuilder socketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
        return this;
    }

    public PushReplicationSettingsBuilder username(String username) {
        this.username = username;
        return this;
    }

    public PushReplicationSettingsBuilder password(String password) {
        this.password = password;
        return this;
    }

    public PushReplicationSettingsBuilder deleteExisting(boolean deleteExisting) {
        this.deleteExisting = deleteExisting;
        return this;
    }

    public PushReplicationSettingsBuilder includeProperties(boolean includeProperties) {
        this.includeProperties = includeProperties;
        return this;
    }

    public PushReplicationSettingsBuilder checkBinaryExistenceInFilestore(boolean checkBinaryExistenceInFilestore) {
        this.checkBinaryExistenceInFilestore = checkBinaryExistenceInFilestore;
        return this;
    }

    public PushReplicationSettingsBuilder includeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }

    public PushReplicationSettingsBuilder replicationKey(String replicationKey) {
        this.replicationKey = replicationKey;
        return this;
    }

    public PushReplicationSettings build() {
        return new PushReplicationSettings(repoPath, url, proxyDescriptor, socketTimeoutMillis, username, password,
                deleteExisting, includeProperties, checkBinaryExistenceInFilestore, includeStatistics, replicationKey);
    }
}
