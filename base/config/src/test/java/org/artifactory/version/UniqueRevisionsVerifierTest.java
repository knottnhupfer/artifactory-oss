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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.artifactory.common.ArtifactoryHome.ARTIFACTORY_VERSION_PROPERTIES;
import static org.artifactory.version.ArtifactoryVersionReader.readPropsContent;
import static org.artifactory.version.CurrentVersionLoader.RELEASE_VERSION_PATTERN;

/**
 * This test is supposed to fail the build if more than one version holds the same revision - revisions must be unique
 * for all versions.
 *
 * @author Dan Feldman
 * @author Fred Simon
 */
@Test
public class UniqueRevisionsVerifierTest {
    private static final Logger log = LoggerFactory.getLogger(UniqueRevisionsVerifierTest.class);

    public void verifyRevisions() {
        Assert.assertFalse(getUniqueRevisionCount() < ArtifactoryVersion.values().length,
                "Each Artifactory version must hold a unique build revision! Found duplicates: " + duplicates());
    }

    private long getUniqueRevisionCount() {
        return Stream.of(ArtifactoryVersion.values())
                .map(ArtifactoryVersion::getRevision)
                .distinct()
                .count();
    }

    private Set<Long> duplicates() {
        List<Long> revs = Stream.of(ArtifactoryVersion.values())
                .map(ArtifactoryVersion::getRevision)
                .collect(Collectors.toList());

        return revs.stream()
                .filter(rev -> Collections.frequency(revs, rev) > 1)
                .collect(Collectors.toSet());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void verifyIsCurrentVersionNullVersionAndNullRev() {
        ArtifactoryVersion.isCurrentVersion(null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void verifyIsCurrentVersionNullVersion() {
        ArtifactoryVersion.isCurrentVersion(null, "whatever");
    }

    public void verifyIsNotCurrentVersion() {
        // Null revision
        Assert.assertFalse(ArtifactoryVersion.isCurrentVersion("whatever", null));
    }

    public void verifyIsCurrentVersion() {
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("${forgot}", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever-SNAPSHOT", null));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever.x-DOWN-whatever", null));

        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever", "${ff}"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever", "dev"));
        Assert.assertTrue(ArtifactoryVersion.isCurrentVersion("whatever", "dev-branch-456"));
    }

    public void verifyAllReleasedVersionMatches() {
        InputStream inputStream = ArtifactoryHome.class.getResourceAsStream(ARTIFACTORY_VERSION_PROPERTIES);
        ArtifactoryVersionReader.VersionPropertiesContent versionPropertiesContent = readPropsContent(inputStream,
                ARTIFACTORY_VERSION_PROPERTIES);
        String runningVersion = versionPropertiesContent.versionString;

        for (ArtifactoryVersion version : ArtifactoryVersion.values()) {
            if (version.getVersion().equals(runningVersion)) {
                // nothing here
                continue;
            }
            String versionString = version.getVersion();
            String[] split = versionString.split("\\.");
            Assert.assertTrue(split.length >= 3);
            if (split.length >= 4) {
                Assert.assertTrue(versionString.endsWith(".1"));
                if (version == ArtifactoryVersionProvider.v130beta61.get()) {
                    versionString = "1.3.0_m061";
                } else {
                    versionString = versionString.substring(0, versionString.length() - 2) + "_p001";
                }
            }
            versionString = versionString.
                    replace("u", "_p00").
                    replace("-beta-", "_m00").
                    replace("-rc-", "_m00").
                    replace("-rc", "_m00").
                    replace("-", "_");
            Matcher releaseVersionMatcher = RELEASE_VERSION_PATTERN.matcher(versionString);
            if (releaseVersionMatcher.matches()) {
                if (version.after(ArtifactoryVersionProvider.v542.get())) {
                    assertVersionMatch(version, versionString, releaseVersionMatcher);
                }
            } else {
                log.warn("Version " + version + " does not respect the regexp");
            }
        }
    }

    static void assertVersionMatch(ArtifactoryVersion artifactoryVersion, String releaseVersion,
                                   Matcher releaseVersionMatcher) {
        int major = Integer.valueOf(releaseVersionMatcher.group(1));
        int minor = Integer.valueOf(releaseVersionMatcher.group(2));
        int patch = Integer.valueOf(releaseVersionMatcher.group(3));
        long rev = major * 10_000_000L + minor * 100_000L + patch * 1000L;
        String preRelease = releaseVersionMatcher.group(4);
        if (StringUtils.isNotBlank(preRelease)) {
            char preChar = preRelease.substring(1, 2).charAt(0);
            Assert.assertTrue(preChar == 'm' || preChar == 'p',
                    "Pre release character '" + preChar + "' isn't a valid pre release character in " +
                            releaseVersion);
            int preReleaseNumber = Integer.valueOf(preRelease.substring(2));
            Assert.assertTrue(preReleaseNumber > 0,
                    "Version pattern pre release number is negative in " + releaseVersion);

            Assert.assertTrue(
                    artifactoryVersion.getVersion()
                            .startsWith("" + major + "." + minor + "." + patch + "-" + preChar));
            if (preChar == 'm') {
                Assert.assertEquals(rev + preReleaseNumber, artifactoryVersion.getRevision());
            } else {
                Assert.assertEquals(rev + preReleaseNumber + 900L, artifactoryVersion.getRevision());
            }
        } else {
            // Full release no milestone or customer patch
            Assert.assertEquals(releaseVersion, artifactoryVersion.getVersion());
            Assert.assertEquals(rev + 900L, artifactoryVersion.getRevision());
        }
    }

}
