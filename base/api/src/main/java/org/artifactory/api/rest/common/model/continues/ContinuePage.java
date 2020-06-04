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
package org.artifactory.api.rest.common.model.continues;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.artifactory.api.rest.common.model.continues.util.Direction;
import org.artifactory.common.ConstantValues;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;
import java.util.Map;

import static org.artifactory.api.rest.common.model.continues.util.Direction.ASC;

@Data
@AllArgsConstructor
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ContinuePage {

    private static final Direction DEFAULT_DIRECTION = ASC;

    String mustInclude;
    Long limit;
    Direction direction = ASC;

    public ContinuePage() {
        limit = ConstantValues.uiContinuePagingLimit.getLong();
    }

    public ContinuePage(ContinuePage orig) {
        this(orig.mustInclude, orig.limit, orig.direction);
    }

    protected String getFirstOrDefault(Map<String, List<String>> params, String key, String defaultStr) {
        return CollectionUtils.isNotEmpty(params.get(key)) ?
                params.get(key).get(0) : defaultStr;
    }

}
