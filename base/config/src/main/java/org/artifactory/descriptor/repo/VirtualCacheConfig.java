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
import java.util.Objects;

/**
 * Virtual cache configuration.
 *
 * @author Dan Feldman
 */
@XmlType(name = "VirtualCacheConfigType", propOrder = {"virtualRetrievalCachePeriodSecs"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class VirtualCacheConfig implements Descriptor {

    @XmlElement(required = true)
    private long virtualRetrievalCachePeriodSecs = 600;

    public long getVirtualRetrievalCachePeriodSecs() {
        return virtualRetrievalCachePeriodSecs;
    }

    public void setVirtualRetrievalCachePeriodSecs(long virtualRetrievalCachePeriodSecs) {
        this.virtualRetrievalCachePeriodSecs = virtualRetrievalCachePeriodSecs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VirtualCacheConfig)) {
            return false;
        }
        VirtualCacheConfig that = (VirtualCacheConfig) o;
        return getVirtualRetrievalCachePeriodSecs() == that.getVirtualRetrievalCachePeriodSecs();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVirtualRetrievalCachePeriodSecs());
    }
}
