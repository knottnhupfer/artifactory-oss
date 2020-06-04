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

package org.artifactory.api.rest.distribution.bundle.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Objects;

/**
 * @author Tomer Mayost
 */
@Data
@NoArgsConstructor
public class BundleVersion implements Comparable{
    String version;
    String created;
    String status;
    String storingRepo;

    @JsonIgnore
    public String getStoringRepo() {
        return storingRepo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BundleVersion)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        BundleVersion that = (BundleVersion) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(created, that.created) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), version, created, status);
    }

    @Override
    public int compareTo(Object o) {
        return this.version.compareTo(((BundleVersion)o).version);
    }
}
