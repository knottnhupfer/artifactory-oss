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

import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import static org.artifactory.util.distribution.DistributionConstants.PATH_TOKEN;

/**
 * Coordinates that are used when distributing artifacts to Bintray to specify exactly where the artifact goes.
 * Each coordinate is a string that can be a 'hardcoded' user input, the set of distribution rule tokens available to
 * the rule or a combination of both.
 *
 * @author Dan Feldman
 */
@XmlType(name = "DistributionCoordinatesType", propOrder = {"repo", "pkg", "version", "path"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class DistributionCoordinates implements Descriptor {

    @XmlElement(required = true)
    protected String repo;

    @XmlElement(required = true)
    protected String pkg;

    @XmlElement(required = true)
    protected String version;

    @XmlElement(required = true)
    protected String path = PATH_TOKEN;

    public DistributionCoordinates() {

    }

    public DistributionCoordinates(String repo, String pkg, String version, String path) {
        this.repo = repo;
        this.pkg = pkg;
        this.version = version;
        this.path = path;
    }

    public DistributionCoordinates(DistributionCoordinates copy) {
        this.repo = copy.repo;
        this.pkg = copy.pkg;
        this.version = copy.version;
        this.path = copy.path;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DistributionCoordinates)) return false;

        DistributionCoordinates that = (DistributionCoordinates) o;

        if (getRepo() != null ? !getRepo().equals(that.getRepo()) : that.getRepo() != null) return false;
        if (getPkg() != null ? !getPkg().equals(that.getPkg()) : that.getPkg() != null) return false;
        if (getVersion() != null ? !getVersion().equals(that.getVersion()) : that.getVersion() != null) return false;
        return getPath() != null ? getPath().equals(that.getPath()) : that.getPath() == null;
    }

    @Override
    public int hashCode() {
        int result = getRepo() != null ? getRepo().hashCode() : 0;
        result = 31 * result + (getPkg() != null ? getPkg().hashCode() : 0);
        result = 31 * result + (getVersion() != null ? getVersion().hashCode() : 0);
        result = 31 * result + (getPath() != null ? getPath().hashCode() : 0);
        return result;
    }
}
