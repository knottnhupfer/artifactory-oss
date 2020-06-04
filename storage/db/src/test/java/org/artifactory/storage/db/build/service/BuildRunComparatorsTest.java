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

package org.artifactory.storage.db.build.service;

import com.google.common.collect.Lists;
import org.artifactory.api.build.BuildRunComparators;
import org.artifactory.build.BuildRun;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import static org.testng.Assert.assertEquals;

/**
 * Tests the behavior of {@link org.artifactory.api.build.BuildRunComparators} class factory
 *
 * @author Lior Hasson
 */
@Test
public class BuildRunComparatorsTest {

    public void testDateComparator() {
        Calendar instance1 = Calendar.getInstance();
        instance1.set(2014, Calendar.JANUARY, 1);
        Calendar instance2 = Calendar.getInstance();
        instance2.set(2014, Calendar.FEBRUARY, 1);

        BuildRun b1 = new BuildRunImpl("Alice", "1", instance1.getTime());
        BuildRun b2 = new BuildRunImpl("Bob", "2", instance2.getTime());

        Comparator<BuildRun> dateComparator = BuildRunComparators.getBuildStartDateComparator();
        assertEquals(dateComparator.compare(b1, b2), -1);
        assertEquals(dateComparator.compare(b2, b1), 1);
        assertEquals(dateComparator.compare(b1, b1), 0);
    }

    public void testNumberComparator() {
        BuildRun b1 = new BuildRunImpl("Alice", "1", new Date());
        BuildRun b2 = new BuildRunImpl("Bob", "2", new Date());
        ArrayList<BuildRun> buildRuns = Lists.newArrayList(b1, b2);

        //Also implicit check of the comparator factory, in this case should be Number compare.
        Comparator<BuildRun> numberComparator = BuildRunComparators.getComparatorFor(buildRuns);
        assertEquals(numberComparator.compare(b1, b2), -1);
        assertEquals(numberComparator.compare(b2, b1), 1);
        assertEquals(numberComparator.compare(b1, b1), 0);
    }

    public void testStringComparator() {
        BuildRun b1 = new BuildRunImpl("Alice", "11a", new Date());
        BuildRun b2 = new BuildRunImpl("Bob", "11b", new Date());
        ArrayList<BuildRun> buildRuns = Lists.newArrayList(b1, b2);

        //Also implicit check of the comparator factory, in this case should be String compare.
        Comparator<BuildRun> numberComparator = BuildRunComparators.getComparatorFor(buildRuns);
        assertEquals(numberComparator.compare(b1, b2), -1);
        assertEquals(numberComparator.compare(b2, b1), 1);
        assertEquals(numberComparator.compare(b1, b1), 0);
    }
}