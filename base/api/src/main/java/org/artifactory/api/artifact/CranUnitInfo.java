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

package org.artifactory.api.artifact;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.jfrog.client.util.PathUtils;

/**
 * @author Inbar Tal
 */
@JsonTypeName("cran")
public class CranUnitInfo implements UnitInfo  {

    private String artifactType = "cran";
    private String path;
    private String distribution;
    private String rVersion;

    public String getDistribution() {
        return distribution;
    }

    public String getrVersion() {
        return rVersion;
    }

    @Override
    public boolean isMavenArtifact() {
        return false;
    }

    @Override
    public String getPath() {
        if (StringUtils.isEmpty(distribution) || StringUtils.isEmpty(rVersion)) {
            return path;
        }
        return String.join("/", "bin", distribution, "contrib", rVersion, PathUtils.trimSlashes(path));
    }

    public String getArtifactType() {
        return artifactType;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public void setRVersion(String rVersion) {
        this.rVersion = rVersion;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }
}
