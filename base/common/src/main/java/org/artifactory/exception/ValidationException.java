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

package org.artifactory.exception;

/**
 * Exception thrown on invalid input from a UI control.
 *
 * @author Yossi Shaul
 */
public class ValidationException extends Exception {
    private Integer index;
    /**
     * Builds a new validation exception with message to display to the user.
     *
     * @param uiMessage Message to display in the UI
     */
    public ValidationException(String uiMessage) {
        super(uiMessage);
    }
    /**
     * Builds a new validation exception with message to display to the user.
     *
     * @param uiMessage Message to display in the UI
     * @param index the index of invalid char
     */
    public ValidationException(String uiMessage, int index) {
        super(uiMessage);
        this.index = Integer.valueOf(index);
    }

    /**
     * @return the index of invalid char
     */
    public int getIndex() {
        return index == null ? -1 : index.intValue();
    }
}
