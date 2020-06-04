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

package org.artifactory.storage.db.aql.itest.service.decorator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yinon Avraham
 */
public class AqlMatchesPattern {

    private static final Pattern PATTERN = Pattern.compile("(\\?)|(\\*)|([^?*]+)");

    private final Pattern pattern;

    private AqlMatchesPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public static AqlMatchesPattern compile(String matchesPattern) {
        String regex = matchesPatternToRegex(matchesPattern);
        return new AqlMatchesPattern(Pattern.compile(regex));
    }

    public boolean matches(String value) {
        return pattern.matcher(value).matches();
    }

    private static String matchesPatternToRegex(String matchesPattern) {
        StringBuilder regex = new StringBuilder("^");
        Matcher matcher = PATTERN.matcher(matchesPattern);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                regex.append(".?");
            } else if (matcher.group(2) != null) {
                regex.append(".*");
            } else if (matcher.group(3) != null) {
                regex.append(Pattern.quote(matcher.group(3)));
            } else {
                throw new IllegalStateException("Unexpected state - check the pattern");
            }
        }
        regex.append("$");
        return regex.toString();
    }
}
