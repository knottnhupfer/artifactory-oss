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

package org.artifactory.rest.resource.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * See <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC6749 - Section 5.1</a>
 * @author Yinon Avraham
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseModel {

    @com.fasterxml.jackson.annotation.JsonProperty("access_token")
    @JsonProperty("access_token")
    private String accessToken;

    @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @com.fasterxml.jackson.annotation.JsonProperty("expires_in")
    @JsonProperty("expires_in")
    private Long expiresIn;

    @com.fasterxml.jackson.annotation.JsonProperty("scope")
    @JsonProperty("scope")
    private String scope;

    @com.fasterxml.jackson.annotation.JsonProperty("token_type")
    @JsonProperty("token_type")
    private String tokenType;
}
