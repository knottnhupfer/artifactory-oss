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

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.converter.helpers.ConvertersManagerTestBase;
import org.artifactory.converter.helpers.MockArtifactoryHome;
import org.artifactory.environment.converter.local.version.v2.NoNfsArtifactoryPropertiesConverter;
import org.artifactory.test.TestUtils;
import org.artifactory.version.CompoundVersionDetails;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.artifactory.common.ArtifactoryHome.ARTIFACTORY_HA_NODE_PROPERTIES_FILE;
import static org.artifactory.converter.nonfs.NoNfsConverterTest.cleanAndAssert;
import static org.artifactory.environment.converter.BasicEnvironmentConverter.BACKUP_FILE_EXT;
import static org.artifactory.version.ArtifactoryVersionProvider.v4111;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Rotem Kfir
 */
public class NoNfsArtifactoryPropertiesConverterTest extends ConvertersManagerTestBase {

    public static CompoundVersionDetails V4 = new CompoundVersionDetails(v4111.get(),"v4111_TEST", 0L);
    private File home;

    @BeforeClass
    public void init() throws IOException {
        home = TestUtils.createTempDir(getClass());
        createHomeEnvironment(home, v4111.get());
    }

    @Test
    public void convert() throws Exception {
        File artDir = new File(home, ".artifactory");
        File localDataDir = new File(artDir, "data");
        File etcDir = new File(artDir, "etc");
        File props = new File(etcDir, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
        cleanAndAssert(props);
        File haNodeProps = new File(etcDir, ArtifactoryHome.ARTIFACTORY_HA_NODE_PROPERTIES_FILE);
        cleanAndAssert(haNodeProps);

        ArtifactoryHome artifactoryHome = new MockArtifactoryHome(home);
        try {
            ArtifactoryHome.bind(artifactoryHome);
            NoNfsArtifactoryPropertiesConverter converter = new NoNfsArtifactoryPropertiesConverter();
            assertTrue(converter.isInterested(artifactoryHome, V4, artifactoryHome.getRunningArtifactoryVersion()));

            converter.convert(artifactoryHome, V4, artifactoryHome.getRunningArtifactoryVersion());

            File localOldProps = new File(localDataDir, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
            assertFalse(localOldProps.exists());

            File localOldPropsBackup = new File(localDataDir, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE + BACKUP_FILE_EXT);
            assertTrue(localOldPropsBackup.exists());

            assertTrue(props.exists());
        } finally {
            ArtifactoryHome.unbind();
        }
    }

    @Test
    public void convertHA() throws Exception {
        File artDir = new File(home, ".artifactory-ha");
        File homesDir = new File(artDir, "homes");
        assertTrue(homesDir.mkdir());
        File node1Dir = new File(homesDir, "pom");
        assertTrue(node1Dir.mkdir());
        File localDataDir = new File(node1Dir, "data");
        assertTrue(localDataDir.mkdir());
        Properties artifactoryProperties = createArtifactoryProperties(v4111.get());
        try (FileOutputStream out = new FileOutputStream(
                home + "/.artifactory-ha/homes/pom/data/" + ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE)) {
            artifactoryProperties.store(out, "");
        }
        File etcDir = new File(node1Dir, "etc");
        assertTrue(etcDir.mkdir());
        File props = new File(etcDir, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
        cleanAndAssert(props);
        try (FileOutputStream out = new FileOutputStream(new File(etcDir, ARTIFACTORY_HA_NODE_PROPERTIES_FILE))) {
            Properties haNodeProperties = createHaNodeProperties(home);
            haNodeProperties.store(out, "");
        }

        ArtifactoryHome artifactoryHome = new MockArtifactoryHome(home, true);
        try {
            ArtifactoryHome.bind(artifactoryHome);
            NoNfsArtifactoryPropertiesConverter converter = new NoNfsArtifactoryPropertiesConverter();
            assertTrue(converter.isInterested(artifactoryHome, V4, artifactoryHome.getRunningArtifactoryVersion()));

            converter.convert(artifactoryHome, V4, artifactoryHome.getRunningArtifactoryVersion());

            File haOldProps = new File(artDir, "ha-data/" + ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
            assertFalse(haOldProps.exists());
            File localOldProps = new File(localDataDir, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE);
            assertFalse(localOldProps.exists());

            File haOldPropsBackup = new File(artDir, "ha-data/" + ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE + BACKUP_FILE_EXT);
            assertTrue(haOldPropsBackup.exists());
            File localOldPropsBackup = new File(localDataDir, ArtifactoryHome.ARTIFACTORY_PROPERTIES_FILE + BACKUP_FILE_EXT);
            assertTrue(localOldPropsBackup.exists());

            assertTrue(props.exists());
        } finally {
            ArtifactoryHome.unbind();
        }
    }

    private static Properties createHaNodeProperties(File home) {
        Properties properties = new Properties();
        properties.put(HaNodeProperties.PROP_NODE_ID, "pom");
        properties.put(HaNodeProperties.PROP_CONTEXT_URL, "localhost");
        properties.put(HaNodeProperties.PROP_PRIMARY, "true");
        properties.put(HaNodeProperties.PROP_HA_DATA_DIR, new File(home, ".artifactory-ha/ha-data").getAbsolutePath());
        return properties;
    }

}