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

package org.artifactory.storage.jobs.migration.sha256;

/**
 * This exception denotes a fatal, unrecoverable error that deems all related nodes as failures that must be resolved
 * by user intervention
 *
 * @author Dan Feldman
 */
public class Sha256CalculationFatalException extends Exception {

    Sha256CalculationFatalException(String message) {
        super(message);
    }

    Sha256CalculationFatalException(String message, Throwable cause) {
        super(message, cause);
    }
}
