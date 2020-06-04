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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Download redirect configuration for repositories.
 * In case enabled, upon each download request the response will be HTTP 302 with "location" header
 *
 * @author Yuval Reches
 */
@XmlType(name = "RepoDownloadRedirectConfigType", propOrder = {"enabled"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class DownloadRedirectRepoConfig implements Descriptor {

    @XmlElement(defaultValue = "false", required = true)
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadRedirectRepoConfig that = (DownloadRedirectRepoConfig) o;

        return enabled == that.enabled;
    }

    @Override
    public int hashCode() {
        return (enabled ? 1 : 0);
    }
}