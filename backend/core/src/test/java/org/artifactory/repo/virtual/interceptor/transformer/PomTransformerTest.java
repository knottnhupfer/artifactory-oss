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

package org.artifactory.repo.virtual.interceptor.transformer;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.io.checksum.ChecksumUtils;
import org.artifactory.maven.MavenModelUtils;
import org.jfrog.common.ResourceUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * IMPORTANT FOR FUTURE EDITORS: WHEN ADDING A NEW POM TRANSFORMATION SCENARIO, MAKE SURE TO ADD ONE FOR THE
 * TRANSFORMABLE ONE AND ONE FOR THE CLEAN ONE AND COMPARE CHECKSUMS AT THE END
 *
 * @author Eli Givoni
 */
@Test
public class PomTransformerTest {

    private String transformablePomAsString;
    private String cleanPomAsString;

    @BeforeClass
    private void setup() throws IOException {
        InputStream transformablePomResource = ResourceUtils.getResource(
                "/org/artifactory/repo/virtual/interceptor/activeByDefault-test.pom");
        InputStream cleanPomResource = ResourceUtils.getResource(
                "/org/artifactory/repo/virtual/interceptor/clean-test.pom");
        transformablePomAsString = IOUtils.toString(transformablePomResource, "utf-8");
        cleanPomAsString = IOUtils.toString(cleanPomResource, "utf-8");
        IOUtils.closeQuietly(transformablePomResource);
        IOUtils.closeQuietly(cleanPomResource);
    }

    public void transformableWithNothingPolicy() throws IOException {
        PomTransformer pomTransformer = new PomTransformer(transformablePomAsString, PomCleanupPolicy.nothing);
        String transformedPom = pomTransformer.transform();

        assertEquals(transformablePomAsString, transformedPom, "Expected a matching document");
        assertTrue(transformablePomAsString.contains("This is a comment"));
        assertFalse(pomTransformer.isPomChanged(), "POM should not have changed");
        compareChecksums(transformablePomAsString, transformedPom, true);
    }

    public void cleanWithNothingPolicy() throws IOException {
        PomTransformer pomTransformer = new PomTransformer(cleanPomAsString, PomCleanupPolicy.nothing);
        String transformedPom = pomTransformer.transform();

        assertEquals(cleanPomAsString, transformedPom, "Expected a matching document");
        assertTrue(cleanPomAsString.contains("This is a comment"));
        assertFalse(pomTransformer.isPomChanged(), "POM should not have changed");
        compareChecksums(cleanPomAsString, transformedPom, true);
    }

    @SuppressWarnings({"unchecked"})
    public void transformableWithDiscardActiveReference() throws IOException {

        PomTransformer pomTransformer = new PomTransformer(transformablePomAsString,
                PomCleanupPolicy.discard_active_reference);
        String transformedPom = pomTransformer.transform();

        Model pom = MavenModelUtils.stringToMavenModel(transformedPom);
        List repositoriesList = pom.getRepositories();
        List pluginsRepositoriesList = pom.getPluginRepositories();

        assertEmptyList(repositoriesList, pluginsRepositoriesList);

        List<Profile> pomProfiles = pom.getProfiles();
        for (Profile profile : pomProfiles) {
            boolean activeByDefault = false;
            Activation activation = profile.getActivation();
            if (activation != null) {
                activeByDefault = activation.isActiveByDefault();
            }
            List profileRepositories = profile.getRepositories();
            List profilePluginsRepositories = profile.getPluginRepositories();
            if (activeByDefault) {
                assertEmptyList(profileRepositories, profilePluginsRepositories);
            } else {
                assertNotEmptyList(profileRepositories, profilePluginsRepositories);
            }
        }
        assertTrue(transformablePomAsString.contains("This is a comment"));
        assertTrue(pomTransformer.isPomChanged(), "POM should have changed");
        compareChecksums(transformablePomAsString, transformedPom, false);
    }

    public void cleanWithDiscardActiveReference() throws IOException {
        PomTransformer pomTransformer = new PomTransformer(cleanPomAsString, PomCleanupPolicy.discard_active_reference);
        String transformedPom = pomTransformer.transform();

        assertEquals(cleanPomAsString, transformedPom, "Expected a matching document");
        assertTrue(cleanPomAsString.contains("This is a comment"));
        assertFalse(pomTransformer.isPomChanged(), "POM should not have changed");
        compareChecksums(cleanPomAsString, transformedPom, true);
    }

    public void transformableWithDiscardAnyReference() throws IOException {
        PomTransformer pomTransformer = new PomTransformer(transformablePomAsString,
                PomCleanupPolicy.discard_any_reference);
        String transformedPom = pomTransformer.transform();

        Model pom = MavenModelUtils.stringToMavenModel(transformedPom);
        List repositoriesList = pom.getRepositories();
        List pluginsRepositoriesList = pom.getPluginRepositories();

        assertEmptyList(repositoriesList, pluginsRepositoriesList);

        List<Profile> pomProfiles = pom.getProfiles();
        for (Profile profile : pomProfiles) {
            List profileRepositories = profile.getRepositories();
            List profilePluginsRepositories = profile.getPluginRepositories();

            assertEmptyList(profileRepositories, profilePluginsRepositories);
        }
        assertTrue(transformablePomAsString.contains("This is a comment"));
        assertTrue(pomTransformer.isPomChanged(), "POM should have changed");
        compareChecksums(transformablePomAsString, transformedPom, false);
    }

    public void cleanWithDiscardAnyReference() throws IOException {
        PomTransformer pomTransformer = new PomTransformer(cleanPomAsString, PomCleanupPolicy.discard_any_reference);
        String transformedPom = pomTransformer.transform();

        assertEquals(cleanPomAsString, transformedPom, "Expected a matching document");
        assertTrue(cleanPomAsString.contains("This is a comment"));
        assertFalse(pomTransformer.isPomChanged(), "POM should not have changed");
        compareChecksums(cleanPomAsString, transformedPom, true);
    }

    public void transformBadPom() throws IOException {
        InputStream badPomResource = ResourceUtils.getResource(
                "/org/artifactory/repo/virtual/interceptor/bad.pom");
        String badPom = IOUtils.toString(badPomResource, "utf-8");
        IOUtils.closeQuietly(badPomResource);
        PomTransformer pomTransformer = new PomTransformer(badPom, PomCleanupPolicy.discard_active_reference);
        String nonTransformedPom = pomTransformer.transform();
        assertEquals(nonTransformedPom, badPom, "xml document should not have been altered");
        assertFalse(pomTransformer.isPomChanged(), "POM should not have changed");
        assertTrue(transformablePomAsString.contains("This is a comment"));
    }

    private void assertEmptyList(Object... list) {
        for (Object o : list) {
            List elementList = (ArrayList) o;
            assertTrue(elementList.isEmpty(), "Expected an empty list");
        }
    }

    private void assertNotEmptyList(Object... list) {
        for (Object o : list) {
            List elementList = (ArrayList) o;
            assertFalse(elementList.isEmpty(), "Expected not an empty list");
        }
    }

    /**
     * Compares the checksums of two strings
     *
     * @param a             String to compare
     * @param b             String to compare
     * @param shouldBeEqual True if both strings should have equal checksums
     */
    private void compareChecksums(String a, String b, boolean shouldBeEqual) throws IOException {
        InputStream streamA = IOUtils.toInputStream(a);
        InputStream streamB = IOUtils.toInputStream(b);
        ChecksumsInfo checksumsA = ChecksumUtils.getChecksumsInfo(streamA);
        ChecksumsInfo checksumsB = ChecksumUtils.getChecksumsInfo(streamB);

        if (shouldBeEqual) {
            assertTrue(checksumsA.isIdentical(checksumsB), "POM checksums should be equal.");
        } else {
            assertFalse(checksumsA.isIdentical(checksumsB), "POM checksums should not be equal.");
        }
        IOUtils.closeQuietly(streamA);
        IOUtils.closeQuietly(streamB);
    }
}
