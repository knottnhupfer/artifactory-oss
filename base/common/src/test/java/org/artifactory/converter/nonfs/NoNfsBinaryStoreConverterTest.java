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

package org.artifactory.converter.nonfs;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.environment.converter.shared.version.v1.NoNfsBinaryStoreConverter;
import org.jfrog.common.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test for the NoNfsBinaryStoreConverter
 * load binarystore.xml and storage.properties, convert to artifactory 5.x
 * and assert the converter create the expected binarystore.xml
 *
 * @author gidis
 */
@Test
public class NoNfsBinaryStoreConverterTest {

    private static final String DEFAULT_STORAGE_PROPERTIES = "/converters/binarystore/storage.properties";
    private NoNfsBinaryStoreConverter noNfsBinaryStoreConverter;
    private Pattern PROVIDER_PATTERN = Pattern.compile("<provider.+?(</provider>|/>)", Pattern.DOTALL);
    private Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\w+?=\"(\\w|-)+?\"");

    @BeforeTest
    public void setup() {
        noNfsBinaryStoreConverter = new NoNfsBinaryStoreConverter();
    }

    private void createEnvironmentAndTest(String binaryStorePath, String storagePropertiesPath, String expectedPath,
            String testName) throws IOException {
        ArtifactoryHome artifactoryHome = new ArtifactoryHome(
                new File("./target/test/NoNfsBinaryStoreConverterTest/" + testName));
        File etcDir = artifactoryHome.getEtcDir();
        File storagePropertiesFile = new File(etcDir, "storage.properties");
        File binaryStoreXmlFile = new File(etcDir, "binarystore.xml");
        loadEnvironment(binaryStorePath, storagePropertiesPath, storagePropertiesFile, binaryStoreXmlFile);
        noNfsBinaryStoreConverter.doConvert(artifactoryHome, null);
        assertExpectedBinaryStore(expectedPath, binaryStoreXmlFile);
    }

    @Test
    public void testSimpleTemplateChain() throws IOException {
        createEnvironmentAndTest("/converters/binarystore/binarystore.xml", DEFAULT_STORAGE_PROPERTIES,
                "/converters/binarystore/expected_binarystore.xml", "testSimpleTemplateChain");
    }

    /**
     * Expect no changes here
     */
    @Test
    public void testArtifactory5Convert() throws IOException {
        createEnvironmentAndTest("/converters/binarystore/expected_binarystore.xml", null,
                "/converters/binarystore/expected_binarystore.xml", "testArtifactory5Convert");
    }

    @Test
    public void testS3BinaryStore() throws IOException {
        createEnvironmentAndTest("/converters/binarystore/s3binarystore.xml", DEFAULT_STORAGE_PROPERTIES,
                "/converters/binarystore/expected_s3binarystore.xml", "testS3BinaryStore");
    }

    @Test
    public void testGoogleBinaryStore() throws IOException {
        createEnvironmentAndTest("/converters/binarystore/google_binarystore.xml", DEFAULT_STORAGE_PROPERTIES,
                "/converters/binarystore/expected_google_binarystore.xml", "testGoogleBinaryStore");
    }

    @Test
    public void testS3OldBinaryStore() throws IOException {
        createEnvironmentAndTest(null, "/converters/binarystore/s3storage.properties",
                "/converters/binarystore/expected_s3old_binarystore.xml", "testS3OldBinaryStore");
    }


    @Test
    public void testUserChain() throws IOException {
        createEnvironmentAndTest("/converters/binarystore/user_chain_binarystore.xml", DEFAULT_STORAGE_PROPERTIES,
                "/converters/binarystore/expected_user_chain_binarystore.xml", "testUserChain");
    }

    /**
     * Tests that the binarystore.xml in the /etc directory is like the expected binarystore.xml in the expected path
     */
    private void assertExpectedBinaryStore(String expectedPath, File binaryStoreXmlFile) throws IOException {
        String expectedBinaryStoreXml = ResourceUtils.getResourceAsString(expectedPath);
        Set<Set<String>> expectedProviders = getProvidersAsSet(expectedBinaryStoreXml);
        String binaryStoreXmlResult = FileUtils.readFileToString(binaryStoreXmlFile);
        Set<Set<String>> binaryStoreProviders = getProvidersAsSet(binaryStoreXmlResult);
        Assert.assertEquals(binaryStoreProviders, expectedProviders);
    }

    /**
     * Load test storage.properties / binarystore.xml to /etc folder
     */
    private void loadEnvironment(String binaryStorePath, String storagePropertiesPath, File storagePropertiesFile,
            File binaryStoreXmlFile) throws IOException {
        saveXmlPathToFile(storagePropertiesPath, storagePropertiesFile);
        saveXmlPathToFile(binaryStorePath, binaryStoreXmlFile);
    }

    /**
     * If path is not null, save it to output file. try to delete output file otherwise.
     * used to make sure storage.properties / binarystore.xml don't get mistakenly reused between tests
     */
    private void saveXmlPathToFile(String xmlPath, File outputFile) throws IOException {
        if (xmlPath != null) {
            String xmlFile = ResourceUtils.getResourceAsString(xmlPath);
            FileUtils.writeStringToFile(outputFile, xmlFile);
        }
    }

    /**
     * Parse a string to set of providers, each containing his lines in a String set
     */
    private Set<Set<String>> getProvidersAsSet(String xmlInput) {
        Set<Set<String>> providers = Sets.newLinkedHashSet();
        Matcher match = PROVIDER_PATTERN.matcher(xmlInput);
        while (match.find()) {
            ArrayList<String> lines = Lists.newArrayList(match.group().split("\\r?\\n"));
            Set<String> provider = getAttributes(lines);
            providers.add(provider);
        }
        return providers;
    }

    /**
     * Get all elements and attributes from a provider xml string section
     */
    private Set<String> getAttributes(ArrayList<String> lines) {
        Set<String> attributeSet = new TreeSet<>();
        lines.forEach(line -> getLineAttribute(line, attributeSet));
        return attributeSet;
    }

    private void getLineAttribute(String line, Set<String> attributeSet) {
        line = line.trim();
        if (line.startsWith("<provider")) {
            Matcher attributeMatcher = ATTRIBUTE_PATTERN.matcher(line);
            while (attributeMatcher.find()) {
                attributeSet.add(attributeMatcher.group());
            }
        } else {
            if (!line.startsWith("<binariesDir>") && !line.equals("</provider>")) {
                attributeSet.add(line);
            }
        }
    }
}
