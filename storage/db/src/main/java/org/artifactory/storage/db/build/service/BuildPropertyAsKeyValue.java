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

package org.artifactory.storage.db.build.service;

import lombok.Data;
import org.artifactory.api.build.BuildProps;

/**
 * Used for doing programmatically distinct
 * Must be package level class (not inner) when we use lombok
 *
 * @author Saffi Hartal
 */
@Data
class BuildPropertyAsKeyValue {
    final String propKey;
    final String propValue;

    @Override
    public String toString() {
        return "BuildPropertyAsKeyValue{" +
                "propKey='" + propKey + '\'' +
                ", propValue='" + propValue + '\'' +
                '}';
    }

    BuildPropertyAsKeyValue(BuildProps bp) {
        this(bp.getKey(), bp.getValue());
    }


    BuildPropertyAsKeyValue(String propKey, String propValue) {
        this.propKey = propKey;
        this.propValue = propValue;
    }
}
