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
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * NOTE: indexContextPath, packagesContextPath are as far as I know not in use.
 * Kept them for legacy and descriptor converter usage.
 *
 * @author Yoav Luft
 */
@XmlType(name = "PypiConfigurationType", propOrder = {"indexContextPath", "packagesContextPath", "pyPIRegistryUrl", "repositorySuffix"})
@GenerateDiffFunction
public class PypiConfiguration implements Descriptor {

    @XmlElement(defaultValue = "", required = false)
    private String indexContextPath;

    @XmlElement(defaultValue = "", required = false)
    private String packagesContextPath;

    @XmlElement(defaultValue = "https://pypi.org", required = false)
    private String pyPIRegistryUrl = "https://pypi.org";

    @XmlElement(defaultValue = "simple", required = false)
    private String repositorySuffix = "simple";

    public String getPyPIRegistryUrl() {
        return pyPIRegistryUrl;
    }

    public void setPyPIRegistryUrl(String pyPIRegistryUrl) {
        this.pyPIRegistryUrl = pyPIRegistryUrl;
    }

    public String getPackagesContextPath() {
        return packagesContextPath;
    }

    public void setPackagesContextPath(@Nonnull String packagesContextPath) {
        this.packagesContextPath = packagesContextPath;
    }

    public String getIndexContextPath() {
        return indexContextPath;
    }

    public void setIndexContextPath(@Nonnull String indexContextPath) {
        this.indexContextPath = indexContextPath;
    }

    public String getRepositorySuffix() {
        return repositorySuffix;
    }

    public void setRepositorySuffix(String repositorySuffix) {
        this.repositorySuffix = repositorySuffix;
    }
}
