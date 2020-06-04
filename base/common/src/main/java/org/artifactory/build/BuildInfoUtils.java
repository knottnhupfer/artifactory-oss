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

package org.artifactory.build;

import org.jfrog.build.api.Build;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class to work with build info common operation.
 *
 * @author Yossi Shaul
 */
public abstract class BuildInfoUtils {

    private static final DateTimeFormatter BUILD_FORMATTER = DateTimeFormat.forPattern(Build.STARTED_FORMAT);

    /**
     * @param time Time in millis to format
     * @return Formatted time using the {@link org.jfrog.build.api.Build#STARTED_FORMAT} time format (ISO time format).
     */
    public static String formatBuildTime(long time) {
        return BUILD_FORMATTER.print(time);
    }

    /**
     * @param time Time in millis to format
     * @return Formatted time using the {@link org.jfrog.build.api.Build#STARTED_FORMAT} time format (ISO time format).
     */
    public static String formatBuildTime(String time) {
        return BUILD_FORMATTER.print(Long.parseLong(time));
    }

    /**
     * @param buildTimeFormat Build time with {@link org.jfrog.build.api.Build#STARTED_FORMAT} format
     * @return Time in millis represented by the string
     */
    public static long parseBuildTime(String buildTimeFormat) {
        return BUILD_FORMATTER.parseDateTime(buildTimeFormat).getMillis();
    }
}
