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

package org.artifactory.rest.common.model.reverseproxy;

import org.artifactory.descriptor.repo.ReverseProxyRepoConfig;

/**
 * @author Chen Keinan
 */
public class ReverseProxyRepoConfigs {
    private String repoRef;
    private String serverName;
    private int port;

    public ReverseProxyRepoConfigs() {
        // for jackson
    }

    public ReverseProxyRepoConfigs(ReverseProxyRepoConfig reverseProxyRepoConfig){
        this.repoRef = reverseProxyRepoConfig.getRepoRef().getKey();
        this.serverName = reverseProxyRepoConfig.getServerName();
        this.port = reverseProxyRepoConfig.getPort();
    }

    public String getRepoRef() {
        return repoRef;
    }

    public void setRepoRef(String repoRef) {
        this.repoRef = repoRef;
    }

    public String getServerName() {

        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
