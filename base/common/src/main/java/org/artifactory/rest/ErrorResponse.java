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

package org.artifactory.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A JSON object to be sent as an error thrown by the REST API.
 *
 * @author Shay Yaakov
 */
public class ErrorResponse {

    @JsonProperty("errors")
    List<Error> errors = Lists.newArrayList();

    public ErrorResponse() {
        // for serialization
    }

    public ErrorResponse(int status, String message) {
        errors.add(new Error(status, message != null ? message : ""));
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static class Error {
        private int status = 500;
        private String message = "";

        public Error() {
            // for serialization
        }

        public Error(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
