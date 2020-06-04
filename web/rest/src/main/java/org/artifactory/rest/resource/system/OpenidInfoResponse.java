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
package org.artifactory.rest.resource.system;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Noam Shemesh
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OpenidInfoResponse implements RestModel {
    @JsonProperty("login_url")
    private String loginUrl;
    @JsonProperty("logout_url")
    private String logoutUrl;
    @JsonProperty("token_url")
    private String tokenUrl;
    @JsonProperty("userinfo_url")
    private String userinfoUrl;

    private boolean enabled;
}
