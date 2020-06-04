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

package org.artifactory.sapi.search;

/**
 * Date: 8/5/11
 * Time: 6:38 PM
 *
 * @author Fred Simon
 */
public enum VfsComparatorType {
    ANY("IS NOT NULL"), IN("IN"), NONE("IS NULL"),
    EQUAL("="), NOT_EQUAL("!="),
    GREATER_THAN(">"), LOWER_THAN("<"),
    GREATER_THAN_EQUAL(">="), LOWER_THAN_EQUAL("<="),
    CONTAINS("LIKE"), NOT_CONTAINS("NOT LIKE");

    public final String str;

    VfsComparatorType(String str) {
        this.str = str;
    }

    public boolean acceptValue() {
        return this != ANY && this != NONE;
    }

    public boolean acceptFunction() {
        return acceptValue() && this != CONTAINS && this != IN;
    }
}
