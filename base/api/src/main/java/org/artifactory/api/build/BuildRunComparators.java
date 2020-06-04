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

package org.artifactory.api.build;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.build.BuildRun;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator class factory
 *
 * @author Lior Hasson
 */
public class BuildRunComparators {

    /**
     * Determines the type of comparator based on the value type of the build number.
     *
     * @param builds Builds to compare
     * @return Build number comparator
     */
    public static Comparator<BuildRun> getComparatorFor(List<BuildRun> builds) {
        for (BuildRun build : builds) {
            if (!StringUtils.isNumeric(build.getNumber())) {
                return new BuildNumberStringComparator();
            }
        }
        return new BuildNumberLongComparator();
    }

    /**
     * @return Comparator based on the build start date. Older builds first.
     */
    public static Comparator<BuildRun> getBuildStartDateComparator() {
        return new BuildStartDateComparator();
    }

    /**
     * Compares builds based on numeric order of the build number.
     * All build numbers must have valid Long values.
     * In case Build numbers are equal we compare the start date
     * In case the Dates are also equals compare the builds names
     */
    private static class BuildNumberLongComparator implements Comparator<BuildRun> {
        @Override
        public int compare(BuildRun build1, BuildRun build2) {
            if (build1 == null || build2 == null) {
                return 0;
            }
            int compareTo = Long.valueOf(build1.getNumber()).compareTo(Long.valueOf(build2.getNumber()));
            if (compareTo == 0) {
                compareTo = build1.getStartedDate().compareTo(build2.getStartedDate());
            }
            return compareTo == 0 ? build1.getName().compareTo(build2.getName()) : compareTo;
        }
    }

    /**
     * Compares builds based on the lexicographic order of the build number.
     * In case Build numbers are equal we compare the start date
     * In case the Dates are also equals compare the builds names
     */
    private static class BuildNumberStringComparator implements Comparator<BuildRun> {
        @Override
        public int compare(BuildRun build1, BuildRun build2) {
            if (build1 == null || build2 == null) {
                return 0;
            }
            int compareTo = build1.getNumber().compareTo(build2.getNumber());
            if (compareTo == 0) {
                compareTo = build1.getStartedDate().compareTo(build2.getStartedDate());
            }
            return compareTo == 0 ? build1.getName().compareTo(build2.getName()) : compareTo;
        }
    }

    /**
     * Compares builds based on the start date.
     * If the start date are equals compare by build number.
     * If the build number is also equals compare the builds names
     */
    private static class BuildStartDateComparator implements Comparator<BuildRun>, Serializable {
        @Override
        public int compare(BuildRun build1, BuildRun build2) {
            if (build1 == null || build2 == null) {
                return 0;
            }
            int compareTo = build1.getStartedDate().compareTo(build2.getStartedDate());
            if (compareTo == 0) {
                return getComparatorFor(Lists.newArrayList(build1, build2)).compare(build1, build2);
            }
            return compareTo;
        }
    }
}
