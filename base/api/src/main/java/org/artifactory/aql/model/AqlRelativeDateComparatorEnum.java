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

package org.artifactory.aql.model;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * @author gidis
 */
public enum AqlRelativeDateComparatorEnum {
    last("$last", AqlComparatorEnum.greater),
    before("$before", AqlComparatorEnum.less);

    public String signature;
    public AqlComparatorEnum aqlComparatorEnum;

    AqlRelativeDateComparatorEnum(String signature, AqlComparatorEnum aqlComparatorEnum) {
        this.signature = signature;
        this.aqlComparatorEnum = aqlComparatorEnum;
    }

    public static AqlRelativeDateComparatorEnum value(String comparator) {
        for (AqlRelativeDateComparatorEnum comparatorEnum : values()) {
            if (comparatorEnum.signature.equals(comparator)) {
                return comparatorEnum;
            }
        }
        return null;
    }

    public long toDate(String value) {
        try {
            PeriodFormatter formatter = new PeriodFormatterBuilder()
                    .appendMillis().appendSuffix("millis")
                    .appendMillis().appendSuffix("ms")
                    .appendMinutes().appendSuffix("minutes")
                    .appendMinutes().appendSuffix("mi")
                    .appendDays().appendSuffix("days")
                    .appendDays().appendSuffix("d")
                    .appendMonths().appendSuffix("months")
                    .appendMonths().appendSuffix("mo")
                    .appendYears().appendSuffix("years")
                    .appendYears().appendSuffix("y")
                    .appendSeconds().appendSuffix("seconds")
                    .appendSeconds().appendSuffix("s")
                    .appendWeeks().appendSuffix("weeks")
                    .appendWeeks().appendSuffix("w")
                    .toFormatter();
            Period period = formatter.parsePeriod(value);
            DateTime now = DateTime.now();
            return now.minus(period).getMillis();
        }catch (IllegalArgumentException e){
            return -1;
        }
    }
}
