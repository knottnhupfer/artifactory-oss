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

package org.artifactory.descriptor.replication;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Noam Y. Tenne
 */
@XmlType(name = "LocalReplicationType", propOrder = {"url", "proxy", "socketTimeoutMillis", "username", "password",
        "syncStatistics"}, namespace = Descriptor.NS)
@Data
@EqualsAndHashCode(callSuper = true)
@GenerateDiffFunction
public class LocalReplicationDescriptor extends ReplicationBaseDescriptor {

    @XmlElement(required = false)
    private String url;

    @XmlIDREF
    @XmlElement(name = "proxyRef", required = false)
    @DiffReference
    private ProxyDescriptor proxy;

    @XmlElement(defaultValue = "15000", required = false)
    private int socketTimeoutMillis = 15000;//Default socket timeout

    @XmlElement(required = false)
    private String username;

    @XmlElement(required = false)
    private String password;

    @XmlElement(defaultValue = "false")
    private boolean syncStatistics = false;

    @Override
    public String toString() {
        return "LocalReplication[" + getRepoKey() + "|" + url + "|" + getReplicationKey() + "]";
    }
}
