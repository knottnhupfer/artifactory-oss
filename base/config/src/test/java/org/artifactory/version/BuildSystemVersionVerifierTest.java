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

package org.artifactory.version;

import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ArtifactoryHome;
import org.jfrog.common.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;

import static org.artifactory.version.CurrentVersionLoader.RELEASE_VERSION_PATTERN;

/**
 * This test verify that depending on the build environment the version properties file and param are OK.
 * 4 separate environments needs to be tested:<ol>
 * <li>IntelliJ</li>
 * <li>Local Maven</li>
 * <li>Jenkins snapshot maven</li>
 * <li>Jenkins release maven</li>
 * </ol>
 * <p>1) In IntelliJ execution maven filter is still executed so identical to env 2
 * <p>2) In Local Maven:
 * No Java properties are inserted but all artifactory.version.prop, artifactory.revision.prop, artifactory.timestamp.prop
 * are replaced with values from the default pom in the artifactory.version.properties file</p>
 * <p>3) In Jenkins Snapshot Maven:
 * Java properties are inserted for artifactory.revision.prop and artifactory.timestamp.prop.
 * The property artifactory.version.prop coming from the pom.
 * and are replaced with values in the artifactory.version.properties file.</p>
 * <p>4) In Jenkins Release Maven:
 * Java properties are inserted for artifactory.timestamp.prop.
 * The properties artifactory.version.prop and artifactory.revision.prop are hardcoded in the pom.
 * All props are replaced with values in the artifactory.version.properties file.</p>
 *
 * @author Dan Feldman, Fred Simon
 */
public class BuildSystemVersionVerifierTest {
    private static final Logger log = LoggerFactory.getLogger(BuildSystemVersionVerifierTest.class);

    @Test
    public void verifyIsCurrentDevVersion() {
        // correct snapshot format
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever-SNAPSHOT", null));

        // wrong snapshot format
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("whatever-SNAPSHOT-wrong", null));

        // dev revision always true
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever", "dev"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever", "dev.whatever"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever", "dev-whatever"));

        // numeric revision not
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("whatever", "456"));

        // correct down format
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever.x-DOWN-ok", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x-DOWN-bs-2.0.9", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x-DOWN-bs-2.0.9-m001", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever.x-DOWN.ok", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x-DOWN-bs.2.0.9", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x-DOWN-bs.2.0.9-m001", null));

        // wrong down format
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("wrong.DOWN.", null));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("wrong.x-DOWN", null));

        // New distribution format enforce revision in numeric format
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.branch", null));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.co.branch", null));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.bs.2.0.9", null));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.bs.2.0.9_m001", null));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.branch", "not.dev.or.number"));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.bs.2.0.9", "not.dev.or.number"));
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("5.x.bs.2.0.9_m001", "not.dev.or.number"));

        // Correct format new distribution version
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x.branch", "333"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x.co.branch", "333"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x.bs.2.0.9", "222"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x.bs.2.0.9_m001", "222"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x_branch", "333"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x_bs_2.0.9", "222"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("5.x_bs_2.0.9_m001", "222"));
    }

    @Test
    public void verifyBuildPropAndVersionMatch() throws IOException {
        ArtifactoryVersionReader.VersionPropertiesContent propsContent;
        try (InputStream currentPropsFile = ResourceUtils.getResource(ArtifactoryHome.ARTIFACTORY_VERSION_PROPERTIES)) {
            propsContent = ArtifactoryVersionReader
                    .readPropsContent(currentPropsFile, ArtifactoryHome.ARTIFACTORY_VERSION_PROPERTIES);
        }
        if (propsContent.revisionString.equals("dev")) {
            propsContent.revisionString = String.valueOf(Integer.MAX_VALUE);
        }

        assertPropsContentMatchBuildEnvironment(propsContent);
    }

    public static void assertPropsContentMatchBuildEnvironment(ArtifactoryVersionReader.VersionPropertiesContent propsContent) {
        ArtifactoryVersion versionInEnum = ArtifactoryVersion.getCurrent();

        String envVersion = System.getProperty("artifactory.version.prop");
        String envRpmVersion = System.getProperty("artifactory.rpmVersion.prop");
        String envRevision = System.getProperty("artifactory.revision.prop");
        String envBuildNumber = System.getProperty("artifactory.buildNumber.prop");
        String envTimestamp = System.getProperty("artifactory.timestamp.prop");

        // Which environment based on Jenkins param
        // This is meant to run on the build env only, not local machines (that might also have $JENKINS_HOME defined)
        boolean envUser = System.getenv("USER") != null && System.getenv("USER").toLowerCase().contains("jenkins");
        String home = System.getenv("HOME");
        boolean isJenkinsHome = home != null && home.toLowerCase().contains("jenkins");
        String jenkinsHome = (envUser || isJenkinsHome)
                ? System.getenv("JENKINS_HOME") : "";
        log.info("Build version test env JENKINS_HOME=" + jenkinsHome);

        if (StringUtils.isBlank(jenkinsHome)) {
            // No jenkins, in environment 1) or 2)
            Assert.assertNull(envVersion);
            Assert.assertNull(envRpmVersion);
            Assert.assertNull(envRevision);
            Assert.assertNull(envBuildNumber);
            Assert.assertNull(envTimestamp);
            //Assert.assertEquals(propsContent.versionString, "6.x-SNAPSHOT");
            Assert.assertEquals(propsContent.buildNumberString, "LOCAL");
            Assert.assertEquals(Integer.parseInt(propsContent.revisionString), Integer.MAX_VALUE);
            if (!"${timestamp}".equals(propsContent.timestampString) && !"LOCAL".equals(propsContent.timestampString)) {
                // Dev value
                Assert.assertEquals(propsContent.timestampString, "" + ArtifactoryVersionReader.UNFILTERED_TIMESTAMP);
            }
        } else {
            // In Jenkins the buildNumber is always passed as java param
            Assert.assertNotNull(envBuildNumber);
            Assert.assertEquals(envBuildNumber, propsContent.buildNumberString);
            // In Jenkins the timestamp is always passed as java param
            Assert.assertNotNull(envTimestamp);
            Assert.assertEquals(envTimestamp, propsContent.timestampString);
            long t = Long.parseLong(envTimestamp);
            Assert.assertTrue(t < System.currentTimeMillis());

            // In jenkins mode the version should not have SNAPSHOT or -DOWN- in it
            Assert.assertFalse(propsContent.versionString.contains("SNAPSHOT"),
                    "In Jenkins service version should not have SNAPSHOT in it: " + propsContent.versionString);
            Assert.assertFalse(propsContent.versionString.contains("DOWN"),
                    "In Jenkins service version should not have DOWN in it: " + propsContent.versionString);

            // Find if a release or not
            if (propsContent.versionString.contains(".x_") || propsContent.versionString.contains(".x.")) {
                // Dev mode
                Assert.assertNotNull(envVersion);
                Assert.assertEquals(envVersion, propsContent.versionString);
                Assert.assertNotNull(envRevision);
                Assert.assertEquals(envRevision, propsContent.revisionString);

                // The revision is a long now
                long r = Long.parseLong(propsContent.revisionString);
                Assert.assertTrue(r > 0,
                        "Version pattern pre release number is negative in $version");

                //Assert.assertEquals(versionInEnum.getVersion(), "99.99.99");
                Assert.assertEquals(versionInEnum.getRevision(), Integer.MAX_VALUE);
            } else {
                // Release mode, version and revision are coded in the pom file
                Assert.assertNull(envVersion);
                Assert.assertNull(envRevision);
                // The revision is a long now
                long r = Long.parseLong(propsContent.revisionString);
                Assert.assertEquals(versionInEnum.getRevision(), r);
                // Should match release versioning
                String releaseVersion = propsContent.versionString;
                Matcher releaseVersionMatcher = RELEASE_VERSION_PATTERN.matcher(releaseVersion);
                Assert.assertTrue(releaseVersionMatcher.matches(),
                        "Version " + propsContent.versionString + " does not match release version pattern");

                UniqueRevisionsVerifierTest.assertVersionMatch(versionInEnum, releaseVersion, releaseVersionMatcher);
            }
        }
    }

}
