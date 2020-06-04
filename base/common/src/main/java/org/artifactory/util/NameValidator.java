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

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.exception.ValidationException;

/**
 * Generic name validator for root entity names (repos, descriptor keys etc.).
 *
 * @author Yossi Shaul
 */
public final class NameValidator {
    private static final char[] forbiddenChars = {'/', '\\', ':', '|', '?', '*', '"', '<', '>'};

    public static void validate(String name) throws ValidationException {
        if (StringUtils.isBlank(name)) {
            throw new ValidationException("Name cannot be blank");
        }

        if (name.equals(".") || name.equals("..") || name.equals("&")) {
            throw new ValidationException("Name cannot be empty link: '" + name + "'");
        }

        char[] nameChars = name.toCharArray();
        for (int i = 0; i < nameChars.length; i++) {
            char c = nameChars[i];
            for (char fc : forbiddenChars) {
                if (c == fc) {
                    throw new ValidationException("Illegal name character: '" + c + "' at index " + i + ": " + name, i);
                }
            }
        }
    }

    /**
     * @return ForbiddenChars
     */
    public static char[] getForbiddenChars() {
        return forbiddenChars;
    }
}
