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

package org.artifactory.storage.db;

import org.apache.commons.io.FileUtils;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.db.upgrades.common.UpgradeBaseTest;
import org.jfrog.common.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This test should fail the build if any DB test is not registered in the testng-db.xml or not located under an
 * 'itest' package.
 * This is because the unit tests are not isolated and their home(db in derby as well) is always the same, so when
 * having DB test with dependsOnMethod, it might be that between these methods, another test will corrupt the DB.
 *
 * @author Yossi Shaul
 */
@Test
public class DbEnforcerTest {

    // block the build if a DB test is in the incorrect location (not under an 'itest' package and not int he testng-db.xml file)
    public void testValidDbTestLocation() {
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        File classesPath = new File(src.getLocation().toString().replace("file:", ""));
        Assert.assertTrue(classesPath.isDirectory());
        System.out.println("classesPath = " + classesPath);
        String[] extensions = {"class"};
        Collection<File> files = FileUtils.listFiles(classesPath, extensions, true);
        System.out.println("files = " + files.size());
        List<Class<?>> classes = files.stream()
                .map(file -> file.getAbsolutePath()
                        .replace(classesPath.getAbsolutePath() + File.separator, "")
                        .replace(File.separator, ".")
                        .replace(".class", ""))
                .map(this::loadClass)
                .collect(Collectors.toList());

        System.out.println("Loaded " + classes.size() + " classes");

        String testNg = ResourceUtils.getResourceAsString("/testng-db.xml");
        List<Class<?>> missing = classes.stream()
                .filter(c -> DbBaseTest.class.isAssignableFrom(c) || UpgradeBaseTest.class.isAssignableFrom(c))
                .filter(c -> !Modifier.isAbstract(c.getModifiers()) && (!testNg.contains(c.getName()) || !c.getCanonicalName().contains("itest.")))
                .collect(Collectors.toList());
        if (missing.size() > 0) {
            Assert.fail(missing.size() + " DB Test/s either not exists in the 'testng-db.xml' or not located under the 'itest' package. " + System.lineSeparator() + missing);
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
