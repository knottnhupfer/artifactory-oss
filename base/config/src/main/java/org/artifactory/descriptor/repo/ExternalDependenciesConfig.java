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
import org.jfrog.common.config.diff.DiffAtomic;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Shay Yaakov
 */
@XmlType(name = "ExternalDependenciesConfigType", propOrder = {"enabled", "patterns", "remoteRepo"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class ExternalDependenciesConfig implements Descriptor {

    @XmlElement(defaultValue = "false", required = false)
    private boolean enabled = false;

    @XmlElementWrapper(name = "patterns")
    @XmlElement(name = "pattern", type = String.class, required = false)
    @DiffAtomic
    private List<String> patterns = new ArrayList<>();

    @XmlIDREF
    @XmlElement(name = "remoteRepo", type = RemoteRepoDescriptor.class, required = false)
    @DiffReference
    private RemoteRepoDescriptor remoteRepo;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = Optional.ofNullable(patterns).orElseGet(ArrayList::new);
    }

    public RemoteRepoDescriptor getRemoteRepo() {
        return remoteRepo;
    }

    public void setRemoteRepo(RemoteRepoDescriptor remoteRepo) {
        this.remoteRepo = remoteRepo;
    }
}
