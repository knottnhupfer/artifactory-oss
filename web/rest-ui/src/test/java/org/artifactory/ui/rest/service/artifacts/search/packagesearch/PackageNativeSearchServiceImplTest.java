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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch;

import com.google.common.collect.Lists;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageNativeModel;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Inbar Tal
 */
public class PackageNativeSearchServiceImplTest {
    private PackageNativeSearchService packageNativeSearchService = new PackageNativeSearchService(
            null);
    @Test
    public void testSortPackagesByNameAsc()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method sortResults = packageNativeSearchService.getClass()
                .getDeclaredMethod("sortResults", List.class, String.class);
        sortResults.setAccessible(true);

        PackageNativeModel pkg1 = new PackageNativeModel();
        pkg1.setName("@scope1/test_pkg1");

        PackageNativeModel pkg2 = new PackageNativeModel();
        pkg2.setName("@abc/dod2");

        PackageNativeModel pkg3 = new PackageNativeModel();
        pkg3.setName("@test/ab");

        PackageNativeModel pkg4 = new PackageNativeModel();
        pkg4.setName("abad");

        PackageNativeModel pkg5 = new PackageNativeModel();
        pkg5.setName("@shrek/tv");

        PackageNativeModel pkg6 = new PackageNativeModel();
        pkg6.setName("viva");

        List<PackageNativeModel> pkgs = Lists.newArrayList(pkg1, pkg2, pkg3, pkg4, pkg5, pkg6);

        List<PackageNativeModel> sortedResults = (List<PackageNativeModel>) sortResults
                .invoke(packageNativeSearchService, pkgs, "asc");

        assertEquals(sortedResults.get(0).getName(), "@test/ab");
        assertEquals(sortedResults.get(1).getName(), "abad");
        assertEquals(sortedResults.get(2).getName(), "@abc/dod2");
        assertEquals(sortedResults.get(3).getName(), "@scope1/test_pkg1");
        assertEquals(sortedResults.get(4).getName(), "@shrek/tv");
        assertEquals(sortedResults.get(5).getName(), "viva");
    }

    @Test
    public void testSortPackagesByNameAscWithoutScopes()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method sortResults = packageNativeSearchService.getClass()
                .getDeclaredMethod("sortResults", List.class, String.class);
        sortResults.setAccessible(true);

        PackageNativeModel pkg1 = new PackageNativeModel();
        pkg1.setName("test_pkg1");

        PackageNativeModel pkg2 = new PackageNativeModel();
        pkg2.setName("dod2");

        PackageNativeModel pkg3 = new PackageNativeModel();
        pkg3.setName("ab");

        PackageNativeModel pkg4 = new PackageNativeModel();
        pkg4.setName("abad");

        PackageNativeModel pkg5 = new PackageNativeModel();
        pkg5.setName("tv");

        PackageNativeModel pkg6 = new PackageNativeModel();
        pkg6.setName("viva");

        List<PackageNativeModel> pkgs = Lists.newArrayList(pkg1, pkg2, pkg3, pkg4, pkg5, pkg6);

        List<PackageNativeModel> sortedResults = (List<PackageNativeModel>) sortResults
                .invoke(packageNativeSearchService, pkgs, "asc");

        assertEquals(sortedResults.get(0).getName(), "ab");
        assertEquals(sortedResults.get(1).getName(), "abad");
        assertEquals(sortedResults.get(2).getName(), "dod2");
        assertEquals(sortedResults.get(3).getName(), "test_pkg1");
        assertEquals(sortedResults.get(4).getName(), "tv");
        assertEquals(sortedResults.get(5).getName(), "viva");
    }

    @Test
    public void testSortPackagesByNameDesc()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method sortResults = packageNativeSearchService.getClass()
                .getDeclaredMethod("sortResults", List.class, String.class);
        sortResults.setAccessible(true);

        PackageNativeModel pkg1 = new PackageNativeModel();
        pkg1.setName("@scope1/test_pkg1");

        PackageNativeModel pkg2 = new PackageNativeModel();
        pkg2.setName("@abc/dod2");

        PackageNativeModel pkg3 = new PackageNativeModel();
        pkg3.setName("@test/ab");

        PackageNativeModel pkg4 = new PackageNativeModel();
        pkg4.setName("abad");

        PackageNativeModel pkg5 = new PackageNativeModel();
        pkg5.setName("@shrek/tv");

        PackageNativeModel pkg6 = new PackageNativeModel();
        pkg6.setName("viva");

        List<PackageNativeModel> pkgs = Lists.newArrayList(pkg1, pkg2, pkg3, pkg4, pkg5, pkg6);

        List<PackageNativeModel> sortedResults = (List<PackageNativeModel>) sortResults
                .invoke(packageNativeSearchService, pkgs, "desc");

        assertEquals(sortedResults.get(0).getName(), "viva");
        assertEquals(sortedResults.get(1).getName(), "@shrek/tv");
        assertEquals(sortedResults.get(2).getName(), "@scope1/test_pkg1");
        assertEquals(sortedResults.get(3).getName(), "@abc/dod2");
        assertEquals(sortedResults.get(4).getName(), "abad");
        assertEquals(sortedResults.get(5).getName(), "@test/ab");
    }

    @Test
    public void testSortPackagesByNameDescWithoutScopes()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method sortResults = packageNativeSearchService.getClass()
                .getDeclaredMethod("sortResults", List.class, String.class);
        sortResults.setAccessible(true);

        PackageNativeModel pkg1 = new PackageNativeModel();
        pkg1.setName("test_pkg1");

        PackageNativeModel pkg2 = new PackageNativeModel();
        pkg2.setName("dod2");

        PackageNativeModel pkg3 = new PackageNativeModel();
        pkg3.setName("ab");

        PackageNativeModel pkg4 = new PackageNativeModel();
        pkg4.setName("abad");

        PackageNativeModel pkg5 = new PackageNativeModel();
        pkg5.setName("tv");

        PackageNativeModel pkg6 = new PackageNativeModel();
        pkg6.setName("viva");

        List<PackageNativeModel> pkgs = Lists.newArrayList(pkg1, pkg2, pkg3, pkg4, pkg5, pkg6);

        List<PackageNativeModel> sortedResults = (List<PackageNativeModel>) sortResults
                .invoke(packageNativeSearchService, pkgs, "desc");

        assertEquals(sortedResults.get(0).getName(), "viva");
        assertEquals(sortedResults.get(1).getName(), "tv");
        assertEquals(sortedResults.get(2).getName(), "test_pkg1");
        assertEquals(sortedResults.get(3).getName(), "dod2");
        assertEquals(sortedResults.get(4).getName(), "abad");
        assertEquals(sortedResults.get(5).getName(), "ab");
    }
}
