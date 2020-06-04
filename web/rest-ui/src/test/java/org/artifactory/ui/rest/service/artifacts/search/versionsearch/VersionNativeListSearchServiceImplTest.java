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

package org.artifactory.ui.rest.service.artifacts.search.versionsearch;

import com.google.common.collect.Lists;
import org.artifactory.ui.rest.model.artifacts.search.versionsearch.result.VersionNativeModel;
import org.artifactory.ui.rest.service.artifacts.search.packagesearch.util.PackageNativeSearchHelper;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Inbar Tal
 */
public class VersionNativeListSearchServiceImplTest {

    private VersionNativeListSearchService versionNativeListSearchService = new VersionNativeListSearchService(
            new PackageNativeSearchHelper(null), null, null, null);

    @Test
    public void testSortVersionsByLastModifiedAsc()
            throws NoSuchMethodException, ParseException, InvocationTargetException, IllegalAccessException {

        Method sortByLastModified = versionNativeListSearchService.getClass()
                .getDeclaredMethod("sortByLastModified", List.class, String.class);

        sortByLastModified.setAccessible(true);

        VersionNativeModel ver1 = new VersionNativeModel();
        ver1.setName("1.0.0");
        ver1.setLastModified(getDateFromString("Thu Jul 26 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver2 = new VersionNativeModel();
        ver2.setName("4.0.0");
        ver2.setLastModified(getDateFromString("Thu Jul 27 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver3 = new VersionNativeModel();
        ver3.setName("6.0.0");
        ver3.setLastModified(getDateFromString("Thu Jul 25 19:46:31 IDT 2018").getTime());

        List<VersionNativeModel> orderedResults = (List<VersionNativeModel>) sortByLastModified
                .invoke(versionNativeListSearchService, Lists.newArrayList(ver1, ver2, ver3), "asc");

        assertEquals(orderedResults.get(0).getName(), "6.0.0");
        assertEquals(orderedResults.get(1).getName(), "1.0.0");
        assertEquals(orderedResults.get(2).getName(), "4.0.0");
    }

    @Test
    public void testSortVersionsByLastModifiedDesc()
            throws NoSuchMethodException, ParseException, InvocationTargetException, IllegalAccessException {

        Method sortByLastModified = versionNativeListSearchService.getClass()
                .getDeclaredMethod("sortByLastModified", List.class, String.class);

        sortByLastModified.setAccessible(true);

        VersionNativeModel ver1 = new VersionNativeModel();
        ver1.setName("1.0.0");
        ver1.setLastModified(getDateFromString("Thu Jul 26 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver2 = new VersionNativeModel();
        ver2.setName("4.0.0");
        ver2.setLastModified(getDateFromString("Thu Jul 27 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver3 = new VersionNativeModel();
        ver3.setName("6.0.0");
        ver3.setLastModified(getDateFromString("Thu Jul 25 19:46:31 IDT 2018").getTime());

        List<VersionNativeModel> orderedResults = (List<VersionNativeModel>) sortByLastModified
                .invoke(versionNativeListSearchService, Lists.newArrayList(ver1, ver2, ver3), "desc");

        assertEquals(orderedResults.get(0).getName(), "4.0.0");
        assertEquals(orderedResults.get(1).getName(), "1.0.0");
        assertEquals(orderedResults.get(2).getName(), "6.0.0");
    }

    @Test
    public void testSortVersionsByNameAsc()
            throws NoSuchMethodException, ParseException, InvocationTargetException, IllegalAccessException {

        Method sortByNameMethod = versionNativeListSearchService.getClass()
                .getDeclaredMethod("sortByVersion", List.class, String.class);

        sortByNameMethod.setAccessible(true);

        VersionNativeModel ver1 = new VersionNativeModel();
        ver1.setName("1.0.0");
        ver1.setLastModified(getDateFromString("Thu Jul 26 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver2 = new VersionNativeModel();
        ver2.setName("4.0.0");
        ver2.setLastModified(getDateFromString("Thu Jul 27 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver3 = new VersionNativeModel();
        ver3.setName("10.1.0");
        ver3.setLastModified(getDateFromString("Thu Jul 25 19:46:31 IDT 2018").getTime());

        List<VersionNativeModel> orderedResults = (List<VersionNativeModel>) sortByNameMethod
                .invoke(versionNativeListSearchService, Lists.newArrayList(ver1, ver2, ver3), "asc");

        assertEquals(orderedResults.get(0).getName(), "1.0.0");
        assertEquals(orderedResults.get(1).getName(), "4.0.0");
        assertEquals(orderedResults.get(2).getName(), "10.1.0");
    }

    @Test
    public void testSortVersionsByNameDesc()
            throws NoSuchMethodException, ParseException, InvocationTargetException, IllegalAccessException {

        Method sortByNameMethod = versionNativeListSearchService.getClass()
                .getDeclaredMethod("sortByVersion", List.class, String.class);

        sortByNameMethod.setAccessible(true);

        VersionNativeModel ver1 = new VersionNativeModel();
        ver1.setName("1.0.0");
        ver1.setLastModified(getDateFromString("Thu Jul 26 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver2 = new VersionNativeModel();
        ver2.setName("4.0.0");
        ver2.setLastModified(getDateFromString("Thu Jul 27 18:46:31 IDT 2018").getTime());

        VersionNativeModel ver3 = new VersionNativeModel();
        ver3.setName("10.1.0");
        ver3.setLastModified(getDateFromString("Thu Jul 25 19:46:31 IDT 2018").getTime());

        List<VersionNativeModel> orderedResults = (List<VersionNativeModel>) sortByNameMethod
                .invoke(versionNativeListSearchService, Lists.newArrayList(ver1, ver2, ver3), "desc");

        assertEquals(orderedResults.get(0).getName(), "10.1.0");
        assertEquals(orderedResults.get(1).getName(), "4.0.0");
        assertEquals(orderedResults.get(2).getName(), "1.0.0");
    }

    private Date getDateFromString(String date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        return dateFormat.parse(date);
    }
}
