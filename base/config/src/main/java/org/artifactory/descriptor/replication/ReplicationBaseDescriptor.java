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
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.TaskDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.jfrog.common.config.diff.DiffKey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Noam Y. Tenne
 */
@XmlType(name = "ReplicationBaseType", propOrder = {"enabled", "cronExp", "syncDeletes", "syncProperties", "pathPrefix",
        "repoKey", "enableEventReplication", "replicationKey", "checkBinaryExistenceInFilestore"}, namespace = Descriptor.NS)
@Data
public abstract class ReplicationBaseDescriptor implements TaskDescriptor {

    @XmlElement(defaultValue = "false")
    private boolean enabled;

    @XmlElement(required = false)
    private String cronExp;

    @XmlElement(defaultValue = "true")
    private boolean syncDeletes = false;

    @XmlElement(defaultValue = "true")
    private boolean syncProperties = true;

    @XmlElement(required = false)
    private String pathPrefix;

    @XmlElement(required = true)
    private String repoKey;

    @XmlElement(required = true)
    @DiffKey
    private String replicationKey;

    @XmlElement(defaultValue = "true")
    private boolean enableEventReplication = false;

    @XmlElement(defaultValue = "false")
    private boolean checkBinaryExistenceInFilestore = false;

    public String getReplicationKey() {
        return replicationKey;
    }

    public RepoPath getRepoPath() {
        return InternalRepoPathFactory.create(repoKey, pathPrefix);
    }

    @Override
    public boolean sameTaskDefinition(TaskDescriptor otherDescriptor) {
        if (otherDescriptor == null || !(otherDescriptor instanceof ReplicationBaseDescriptor)) {
            throw new IllegalArgumentException(
                    "Cannot compare replication descriptor " + this + " with " + otherDescriptor);
        }
        ReplicationBaseDescriptor replicationDescriptor = (ReplicationBaseDescriptor) otherDescriptor;
        return replicationDescriptor.enabled == this.enabled &&
                StringUtils.equals(replicationDescriptor.repoKey, this.repoKey) &&
                StringUtils.equals(replicationDescriptor.cronExp, this.cronExp);
    }
}
