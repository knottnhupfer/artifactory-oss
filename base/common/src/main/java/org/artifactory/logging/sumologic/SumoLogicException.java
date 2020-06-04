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

package org.artifactory.logging.sumologic;

/**
 * @author Shay Yaakov
 */
public class SumoLogicException extends RuntimeException {

    private final int status;

    public SumoLogicException(String message) {
        super(message);
        status = 500;
    }

    public SumoLogicException(String message, int status) {
        super(message);
        this.status = status;
    }

    public SumoLogicException(String message, Throwable cause) {
        super(message, cause);
        status = 500;
    }

    public int getStatus() {
        return status;
    }

    public int getRelaxedStatus() {
        if (status == 401 || status == 403) {
            //Artifactory UI has a default behavior for unauthorized and forbidden. To avoid it we change to bad request.
            return 400;
        } else if (status == 503) {
            //Artifactory UI has a default behavior for service unavailable. To avoid it we change to internal server error.
            return 500;
        }
        return status;
    }
}