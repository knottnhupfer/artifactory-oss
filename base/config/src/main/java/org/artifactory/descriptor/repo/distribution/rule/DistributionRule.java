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

package org.artifactory.descriptor.repo.distribution.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jfrog.common.config.diff.DiffKey;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * A Distribution rule used to map an artifacts to its distribution coordinates in Bintray
 *
 * @author Dan Feldman
 */
@XmlType(name = "DistributionRuleType", propOrder = {"name", "type", "repoFilter", "pathFilter", "distributionCoordinates"},
        namespace = Descriptor.NS)
@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
@GenerateDiffFunction
public class DistributionRule implements Descriptor {

    @XmlID
    @XmlElement(required = true)
    @DiffKey
    private String name;

    @XmlElement(required = true)
    private RepoType type;

    @XmlElement(required = false)
    private String repoFilter;

    @XmlElement(required = false)
    private String pathFilter;

    @XmlElement(required = true)
    private DistributionCoordinates distributionCoordinates = new DistributionCoordinates();

    public DistributionRule(DistributionRule copy) {
        this.name = copy.name;
        this.type = copy.type;
        this.repoFilter = copy.repoFilter;
        this.pathFilter = copy.pathFilter;
        this.distributionCoordinates = copy.distributionCoordinates;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = RepoType.fromType(type);
    }

    @Override
    public String toString() {
        return name;
    }
}
