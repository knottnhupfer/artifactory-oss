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

package org.artifactory.model.xstream.security;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.artifactory.security.UserPropertyInfo;

/**
 * @author Gidi Shabat
 */
@XStreamAlias("userProperty")
public class UserProperty implements UserPropertyInfo {

    private final String propKey;
    private final String propValue;

    public UserProperty(String propKey, String propValue) {
        this.propKey = propKey;
        this.propValue = propValue;
    }

    @Override
    public String getPropKey() {
        return propKey;
    }

    @Override
    public String getPropValue() {
        return propValue;
    }


    @Override
    public String toString() {
        return "UserProperty{" +
                "propKey='" + propKey + '\'' +
                ", propValue='" + propValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProperty that = (UserProperty) o;

        return propKey != null ? propKey.equals(that.propKey) : that.propKey == null;

    }

    @Override
    public int hashCode() {
        return propKey != null ? propKey.hashCode() : 0;
    }
}
