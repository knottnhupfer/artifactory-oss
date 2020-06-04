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
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Rotem Kfir
 */
@Accessors(fluent = true)
@Data
@NoArgsConstructor
public class ReplicateFileResponse {
    @JsonProperty
    private Status status;

    @JsonProperty
    private String message;

    @JsonProperty("status_code")
    private Integer statusCode;

    @JsonProperty("bytes_transferred")
    private Long bytesTransferred;

    @JsonProperty("file_transaction_id")
    private String fileTransactionId;


    public enum Status {
        INPROGRESS, ERROR, COMPLETED
    }
}
