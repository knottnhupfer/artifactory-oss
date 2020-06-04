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

package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.DiffKey;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Shay Yaakov
 */
@XmlType(name = "ReverseProxyRepoConfigType", propOrder = {"repoRef", "serverName", "port"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class ReverseProxyRepoConfig implements Descriptor {

    @XmlIDREF
    @XmlElement(name = "repoRef",required = false)
    @DiffReference
    private RepoBaseDescriptor repoRef;

    @XmlElement(name = "serverName",required = false)
    private String serverName;

    @DiffKey
    private int port = -1;

    public RepoBaseDescriptor getRepoRef() {
        return repoRef;
    }

    public void setRepoRef(RepoBaseDescriptor repoRef) {
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

    public void setPort(Integer port) {
        if (port != null) {
            this.port = port;
        } else {
            this.port = -1;
        }
    }
}
