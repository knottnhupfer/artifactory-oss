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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Value wrapper that masks the string value (produced by applying {@link String#valueOf(Object)} on the given object).
 * For example, the masked value of <code>"1234567890abcdef"</code> is <code>"*******def"</code>.
 * The goal is to hide the full text representation but still give some indication on the actual value.
 *
 * @author Yinon Avraham
 */
public class MaskedValue {
    private final Object obj;

    private MaskedValue(Object obj) {
        this.obj = obj;
    }

    public static MaskedValue of(@Nullable Object obj) {
        return new MaskedValue(obj);
    }

    @Override
    public String toString() {
        return obj == null ? "null" : masked(String.valueOf(obj));
    }

    private String masked(@Nonnull String value) {
        if (value.length() < 10) {
            return "**********";
        }
        return "*******" + value.substring(value.length()-3);
    }
}
