package org.artifactory.properties.validation;

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

import org.artifactory.exception.ValidationException;

import java.util.regex.Pattern;


/**
 * Validator for property names.
 *
 * @author Tomer Mayost
 */
public final class PropertyNameValidator {
    private static final String escapedSpecialChars = Pattern.quote(")(}{][*+^$\\/~`!@#%&<>;=,±§");
    private static final Pattern namePattern = Pattern.compile("^[a-zA-Z][^\\s" + escapedSpecialChars + "]*$");

    public static void validate(String name) throws ValidationException {
        if (name == null || !namePattern.matcher(name).matches()) {
            throw new ValidationException("Name must start with a letter and cannot contain whitespace or special characters");
        }
    }


}
