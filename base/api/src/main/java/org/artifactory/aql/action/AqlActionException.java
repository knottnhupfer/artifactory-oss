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

package org.artifactory.aql.action;

/**
 * @author Dan Feldman
 */
public class AqlActionException extends Exception {

    private Reason reason;

    public AqlActionException(String msg, Reason reason) {
        super(msg);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        ACTION_FAILED("Action failed"), UNSUPPORTED_FOR_DOMAIN("Action unsupported for domain"),
        UNEXPECTED_CONTENT("Missing required row content");

        public String code;

        Reason(String code) {
            this.code = code;
        }
    }
}
