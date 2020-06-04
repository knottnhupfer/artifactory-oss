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

package org.artifactory.storage.db.security.entity;

/**
 * The link between a User and a User Property (users -> user_props)
 *
 * @deprecated Users and groups are in access
 * @author Shay Yaakov
 */
@Deprecated
public class UserProp {
    private final long userId;
    private final String propKey;
    private final String propVal;

    public UserProp(long userId, String propKey, String propVal) {
        if (userId <= 0L) {
            throw new IllegalArgumentException("User id cannot be zero or negative!");
        }
        this.userId = userId;
        this.propKey = propKey;
        this.propVal = propVal;
    }

    public long getUserId() {
        return userId;
    }

    public String getPropKey() {
        return propKey;
    }

    public String getPropVal() {
        return propVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProp userProp = (UserProp) o;

        if (userId != userProp.userId) return false;
        if (propKey != null ? !propKey.equals(userProp.propKey) : userProp.propKey != null) return false;
        return propVal != null ? propVal.equals(userProp.propVal) : userProp.propVal == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (propKey != null ? propKey.hashCode() : 0);
        result = 31 * result + (propVal != null ? propVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserProp{" +
                "userId=" + userId +
                ", propKey='" + propKey + '\'' +
                ", propVal='" + propVal + '\'' +
                '}';
    }
}
