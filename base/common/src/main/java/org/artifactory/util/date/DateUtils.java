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

package org.artifactory.util.date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Chen Keinan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {
    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

    /**
     * return build duration in human
     * @param durationMill - build duration in mill
     * @return duration in human
     */
    public static String getDuration(long durationMill){
        int minutes = (int) ((durationMill / (1000 * 60)) % 60);
        int hours   = (int) ((durationMill / (1000 * 60 * 60)) % 24);

        if (hours > 0){
            BigDecimal duration = BigDecimal.valueOf(durationMill / (1000.0 * 60.0 * 60.0)).setScale(1, RoundingMode.UP);
            return duration.toString()+" hours";
        }
        else if (minutes > 0 ) {
            BigDecimal duration = BigDecimal.valueOf(durationMill / (1000.0 * 60.0)).setScale(1, RoundingMode.UP);
            return  duration.toString() + " minutes";
        }
        else {
            BigDecimal duration = BigDecimal .valueOf(durationMill/1000.0).setScale(1,RoundingMode.UP);
            return  duration.toString() + " seconds";
        }
    }

    /**
     * format Build date
     * @param time - long date
     */
    public static String formatBuildDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat(Build.STARTED_FORMAT);
        return formatter.format(date);
    }

    public static Long parseBuildDate(String buildStarted) {
        Long time = null;
        try {
            time = DateUtils.toBuildDate(buildStarted);
        } catch (Exception e) {
            log.warn("Failed to parse the build started field: setting it as null.");
        }
        return time;
    }

    /**
     * build date in string format to long
     */
    private static long toBuildDate(String date) throws ParseException {
        SimpleDateFormat df2 = new SimpleDateFormat(Build.STARTED_FORMAT);
        Date parse = df2.parse(date);
        return parse.getTime();
    }
}
