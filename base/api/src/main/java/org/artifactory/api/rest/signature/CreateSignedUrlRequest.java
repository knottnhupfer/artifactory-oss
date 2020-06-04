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

package org.artifactory.api.rest.signature;

import lombok.*;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;
import org.jfrog.common.ClockUtils;

import javax.annotation.Nullable;

/**
 * A request for creating a signed URL for a repo path.
 */
@Accessors(chain = true)
@Setter
@NoArgsConstructor
@ToString
public class CreateSignedUrlRequest {

    @NonNull
    @Getter
    @JsonProperty("repo_path")
    private String repoPath;

    /**
     * Optional. An expiry date for the URL after which the URL will be invalid, expiry value is in Unix epoch time in milliseconds.
     * By default, expiry date will be 24 hours if not specified. Cannot be more than a configurable threshold. Mutually exclusive with valid_for_secs.
     */
    @Nullable
    @JsonProperty
    private Long expiry;

    /**
     * Optional. The number of seconds since generation before the URL expires.
     * By default, expiry date will be 24 hours if not specified. Cannot be more than a configurable threshold. Mutually exclusive with expiry.
     */
    @Nullable
    @JsonProperty("valid_for_secs")
    private Long validForSeconds;


    public Long getValidForSeconds() {
        if (validForSeconds != null) {
            return validForSeconds;
        }
        if (expiry != null) {
            return expiry - ClockUtils.epochMillis()/1000;
        }
        return null;
    }
}
