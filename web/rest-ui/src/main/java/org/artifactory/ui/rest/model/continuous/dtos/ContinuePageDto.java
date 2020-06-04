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
package org.artifactory.ui.rest.model.continuous.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.artifactory.api.rest.common.model.continues.util.Direction;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ContinuePageDto {

    String continueState;
    String mustInclude;
    Long limit;
    Direction direction;

    ContinuePageDto() {

    }

    public ContinuePageDto(ArtifactoryRestRequest request) {
        Map<String, List<String>> params = request.getUriInfo().getQueryParameters();

        this.continueState = getFirst(params, "continueState");
        this.mustInclude = getFirst(params, "mustInclude");
        String limitStr = getFirst(params, "limit");
        if (limitStr != null) {
            this.limit = Long.parseLong(limitStr);
        }
        String directionStr = getFirst(params, "direction");
        if (directionStr != null) {
            this.direction = Direction.valueOf(directionStr.toUpperCase());
        }
    }

    ContinuePageDto(ContinuePageDto orig) {
        this(orig.continueState, orig.mustInclude, orig.limit, orig.direction);
    }

    protected String getFirst(Map<String, List<String>> params, String key) {
        return CollectionUtils.isNotEmpty(params.get(key)) ?
                params.get(key).get(0) : null;
    }

}
